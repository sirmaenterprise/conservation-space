package com.sirma.itt.seip.concurrent.locks;

/**
 * Sync interface defines methods for doing synchronization when simple {@link Object#wait()} and
 * {@link Object#notify()} are not enough. The interface could be used for complex locking with in memory or distributed
 * implementations. <br>
 * The default implementation uses a synchronized blocks to lock the implementing instance. If this is not desired a
 * custom implementation need to be provided.
 *
 * @author BBonev
 * @see ContextualSync
 */
public interface Sync {
	/**
	 * Waits over a mutex until {@link #signal()} or {@link #signalAll()} is called.
	 *
	 * @throws InterruptedException
	 *             if the waiting thread was interrupted
	 * @see Object#wait()
	 */
	default void await() throws InterruptedException {
		synchronized (this) {
			wait(); // NOSONAR
		}
	}

	/**
	 * Waits over a mutex until {@link #signal()} or {@link #signalAll()} is called up until the given time
	 *
	 * @param timeout
	 *            the timeout in milliseconds to wait
	 * @throws InterruptedException
	 *             if the waiting thread was interrupted
	 * @see Object#wait(long)
	 */
	default void await(long timeout) throws InterruptedException {
		synchronized (this) {
			wait(timeout);
		}
	}

	/**
	 * Waits over a mutex until {@link #signal()} or {@link #signalAll()} is called up until the given time
	 *
	 * @param timeout
	 *            the timeout in milliseconds to wait
	 * @param nanos
	 *            the timeout in nanoseconds
	 * @throws InterruptedException
	 *             if the waiting thread was interrupted
	 * @see Object#wait(long, int)
	 */
	default void await(long timeout, int nanos) throws InterruptedException {
		synchronized (this) {
			wait(timeout, nanos);
		}
	}

	/**
	 * Notify that waiting condition is completed and the waiting threads could continue. The call will notify only a
	 * single waiting thread.
	 *
	 * @see Object#notify()
	 */
	default void signal() {
		synchronized (this) {
			notify(); // NOSONAR
		}
	}

	/**
	 * Notify that waiting condition is completed and the waiting threads could continue. The call will notify all
	 * waiting threads.
	 *
	 * @see Object#notifyAll()
	 */
	default void signalAll() {
		synchronized (this) {
			notifyAll(); // NOSONAR
		}
	}

	/**
	 * Simple implementation of {@link Sync} interface that uses the default methods
	 *
	 * @author BBonev
	 */
	class SimpleSync implements Sync {
		// nothing more to define
	}
}
