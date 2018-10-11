package com.sirma.itt.seip.tasks;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.OnTenantRemove;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirmaenterprise.sep.jms.annotations.TopicListener;

/**
 * Schedule executor that executes the timed scheduled actions using a {@link SchedulerTaskExecutor}
 *
 * @author BBonev
 */
@ApplicationScoped
public class TimedScheduleTrigger extends BaseSchedulerTrigger {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimedScheduleTrigger.class);
	private static final Set<SchedulerEntryType> ENTRY_TYPE = EnumSet.of(SchedulerEntryType.TIMED);
	private static final String DEFAULT_GROUP = "DEFAULT_GROUP";

	/**
	 * The delay between check for new tasks of the timed scheduler executor in seconds. <b>Default value: 60</b>
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "scheduler.timer.checkInterval", defaultValue = "60", sensitive = true, type = Long.class, label = "The delay between check for new tasks of the timed scheduler executor in seconds.")
	private ConfigurationProperty<Long> timedExecutorDelay;

	@Inject
	private ContextualConcurrentMap<String, AtomicInteger> activateGroups;

	@Inject
	private SchedulerService schedulerService;

	/**
	 * The maximum number of threads that should be active for all tenants per node. The default value is number of
	 * cores times 20
	 */
	private int maxExecutionThreads = Runtime.getRuntime().availableProcessors() * 40;

	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.DEPLOYMENT)
	void preloadConfigurations() {
		// this ensures that the configuration is loaded before the first use in case of missing transaction
		// otherwise it causes problems when loading the configuration value from the database
		timedExecutorDelay.get();
	}

	/**
	 * Check and schedule core tasks in the system security context
	 */
	@RunAsSystem(protectCurrentTenant = false)
	// load before automatic schedule tasks
	@Startup(order = 10)
	void checkAndScheduleTimedTasks() {
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

	@OnTenantRemove
	void onTenantRemove() {
		stopRunningTasks();
		activateGroups.clearContextValue();
	}
	
	@Override
	protected int getMaxExecutionThreads() {
		return maxExecutionThreads;
	}

	@Override
	protected long getExecutorDelayMillis() {
		return TimeUnit.SECONDS.toMillis(timedExecutorDelay.get());
	}

	@Override
	protected Set<SchedulerEntryType> getEntryTypes() {
		return ENTRY_TYPE;
	}

	/**
	 * On shutdown stops all tasks and executors.
	 */
	@Override
	@PreDestroy
	void onShutdown() {
		super.onShutdown();

		activateGroups.clear();
	}

	@TopicListener(jndi = SchedulerQueues.TASK_QUEUE, subscription = "timedTasks", selector = "type = 'TIMED'")
	void onTask(Message message) throws JMSException {
		long id = message.getLongProperty("id");
		String time = message.getStringProperty("time");
		Date scheduleTime = ISO8601DateFormat.parse(time);
		LOGGER.trace("Got new scheduler entry with id={} to be executed at {}", id, time);
		try {
			SchedulerEntry entry = schedulerService.activate(id);
			if (entry == null) {
				// this happens when multiple tasks are scheduled in a short period of time
				// and the time for accepting all messages is greater than the scheduler API internal timer
				// for picking tasks from database. The timer may schedule task for execution from the database before
				// the message to be processed from the queue
				LOGGER.trace("Could not load TIMED entry with id={}. Probably already processed", id);
				return;
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

		// if the task is scheduled for execution we should return because we cannot do more
		if (isTaskScheduledAndRunning(entry)) {
			LOGGER.trace("Task with ID={} is already running. Skipping request", entry.getIdentifier());
			return;
		}

		rescheduleEntry(entry);
	}

	private void removeAndCancelCompletedTask(String taskId) {
		SchedulerTask task = runningTasks.get(taskId);
		if (task != null && SchedulerUtil.COMPLETED_STASUS.contains(task.getStatus())) {
			LOGGER.trace("Removing running task with id={}", taskId);
			runningTasks.remove(taskId).cancel();
		}
	}

	void rescheduleEntry(SchedulerEntry entry) {
		Date nextScheduleDate = entry.getConfiguration().getNextScheduleTime();

		// ensure the next check for time difference is enough not to be in the past
		long nextScheduleTime = System.currentTimeMillis() + 50;
		if (nextScheduleDate != null) {
			nextScheduleTime = nextScheduleDate.getTime();
		}

		// how much time is needed to pass before we need to execute the entry
		long diff = nextScheduleTime - System.currentTimeMillis();
		// the second check will prevent new tasks to override the group limit and to be executed immediately
		if (diff < getExecutorDelayMillis() && isGroupAllowed(entry)) {
			// scheduled time is in the limit of the checker interval we schedule the task write
			// away. when picked up by the query it will be skipped when called the schedule method again
			scheduleTask(entry, -1);
		}
		// check when we should wake up the executor when the delay is more then the default
		// wait time
	}

	@Override
	protected void scheduleTask(SchedulerEntry entry) {
		scheduleTask(entry, -1);
	}

	/**
	 * Schedule task for execution to wait in the execution queue in memory.
	 *
	 * @param schedulerEntry
	 *            the scheduler entry
	 * @param fixedDelay
	 *            the fixed delay
	 */
	private void scheduleTask(SchedulerEntry schedulerEntry, long fixedDelay) {
		if (!isActive) {
			return;
		}
		if (isTaskScheduledAndRunning(schedulerEntry)) {
			LOGGER.trace("Task {} is currently being processed. Skipping request to schedule it",
					schedulerEntry.getIdentifier());
			// ignore request still in execution
			return;
		}
		// force active security context
		checkSecurityContext();

		LOGGER.trace("Creating new task for execution for id={}", schedulerEntry.getId());

		long delay = fixedDelay >= 0 ? fixedDelay : 0;

		incrementGroupMember(schedulerEntry);
		// schedule the task for execution
		SchedulerTask task = scheduledExecutor.submit(schedulerEntry, delay, this);

		addRunningTask(schedulerEntry.getIdentifier(), task);
	}

	@SuppressWarnings("boxing")
	private void addRunningTask(String taskId, SchedulerTask task) {
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
		SchedulerTask executor = getRunningTask(schedulerEntry);
		if (executor != null) {
			SchedulerEntryStatus status = executor.getStatus();
			// if the task is already scheduled we ignore the request
			switch (status) {
				case NOT_RUN:
				case RUNNING:
				case PENDING:
					return true;
				default:
					break;
			}
		}
		return false;
	}

	private SchedulerTask getRunningTask(SchedulerEntry schedulerEntry) {
		return runningTasks.get(schedulerEntry.getIdentifier());
	}

	@Override
	protected Predicate<SchedulerEntry> getScheduleTaskFilter() {
		return this::isGroupAllowed;
	}

	private boolean isGroupAllowed(SchedulerEntry entry) {
		int maxActive = getMaxAllowed(entry.getConfiguration().getMaxActivePerGroup());
		String group = getValidGroupName(entry.getConfiguration().getGroup());
		return activateGroups.computeIfAbsent(group, k -> new AtomicInteger()).get() < maxActive;
	}

	@SuppressWarnings("boxing")
	private synchronized void incrementGroupMember(SchedulerEntry entry) {
		String validGroup = getValidGroupName(entry.getConfiguration().getGroup());
		int newValue = activateGroups.computeIfAbsent(validGroup, k -> new AtomicInteger()).incrementAndGet();
		LOGGER.trace("Incremented group count to {}-{}={}", entry.getIdentifier(), validGroup, newValue);
	}

	@SuppressWarnings("boxing")
	private synchronized void decrementGroupMember(SchedulerEntry entry) {
		String validGroup = getValidGroupName(entry.getConfiguration().getGroup());
		// if for some reason this is removed while there are running tasks
		int newValue = activateGroups.computeIfAbsent(validGroup, k -> new AtomicInteger(1)).decrementAndGet();
		LOGGER.trace("Decremented group count to {}-{}={}", entry.getIdentifier(), validGroup, newValue);
		if (newValue == 0) {
			activateGroups.remove(validGroup);
		}
	}

	private static String getValidGroupName(String group) {
		return StringUtils.isEmpty(group) ? DEFAULT_GROUP : group;
	}

	private static int getMaxAllowed(int max) {
		return max <= 0 ? Integer.MAX_VALUE : max;
	}

	@SuppressWarnings("boxing")
	public Map<String, Integer> getActiveGroupsCount() {
		Map<String, Integer> active = new HashMap<>();
		for (Entry<String, AtomicInteger> entry : activateGroups.entrySet()) {
			active.put(entry.getKey(), entry.getValue().get());
		}
		return active;
	}

	@Override
	protected synchronized boolean removeRunningIfSame(SchedulerTask task) {
		decrementGroupMember(task.getEntry());
		return super.removeRunningIfSame(task);
	}

	@Override
	protected void onFailInTx(SchedulerTask task) {
		if (!isActive) {
			return;
		}
		LOGGER.trace("Finished task {} with error", task.getEntry().getIdentifier());
		// already running new instance of the task nothing to do here
		if (!removeRunningIfSame(task)) {
			LOGGER.trace("Failed task {} was probably cancel and already running or already stopped",
					task.getEntry().getIdentifier());
			return;
		}

		SchedulerEntry entry = task.getEntry();
		SchedulerConfiguration configuration = entry.getConfiguration();

		Date nextScheduleTime = configuration.getNextScheduleTime();
		if (nextScheduleTime == null) {
			LOGGER.trace("Mark task {} for failed", task.getEntry().getIdentifier());
			// reached max retries probably or nothing to do more
			entry.setStatus(SchedulerEntryStatus.FAILED);
			schedulerStore.saveChanges(entry);
			return;
		}

		entry.setStatus(SchedulerEntryStatus.RUN_WITH_ERROR);

		long diff = nextScheduleTime.getTime() - System.currentTimeMillis();
		try {
			schedulerStore.saveChanges(entry);
		} finally {
			if (diff < getExecutorDelayMillis()) {
				LOGGER.trace("Rescheduling failed task {} in {} ms", task.getEntry().getIdentifier(), diff);
				scheduleTask(entry, diff);
			}
		}
	}
}
