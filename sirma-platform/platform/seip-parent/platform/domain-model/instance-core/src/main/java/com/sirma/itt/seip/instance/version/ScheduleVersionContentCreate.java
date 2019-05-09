package com.sirma.itt.seip.instance.version;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.version.VersionProperties.WidgetsHandlerContextProperties;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.handler.SearchContentNodeHandler;
import com.sirma.sep.content.idoc.handler.VersionContentNodeHandler;

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

	private static final String CONTENT_ID = "contentId";
	private static final String ORIGINAL_INSTANCE_ID = "originalInstanceId";
	private static final String VERSION_INSTANCE_ID = "versionInstanceId";
	private static final String VERSION_CREATED_ON_DATE = "versionCreatedOn";
	private static final String PROCESS_WIDGETS = "processWidgets";
	private static final String IS_VERSION_MODE_UPDATE = "isVersionModeUpdate";

	private static final List<Pair<String, Class<?>>> ARGUMENTS_VALIDATION = Arrays.asList(
			new Pair<>(VERSION_INSTANCE_ID, Serializable.class), new Pair<>(VERSION_CREATED_ON_DATE, Date.class),
			new Pair<>(PROCESS_WIDGETS, Boolean.class), new Pair<>(IS_VERSION_MODE_UPDATE, Boolean.class));

	private static final int MAX_RETRIES = 5;

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private VersionDao versionDao;

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return ARGUMENTS_VALIDATION;
	}

	/**
	 * Intercepts {@link CreateVersionContentEvent} and creates scheduled task for version view content creation. The
	 * task will be asynchronous and will be executed after the transaction is completed successfully and if the current
	 * version does not have view content.
	 *
	 * @param event {@link CreateVersionContentEvent} which triggers the creation of the task, which will process and
	 *        store the version content. Contains required information for the correct task creation
	 * @see TransactionSupport#invokeOnSuccessfulTransactionInTx(Executable)
	 * @see SchedulerService#schedule(String, SchedulerConfiguration, SchedulerContext)
	 */
	public void onCreateVersionContent(@Observes CreateVersionContentEvent event) {
		String versionInstanceId = event.getContext().getVersionInstanceId();
		if (versionInstanceId.isEmpty()) {
			throw new IllegalArgumentException("Version id should be available at this point.");
		}

		boolean isUpdate = VersionMode.UPDATE.equals(event.getContext().getVersionMode());
		if (!isUpdate && instanceContentService.getContent(versionInstanceId, Content.PRIMARY_VIEW).exists()) {
			LOGGER.warn("Version instance - [{}] already have view content.", versionInstanceId);
			return;
		}

		String originalInstanceId = event.getContext().getTargetInstanceId();
		ContentInfo content = instanceContentService.getContent(originalInstanceId, Content.PRIMARY_VIEW);
		if (!content.exists()) {
			LOGGER.warn("There is no [{}] content for instance - [{}]. Version content won't be stored!",
					Content.PRIMARY_VIEW, originalInstanceId);
			return;
		}

		SchedulerContext context = createContext(event, content.getContentId(), isUpdate);
		SchedulerConfiguration configuration = buildConfiguration(versionInstanceId);
		schedulerService.schedule(NAME, configuration, context);
	}

	private static SchedulerContext createContext(CreateVersionContentEvent event, String contentId, boolean isUpdate) {
		VersionContext versionContext = event.getContext();
		SchedulerContext context = new SchedulerContext(6);
		context.put(CONTENT_ID, contentId);
		context.put(VERSION_INSTANCE_ID, versionContext.getVersionInstanceId());
		context.put(ORIGINAL_INSTANCE_ID, versionContext.getTargetInstanceId());
		context.put(VERSION_CREATED_ON_DATE, versionContext.getCreationDate());
		context.put(PROCESS_WIDGETS, versionContext.shouldProcessWidgets());
		context.put(IS_VERSION_MODE_UPDATE, isUpdate);
		return context;
	}

	private static SchedulerConfiguration buildConfiguration(String id) {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration()
				.setType(SchedulerEntryType.TIMED)
					.setRemoveOnSuccess(true)
					.setPersistent(true)
					.setTransactionMode(TransactionMode.REQUIRED)
					.setMaxRetryCount(MAX_RETRIES)
					.setIncrementalDelay(true)
					.setMaxActivePerGroup(ScheduleVersionContentCreate.NAME, 5);
		configuration.setIdentifier(String.join("-", NAME, id));
		return configuration;
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String originalInstanceId = context.get(ORIGINAL_INSTANCE_ID).toString();
		// for backward compatibility: the content id is added later in the lifecycle of the action
		// there are old actions that when reexecuted failed due to missing content id
		String contentId = Objects.toString(context.get(CONTENT_ID), originalInstanceId);
		String versionId = context.get(VERSION_INSTANCE_ID).toString();
		TimeTracker tracker = TimeTracker.createAndStart();
		try {
			ContentInfo content = instanceContentService.getContent(contentId, Content.PRIMARY_VIEW);
			if (!content.exists()) {
				LOGGER.debug("There is no [{}] content for instance - [{}]. Version content won't be stored!",
						Content.PRIMARY_VIEW, originalInstanceId);
				return;
			}
			String contentAsString;
			if (context.getIfSameType(PROCESS_WIDGETS, Boolean.class)) {
				contentAsString = processContent(content, originalInstanceId,
						context.getIfSameType(VERSION_CREATED_ON_DATE, Date.class)).asHtml();
			} else {
				contentAsString = content.asString();
			}

			Boolean isVersionModeUpdate = context.getIfSameType(IS_VERSION_MODE_UPDATE, Boolean.class);
			ContentInfo versionContent = instanceContentService.saveContent(new EmfInstance(versionId),
					buildVersionViewContent(contentAsString, isVersionModeUpdate));
			if (!versionContent.exists()) {
				throw new EmfRuntimeException("Failed to store view content for version instance - " + versionId);
			}
		} finally {
			LOGGER.debug("View content for version - {} was processed and stored for {} ms.", versionId,
					tracker.stop());
		}
	}

	private Idoc processContent(ContentInfo content, String originalInstanceId, Date versionCreatedOn) {
		try {
			Idoc idoc = Idoc.parse(content.getInputStream());
			// current instance id is used in the widget searches, but it should be the original id, not the version
			HandlerContext context = new HandlerContext(originalInstanceId);
			VersionIdsCache cache = new VersionIdsCache(versionCreatedOn, versionDao::findVersionIdsByTargetIdAndDate);
			context.put(WidgetsHandlerContextProperties.VERSIONED_INSTANCES_CACHE_KEY, cache);
			context.put(WidgetsHandlerContextProperties.VERSION_DATE_KEY, versionCreatedOn);
			SearchContentNodeHandler.handle(idoc.widgets(), context);
			// search results are stored in the WidgetConfiguration from where the version handlers will retrieve them
			VersionContentNodeHandler.handle(idoc.widgets(), context);
			return idoc;
		} catch (IOException e) {
			throw new EmfRuntimeException("Version view content parsing failed.", e);
		}
	}

	private static Content buildVersionViewContent(String contentAsString, boolean isVersionModeUpdate) {
		return Content
				.createEmpty()
					.setContent(contentAsString, StandardCharsets.UTF_8)
					.setName(UUID.randomUUID() + "-instanceView.html")
					.setMimeType(MediaType.TEXT_HTML)
					.setPurpose(Content.PRIMARY_VIEW)
					.setVersionable(!isVersionModeUpdate)
					.setIndexable(true)
					.setView(true);
	}
}
