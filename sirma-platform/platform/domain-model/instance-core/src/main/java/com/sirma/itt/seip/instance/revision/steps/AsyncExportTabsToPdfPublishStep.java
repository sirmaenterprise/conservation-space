package com.sirma.itt.seip.instance.revision.steps;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT_LENGTH;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;

import java.io.File;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.export.PDFExporter;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.SectionNode.PublishMode;

/**
 * Export the tabs marked as publishable and replace them with a tab that will display the content of the instance. This
 * generally is the exported PDF.
 * <p>
 * <b>Note this step is not active as it's limited by the current authentication and IDP server. The JWT authentication
 * is not reliable after server restart. It's not deleted because it works but have a problem with authentication
 * initialization after server restart as the generated token is no longer recognized by the server and due to invalid
 * certificates it blocks the export process as it requires user interaction</b>
 *
 * @author BBonev
 */
@Named(AsyncExportTabsToPdfPublishStep.NAME)
@Extension(target = PublishStep.EXTENSION_NAME, order = 102.1, enabled = false)
public class AsyncExportTabsToPdfPublishStep extends SchedulerActionAdapter implements PublishStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String NAME = "exportTabsToPdfPublishStep";
	private static final String PATH = "/#/idoc/";

	private static final String EXPORT_TABS = "exportTabs";
	private static final String REVISION_ID = "revisionId";
	private static final String SOURCE_INSTANCE_ID = "sourceInstance";
	private static final String AUTH_TOKEN = "token";

	private static final List<Pair<String, Class<?>>> ARGS = Arrays.asList(new Pair<>(EXPORT_TABS, Collection.class),
			new Pair<>(REVISION_ID, String.class), new Pair<>(SOURCE_INSTANCE_ID, String.class),
			new Pair<>(AUTH_TOKEN, String.class));

	@Inject
	private PDFExporter exporter;
	@Inject
	private SecurityTokensManager tokensManager;
	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private SchedulerService schedulerService;
	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return ARGS;
	}

	@Override
	public String getName() {
		return Steps.EXPORT_TABS_AS_PDF.getName() + "_async";
	}

	@Override
	public void execute(PublishContext publishContext) {
		SchedulerContext context = new SchedulerContext();
		context.put(AUTH_TOKEN, getSecurityToken());
		context.put(EXPORT_TABS, (Serializable) getExportedTabs(publishContext));
		context.put(REVISION_ID, publishContext.getRevision().getId());
		context.put(SOURCE_INSTANCE_ID, publishContext.getRequest().getInstanceToPublish().getId());

		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.TIMED)
					.setScheduleTime(new Date())
					.setMaxActivePerGroup(NAME, 10)
					.setMaxRetryCount(50)
					.setPersistent(true)
					.setRemoveOnSuccess(true)
					.setRetryDelay(Long.valueOf(60))
					.setTransactionMode(TransactionMode.NOT_SUPPORTED);

		schedulerService.schedule(NAME, configuration, context);
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {

		String token = context.getIfSameType(AUTH_TOKEN, String.class);
		Collection<String> tabIds = context.getIfSameType(EXPORT_TABS, Collection.class);
		String sourceInstanceId = context.getIfSameType(SOURCE_INSTANCE_ID, String.class);
		String revisionId = context.getIfSameType(REVISION_ID, String.class);

		TimeTracker tracker = TimeTracker.createAndStart();

		Instance revision = domainInstanceService.loadInstance(revisionId);
		if (revision == null) {
			throw new EmfRuntimeException("Target revision " + revisionId + " not found");
		}
		LOGGER.debug("Exporting tabs {} for revision {}", tabIds, revisionId);

		String requestUri = buildPublishRequestUri(sourceInstanceId, tabIds, token);

		File exportedFile = callExport(requestUri);

		ContentInfo info = saveContent(revision, exportedFile);

		revision.add(PRIMARY_CONTENT_ID, info.getContentId());
		revision.add(CONTENT_LENGTH, info.getLength());
		revision.add(NAME, info.getName());
		revision.add(MIMETYPE, info.getMimeType());

		domainInstanceService
				.save(InstanceSaveContext.create(revision, new Operation(ActionTypeConstants.EDIT_DETAILS)));
		LOGGER.debug("Export for revision {} completed in {} ms", revisionId, tracker.stop());
	}

	private File callExport(String printRequestUri) {
		try {
			return exporter.export(printRequestUri);
		} catch (TimeoutException e) {
			throw new RollbackedRuntimeException("Export to pdf could not finish in time", e);
		}
	}

	private ContentInfo saveContent(Instance revision, File exportedFile) {
		Content content = Content
				.createEmpty()
					.setContent(exportedFile)
					.setIndexable(true)
					.setMimeType("application/pdf")
					.setPurpose(Content.PRIMARY_CONTENT)
					.setVersionable(true)
					.setName(exportedFile.getName());
		return instanceContentService.saveContent(revision, content);
	}

	private static String buildPublishRequestUri(String instanceId, Collection<String> tabs, String token) {
		URIBuilder builder = new URIBuilder();
		tabs.forEach(tabId -> builder.addParameter("tab", tabId));

		builder.addParameter("jwt", token).addParameter("mode", "print");

		try {
			return PATH + instanceId + builder.build().toASCIIString();
		} catch (URISyntaxException e) {
			throw new RollbackedRuntimeException("Could not build publish address", e);
		}
	}

	private static Collection<String> getExportedTabs(PublishContext publishContext) {
		return publishContext
				.getView()
					.getSections()
					.stream()
					.filter(node -> node.getPublishMode() == PublishMode.EXPORT)
					.map(ContentNode::getId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
	}

	private String getSecurityToken() {
		return tokensManager.getCurrentJwtToken().orElseThrow(
				() -> new SecurityException("Current user should have a security token"));
	}

}
