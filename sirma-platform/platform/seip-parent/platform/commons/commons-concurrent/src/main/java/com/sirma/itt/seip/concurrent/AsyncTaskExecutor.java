package com.sirma.itt.seip.concurrent;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.concurrent.event.NonTxAsyncCallableEvent;
import com.sirma.itt.seip.concurrent.event.TxAsyncCallableEvent;
import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Default implementation for the {@link TaskExecutor} service.
 * <p>
 * REVIEW: add per user perUserUploadPoolSize mapping so that the threads used by a user can be limited based on the
 * global property not to per method call.
 *
 * @author BBonev
 */
@ApplicationScoped
class AsyncTaskExecutor implements TaskExecutor {
	// per user configuration
	public static final int MIN_PARALLEL_THREADS_PER_USER = 5;
	public static final int MAX_PARALLEL_THREADS_PER_USER = 20;
	public static final int DEFAULT_PARALLEL_THREADS_PER_USER = 10;
	// Pool configuration
	public static final int MIN_RUNNING_THREADS = 20;
	public static final int MAX_RUNNING_THREADS = 500;
	public static final int DEFAULT_RUNNING_THREADS = 200;

	private static final String SYSTEM_USER = "%SYSTEM%";

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskExecutor.class);

	/** The parallel uploads pool size config. */
	@ConfigurationPropertyDefinition(system = true, defaultValue = ""
			+ DEFAULT_RUNNING_THREADS, sensitive = true, type = Integer.class, label = "Defines the pool size for parallel tasks. The pool is shared between all users.<br>Minimum value should not be less then the per user configuration.<br>Max pool size is 100. Values greater than this will be set to 100")
	private static final String ASYNCHRONOUS_TASK_POOL_SIZE = "asynchronous.task.poolSize";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "asynchronous.task.perUser.poolSize", defaultValue = ""
			+ DEFAULT_PARALLEL_THREADS_PER_USER, sensitive = true, type = Integer.class, system = true, label = "Defines the number of parallel tasks allowed per user.<br>Maximum value: 10. Values greater than this will be set to 10.")
	private ConfigurationProperty<Integer> parallelUploadsConfig;

	@ConfigurationGroupDefinition(type = ForkJoinPool.class, system = true, properties = {
			ASYNCHRONOUS_TASK_POOL_SIZE })
	private static final String ASYNCHRONOUS_TASK_POOL = "asynchronous.task.pool";

	@Inject
	@Configuration(ASYNCHRONOUS_TASK_POOL)
	private ConfigurationProperty<ForkJoinPool> threadPool;

	@Inject
	private EventService eventService;

	@Inject
	private SecurityContextManager contextManager;
	@Inject
	private SecurityContext securityContext;

	@Inject
	private ContextualLock lock;

	/** The per user pool size. */
	private int perUserPoolSize;

	/** The running user threads. */
	@Inject
	private ContextualMap<String, AtomicInteger> runningUserThreads;

	/** The thread worker factory. */
	private static ForkJoinWorkerThreadFactory threadWorkerFactory = new AsyncForkJoinWorkerThreadFactory();

	/** The current pool size. */
	private AtomicInteger currentPoolSize = new AtomicInteger(0);

	@Inject
	private AsyncSecurityDecorator securityDecorator;

	@ConfigurationConverter(ASYNCHRONOUS_TASK_POOL)
	static ForkJoinPool createAsyncPool(GroupConverterContext context) {
		int poolSize = getThreadPoolSize(context.getValue(ASYNCHRONOUS_TASK_POOL_SIZE));
		LOGGER.debug("Creating pool for up to {} threads", poolSize);
		return new ForkJoinPool(poolSize, threadWorkerFactory, new ExecutorUncaughtExceptionHandler(), true);
	}

	/**
	 * Initialize the thread pool.
	 */
	@PostConstruct
	void initialize() {
		initializeVariables();
		// if thread pool size is changed the old thread pool will be shutdown gracefully and
		// new will be created using the new configuration
		threadPool.addValueDestroyListener(ForkJoinPool::shutdown);
	}

	/**
	 * Initialize variables.
	 */
	private void initializeVariables() {
		setPerUserPoolSize(parallelUploadsConfig);
		parallelUploadsConfig.addConfigurationChangeListener(this::setPerUserPoolSize);
	}

	private static int getThreadPoolSize(ConfigurationProperty<Integer> poolSizeConfig) {
		int config = poolSizeConfig.get();
		if (config > MAX_RUNNING_THREADS) {
			return MAX_RUNNING_THREADS;
		} else if (config < MIN_RUNNING_THREADS) {
			// no parallelism
			return MIN_RUNNING_THREADS;
		}
		return config;
	}

	private void setPerUserPoolSize(ConfigurationProperty<Integer> perUserPoolSizeConfig) {
		int config = perUserPoolSizeConfig.get();
		if (config > MAX_PARALLEL_THREADS_PER_USER) {
			perUserPoolSize = MAX_PARALLEL_THREADS_PER_USER;
		} else if (config <= 0) {
			perUserPoolSize = MIN_PARALLEL_THREADS_PER_USER;
		} else {
			perUserPoolSize = config;
		}
	}

	@Override
	public <T extends GenericAsyncTask> void execute(List<T> tasks) {
		if (tasks == null || tasks.isEmpty()) {
			// nothing to execute
			return;
		}
		// we try to determine a user so we can schedule and control how many threads are separated
		// for each user
		execute(tasks, getExecutingUser());
	}

	@Override
	public <T extends GenericAsyncTask> void execute(List<T> tasks, String executeAs) {
		if (tasks == null || tasks.isEmpty()) {
			// nothing to execute
			return;
		}
		String runFor = executeAs;
		if (runFor == null) {
			runFor = SYSTEM_USER;
		}
		// initialize the thread pool size for that user
		initializeUserThreadPool(runFor);
		try {
			// execute tasks with option to try to rollback if possible
			executeInternal(tasks, runFor, true);
		} finally {
			releaseSystemUserThreadPool();
		}
	}

	@Override
	public <T extends GenericAsyncTask> Future<Object> submit(List<T> tasks, boolean transactionalContext,
			String executeAs) {
		SimpleFuture<Object> futureResult = new SimpleFuture<>();
		if (tasks == null || tasks.isEmpty()) {
			// nothing to execute
			futureResult.completed(Boolean.TRUE);
			return futureResult;
		}
		List<T> taskCopy = new ArrayList<>(tasks);
		String caller = executeAs == null ? getExecutingUser() : executeAs;
		EmfEvent event;
		Supplier<?> callable = () -> {
			this.execute(taskCopy, caller);
			return Boolean.TRUE;
		};
		if (transactionalContext) {
			event = new TxAsyncCallableEvent(callable, futureResult);
		} else {
			event = new NonTxAsyncCallableEvent(callable, futureResult);
		}
		eventService.fire(event);
		return futureResult;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Future<T> executeAsync(Supplier<T> supplier, FutureCallback<T> futureCallback) {
		SimpleFuture<T> futureResult = new SimpleFuture<>(futureCallback);
		eventService.fire(new NonTxAsyncCallableEvent(supplier, (CompletableOperation<Object>) futureResult));
		return futureResult;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Future<T> executeAsyncInTx(Supplier<T> supplier, FutureCallback<T> futureCallback) {
		SimpleFuture<T> futureResult = new SimpleFuture<>(futureCallback);
		eventService.fire(new TxAsyncCallableEvent(supplier, (CompletableOperation<Object>) futureResult));
		return futureResult;
	}

	@Override
	public Future<?> executeAsync(Executable executable) {
		SimpleFuture<Object> futureResult = new SimpleFuture<>();
		eventService.fire(new NonTxAsyncCallableEvent(executable.asSupplier(), futureResult));
		return futureResult;
	}

	@Override
	public Future<?> executeAsyncInTx(Executable executable) {
		SimpleFuture<Object> futureResult = new SimpleFuture<>();
		eventService.fire(new TxAsyncCallableEvent(executable.asSupplier(), futureResult));
		return futureResult;
	}

	@Override
	public <V> Future<V> submit(Supplier<V> supplier) {
		return securityDecorator.invokeSecure(threadPool.get()::submit, supplier);
	}

	@Override
	public Future<?> submit(Executable executable) {
		return securityDecorator.invokeSecure(threadPool.get()::submit, executable);
	}

	@Override
	public <V> Future<V> submit(Supplier<V> supplier, Consumer<V> onSuccess, Consumer<Throwable> onFail) {
		Consumer<V> onSuccessLocal = onSuccess == null ? CollectionUtils.emptyConsumer() : onSuccess;
		Consumer<Throwable> onFailLocal = onFail == null ? CollectionUtils.emptyConsumer() : onFail;

		return securityDecorator.invokeSecure(threadPool.get()::submit, () -> {
			try {
				V v = supplier.get();
				onSuccessLocal.accept(v);
				return v;
			} catch (Exception e) {
				onFailLocal.accept(e);
				throw e;
			}
		});
	}

	@Override
	public <V, R> Future<R> submitMapped(Supplier<V> supplier, Function<V, R> onSuccess,
			Function<Throwable, R> onFail) {
		Objects.requireNonNull(onSuccess, "On success function is required!");

		return securityDecorator.invokeSecure(threadPool.get()::submit, () -> {
			try {
				return onSuccess.apply(supplier.get());
			} catch (Exception e) {
				if (onFail != null) {
					return onFail.apply(e);
				}
				LOGGER.warn("", e);
				throw e;
			}
		});
	}

	@Override
	public void waitForAll(Collection<? extends Future<?>> futures) {
		LinkedList<Future<?>> copy = new LinkedList<>(futures);
		while (isNotEmpty(copy)) {
			sleepForAWhile();
			copy.removeIf(Future::isDone);
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
	 */
	private void initializeUserThreadPool(String runFor) {
		try {
			// initialize the user thread count
			lock.lock();
			runningUserThreads.computeIfAbsent(runFor, s -> new AtomicInteger(perUserPoolSize));
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
			LOGGER.trace("Will initiate cascade thread pool execution for thread: {}", runFor);
		} else {
			runFor = SYSTEM_USER;
			if (securityContext.isActive()) {
				runFor = securityContext.getAuthenticated().getSystemId().toString();
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
	protected <T extends GenericAsyncTask> void executeInternal(List<T> tasks, String user, boolean rollbackOnError) {
		List<T> runningTasks = new ArrayList<>(perUserPoolSize);
		List<T> queuedTasks = new LinkedList<>();
		List<T> allTasks = new LinkedList<>();

		// start first batch of tasks up to the allowed per user limit
		scheduleInitialBatchOfTasks(tasks, user, runningTasks, queuedTasks, allTasks);

		List<Throwable> errors = new LinkedList<>();
		List<GenericAsyncTask> fails = new LinkedList<>();
		List<GenericAsyncTask> successfulTasks = new ArrayList<>(tasks.size());
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
	 */
	private <T extends GenericAsyncTask> void executeTasks(String user, List<T> runningTasks, List<T> queuedTasks,
			List<T> allTasks, List<Throwable> errors, List<GenericAsyncTask> fails,
			List<GenericAsyncTask> successfulTasks) {
		// REVIEW: this probably need to be rewritten using only the running and queued tasks
		while (!allTasks.isEmpty() || !queuedTasks.isEmpty()) {
			sleepForAWhile();
			Iterator<T> it = allTasks.iterator();
			while (it.hasNext()) {
				// check if the task is complete and put it in the proper list
				processCompletedTask(user, errors, fails, successfulTasks, it);

				// if we have more tasks to run submit them for execution
				it = submitRemainingTasks(user, runningTasks, queuedTasks, allTasks, it);
			}
		}
	}

	private static void sleepForAWhile() {
		try {
			// we will wait some time before first next check
			Thread.sleep(10);
		} catch (InterruptedException e) {
			LOGGER.trace("", e);
			// not interested
		}
	}

	/**
	 * Submit remaining tasks. If we have more tasks to run submit them for execution
	 */
	private <T extends GenericAsyncTask> Iterator<T> submitRemainingTasks(String user, List<T> runningTasks,
			List<T> queuedTasks, List<T> allTasks, Iterator<T> it) {
		// if we have more tasks we add them to the list
		// schedule it and reset the iterator
		Iterator<T> localIterator = it;
		if (!queuedTasks.isEmpty() && isThreadAvailable(user)) {
			acquire(user);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Acquired new thread for user {} remaining={}", user, runningUserThreads.get(user));
			}
			T newTask = queuedTasks.remove(0);
			runningTasks.add(newTask);
			threadPool.get().submit((ForkJoinTask<?>) newTask);
			localIterator = allTasks.iterator();
		}
		return localIterator;
	}

	/**
	 * Process completed task. Check if the task is complete and put it in the proper list
	 */
	private <T extends GenericAsyncTask> void processCompletedTask(String user, List<Throwable> errors,
			List<GenericAsyncTask> fails, List<GenericAsyncTask> successfulTasks, Iterator<T> it) {
		GenericAsyncTask asyncTask = it.next();
		if (asyncTask.isDone()) {
			it.remove();
			release(user);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Releasing worker for user {} available threads now: {}", user,
						runningUserThreads.get(user));
			}
		}
		if (asyncTask.isCompletedNormally()) {
			if (!Boolean.TRUE.equals(asyncTask.getRawResult())) {
				LOGGER.error("Failed to execute task {}", asyncTask.getClass().getSimpleName());
			}
			// execute the success method when all are successful
			successfulTasks.add(asyncTask);
		} else if (asyncTask.isCompletedAbnormally()) {
			Throwable exception = asyncTask.getException();
			if (exception != null) {
				LOGGER.error("Failed to execute task {} with: {}", asyncTask.getClass().getSimpleName(),
						exception.getMessage());
				errors.add(exception);
			} else {
				LOGGER.error("Failed to execute task {} without exception", asyncTask.getClass().getSimpleName());
			}
			fails.add(asyncTask);
		}
	}

	/**
	 * Schedule initial batch of tasks. Start first batch of tasks up to the allowed per user limit
	 */
	private <T extends GenericAsyncTask> void scheduleInitialBatchOfTasks(List<T> tasks, String user,
			List<T> runningTasks, List<T> queuedTasks, List<T> allTasks) {
		for (T genericAsyncTask : tasks) {
			genericAsyncTask.initializeSecurity(contextManager);
			allTasks.add(genericAsyncTask);
			if (isThreadAvailable(user)) {
				acquire(user);
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Acquired new thread for user {} remaining={}", user, runningUserThreads.get(user));
				}
				threadPool.get().submit((ForkJoinTask<?>) genericAsyncTask);
				runningTasks.add(genericAsyncTask);
			} else {
				queuedTasks.add(genericAsyncTask);
			}
		}
	}

	private static void processOnSuccess(List<GenericAsyncTask> successfulTasks) {
		for (GenericAsyncTask task : successfulTasks) {
			try {
				LOGGER.trace("Executed task {} successfully", task.getClass().getSimpleName());
				task.executeOnSuccess();
			} catch (Exception e) {
				LOGGER.error("Failed to execute {}.executeOnSuccess() method due to {}", task.getClass().getName(),
						e.getMessage(), e);
			}
		}
	}

	private static <T extends GenericAsyncTask> void processOnFail(List<T> tasks, boolean rollbackOnError,
			List<Throwable> errors, List<GenericAsyncTask> fails, List<GenericAsyncTask> successfullTasks) {
		// if found any errors and rollback if required
		if (rollbackOnError) {
			LOGGER.warn("There are {} failed tasks out of {}. Rolling back {} successful tasks.", fails.size(),
					tasks.size(), successfullTasks.size());
			for (GenericAsyncTask task : successfullTasks) {
				try {
					task.onRollback();
				} catch (Exception e) {
					LOGGER.trace("Failed to roll back a task ", e);
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
			LOGGER.warn("When executing async tasks {} failed with errors. Execeptions are:", errors.size());
			for (Throwable throwable : errors) {
				LOGGER.error(throwable.getMessage(), throwable);
			}
			throw new RollbackedRuntimeException(errors.get(0));
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
		AtomicInteger atomicInteger = runningUserThreads.get(user);
		if (atomicInteger != null) {
			// probably already released
			// could happen if multiple tasks for same users are executing in parallel
			atomicInteger.incrementAndGet();
		} else {
			LOGGER.warn("Could not find running tasks for user {}", user);
		}
	}

	/**
	 * Default exception handler for the execution pool.
	 *
	 * @author BBonev
	 */
	static class ExecutorUncaughtExceptionHandler implements UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			LOGGER.error("Uncaught exception", e);
		}

	}
}
