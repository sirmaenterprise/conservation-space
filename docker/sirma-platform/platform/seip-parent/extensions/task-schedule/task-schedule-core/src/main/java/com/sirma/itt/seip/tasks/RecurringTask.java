package com.sirma.itt.seip.tasks;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Scheduler asynchronous task for recurring cron events. The task will run until the way time is bigger than 10 times
 * the {@link SchedulerTaskCallback#getExecutorDelay()}. If not the task will be saved and the current instance will be
 * terminated and the invoker is supposed to create and start new instance when the times come for next execution.
 * Normally this is a case for recurring tasks with interval bigger than 15 minutes
 *
 * @author BBonev
 */
class RecurringTask extends TaskExecutor {

	/**
	 * Multiplier used when calculating if the task should be persisted as pending and stopped or should continue to
	 * stay active if possible
	 */
	// current value is equal to 5 min and 30 sec. normally the check interval is at one minute so 30 seconds so that to
	// minimize race conditions
	private static final long DEFAULT_IDLE_TIME = 330;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final long keepAliveTime;
	private boolean finished = false;
	private long runCount = 0;
	private long failCount = 0;

	/**
	 * Instantiates a new recurring task with default keep alive time
	 *
	 * @param entry
	 *            the entry
	 * @param callback
	 *            the to be used for time events
	 * @param securityContextManager
	 *            the security context manager
	 */
	RecurringTask(SchedulerEntry entry, SchedulerTaskCallback callback,
			SecurityContextManager securityContextManager) {
		this(entry, callback, DEFAULT_IDLE_TIME, TimeUnit.SECONDS, securityContextManager);
	}

	/**
	 * Instantiates a new recurring task with custom keep alive time
	 *
	 * @param entry
	 *            the entry
	 * @param executor
	 *            the executor
	 * @param keepAliveTime
	 *            the keep alive time. If the interval between the task activations is bigger than the given
	 *            {@code keepAliveTime} then the task will be terminated
	 * @param unit
	 *            the time unit for the keep alive time
	 * @param securityContextManager
	 *            the security context manager
	 */
	RecurringTask(SchedulerEntry entry, SchedulerTaskCallback executor, long keepAliveTime, TimeUnit unit,
			SecurityContextManager securityContextManager) {
		super(entry, executor, 0, securityContextManager);
		this.keepAliveTime = unit.toMillis(keepAliveTime);
	}

	@Override
	protected void doRun() {
		try {
			// wait for the first activation
			waitUntilReadyToExecute();

			do {
				if (isCanceled()) {
					return;
				}
				beforeExecute();

				execute();

				afterExecute();
			} while (waitUntilReadyToExecute());
		} finally {
			beforeTermination();
		}
	}

	@Override
	protected void afterExecute() {
		super.afterExecute();
		idleTime = System.currentTimeMillis();
		activeTime = 0L;
		runCount = runCount + 1L;
		if (status == SchedulerEntryStatus.RUN_WITH_ERROR) {
			failCount = failCount + 1L;
		}
	}

	@Override
	protected void beforeTermination() {
		if (finished) {
			return;
		}
		if (isCanceled()) {
			callback.onExecuteCanceled(this);
		} else {
			status = SchedulerEntryStatus.COMPLETED;
			callback.onExecuteSuccess(this);
		}
	}

	/**
	 * Wait until ready to execute.
	 *
	 * @return true, if successful
	 */
	@Override
	protected boolean waitUntilReadyToExecute() {
		long waitTime = getWaitTime();
		if (waitTime > 0L) {
			// if the wait time is bigger than the allowed idle time, no need to keep the entry in the memory
			// so store it as pending, and stop the tasks this should be for tasks that are with interval bigger than
			// 10-15 minutes. If for some reason the keepAlive time is less then the wait time we do not cancel the job
			// if not run at least once
			if (waitTime > keepAliveTime && status != SchedulerEntryStatus.NOT_RUN) {
				entry.setStatus(SchedulerEntryStatus.PENDING);
				callback.onExecuteSuccess(this);
				finished = true;
				return false;
			}
			try {
				status = SchedulerEntryStatus.PENDING;
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				cancel();
				LOGGER.warn("Task {} was interrupted", entry.getIdentifier());
				LOGGER.trace("Task {} was interrupted", entry.getIdentifier(), e);
			}
		}
		// if we are interrupted by external call we probably need to abort the action
		if (isCanceled()) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the wait time based on the current configuration.
	 *
	 * @return the wait time before executing the task
	 */
	@Override
	protected long getWaitTime() {
		Date scheduleTime = entry.getConfiguration().getNextScheduleTime();
		if (scheduleTime != null) {
			long waitTime = scheduleTime.getTime() - System.currentTimeMillis();
			if (waitTime > 0L) {
				return waitTime;
			}
		}
		return -1L;
	}

	@Override
	@SuppressWarnings("boxing")
	public JSONObject toJSONObject() {
		JSONObject object = super.toJSONObject();
		JsonUtil.addToJson(object, "runCount", runCount);
		JsonUtil.addToJson(object, "failCount", failCount);
		return object;
	}

	@Override
	protected String getExpectedExecutionTime() {
		return TypeConverterUtil.getConverter().convert(String.class, entry.getConfiguration().getNextScheduleTime());
	}
}