package com.sirma.itt.seip.concurrent;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Basic implementation of the {@link Future} interface. {@code SimpleFuture} can be put into a completed state by
 * invoking any of the following methods: {@link #cancel(boolean)}, {@link #failed(Exception)}, or
 * {@link #completed(Object)}.
 *
 * @param <T>
 *            the future result type of an asynchronous operation.
 */
public class SimpleFuture<T> implements Future<T>, CompletableOperation<T> {

	private final FutureCallback<T> callback;

	private volatile boolean completed;
	private volatile boolean cancelled;
	private volatile T result;
	private volatile Exception ex;

	/**
	 * Instantiates a new simple future without a callback
	 */
	public SimpleFuture() {
		this((FutureCallback<T>) null);
	}

	/**
	 * Instantiates a new simple future.
	 *
	 * @param callback
	 *            the callback
	 */
	public SimpleFuture(final FutureCallback<T> callback) {
		this.callback = callback;
	}

	/**
	 * Instantiates a new simple future that will return the given result value immediately.
	 *
	 * @param resultValue
	 *            the value to return from the {@link #get()}. The method will not block at all when initialized from
	 *            this constructor
	 */
	public SimpleFuture(T resultValue) {
		this();
		result = resultValue;
		completed = true;
	}

	/**
	 * Instantiates a new simple future that will always throw the given exception.
	 *
	 * @param exception
	 *            the exception to throw when {@link #get()} is called.
	 */
	public SimpleFuture(Exception exception) {
		this();
		ex = exception;
		completed = true;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public boolean isDone() {
		return this.completed;
	}

	private T getResult() throws ExecutionException {
		if (this.ex != null) {
			throw new ExecutionException(this.ex);
		}
		if (this.cancelled) {
			throw new CancellationException();
		}
		return this.result;
	}

	@Override
	public synchronized T get() throws InterruptedException, ExecutionException {
		while (!this.completed) {
			wait();
		}
		return getResult();
	}

	@Override
	public synchronized T get(final long timeout, final TimeUnit unit) // NOSONAR
			throws InterruptedException, ExecutionException, TimeoutException {
		Objects.requireNonNull(unit, "Time unit");
		final long msecs = unit.toMillis(timeout);
		final long startTime = msecs <= 0 ? 0 : System.currentTimeMillis();
		long waitTime = msecs;
		if (this.completed) {
			return getResult();
		} else if (waitTime <= 0) {
			throw new TimeoutException();
		}
		for (;;) {
			wait(waitTime);
			if (this.completed) {
				return getResult();
			}
			waitTime = msecs - (System.currentTimeMillis() - startTime);
			if (waitTime <= 0) {
				throw new TimeoutException();
			}
		}
	}

	@Override
	public boolean completed(final T aResult) {
		synchronized (this) {
			if (this.completed) {
				return false;
			}
			this.completed = true;
			this.result = aResult;
			notifyAll();
		}
		if (this.callback != null) {
			this.callback.completed(aResult);
		}
		return true;
	}

	@Override
	public boolean failed(final Exception exception) {
		synchronized (this) {
			if (this.completed) {
				return false;
			}
			this.completed = true;
			this.ex = exception;
			notifyAll();
		}
		if (this.callback != null) {
			this.callback.failed(exception);
		}
		return true;
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		synchronized (this) {
			if (this.completed) {
				return false;
			}
			this.completed = true;
			this.cancelled = true;
			notifyAll();
		}
		if (this.callback != null) {
			this.callback.cancelled();
		}
		return true;
	}

	@Override
	public boolean cancel() {
		return cancel(false);
	}
}
