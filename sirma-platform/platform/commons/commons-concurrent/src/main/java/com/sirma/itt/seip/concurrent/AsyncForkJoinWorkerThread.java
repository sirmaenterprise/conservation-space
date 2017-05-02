package com.sirma.itt.seip.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * Extension for the {@link ForkJoinWorkerThread}. The specific implementation is to be able to monitor threads that are
 * created from our custom thread factory.
 *
 * @author BBonev
 */
class AsyncForkJoinWorkerThread extends ForkJoinWorkerThread {

	/**
	 * Instantiates a new async fork join worker thread.
	 *
	 * @param pool
	 *            the pool
	 */
	public AsyncForkJoinWorkerThread(ForkJoinPool pool) {
		super(pool);
	}

}
