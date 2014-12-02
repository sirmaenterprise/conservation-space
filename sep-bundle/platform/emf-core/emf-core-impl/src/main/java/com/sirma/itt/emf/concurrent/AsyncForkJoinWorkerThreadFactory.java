package com.sirma.itt.emf.concurrent;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;



/**
 * Extension for {@link ForkJoinWorkerThreadFactory}. The class is used for custom logic in the
 * {@link AsyncTaskExecutor} implementation. The implementation returns
 * {@link AsyncForkJoinWorkerThread} instances.
 * 
 * @author BBonev
 */
public class AsyncForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
		return new AsyncForkJoinWorkerThread(pool);
	}

}
