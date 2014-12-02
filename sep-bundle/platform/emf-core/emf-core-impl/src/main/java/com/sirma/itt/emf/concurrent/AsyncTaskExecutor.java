package com.sirma.itt.emf.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;

/**
 * Default implementation for the {@link TaskExecutor} service.
 * <p>
 * REVIEW: add per user perUserUploadPoolSize mapping so that the threads used by a user can be
 * limited based on the global property not to per method call.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class AsyncTaskExecutor implements TaskExecutor {
	// per user configuration
	/** The Constant MAX_PARALLEL_THREADS_PER_USER. */
	public static final Integer MAX_PARALLEL_THREADS_PER_USER = 10;

	/** The Constant DEFAULT_PARALLEL_THREADS_PER_USER. */
	public static final int DEFAULT_PARALLEL_THREADS_PER_USER = 5;
	// Pool configuration
	/** The Constant MIN_RUNNING_THREADS. */
	public static final Integer MIN_RUNNING_THREADS = 20;

	/** The Constant MAX_RUNNING_THREADS. */
	public static final Integer MAX_RUNNING_THREADS = 200;

	/** The Constant DEFAULT_RUNNING_THREADS. */
	public static final int DEFAULT_RUNNING_THREADS = 100;

	/** The Constant SYSTEM_USER. */
	private static final String SYSTEM_USER = "%SYSTEM%";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskExecutor.class);
	/** The trace. */
	private static final boolean TRACE = LOGGER.isTraceEnabled();

	/** The parallel uploads pool size config. */
	@Inject
	@Config(name = EmfConfigurationProperties.ASYNCHRONOUS_TASK_POOL_SIZE, defaultValue = ""
			+ DEFAULT_RUNNING_THREADS)
	private Integer parallelUploadsPoolSizeConfig;
	/** The parallel uploads config. */
	@Inject
	@Config(name = EmfConfigurationProperties.ASYNCHRONOUS_TASK_PER_USER_POOL_SIZE, defaultValue = ""
			+ DEFAULT_PARALLEL_THREADS_PER_USER)
	private Integer parallelUploadsConfig;

	/** The Constant LOCK. */
	private final ReentrantLock lock = new ReentrantLock();
	/** The upload pool size. */
	private int threadPoolSize;

	/** The per user pool size. */
	private int perUserPoolSize;
	/** The upload pool. */
	private ForkJoinPool threadPool;

	/** The running user threads. */
	private Map<String, AtomicInteger> runningUserThreads = new LinkedHashMap<String, AtomicInteger>(
			500);

	/** The thread worker factory. */
	private ForkJoinWorkerThreadFactory threadWorkerFactory = new AsyncForkJoinWorkerThreadFactory();

	/** The current pool size. */
	private AtomicInteger currentPoolSize = new AtomicInteger(0);

	private UncaughtExceptionHandler uncaughtExceptionHandler = new ExecutorUncaughtExceptionHandler();

	/**
	 * Initialize the thread pool.
	 */
	@PostConstruct
	public void initialize() {
		// the first thread that comes here locks takes the lock all other will exit
		if (!lock.tryLock()) {
			return;
		}
		try {
			// if the pool is already created then we a done
			// but if the pool is stopped then we have to recreate it
			if ((threadPool != null) && !threadPool.isShutdown() && !threadPool.isTerminated()) {
				return;
			}

			initializeVariables();
			threadPool = new ForkJoinPool(threadPoolSize, threadWorkerFactory,
					uncaughtExceptionHandler, true);
			LOGGER.debug(
					"Created upload pool for up to {} threads and {} uploads per user uploads",
					threadPoolSize, perUserPoolSize);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Initialize variables.
	 */
	private void initializeVariables() {
		if (parallelUploadsConfig > MAX_PARALLEL_THREADS_PER_USER) {
			perUserPoolSize = MAX_PARALLEL_THREADS_PER_USER;
		} else if (parallelUploadsConfig <= 0) {
			// no parallelism
			perUserPoolSize = 1;
		} else {
			perUserPoolSize = parallelUploadsConfig;
		}

		if (parallelUploadsPoolSizeConfig > MAX_RUNNING_THREADS) {
			threadPoolSize = MAX_RUNNING_THREADS;
		} else if (parallelUploadsPoolSizeConfig < MIN_RUNNING_THREADS) {
			// no parallelism
			threadPoolSize = MIN_RUNNING_THREADS;
		} else {
			threadPoolSize = parallelUploadsPoolSizeConfig;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	public <T extends GenericAsyncTask> void execute(List<T> tasks) {
		if ((tasks == null) || tasks.isEmpty()) {
			// nothing to execute
			return;
		}
		// we try to determine a user so we can schedule and control how many threads are separated
		// for each user
		String runFor = getExecutingUser();
		// initialize the thread pool size for that user
		initializeUserThreadPool(runFor);
		try {
			// execute tasks with option to try to rollback if possible
			executeInternal(tasks, runFor, true);
		} finally {
			releaseSystemUserThreadPool();
		}
	}

	/**
	 * Release system user thread pool.
	 */
	private void releaseSystemUserThreadPool() {
		// if our thread remove the entry from the pool at the end
		if (Thread.currentThread() instanceof AsyncForkJoinWorkerThread) {
			lock.lock();
			try {
				runningUserThreads.remove(Thread.currentThread().getName());
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * Initialize user thread pool.
	 * 
	 * @param runFor
	 *            the run for
	 */
	private void initializeUserThreadPool(String runFor) {
		try {
			// initialize the user thread count
			lock.lock();
			if (!runningUserThreads.containsKey(runFor)) {
				runningUserThreads.put(runFor, new AtomicInteger(perUserPoolSize));
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets the executing user.
	 * 
	 * @return the executing user
	 */
	private String getExecutingUser() {
		String runFor;
		// if we have a pool task that requires a pool execution also we should create for the given
		// user new pool entry so that he could execute the tasks
		if (Thread.currentThread() instanceof AsyncForkJoinWorkerThread) {
			runFor = Thread.currentThread().getName();
			if (TRACE) {
				LOGGER.trace("Will initiate cascade thread pool execution for thread: " + runFor);
			}
		} else {
			User user = SecurityContextManager.getFullAuthentication();
			runFor = SYSTEM_USER;
			if (user != null) {
				runFor = user.getName();
			}
		}
		return runFor;
	}

	/**
	 * Execute the given list of tasks for the given user.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param tasks
	 *            the tasks to execute
	 * @param user
	 *            the user the current user
	 * @param rollbackOnError
	 *            the rollback on error
	 */
	protected <T extends GenericAsyncTask> void executeInternal(List<T> tasks, String user,
			boolean rollbackOnError) {
		List<T> runningTasks = new ArrayList<T>(perUserPoolSize);
		List<T> queuedTasks = new LinkedList<T>();
		List<T> allTasks = new LinkedList<T>();

		// start first batch of tasks up to the allowed per user limit
		scheduleInitialBatchOfTasks(tasks, user, runningTasks, queuedTasks, allTasks);

		List<Throwable> errors = new LinkedList<Throwable>();
		List<GenericAsyncTask> fails = new LinkedList<GenericAsyncTask>();
		List<GenericAsyncTask> successfulTasks = new ArrayList<GenericAsyncTask>(tasks.size());
		// wait for the tasks to complete and execute all remaining tasks
		executeTasks(user, runningTasks, queuedTasks, allTasks, errors, fails, successfulTasks);

		// process results and call the pre complete methods
		// if we have any errors act accordingly
		if (!fails.isEmpty()) {
			processOnFail(tasks, rollbackOnError, errors, fails, successfulTasks);
		} else {
			processOnSuccess(successfulTasks);
		}
	}

	/**
	 * Execute tasks.Wait for the tasks to complete and execute all remaining tasks
	 * 
	 * @param <T>
	 *            the generic type
	 * @param user
	 *            the user
	 * @param runningTasks
	 *            the running tasks
	 * @param queuedTasks
	 *            the queued tasks
	 * @param allTasks
	 *            the all tasks
	 * @param errors
	 *            the errors
	 * @param fails
	 *            the fails
	 * @param successfulTasks
	 *            the successful tasks
	 */
	private <T extends GenericAsyncTask> void executeTasks(String user, List<T> runningTasks,
			List<T> queuedTasks, List<T> allTasks, List<Throwable> errors,
			List<GenericAsyncTask> fails, List<GenericAsyncTask> successfulTasks) {
		// REVIEW: this probably need to be rewritten using only the running and queued tasks
		while (!allTasks.isEmpty() || !queuedTasks.isEmpty()) {
			try {
				// we will wait some time before first next check
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// not interested
			}
			Iterator<T> it = allTasks.iterator();
			while (it.hasNext()) {
				// check if the task is complete and put it in the proper list
				processCompletedTask(user, errors, fails, successfulTasks, it);

				// if we have more tasks to run submit them for execution
				it = submitRemainingTasks(user, runningTasks, queuedTasks, allTasks, it);
			}
		}
	}

	/**
	 * Submit remaining tasks. If we have more tasks to run submit them for execution
	 * 
	 * @param <T>
	 *            the generic type
	 * @param user
	 *            the user
	 * @param runningTasks
	 *            the running tasks
	 * @param queuedTasks
	 *            the queued tasks
	 * @param allTasks
	 *            the all tasks
	 * @param it
	 *            the it
	 * @return the iterator
	 */
	private <T extends GenericAsyncTask> Iterator<T> submitRemainingTasks(String user,
			List<T> runningTasks, List<T> queuedTasks, List<T> allTasks, Iterator<T> it) {
		// if we have more tasks we add them to the list
		// schedule it and reset the iterator
		Iterator<T> localIterator = it;
		if ((isThreadAvailable(user)) && !queuedTasks.isEmpty()) {
			acquire(user);
			if (TRACE) {
				LOGGER.trace("Acquired new thread for user {} remaining={}", user,
						runningUserThreads.get(user));
			}
			T newTask = queuedTasks.remove(0);
			runningTasks.add(newTask);
			threadPool.submit((ForkJoinTask<?>) newTask);
			localIterator = allTasks.iterator();
		}
		return localIterator;
	}

	/**
	 * Process completed task. Check if the task is complete and put it in the proper list
	 * 
	 * @param <T>
	 *            the generic type
	 * @param user
	 *            the user
	 * @param errors
	 *            the errors
	 * @param fails
	 *            the fails
	 * @param successfulTasks
	 *            the successful tasks
	 * @param it
	 *            the it
	 */
	private <T extends GenericAsyncTask> void processCompletedTask(String user,
			List<Throwable> errors, List<GenericAsyncTask> fails,
			List<GenericAsyncTask> successfulTasks, Iterator<T> it) {
		GenericAsyncTask asyncTask = it.next();
		if (asyncTask.isDone()) {
			it.remove();
			release(user);
			if (TRACE) {
				LOGGER.trace("Releasing worker for user " + user
						+ " available threads now: " + runningUserThreads.get(user));
			}
		}
		if (asyncTask.isCompletedNormally()) {
			if (Boolean.TRUE.equals(asyncTask.getRawResult())) {
				// execution completed
			} else {
				LOGGER.error("Failed to execute task {}", asyncTask.getClass()
						.getSimpleName());
			}
			// execute the success method when all are successful
			successfulTasks.add(asyncTask);
		} else if (asyncTask.isCompletedAbnormally()) {
			Throwable exception = asyncTask.getException();
			if (exception != null) {
				LOGGER.error("Failed to execute task {} with: {}", asyncTask.getClass()
						.getSimpleName(), exception.getMessage());
				errors.add(exception);
			} else {
				LOGGER.error("Failed to execute task {} without exception", asyncTask
						.getClass().getSimpleName());
			}
			fails.add(asyncTask);
		}
	}

	/**
	 * Schedule initial batch of tasks. Start first batch of tasks up to the allowed per user limit
	 * 
	 * @param <T>
	 *            the generic type
	 * @param tasks
	 *            the tasks
	 * @param user
	 *            the user
	 * @param runningTasks
	 *            the running tasks
	 * @param queuedTasks
	 *            the queued tasks
	 * @param allTasks
	 *            the all tasks
	 */
	private <T extends GenericAsyncTask> void scheduleInitialBatchOfTasks(List<T> tasks,
			String user, List<T> runningTasks, List<T> queuedTasks, List<T> allTasks) {
		for (T genericAsyncTask : tasks) {
			allTasks.add(genericAsyncTask);
			if (isThreadAvailable(user)) {
				acquire(user);
				if (TRACE) {
					LOGGER.trace("Acquired new thread for user {} remaining={}", user,
							runningUserThreads.get(user));
				}
				threadPool.submit((ForkJoinTask<?>) genericAsyncTask);
				runningTasks.add(genericAsyncTask);
			} else {
				queuedTasks.add(genericAsyncTask);
			}
		}
	}

	/**
	 * Process on success.
	 * 
	 * @param successfulTasks
	 *            the successful tasks
	 */
	private void processOnSuccess(List<GenericAsyncTask> successfulTasks) {
		for (GenericAsyncTask task : successfulTasks) {
			try {
				if (TRACE) {
					LOGGER.trace("Executed task {} successfully", task.getClass().getSimpleName());
				}
				task.executeOnSuccess();
			} catch (Exception e) {
				LOGGER.error("Failed to execute {}.executeOnSuccess() method due to {}", task
						.getClass().getName(), e.getMessage(), e);
			}
		}
	}

	/**
	 * Process on fail.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param tasks
	 *            the tasks
	 * @param rollbackOnError
	 *            the rollback on error
	 * @param errors
	 *            the errors
	 * @param fails
	 *            the fails
	 * @param successfullTasks
	 *            the successfull tasks
	 */
	private <T extends GenericAsyncTask> void processOnFail(List<T> tasks, boolean rollbackOnError,
			List<Throwable> errors, List<GenericAsyncTask> fails,
			List<GenericAsyncTask> successfullTasks) {
		// if found any errors and rollback if required
		if (rollbackOnError) {
			LOGGER.warn("There are " + fails.size() + " failed tasks out of " + tasks.size()
					+ ". Rolling back " + successfullTasks.size() + " successful tasks.");
			for (GenericAsyncTask task : successfullTasks) {
				try {
					task.onRollback();
				} catch (Exception e) {
					if (TRACE) {
						LOGGER.trace("Failed to roll back a task ", e);
					}
					errors.add(e);
				}
			}
		}
		// if any errors call the proper methods and if some throws an exception it will be
		// thrown back later.
		for (GenericAsyncTask genericAsyncTask : fails) {
			try {
				genericAsyncTask.executeOnFail();
			} catch (Exception e) {
				errors.add(e);
			}
		}

		// if we have some errors we throw the first one so we can rollback
		// transaction.
		if (!errors.isEmpty()) {
			LOGGER.warn("When executing async tasks {} failed with errors. Execeptions are:",
					errors.size());
			for (Throwable throwable : errors) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			throw new EmfRuntimeException(errors.get(0));
		}
	}

	/**
	 * Checks if is thread available for execution.
	 * 
	 * @param user
	 *            the user
	 * @return true, if is thread available
	 */
	protected boolean isThreadAvailable(String user) {
		return runningUserThreads.get(user).get() > 0;
	}

	/**
	 * Decrement the available threads for the user.
	 * 
	 * @param user
	 *            the user
	 */
	protected void acquire(String user) {
		currentPoolSize.incrementAndGet();
		runningUserThreads.get(user).decrementAndGet();
	}

	/**
	 * Release a thread for the user.
	 * 
	 * @param user
	 *            the user
	 */
	protected void release(String user) {
		currentPoolSize.decrementAndGet();
		runningUserThreads.get(user).incrementAndGet();
	}

	/**
	 * Default exception handler for the execution pool.
	 * 
	 * @author BBonev
	 */
	private static class ExecutorUncaughtExceptionHandler implements UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			// handle custom exceptions - non for now
		}

	}

}
