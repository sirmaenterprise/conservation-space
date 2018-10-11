package com.sirma.itt.seip.tasks;

/**
 * Callback interface that defines methods to be called from {@link SchedulerTask} invocation.
 *
 * @author BBonev
 */
public interface SchedulerTaskCallback {

	/**
	 * Called when the time for the task invocation happens. Should execute the task synchronously and return
	 * <code>true</code> for success
	 *
	 * @param task
	 *            the task that triggered the call
	 * @return true, if successful
	 */
	boolean onTimeout(SchedulerTask task);

	/**
	 * Called before task termination after successful execution.
	 *
	 * @param task
	 *            the task that triggered the call
	 */
	void onExecuteSuccess(SchedulerTask task);

	/**
	 * Called before task termination after unsuccessful execution.
	 *
	 * @param task
	 *            the task that triggered the call
	 */
	void onExecuteFail(SchedulerTask task);

	/**
	 * Called before task termination after task cancellation. The task may or may not be executed when this method is
	 * called if canceled before calling the {@link #onTimeout(SchedulerTask)} method.
	 *
	 * @param task
	 *            the task that triggered the call
	 */
	void onExecuteCanceled(SchedulerTask task);
}
