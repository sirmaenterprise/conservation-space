package com.sirma.itt.seip.tasks;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Base implementation for scheduler triggers. Handles main task scheduler that reads the database and schedules tasks
 * for executions
 *
 * @author BBonev
 */
abstract class BaseSchedulerTrigger implements SchedulerTaskCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private MainSchedulerTrigger mainSchedulerTrigger;
	@Inject
	protected SchedulerTaskExecutor scheduledExecutor;
	@Inject
	private SchedulerEntryProvider dbDao;
	@Inject
	protected SchedulerEntryStore schedulerStore;
	@Inject
	private SchedulerExecuter schedulerExecuter;

	@Inject
	private SecurityContext securityContext;
	@Inject
	protected TransactionSupport transactionSupport;

	@Inject
	protected ContextualConcurrentMap<String, SchedulerTask> runningTasks;

	protected volatile boolean isActive = false;

	@PostConstruct
	void initialize() {
		isActive = true;

		runningTasks.onDestroy(map -> map.values().forEach(SchedulerTask::cancel));
		scheduledExecutor.setMaxConcurrentTasks(getMaxExecutionThreads());
	}

	/**
	 * On shutdown stops all tasks and executors.
	 */
	@PreDestroy
	void onShutdown() {
		LOGGER.info("Shutting down {}", getClass());

		isActive = false;

		SchedulerUtil.executeSilently((Executable) runningTasks::destroy);
		SchedulerUtil.executeSilently(scheduledExecutor::shutdown);
	}

	/**
	 * Remove all tasks from the runningTasks and cancel them.
	 */
	protected void stopRunningTasks() {
		Map<String, SchedulerTask> tasks = SchedulerUtil.supplySilently(runningTasks::clearContextValue);
		if (tasks != null) {
			tasks.values().forEach(SchedulerTask::cancel);
		}
	}

	/**
	 * Check and schedule tasks
	 */
	protected void checkAndScheduleTasks() {
		LOGGER.trace("Starting initial check for task in context: {}", securityContext.getCurrentTenantId());
		// the security context should already be initialized from the thread factory
		scheduleMainChecker();
	}

	private void scheduleMainChecker() {
		mainSchedulerTrigger.scheduleMainChecker(this::scheduleTimeout, getExecutorDelayMillis(),
				TimeUnit.MILLISECONDS);
	}

	private void scheduleTimeout() {
		if (!isActive) {
			return;
		}
		// the time for check is 1x and the checked interval is 2x
		// the time deviation is multiplied by two so that tasks that are at the boundary will be read before time
		// and should prevent issues with passed time for cron jobs
		dbDao.getTasksForExecution(getEntryTypes(), SchedulerUtil.ACTIVE_STASUS, getExecutorDelayMillis() * 2,
				getScheduleTaskFilter(), scheduleTaskConsumer());
	}

	private Consumer<SchedulerEntry> scheduleTaskConsumer() {
		return entry -> {
			try {
				scheduleTask(entry);
			} catch (RuntimeException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Could not add task {} to be executed due to: {}", entry.getIdentifier(),
							e.getMessage(), e);
				} else {
					LOGGER.warn("Could not add task {} to be executed due to: {}", entry.getIdentifier(),
							e.getMessage());
				}
			}
		};
	}

	/**
	 * Schedule task for for execution. This is called for new tasks that are loaded from the store for execution
	 *
	 * @param entry
	 *            the entry to schedule
	 */
	protected abstract void scheduleTask(SchedulerEntry entry);

	/**
	 * Gets the schedule task filter. The filter is used to determine the viability of the tasks. if the predicate
	 * returns <code>false</code> that task will not be scheduled and will be skipped. Default implementation accepts
	 * all tasks.
	 *
	 * @return the schedule task filter
	 */
	@SuppressWarnings("static-method")
	protected Predicate<SchedulerEntry> getScheduleTaskFilter() {
		return entry -> true;
	}

	/**
	 * Gets the entry types to load for processing. Should not be empty set
	 *
	 * @return the entry types
	 */
	protected abstract Set<SchedulerEntryType> getEntryTypes();

	/**
	 * Gets the executor delay in milliseconds. It's the time between 2 checks for new tasks to trigger.
	 *
	 * @return the executor delay in milliseconds
	 */
	protected abstract long getExecutorDelayMillis();

	/**
	 * Gets the max execution threads to use. Active tasks that are over the given limit will be queued
	 *
	 * @return the max execution threads
	 */
	protected abstract int getMaxExecutionThreads();

	/**
	 * Gets the active tasks.
	 *
	 * @return the active tasks
	 */
	public Collection<SchedulerTask> getActiveTasks() {
		return new ArrayList<>(runningTasks.values());
	}

	/**
	 * Checks for active tasks.
	 *
	 * @return true, if there is at least one running task
	 */
	public boolean hasActiveTasks() {
		return !runningTasks.isEmpty();
	}

	@Override
	public boolean onTimeout(SchedulerTask executor) {
		if (!isActive) {
			return false;
		}
		checkSecurityContext();
		try {
			SchedulerEntry entry = executor.getEntry();
			Future<SchedulerEntryStatus> result = schedulerExecuter.execute(entry, false, false);
			return result.get() == SchedulerEntryStatus.COMPLETED;
		} catch (Exception e) {
			LOGGER.warn("Could not execute action", e);
			return false;
		}
	}

	@Override
	public void onExecuteSuccess(SchedulerTask task) {
		if (!isActive) {
			return;
		}
		transactionSupport.invokeConsumerInTx(this::onSuccessInTx, task);
	}

	protected void onSuccessInTx(SchedulerTask task) {
		LOGGER.trace("Completed task {}", task.getEntry().getIdentifier());
		SchedulerEntry entry = task.getEntry();
		try {
			if (Thread.interrupted()) {
				LOGGER.info("Async {} task {} was canceled just before saving the result.",
						entry.getConfiguration().getType(), entry.getIdentifier());
			} else {
				// this method will not trigger cascade entry trigger via the scheduler entry change event
				schedulerStore.saveChanges(entry);
			}
		} finally {
			removeRunningIfSame(task);
		}
	}

	@Override
	public void onExecuteFail(SchedulerTask task) {
		if (!isActive) {
			return;
		}
		transactionSupport.invokeConsumerInTx(this::onFailInTx, task);
	}

	protected abstract void onFailInTx(SchedulerTask task);

	@Override
	public void onExecuteCanceled(SchedulerTask task) {
		if (!isActive) {
			return;
		}
		LOGGER.trace("Canceled cron task {}", task.getEntry().getIdentifier());
		removeRunningIfSame(task);
	}

	/**
	 * Removes the running {@link SchedulerTask} only if the given task matches the the one found in the
	 * running/archived tasks store
	 *
	 * @param task
	 *            the task to remove
	 * @return true, if successfully removed
	 */
	protected synchronized boolean removeRunningIfSame(SchedulerTask task) {
		String taskId = task.getEntry().getIdentifier();
		SchedulerTask current = runningTasks.get(taskId);
		if (nullSafeEquals(current, task)) {
			runningTasks.remove(taskId);
			LOGGER.trace("Removed task with ID={} from current store", taskId);
			return true;
		}
		return false;
	}

	protected void checkSecurityContext() {
		if (!securityContext.isActive()) {
			throw new EmfRuntimeException("Security context is not present. Cannot execute action!");
		}
	}
}
