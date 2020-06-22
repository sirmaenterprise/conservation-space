package com.sirma.itt.seip.tasks;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.util.SecureRunnable;

/**
 * Scheduler entry task executor. The scheduled action will be executed only once. The executor waits some time before
 * executing the task if needed. After execution proper callback method will be called depending on the outcome of the
 * action.
 *
 * @author BBonev
 */
class TaskExecutor extends SecureRunnable implements JsonRepresentable, SchedulerTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	protected final SchedulerEntry entry;
	protected final long fixedDelay;
	protected final SchedulerTaskCallback callback;
	protected volatile SchedulerEntryStatus status = SchedulerEntryStatus.NOT_RUN;
	protected long activeTime;
	protected long idleTime = System.currentTimeMillis();
	private final long expectedExecutionTime;

	/**
	 * Instantiates a new task executor.
	 *
	 * @param entry
	 *            the entry to be executed when the time comes
	 * @param callback
	 *            the callback that need to be called for task execution and results
	 * @param fixedDelay
	 *            the preferred fixed delay to wait before the task execution only if the value is greater than zero.
	 * @param securityContextManager
	 *            the security context manager
	 */
	TaskExecutor(SchedulerEntry entry, SchedulerTaskCallback callback, long fixedDelay,
			SecurityContextManager securityContextManager) {
		super(securityContextManager);
		this.callback = Objects.requireNonNull(callback, "Callback parameter cannot be null");
		this.entry = Objects.requireNonNull(entry, "Scheduler entry parameter cannot be null");
		this.fixedDelay = fixedDelay;
		// fix the execution time at the moment of creation of the task
		expectedExecutionTime = System.currentTimeMillis() + Math.max(fixedDelay, 0);
	}

	@Override
	protected void doRun() {
		try {
			if (!waitUntilReadyToExecute()) {
				// canceled while waiting
				return;
			}
			beforeExecute();

			execute();

			afterExecute();
		} finally {
			beforeTermination();
		}
	}

	/**
	 * Called just before task invocation. Initialize the statistics time and changes the state to running.
	 */
	protected void beforeExecute() {
		idleTime = System.currentTimeMillis() - idleTime;
		activeTime = System.currentTimeMillis();

		status = SchedulerEntryStatus.RUNNING;
	}

	protected void execute() {
		SchedulerEntryStatus nextState = null;
		// execute
		boolean executedSuccessfully = callback.onTimeout(this);
		if (isCanceled()) {
			// cancelled during execution of the task
			return;
		}
		SchedulerConfiguration configuration = entry.getConfiguration();
		if (executedSuccessfully) {
			// reset retries after successful execution.
			// this is not to trigger other late execution if there was an error while executing
			// the and after retry it was a success
			configuration.setRetryCount(0);
		} else {
			LOGGER.trace("Task {} completed with error", entry.getIdentifier());
			// this is left here to keep track how many times the execution has failed
			// consequently
			configuration.setRetryCount(configuration.getRetryCount() + 1);
			nextState = SchedulerEntryStatus.RUN_WITH_ERROR;
			status = SchedulerEntryStatus.RUN_WITH_ERROR;
		}

		if (nextState == null) {
			nextState = SchedulerEntryStatus.PENDING;
			Date scheduleTime = configuration.getNextScheduleTime();
			// if there is no next execution or the time has passed
			if (scheduleTime == null) {
				nextState = SchedulerEntryStatus.COMPLETED;
			}
		}
		entry.setStatus(nextState);
	}

	/**
	 * Called right after task invocation is complete.
	 */
	protected void afterExecute() {
		// TODO implement TaskExecutor.afterExecute!

	}

	/**
	 * Before before task termination. Calls the proper call back method to notify for task execution result.
	 */
	protected void beforeTermination() {
		activeTime = System.currentTimeMillis() - activeTime;

		if (isCanceled()) {
			callback.onExecuteCanceled(this);
		} else if (status == SchedulerEntryStatus.RUN_WITH_ERROR) {
			callback.onExecuteFail(this);
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
	protected boolean waitUntilReadyToExecute() {
		long waitTime = getWaitTime();
		if (waitTime > 0L) {
			try {
				status = SchedulerEntryStatus.PENDING;
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				cancel();
				LOGGER.info("Task {} was interrupted", entry.getIdentifier());
				LOGGER.trace("Task {} was interrupted", entry.getIdentifier(), e);
			}
		}
		// if we are interrupted by external call we probably need to abort the action
		return !isCanceled();
	}

	/**
	 * Gets the wait time based on the expected execution time or fixed preferred delay. If fixedDelay is greater than
	 * zero then it will be returned.
	 *
	 * @return the wait time before executing the task or -1 it should be executed immediately
	 */
	protected long getWaitTime() {
		if (fixedDelay > 0L) {
			return fixedDelay;
		}
		Date scheduleTime = entry.getExpectedExecutionTime();
		if (scheduleTime != null) {
			long waitTime = scheduleTime.getTime() - System.currentTimeMillis();
			if (waitTime > 0L) {
				return waitTime;
			}
		}
		return -1L;
	}

	/**
	 * Gets the current executor status. Note that this is different than the {@link SchedulerEntry#getStatus()}
	 *
	 * @return the status
	 */
	@Override
	public SchedulerEntryStatus getStatus() {
		return status;
	}

	/**
	 * Gets the scheduler entry that triggered the current task
	 *
	 * @return the entry
	 */
	@Override
	public SchedulerEntry getEntry() {
		return entry;
	}

	@Override
	public boolean cancel() {
		status = SchedulerEntryStatus.CANCELED;
		return super.cancel();
	}

	@Override
	@SuppressWarnings("boxing")
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "id", entry.getIdentifier());
		JsonUtil.addToJson(object, "status", status);
		JsonUtil.addToJson(object, "type", entry.getConfiguration().getType());
		JsonUtil.addToJson(object, "expectedExecutionTime", getExpectedExecutionTime());
		if (activeTime == 0) {
			JsonUtil.addToJson(object, "idleTime", System.currentTimeMillis() - idleTime);
		} else {
			JsonUtil.addToJson(object, "activeTime", activeTime);
		}
		return object;
	}

	/**
	 * Gets the expected execution time.
	 *
	 * @return the expected execution time
	 */
	protected String getExpectedExecutionTime() {
		return TypeConverterUtil.getConverter().convert(String.class, new Date(expectedExecutionTime));
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		throw new UnsupportedOperationException();
	}
}
