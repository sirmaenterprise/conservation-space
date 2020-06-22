package com.sirma.itt.seip.instance.content.share;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.RunAs;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;

import java.util.Collections;
import java.util.List;

/**
 * Base class for creating shared publicly content. Contains common logic that is needed for both documents that are
 * binary (.pdf, .doc, etc) and documents that are created (iDocs).
 *
 * @author A. Kunchev
 */
public abstract class BaseShareInstanceContentAction extends SchedulerActionAdapter {

	protected static final String DATA = "%data%";

	private static final String SHARE_TASKS_GROUP = "share-group";
	private static final int ACTIVE_TASKS_MAX_COUNT = 3;

	static final String SHARED_CONTENT_PURPOSE_PREFIX = "shared-";

	private static final List<Pair<String, Class<?>>> ARGUMENTS = Collections.singletonList(
			new Pair<>(DATA, ContentShareData.class));

	/**
	 * The number of maximal retries for the action before it is stated as failed.
	 */
	private static final int MAX_RETRIES = 10;

	/**
	 * Builds common configuration for the actions which are responsible for email attachments creation. This task
	 * configuration is executed immediately.
	 *
	 * @return base configuration for email attachment creation
	 */
	public static SchedulerConfiguration buildImmediateConfiguration() {
		return new DefaultSchedulerConfiguration().setType(SchedulerEntryType.TIMED)
				.setRemoveOnSuccess(true)
				.setPersistent(true)
				.setTransactionMode(TransactionMode.REQUIRED)
				.setMaxRetryCount(MAX_RETRIES)
				.setIncrementalDelay(true)
				.setMaxActivePerGroup(SHARE_TASKS_GROUP, ACTIVE_TASKS_MAX_COUNT)
				.setRunAs(RunAs.USER);
	}

	/**
	 * Builds event based schedule API configuration. This means that the schedule task will not be started immediately
	 * but instead will be started when the event {@link ShareInstanceContentEvent} is thrown/
	 *
	 * @param schedulerService
	 * 		the {@link SchedulerService}
	 * @param taskIdentifier
	 * 		the identifier of the scheduled task.
	 * @return the newly created schedule configuration.
	 */
	public static SchedulerConfiguration buildEventConfiguration(SchedulerService schedulerService,
			String taskIdentifier) {
		SchedulerConfiguration configuration = schedulerService.buildConfiguration(
				new ShareInstanceContentEvent(taskIdentifier))
				.setRemoveOnSuccess(true)
				.setPersistent(true)
				.setMaxRetryCount(MAX_RETRIES)
				.setIncrementalDelay(true)
				.setMaxActivePerGroup(SHARE_TASKS_GROUP, ACTIVE_TASKS_MAX_COUNT)
				.setRunAs(RunAs.USER);
		configuration.setIdentifier(taskIdentifier);
		return configuration;
	}

	/**
	 * Creates common context for the actions which are responsible for email attachments creation.
	 *
	 * @param instanceId
	 * 		the data need from the instances. As key is the instance id and value the title of instance that will
	 * 		be used as name of the generated files
	 * @param title
	 * 		the instance's title
	 * @param token
	 * 		the security token that is used when building URI for export
	 * @param contentId
	 * 		the already existing content id.
	 * @param format
	 * 		the format in which creatable documents (iDocs) will be exported. Can be either 'pdf' or 'word'.
	 * @return base context for email attachments creation
	 */
	public static SchedulerContext createContext(String instanceId, String title, String token, String contentId,
			String format) {
		SchedulerContext context = new SchedulerContext();
		ContentShareData contentShareData = ContentShareData.buildEmpty()
				.setInstanceId(instanceId)
				.setTitle(title)
				.setToken(token)
				.setFormat(format)
				.setContentId(contentId);
		context.put(DATA, contentShareData);
		return context;
	}

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return ARGUMENTS;
	}
}
