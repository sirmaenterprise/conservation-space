package com.sirma.itt.seip.concurrent;

import java.util.function.Consumer;

import com.sirma.itt.seip.Executable;

/**
 * A callback interface that gets invoked upon completion of a {@link java.util.concurrent.Future}.
 *
 * @param <T>
 *            the future result type returned by this callback.
 */
public interface FutureCallback<T> {

	/**
	 * Completed.
	 *
	 * @param result
	 *            the result
	 */
	void completed(T result);

	/**
	 * Failed.
	 *
	 * @param ex
	 *            the ex
	 */
	void failed(Exception ex);

	/**
	 * Cancelled.
	 */
	void cancelled();

	/**
	 * Creates a {@link FutureCallback} instance that will handle only the result setting. The value passed to the
	 * {@link #completed(Object)} method will be passed to the given consumer. On error or on cancellation nothing will
	 * be done.
	 *
	 * @param <T>
	 *            the result type
	 * @param resultConsumer
	 *            the result consumer
	 * @return the future callback
	 */
	static <T> FutureCallback<T> create(Consumer<T> resultConsumer) {
		return new SimpleFutureCallback<>(resultConsumer, null, null);
	}

	/**
	 * Creates a {@link FutureCallback} instance that will handle the result and exception setting . The values passed
	 * to the {@link #completed(Object)} and {@link #failed(Exception)} methods will be passed to the given consumers.
	 * In case of cancellation nothing will be done.
	 *
	 * @param <T>
	 *            the result type
	 * @param resultConsumer
	 *            the result consumer
	 * @param errorConsumer
	 *            the error consumer
	 * @return the future callback
	 */
	static <T> FutureCallback<T> create(Consumer<T> resultConsumer, Consumer<Exception> errorConsumer) {
		return new SimpleFutureCallback<>(resultConsumer, errorConsumer, null);
	}

	/**
	 * Creates a {@link FutureCallback} instance that will handle all methods. The values passed to the
	 * {@link #completed(Object)} and {@link #failed(Exception)} methods will be passed to the given consumers. In case
	 * of cancellation the given {@link Executable#execute()} method will be called.
	 *
	 * @param <T>
	 *            the result type
	 * @param resultConsumer
	 *            the result consumer
	 * @param errorConsumer
	 *            the error consumer
	 * @param cancellationOp
	 *            the cancellation op
	 * @return the future callback
	 */
	static <T> FutureCallback<T> create(Consumer<T> resultConsumer, Consumer<Exception> errorConsumer,
			Executable cancellationOp) {
		return new SimpleFutureCallback<>(resultConsumer, errorConsumer, cancellationOp);
	}

	/**
	 * Simple implementation of {@link FutureCallback} that works with functions for each of the methods.
	 *
	 * @param <T>
	 *            the generic type
	 */
	class SimpleFutureCallback<T> implements FutureCallback<T> {

		private final Consumer<T> resultConsumer;
		private final Consumer<Exception> errorConsumer;
		private final Executable cancellationOp;

		/**
		 * Instantiates a new simple future callback.
		 *
		 * @param resultConsumer
		 *            the result consumer
		 * @param errorConsumer
		 *            the error consumer
		 * @param cancellationOp
		 *            the cancellation op
		 */
		public SimpleFutureCallback(Consumer<T> resultConsumer, Consumer<Exception> errorConsumer,
				Executable cancellationOp) {
			this.resultConsumer = resultConsumer;
			this.errorConsumer = errorConsumer;
			this.cancellationOp = cancellationOp;
		}

		@Override
		public void completed(T result) {
			if (resultConsumer != null) {
				resultConsumer.accept(result);
			}
		}

		@Override
		public void failed(Exception ex) {
			if (errorConsumer != null) {
				errorConsumer.accept(ex);
			}
		}

		@Override
		public void cancelled() {
			if (cancellationOp != null) {
				cancellationOp.execute();
			}
		}
	}

}