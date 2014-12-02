package com.sirma.itt.emf.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A factory for creating DaemonThread objects.
 *
 * @author BBonev
 */
public class DaemonThreadFactory implements ThreadFactory {

	/** The Constant DEFAULT_PRIORITY. */
	public static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY - 1;
	/** The default thread factory. */
	private ThreadFactory defaultThreadFactory;

	/** The priority. */
	private int priority;

	/**
	 * Instantiates a new daemon thread factory with default thread priority.
	 */
	public DaemonThreadFactory() {
		this(DEFAULT_PRIORITY);
	}

	/**
	 * Instantiates a new daemon thread factory that used the specified thread priority.
	 * 
	 * @param priority
	 *            the priority
	 */
	public DaemonThreadFactory(int priority) {
		this.priority = priority;
		defaultThreadFactory = Executors.defaultThreadFactory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Thread newThread(Runnable paramRunnable) {
		Thread thread = defaultThreadFactory.newThread(paramRunnable);
		thread.setDaemon(true);
		thread.setPriority(priority);
		return thread;
	}
}