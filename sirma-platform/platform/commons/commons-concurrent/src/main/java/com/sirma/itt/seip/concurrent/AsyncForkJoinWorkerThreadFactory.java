package com.sirma.itt.seip.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * Extension for {@link ForkJoinWorkerThreadFactory}. The class is used for custom logic in the
 * {@link AsyncTaskExecutor} implementation. The implementation returns {@link AsyncForkJoinWorkerThread} instances.
 *
 * @author BBonev
 */
class AsyncForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {

	@Override
	public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
		AsyncForkJoinWorkerThread workerThread = new AsyncForkJoinWorkerThread(pool);
		workerThread.setDaemon(true);
		return workerThread;
	}

}
