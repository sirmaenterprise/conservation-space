package com.sirma.itt.seip.instance.revision.steps;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.sep.export.ExportURIBuilder;

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

	static final String NAME = "exportTabsToPdfPublishStep";

	private static final String EXPORT_TABS = "exportTabs";
	private static final String REVISION_ID = "revisionId";
	private static final String SOURCE_INSTANCE_ID = "sourceInstance";
	private static final String AUTH_TOKEN = "token";

	private static final List<Pair<String, Class<?>>> ARGS = Arrays.asList(new Pair<>(EXPORT_TABS, Collection.class),
			new Pair<>(REVISION_ID, String.class), new Pair<>(SOURCE_INSTANCE_ID, String.class),
			new Pair<>(AUTH_TOKEN, String.class));

	@Inject
	private SchedulerService schedulerService;
	@Inject
	private ExportToPdfPublishService publisher;
	@Inject
	private DomainInstanceService domainInstanceService;
	@Inject
	private ExportURIBuilder uriBuilder;

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
		context.put(AUTH_TOKEN, uriBuilder.getCurrentJwtToken());
		context.put(EXPORT_TABS, (Serializable) ExportToPdfPublishService.getExportedTabs(publishContext));
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
		Instance revision = domainInstanceService.loadInstance(revisionId);
		publisher.publishInstance(() -> token, () -> tabIds, () -> sourceInstanceId, () -> revision);
	}

}
