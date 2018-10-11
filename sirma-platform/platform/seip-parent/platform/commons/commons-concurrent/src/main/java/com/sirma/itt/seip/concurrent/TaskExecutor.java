package com.sirma.itt.seip.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sirma.itt.seip.Executable;

/**
 * Task manager executor service. The service provides means for running asynchronous tasks on a limited threads per
 * user or asynchronous transactional invocation or just asynchronous invocation of single task with callbacks. <br>
 * <b>NOTE:</b> the thread per user means thread per method call. If the method running task calls the executor again it
 * will use another set of threads for now.
 *
 * @author BBonev
 */
public interface TaskExecutor {

	/**
	 * Executes the given list of tasks in parallel on configurable number of threads. The method will block until all
	 * tasks are executed. Will throw a runtime exception {@link com.sirma.itt.seip.exception.EmfRuntimeException} if
	 * any of the tasks fails. One failed tasks will not terminate the execution of the other tasks. Executes the tasks
	 * associated with the currently logged in user. The method makes sure that all of the passed tasks are executed in
	 * on separate thread. Note that thread reuse may occur during execution.
	 *
	 * @param <T>
	 *            The concrete task type
	 * @param tasks
	 *            the list of tasks to execute
	 */
	<T extends GenericAsyncTask> void execute(List<T> tasks);

	/**
	 * Executes the given list of tasks in parallel on configurable number of threads. The method will block until all
	 * tasks are executed. Will throw a runtime exception {@link com.sirma.itt.seip.exception.EmfRuntimeException} if
	 * any of the tasks fails. One failed tasks will not terminate the execution of the other tasks. Executes the tasks
	 * for the provided user id. The method makes sure that all of the passed tasks are executed in on separate thread.
	 * Note that thread reuse may occur during execution.
	 *
	 * @param <T>
	 *            The concrete task type
	 * @param tasks
	 *            the list of tasks to execute
	 * @param executeAs
	 *            the execute as
	 */
	<T extends GenericAsyncTask> void execute(List<T> tasks, String executeAs);

	/**
	 * Submits the given list of tasks for execution. The method will not block and wait for the task completion as
	 * {@link #execute(List)} method. The method will use a transactional context if needed for the task execution
	 * depending on the {@code transactionalContext} argument.
	 *
	 * @param <T>
	 *            The concrete task type
	 * @param tasks
	 *            the list of tasks to execute
	 * @param transactionalContext
	 *            <code>true</code> if transactional context is required or <code>false</code> if not.
	 * @return the future that can be used to check if the tasks completed or not. If completed successfully then the
	 *         method {@link Future#get()} will return <code>true</code>. If execution completed with error than the
	 *         method {@link Future#get()} will thrown {@link ExecutionException} with the original exception.
	 */
	default <T extends GenericAsyncTask> Future<?> submit(List<T> tasks, boolean transactionalContext) {
		return submit(tasks, transactionalContext, null);
	}

	/**
	 * Submits the given list of tasks for execution. The method will not block and wait for the task completion as
	 * {@link #execute(List)} method. The method will use a transactional context if needed for the task execution
	 * depending on the {@code transactionalContext} argument.
	 *
	 * @param <T>
	 *            The concrete task type
	 * @param tasks
	 *            the list of tasks to execute
	 * @param transactionalContext
	 *            <code>true</code> if transactional context is required or <code>false</code> if not.
	 * @param executeAs
	 *            the execute as
	 * @return the future that can be used to check if the tasks completed or not. If completed successfully then the
	 *         method {@link Future#get()} will return <code>true</code>. If execution completed with error than the
	 *         method {@link Future#get()} will thrown {@link ExecutionException} with the original exception.
	 */
	<T extends GenericAsyncTask> Future<Object> submit(List<T> tasks, boolean transactionalContext, String executeAs);

	/**
	 * Submits for asynchronous execution of the given supplier. <br>
	 * This method will manage correct security context during asynchronous execution.
	 *
	 * @param <V>
	 *            the value type
	 * @param supplier
	 *            the supplier to be executed asynchronously
	 * @return the future for accessing the provider value.
	 */
	<V> Future<V> submit(Supplier<V> supplier);

	/**
	 * Submits for asynchronous execution of the given executable. <br>
	 * This method will manage correct security context during asynchronous execution.
	 *
	 * @param executable
	 *            the executable to be called asynchronously
	 * @return the future for accessing the provider value.
	 */
	Future<?> submit(Executable executable);

	/**
	 * Submits for asynchronous execution of the given supplier. If the execution is successful it will be passed to the
	 * given onSuccess consumer. If there is an error during execution it will be passed to the given onFail consumer
	 * that accepts an error. The onSuccess and onFail consumer will be invoked in the other thread that executed the
	 * supplier.<br>
	 * If both {@code onSuccess} and {@code onFail} consumers are not present then the call will be identical to calling
	 * {@link #submit(Supplier)}. <br>
	 * This method will manage correct security context during asynchronous execution.
	 *
	 * @param <V>
	 *            the expected value type
	 * @param supplier
	 *            the supplier that produces the value
	 * @param onSuccess
	 *            the on success consumer to be called with the produced value. Optional argument.
	 * @param onFail
	 *            the on fail consumer that will be called with any exception during consumer producing. Note that the
	 *            exception passed here will be the same as thrown by the {@link Future#get()} method. Optional
	 *            argument.
	 * @return the future for accessing the value provider by the supplier
	 */
	<V> Future<V> submit(Supplier<V> supplier, Consumer<V> onSuccess, Consumer<Throwable> onFail);

	/**
	 * Submits for asynchronous execution the given supplier. Upon successful execution the value will be passed via the
	 * given onSuccess function and then passed to the future object. If exception occur during {@link Supplier#get()}
	 * method or {@link Function#apply(Object)} it will be passed to the function onFail. OnFail function could return
	 * some value or rethrow the exception. If no exception is thrown by the onFail function then the
	 * {@link Future#get()} will return the value returned by onFail function.
	 * <p>
	 * This could be used to chain multiple asynchronous operations like promise feature:
	 *
	 * <pre>
	 * <code>
	 * Future&lt;String&gt; future = executor.submit(this::readFromDatabase);
	 * Future&lt;Integer&gt; futureCount = executor.submitMapped(future::get, this::countWords, (exp) -&gt; 0);
	 * </code>
	 * </pre>
	 *
	 * The code above will execute the method readFromDatabase in a separate thread and the countWords will be called
	 * with the result of that method call and will be executed on other thread
	 * <p>
	 * This method will manage correct security context during asynchronous execution.
	 *
	 * @param <V>
	 *            the supplied type
	 * @param <R>
	 *            the processed type
	 * @param supplier
	 *            the supplier that produces the value
	 * @param onSuccess
	 *            the on success mapping function that can transform the value
	 * @param onFail
	 *            the on fail mapping function that can return some value and not throw an exception. Optional and if
	 *            not passed the exception will be thrown then {@link Future#get()} is called.
	 * @return the future for accessing the mapped future value
	 */
	<V, R> Future<R> submitMapped(Supplier<V> supplier, Function<V, R> onSuccess, Function<Throwable, R> onFail);

	/**
	 * Execute async.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @return the future
	 */
	default <T> Future<T> executeAsync(Supplier<T> supplier) {
		return executeAsync(supplier, null);
	}

	/**
	 * Execute async.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param futureCallback
	 *            the future callback
	 * @return the future
	 */
	<T> Future<T> executeAsync(Supplier<T> supplier, FutureCallback<T> futureCallback);

	/**
	 * Execute the given executable asynchronously. No transaction context will be created for the execution tesk. The
	 * returned future's get method will always return <code>null</code> then the operation completes.
	 *
	 * @param executable
	 *            the executable
	 * @return the future
	 */
	Future<?> executeAsync(Executable executable);

	/**
	 * Execute the given executable asynchronously in a transaction. The returned future's get method will always return
	 * <code>null</code> then the operation completes.
	 *
	 * @param executable
	 *            the executable
	 * @return the future
	 */
	Future<?> executeAsyncInTx(Executable executable);

	/**
	 * Execute async in tx.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @return the future
	 */
	default <T> Future<T> executeAsyncInTx(Supplier<T> supplier) {
		return executeAsyncInTx(supplier, null);
	}

	/**
	 * Execute async in tx.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @param futureCallback
	 *            the future callback
	 * @return the future
	 */
	<T> Future<T> executeAsyncInTx(Supplier<T> supplier, FutureCallback<T> futureCallback);

	/**
	 * Waits for all futures to end before returning from the method. The method should block and wait for all futures
	 * to finish before returning.
	 *
	 * @param futures
	 *            the future objects to process
	 */
	void waitForAll(Collection<? extends Future<?>> futures);
}
