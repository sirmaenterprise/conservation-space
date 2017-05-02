package com.sirma.itt.seip.tasks;

import java.util.concurrent.Future;

/**
 * Represents a scheduler entry task that runs on a separate thread. The task waits some time before executing if
 * needed. If the task fails then is rescheduled for execution if allowed.
 *
 * @author BBonev
 */
public interface SchedulerTask {

	/**
	 * Gets the current executor status. Note that this is different than the {@link SchedulerEntry#getStatus()}
	 *
	 * @return the status
	 */
	SchedulerEntryStatus getStatus();

	/**
	 * Gets the scheduler entry that triggered the current task
	 *
	 * @return the entry
	 */
	SchedulerEntry getEntry();

	/**
	 * Cancel task invocation, If not already run it will be cancelled without execution. If currently executing the
	 * result should not be persisted.
	 *
	 * @return true, if successful
	 */
	boolean cancel();

	/**
	 * Checks if the task has been requested to cancel it's execution.
	 *
	 * @return true, if is canceled
	 */
	boolean isCanceled();

	/**
	 * Gets a future that can be used to wait for the task to complete
	 *
	 * @param <V>
	 *            the value type
	 * @return the future
	 */
	<V> Future<V> getFuture();
}
