package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.instance.version.VersionProperties.HANDLERS_CONTEXT_VERSION_DATE_KEY;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.content.idoc.Idoc;
import com.sirmaenterprise.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirmaenterprise.sep.content.idoc.handler.SearchContentNodeHandler;
import com.sirmaenterprise.sep.content.idoc.handler.VersionContentNodeHandler;

/**
 * Schedules asynchronous task that handles processing and storing of {@link Content#PRIMARY_VIEW} content for the
 * created version. The content for the version is copied from the content of the original instance, then all widgets
 * are processed with {@link SearchContentNodeHandler}s and {@link VersionContentNodeHandler}s in order to make their
 * dynamic data in to a static one, so that we could show the correct data when the version is opened. When the widget
 * processing is done the new content is saved as content for the version instance. If any of the instance ids(original
 * or version) is missing no content will be extracted, process or stored.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
@Named(ScheduleVersionContentCreate.NAME)
public class ScheduleVersionContentCreate extends SchedulerActionAdapter {

	public static final String NAME = "scheduleVersionContentCreate";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String ORIGINAL_INSTANCE_ID = "originalInstanceId";
	private static final String VERSION_INSTANCE_ID = "versionInstanceId";
	private static final String VERSION_CREATED_ON_DATE = "versionCreatedOn";
	private static final String PROCESS_WIDGETS = "processWidgets";

	private static final List<Pair<String, Class<?>>> ARGUMENTS_VALIDATION = Arrays.asList(
			new Pair<>(ORIGINAL_INSTANCE_ID, Serializable.class), new Pair<>(VERSION_INSTANCE_ID, Serializable.class),
			new Pair<>(VERSION_CREATED_ON_DATE, Date.class), new Pair<>(PROCESS_WIDGETS, Boolean.class));

	private static final int MAX_RETRIES = 5;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return ARGUMENTS_VALIDATION;
	}

	/**
	 * Intercepts {@link CreateVersionContentEvent} and creates scheduled task for version view content creation. The
	 * task will be asynchronous and will be executed after the transaction is completed.
	 *
	 * @param event
	 *            {@link CreateVersionContentEvent} which triggers the creation of the task, which will process and
	 *            store the version content. Contains required information for the correct task creation
	 * @see TransactionSupport#invokeAfterTransactionCompletionInTx(Executable)
	 * @see SchedulerService#schedule(String, SchedulerConfiguration, SchedulerContext)
	 */
	public void onCreateVersionContent(@Observes CreateVersionContentEvent event) {
		CreateVersionContentEvent localEvent = event;
		transactionSupport.invokeAfterTransactionCompletionInTx(() -> {
			SchedulerContext context = createContext(localEvent);
			SchedulerConfiguration configuration = buildConfiguration();
			schedulerService.schedule(NAME, configuration, context);
		});
	}

	private static SchedulerContext createContext(CreateVersionContentEvent event) {
		VersionContext versionContext = event.getContext();
		String originalInstanceId = versionContext.getTargetInstanceId();
		String versionId = versionContext.getVersionInstanceId();
		if (StringUtils.isBlank(versionId) || StringUtils.isBlank(originalInstanceId)) {
			throw new IllegalArgumentException();
		}

		SchedulerContext context = new SchedulerContext();
		context.put(ORIGINAL_INSTANCE_ID, originalInstanceId);
		context.put(VERSION_INSTANCE_ID, versionId);
		context.put(VERSION_CREATED_ON_DATE, versionContext.getCreationDate());
		context.put(PROCESS_WIDGETS, versionContext.shouldProcessWidgets());
		return context;
	}

	private static SchedulerConfiguration buildConfiguration() {
		return new DefaultSchedulerConfiguration()
				.setType(SchedulerEntryType.IMMEDIATE)
					.setSynchronous(false)
					.setRemoveOnSuccess(true)
					.setPersistent(true)
					.setTransactionMode(TransactionMode.REQUIRED)
					.setMaxRetryCount(MAX_RETRIES)
					.setIncrementalDelay(true)
					.setMaxActivePerGroup(ScheduleVersionContentCreate.NAME, 5);
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String originalInstanceId = context.get(ORIGINAL_INSTANCE_ID).toString();
		String versionId = context.get(VERSION_INSTANCE_ID).toString();
		TimeTracker tracker = TimeTracker.createAndStart();
		try {
			ContentInfo content = instanceContentService.getContent(originalInstanceId, Content.PRIMARY_VIEW);
			if (!content.exists()) {
				LOGGER.debug("There is no [{}] content for instance - [{}]. Version content won't be stored!",
						Content.PRIMARY_VIEW, originalInstanceId);
				return;
			}

			String contentAsString = null;
			if (context.getIfSameType(PROCESS_WIDGETS, Boolean.class)) {
				contentAsString = processContent(content, originalInstanceId,
						context.getIfSameType(VERSION_CREATED_ON_DATE, Date.class)).asHtml();
			} else {
				contentAsString = content.asString();
			}

			EmfInstance dummy = new EmfInstance();
			dummy.setId(versionId);
			ContentInfo versionContent = instanceContentService.saveContent(dummy,
					buildVersionViewContent(contentAsString));
			if (!versionContent.exists()) {
				throw new EmfRuntimeException("Failed to store view content for version instance - " + versionId);
			}
		} finally {
			LOGGER.debug("View content for version - {} was processed and stored for {} ms.", versionId,
					tracker.stop());
		}
	}

	private static Idoc processContent(ContentInfo content, String originalInstanceId, Date versionCreatedOn) {
		try {
			Idoc idoc = Idoc.parse(content.getInputStream());
			// current instance id is used in the widget searches, but it should be the original id, not the version
			HandlerContext context = new HandlerContext(originalInstanceId);
			context.put(HANDLERS_CONTEXT_VERSION_DATE_KEY, versionCreatedOn);
			SearchContentNodeHandler.handle(idoc.widgets(), context);
			// search results are stored in the WidgetConfiguration from where the version handlers will retrieve them
			VersionContentNodeHandler.handle(idoc.widgets(), context);
			return idoc;
		} catch (IOException e) {
			throw new EmfRuntimeException("Version view content parsing failed.", e);
		}
	}

	private static Content buildVersionViewContent(String contentAsString) {
		return Content
				.createEmpty()
					.setContent(contentAsString, StandardCharsets.UTF_8)
					.setName(UUID.randomUUID() + "-instanceView.html")
					.setMimeType(MediaType.TEXT_HTML)
					.setPurpose(Content.PRIMARY_VIEW)
					.setVersionable(true)
					.setIndexable(true)
					.setView(true);
	}

}
