package com.sirma.itt.seip.security.util;

import java.util.concurrent.Future;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Base secure invoker that can be used for task scheduling. Supports feature store and option to be canceled
 *
 * @author BBonev
 */
public abstract class BaseSecureTask extends SecureInvoker {

	protected final TimeTracker tracker;
	private volatile boolean canceled = false;
	/**
	 * Store the future object created by the executor service for later use.
	 */
	private Future<?> future;
	private Long executionTime;

	/**
	 * Instantiates a new base secure task.
	 *
	 * @param securityContextManager
	 *            the security context manager
	 */
	public BaseSecureTask(SecurityContextManager securityContextManager) {
		super(securityContextManager);
		tracker = new TimeTracker();
	}

	/**
	 * Returns the elapsed execution time or -1 if not started, yet.
	 *
	 * @return elapsed time in milliseconds
	 */
	public long executionTime() {
		if (executionTime != null) {
			return executionTime.longValue();
		}
		return tracker.elapsedTime();
	}

	/**
	 * Getter method for future.
	 *
	 * @param <V>
	 *            the future result type
	 * @return the future
	 */
	@SuppressWarnings("unchecked")
	public <V> Future<V> getFuture() {
		return (Future<V>) future;
	}

	/**
	 * Setter method for future.
	 *
	 * @param <V>
	 *            the future result type
	 * @param future
	 *            the future to set
	 */
	public <V> void setFuture(Future<V> future) {
		this.future = future;
	}

	/**
	 * Checks if the task has been requested to cancel it's execution.
	 *
	 * @return true, if is canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Attempts to cancel execution of this task.
	 *
	 * @return <code>true</code> cancelled successfully
	 */
	public boolean cancel() {
		return cancel(false);
	}

	/**
	 * Attempts to cancel execution of this task.
	 *
	 * @param allowInterrupt
	 *            the allow interrupting of the thread
	 * @return <code>true</code> cancelled successfully
	 */
	public boolean cancel(boolean allowInterrupt) {
		canceled = true;
		return getFuture() == null || getFuture().cancel(allowInterrupt);
	}

	/**
	 * Marks the task for started
	 */
	protected void start() {
		if (!tracker.isStarted()) {
			tracker.begin();
		}
	}

	/**
	 * Marks the task for completed
	 */
	protected void complete() {
		if (executionTime == null) {
			executionTime = Long.valueOf(tracker.stop());
		}
	}

}