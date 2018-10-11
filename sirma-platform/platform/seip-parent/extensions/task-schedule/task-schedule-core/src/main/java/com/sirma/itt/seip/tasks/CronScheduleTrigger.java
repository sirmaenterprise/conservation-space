package com.sirma.itt.seip.tasks;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.OnTenantRemove;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.jms.annotations.TopicListener;

/**
 * Schedule executor that executes the cron scheduled actions using a {@link SchedulerTaskExecutor}
 *
 * @author BBonev
 */
@ApplicationScoped
public class CronScheduleTrigger extends BaseSchedulerTrigger {

	private static final Logger LOGGER = LoggerFactory.getLogger(CronScheduleTrigger.class);
	private static final Set<SchedulerEntryType> ENTRY_TYPE = EnumSet.of(SchedulerEntryType.CRON);

	/**
	 * The delay between check for new tasks of the timed scheduler executor in seconds.
	 */
	private static final long EXECUTOR_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(60L);

	/**
	 * The maximum number of threads that should be active for all tenants per node. The default value is number of
	 * cores times 10
	 */
	private int maxExecutionThreads = Runtime.getRuntime().availableProcessors() * 40;

	@Inject
	private SchedulerService schedulerService;

	@Override
	protected int getMaxExecutionThreads() {
		return maxExecutionThreads;
	}

	@Override
	protected Set<SchedulerEntryType> getEntryTypes() {
		return ENTRY_TYPE;
	}

	@Override
	protected long getExecutorDelayMillis() {
		return EXECUTOR_DELAY_MILLIS;
	}

	/**
	 * Check and schedule core tasks in the system security context
	 */
	@RunAsSystem(protectCurrentTenant = false)
	// load before automatic schedule tasks
	@Startup(order = 10)
	void checkAndScheduleCoreTasks() {
		checkAndScheduleTasks();
	}

	/**
	 * Check and schedule tenant tasks for all tenants
	 */
	@OnTenantAdd
	@RunAsAllTenantAdmins
	@Startup(order = 11)
	void checkAndScheduleTenantTasks() {
		checkAndScheduleTasks();
	}

	/**
	 * On tenant remove, stop all running tasks.
	 */
	@OnTenantRemove
	void onTenantRemove() {
		stopRunningTasks();
	}

	@TopicListener(jndi = SchedulerQueues.TASK_QUEUE, subscription = "cronTasks", selector = "type = 'CRON'")
	void onTask(Message message) throws JMSException {
		long id = message.getLongProperty("id");
		String time = message.getStringProperty("time");
		Date scheduleTime = ISO8601DateFormat.parse(time);
		try {
			SchedulerEntry entry = schedulerService.activate(id);
			if (entry == null) {
				// this should not happen in normal situations
				// unless someone cleared the database while before processing the message, but highly unlikely
				throw new IllegalArgumentException("Could not load CRON scheduler entry with id=" + id + " to activate");
			}
			entry.getConfiguration().setScheduleTime(scheduleTime);
			onEvent(entry);
		} catch (EmfConfigurationException e) {
			LOGGER.warn("Tried to activate scheduler entry {} with invalid action", id, e);
		}
	}

	private void onEvent(SchedulerEntry entry) { // NOSONAR
		if (SchedulerUtil.COMPLETED_STASUS.contains(entry.getStatus())) {
			// if the task is scheduled for execution we should cancel it if possible
			removeAndCancelCompletedTask(entry.getIdentifier());
			// nothing to do with completed entries
			return;
		}

		scheduleTask(entry);
	}

	private void removeAndCancelCompletedTask(String taskId) {
		SchedulerTask executor = runningTasks.get(taskId);
		if (executor != null && SchedulerUtil.COMPLETED_STASUS.contains(executor.getStatus())) {
			LOGGER.trace("Removing running task with id={}", taskId);
			runningTasks.remove(taskId).cancel();
		}
	}

	/**
	 * Schedule task for execution to wait in the execution queue in memory.
	 *
	 * @param entry
	 *            the scheduler entry
	 */
	@Override
	protected void scheduleTask(SchedulerEntry entry) {
		if (!isActive) {
			return;
		}
		if (isTaskScheduledAndRunning(entry)) {
			LOGGER.trace("Task {} is currently active. Skipping request to schedule it", entry.getIdentifier());
			// ignore request still in execution
			return;
		}
		// force active security context
		checkSecurityContext();

		// schedule the task for execution
		addRunningTask(scheduledExecutor.submit(entry, 0, this));
	}

	@SuppressWarnings("boxing")
	private void addRunningTask(SchedulerTask task) {
		String taskId = task.getEntry().getIdentifier();
		SchedulerTask executor = runningTasks.put(taskId, task);
		if (executor != null) {
			boolean cancel = executor.cancel();
			LOGGER.trace("Added new running task with id={}. Found running task with the same id. Cancelled={}", taskId,
					cancel);
		} else {
			LOGGER.trace("Added new running task with id={}", taskId);
		}
	}

	private boolean isTaskScheduledAndRunning(SchedulerEntry schedulerEntry) {
		// check if the task is scheduled for execution and check it's status to be sure.
		// note if the task is in the running tasks it should not have a status different than
		// the one checked bellow
		SchedulerTask task = getRunningTask(schedulerEntry);
		if (task != null) {
			SchedulerEntryStatus status = task.getStatus();
			// if the task is already scheduled we ignore the request
			switch (status) {
				case NOT_RUN:
				case RUNNING:
				case PENDING:
					if (task.isCanceled()) {
						removeRunningIfSame(task);
						return false;
					}
					if (isSchedulerChanged(task.getEntry(), schedulerEntry)) {
						task.cancel();
						removeRunningIfSame(task);
						LOGGER.debug("Task deleted from schedule and rescheduled with new cron");
						return false;
					}
					return true;
				default:
					// if for some reason the task is still marked as running just remove it and allow for the new task
					// to be added
					removeRunningIfSame(task);
					break;
			}
		}
		return false;
	}

	/**
	 * Check if cron expression or context of scheduler is changed.
	 *
	 * @param taskSchedulerEntry
	 *            shedulerEntry on already run task.
	 * @param schedulerEntry
	 *            new sheduleEntry.
	 * @return true if cron expression or context of <code>taskSchedulerEntry</code> and <code>schedulerEntry</code> are
	 *         not equals.
	 */
	private static boolean isSchedulerChanged(SchedulerEntry taskSchedulerEntry, SchedulerEntry schedulerEntry) {
		String taskCronExpression = taskSchedulerEntry.getConfiguration().getCronExpression();
		String newCronExpression = schedulerEntry.getConfiguration().getCronExpression();

		SchedulerContext taskContext = new SchedulerContext(taskSchedulerEntry.getContext());
		// on running entries that have extra element that breaks the checks for modifications so we remove it before doing the check
		taskContext.remove(SchedulerContext.SCHEDULER_ENTRY);
		SchedulerContext newContext = schedulerEntry.getContext();

		return !EqualsHelper.nullSafeEquals(taskCronExpression, newCronExpression)
				|| !EqualsHelper.nullSafeEquals(taskContext, newContext);
	}

	private SchedulerTask getRunningTask(SchedulerEntry schedulerEntry) {
		return runningTasks.get(schedulerEntry.getIdentifier());
	}

	@Override
	protected void onFailInTx(SchedulerTask task) {
		LOGGER.trace("Failed cron task {}", task.getEntry().getIdentifier());
		// for cron tasks we can only remove the entry from the running tasks as we should not terminate the task for
		// reaching any retry limit.
		onExecuteCanceled(task);
	}
}
