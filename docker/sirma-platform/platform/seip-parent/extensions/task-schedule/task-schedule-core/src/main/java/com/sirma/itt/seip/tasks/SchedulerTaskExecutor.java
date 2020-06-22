package com.sirma.itt.seip.tasks;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.context.ThreadFactories;

/**
 * Non singleton component that operates a thread pool for executing {@link SchedulerTask}s.
 * <p>
 * Current implementation supports only {@link SchedulerEntryType#EVENT} and {@link SchedulerEntryType#CRON} types.<br>
 * Tasks for for {@link SchedulerEntryType#EVENT} will run at most one time and the tasks for
 * {@link SchedulerEntryType#CRON} will run zero , one or unspecified number of times depending on the
 * {@link #setKeepAliveTime(long, TimeUnit)}
 *
 * @author BBonev
 */
class SchedulerTaskExecutor {

	private static final int MIN_THREADS = Runtime.getRuntime().availableProcessors() * 5;
	private static final long DEFAULT_KEEP_ALIVE = TimeUnit.SECONDS.toMillis(330);

	private ThreadPoolExecutor scheduledExecutor;
	private final SecurityContextManager securityContextManager;

	private long keepAliveTime = DEFAULT_KEEP_ALIVE;
	/**
	 * The maximum number of threads that should be active for all tenants per node. The default value is number of
	 * cores times 20
	 */
	private int maxExecutionThreads = MIN_THREADS;

	/**
	 * Instantiates a new scheduler task executor that uses the given security context manager to execute tasks in
	 * security context based on the user that calls the {@link #submit(SchedulerEntry, long, SchedulerTaskCallback)}
	 * method.
	 *
	 * @param securityContextManager
	 *            the security context manager
	 */
	@Inject
	SchedulerTaskExecutor(SecurityContextManager securityContextManager) {
		this.securityContextManager = securityContextManager;
	}

	private void initialize() {
		if (scheduledExecutor != null) {
			return;
		}
		ThreadFactory threadFactory = ThreadFactories.createSystemThreadFactory(securityContextManager,
				ThreadFactories::asDaemon);

		maxExecutionThreads = Math.max(maxExecutionThreads, MIN_THREADS);
		scheduledExecutor = new ThreadPoolExecutor(getMaxConcurrentTasks(), getMaxConcurrentTasks(), 1L, TimeUnit.MINUTES,
				new LinkedBlockingQueue<>(), threadFactory);
		scheduledExecutor.allowCoreThreadTimeOut(true);
	}

	/**
	 * Shutdowns the thread pool and cancels all running or scheduled tasks. <br>
	 * This method should be called explicitly on to perform the shutdown
	 */
	void shutdown() {
		SchedulerUtil.supplySilently(scheduledExecutor::shutdownNow);
	}

	/**
	 * Submit a entry for execution after the given optional delay. The created task will report back to the given
	 * {@link SchedulerTaskCallback}.
	 *
	 * @param entry
	 *            the entry to generate task for
	 * @param delay
	 *            the optional delay before task execution. Does not apply for recurring tasks
	 * @param callback
	 *            the callback to be used for reporting the task operations
	 * @return the created and scheduled task
	 */
	@SuppressWarnings("unchecked")
	SchedulerTask submit(SchedulerEntry entry, long delay, SchedulerTaskCallback callback) {
		initialize();
		Objects.requireNonNull(entry, "ScheduleEntry cannot be null");
		Objects.requireNonNull(callback, "Callback cannot be null");
		// schedule the task for execution
		TaskExecutor task = createTask(entry, delay, callback);
		task.setFuture((Future<Object>) scheduledExecutor.submit(task));
		return task;
	}

	private TaskExecutor createTask(SchedulerEntry entry, long delay, SchedulerTaskCallback callback) {
		SchedulerEntryType schedulerType = entry.getConfiguration().getType();
		if (schedulerType == SchedulerEntryType.CRON) {
			return new RecurringTask(entry, callback, keepAliveTime, TimeUnit.MILLISECONDS, securityContextManager);
		} else if (schedulerType == SchedulerEntryType.TIMED) {
			return new TaskExecutor(entry, callback, delay, securityContextManager);
		}
		throw new IllegalArgumentException("Not supported scheduler type " + schedulerType);
	}

	/**
	 * Sets the maximum concurrent tasks. If more tasks are submitted they will be queued
	 *
	 * @param maxTasks
	 *            the new max concurrent tasks
	 */
	void setMaxConcurrentTasks(int maxTasks) {
		maxExecutionThreads = Math.max(maxTasks, MIN_THREADS);
		if (scheduledExecutor != null) {
			scheduledExecutor.setMaximumPoolSize(maxExecutionThreads);
		}
	}

	/**
	 * Gets the maximum concurrent task. If more tasks are submitted they will be queued
	 *
	 * @return the max concurrent tasks
	 */
	int getMaxConcurrentTasks() {
		return maxExecutionThreads;
	}

	/**
	 * Sets the keep alive time for recurring tasks created for {@link SchedulerEntryType#CRON} scheduler entries. if
	 * the wait time is longer than the keep alive time then the task will complete after at least one execution.
	 *
	 * @param time
	 *            the time to wait
	 * @param unit
	 *            the time unit for the given time
	 */
	void setKeepAliveTime(long time, TimeUnit unit) {
		keepAliveTime = unit.toMillis(time);
	}

	/**
	 * Gets the keep alive time in milliseconds.
	 *
	 * @return the keep alive time
	 * @see #setKeepAliveTime(long, TimeUnit)
	 */
	long getKeepAliveTime() {
		return keepAliveTime;
	}

}
