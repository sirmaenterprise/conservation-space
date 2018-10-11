package com.sirma.itt.seip.testutil.fakes;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.concurrent.FutureCallback;
import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.concurrent.SimpleFuture;
import com.sirma.itt.seip.concurrent.TaskExecutor;

/**
 * Mock implementation of {@link TaskExecutor} there all methods execute the given tasks immediately
 *
 * @author BBonev
 */
public class TaskExecutorFake implements TaskExecutor {

	@Override
	public <T extends GenericAsyncTask> void execute(List<T> tasks) {
		tasks.forEach(task->{
			try {
				task.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public <T extends GenericAsyncTask> void execute(List<T> tasks, String executeAs) {
		execute(tasks);
	}

	@Override
	public <T extends GenericAsyncTask> Future<Object> submit(List<T> tasks, boolean transactionalContext,
			String executeAs) {
		execute(tasks);
		return new SimpleFuture<>((Object) null);
	}

	@Override
	public <V> Future<V> submit(Supplier<V> supplier) {
		return new SimpleFuture<>(supplier.get());
	}

	@Override
	public Future<?> submit(Executable executable) {
		SimpleFuture<?> future = new SimpleFuture<>();
		executable.execute();
		future.completed(null);
		return future;
	}

	@Override
	public <V> Future<V> submit(Supplier<V> supplier, Consumer<V> onSuccess, Consumer<Throwable> onFail) {
		SimpleFuture<V> future = new SimpleFuture<>();
		try {
			V v = supplier.get();
			future.completed(v);
			onSuccess.accept(v);
		} catch (RuntimeException e) {
			future.failed(e);
			onFail.accept(e);
		}
		return future;
	}

	@Override
	public <V, R> Future<R> submitMapped(Supplier<V> supplier, Function<V, R> onSuccess,
			Function<Throwable, R> onFail) {
		SimpleFuture<R> future = new SimpleFuture<>();
		try {
			V v = supplier.get();
			future.completed(onSuccess.apply(v));
		} catch (RuntimeException e) {
			future.completed(onFail.apply(e));
		}
		return future;
	}

	@Override
	public <T> Future<T> executeAsync(Supplier<T> supplier, FutureCallback<T> futureCallback) {
		SimpleFuture<T> future = new SimpleFuture<>(futureCallback);
		future.completed(supplier.get());
		return future;
	}

	@Override
	public Future<?> executeAsync(Executable executable) {
		SimpleFuture<?> future = new SimpleFuture<>();
		executable.execute();
		future.completed(null);
		return future;
	}

	@Override
	public Future<?> executeAsyncInTx(Executable executable) {
		return executeAsync(executable);
	}

	@Override
	public <T> Future<T> executeAsyncInTx(Supplier<T> supplier, FutureCallback<T> futureCallback) {
		return executeAsync(supplier, futureCallback);
	}

	@Override
	public void waitForAll(Collection<? extends Future<?>> futures) {
		// futures produced from here are already completed
		for (Future<?> future : futures) {
			assertTrue("The future should have been completed", future.isDone());
		}
	}

}
