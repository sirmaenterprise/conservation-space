package com.sirma.itt.emf.scheduler;

import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

/**
 * Scheduler entry task executor. The executor waits some time before executing the task if
 * needed. If the task fails then is rescheduled for execution if allowed.
 *
 * @author BBonev
 */
public class TaskExecutor implements Callable<Boolean> {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(TaskExecutor.class);
	/** The entry. */
	private SchedulerEntry entry;
	/** The fixed delay. */
	private long fixedDelay;
	/** The executor. */
	private TimedScheduleExecutor executor;

	private volatile SchedulerEntryStatus status = SchedulerEntryStatus.NOT_RUN;

	/**
	 * Instantiates a new task executor.
	 *
	 * @param entry
	 *            the entry to be executed when the time comes
	 * @param executor
	 *            the timed executor that scheduled the task
	 * @param fixedDelay
	 *            the preferred fixed delay to wait before the task execution only if the value is
	 *            greater than zero.
	 */
	public TaskExecutor(SchedulerEntry entry, TimedScheduleExecutor executor, long fixedDelay) {
		this.executor = executor;
		this.entry = entry;
		this.fixedDelay = fixedDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean call() {
		boolean reScheduled = false;
		try {
			SchedulerConfiguration configuration = entry.getConfiguration();
			long waitTime = getWaitTime(configuration, fixedDelay);
			if (waitTime > 0L) {
				try {
					status = SchedulerEntryStatus.PENDING;
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					LOGGER.warn(e);
					// if we are interrupted by external call we probably need to abort the
					// action
					return false;
				}
			}

			status = SchedulerEntryStatus.RUNNING;
			SchedulerEntryStatus nextState = null;
			// execute
			if (!executor.executeAction(entry)) {
				configuration.setRetryCount(configuration.getRetryCount() + 1);
				nextState = SchedulerEntryStatus.RUN_WITH_ERROR;
				status = SchedulerEntryStatus.RUN_WITH_ERROR;
				if (configuration.getRetryCount() >= configuration.getMaxRetryCount()) {
					nextState = SchedulerEntryStatus.FAILED;
				} else {
					long delay = executor.getDefaultRetryDelay();
					if (configuration.getRetryDelay() != null) {
						delay = configuration.getRetryDelay();
					}
					if (configuration.isIncrementalDelay()) {
						delay *= configuration.getRetryCount();
					}
					// convert to milliseconds
					delay *= 1000;
					// if the time for next execution is less then the retry check interval
					// place it in the execution queue now
					if (delay <= executor.getTimedExecutorDelay()) {
						reScheduled = true;
						// set state before pushing it into the queue
						entry.setStatus(nextState);
						executor.scheduleTask(entry, delay);
					}
				}
			} else {
				// reset retries after successful execution.
				// this is not to trigger other late execution if there was an error while executing
				// the and after retry it was a success
				configuration.setRetryCount(0);
			}
			if (!reScheduled && (nextState == null)) {
				nextState = SchedulerEntryStatus.PENDING;
				Date scheduleTime = configuration.getNextScheduleTime();
				// if there is no next execution or the time has passed
				if (scheduleTime == null) {
					nextState = SchedulerEntryStatus.COMPLETED;
				}
			}
			entry.setStatus(nextState);
			executor.finishedExecution(entry);
			return Boolean.TRUE;
		} finally {
			if (!reScheduled) {
				executor.removeRunningTask(entry.getId());
			}
			status = SchedulerEntryStatus.COMPLETED;
		}
	}

	/**
	 * Gets the wait time based on the given configuration and the fixed preferred delay. If
	 * fixedDelay is greater than zero then it will be returned.
	 *
	 * @param configuration
	 *            the configuration
	 * @param fixedDelayParam
	 *            the fixed delay
	 * @return the wait time
	 */
	private long getWaitTime(SchedulerConfiguration configuration, long fixedDelayParam) {
		if (fixedDelayParam > 0L) {
			return fixedDelayParam;
		}
		Date scheduleTime = configuration.getNextScheduleTime();
		if (scheduleTime != null) {
			long waitTime = scheduleTime.getTime() - System.currentTimeMillis();
			if (waitTime > 0L) {
				return waitTime;
			}
		}
		return -1L;
	}

	/**
	 * Gets the current executor status.
	 *
	 * @return the status
	 */
	public SchedulerEntryStatus getStatus() {
		return status;
	}
}