package com.sirma.itt.seip.security.context;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for common {@link ThreadFactory}s that produce threads initialized with security context.
 *
 * @author BBonev
 */
public class ThreadFactories {

	private static final Logger LOGGER = LoggerFactory.getLogger("UncaughtExceptions");

	/**
	 * Instantiates a new thread factories.
	 */
	private ThreadFactories() {
		// utility class
	}

	/**
	 * Creates the system thread factory.
	 *
	 * @param manager the manager
	 * @return the thread factory
	 */
	public static ThreadFactory createSystemThreadFactory(final SecurityContextManager manager) {
		return createSystemThreadFactory(manager, null);
	}

	/**
	 * Creates the system thread factory. The created threads are passed first through the given function before
	 * returning them.
	 *
	 * @param manager the manager
	 * @param updater the updater
	 * @return the thread factory
	 */
	public static ThreadFactory createSystemThreadFactory(final SecurityContextManager manager,
			Function<Thread, Thread> updater) {
		return createSystemThreadFactory(null, manager, updater);
	}

	/**
	 * Creates the system thread factory. The created threads are passed first through the given function before
	 * returning them.
	 *
	 * @param threadNamePrefix the prefix to be used when reading new threads
	 * @param manager the manager
	 * @param updater the updater
	 * @return the thread factory
	 */
	public static ThreadFactory createSystemThreadFactory(String threadNamePrefix, final SecurityContextManager manager,
			Function<Thread, Thread> updater) {
		Function<Thread, Thread> local = updater != null ? exceptionHandlerSetter().andThen(updater)
				: exceptionHandlerSetter();
		return (r) -> local.apply(new SystemThread(threadNamePrefix, manager, r));
	}

	/**
	 * Set the thread as daemon and lowers it's priority by 1. Note that this method could be called only for not
	 * started threads
	 *
	 * @param thread the thread to update
	 * @return the same thread from the argument
	 */
	public static Thread asDaemon(Thread thread) {
		thread.setDaemon(true);
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		return thread;
	}

	private static Function<Thread, Thread> exceptionHandlerSetter() {
		return (t) -> {
			t.setUncaughtExceptionHandler((thread, ex) -> LOGGER.error(ex.getMessage(), ex));
			return t;
		};
	}

	/**
	 * Thread that is authenticated with the system security context in the default tenant
	 *
	 * @author BBonev
	 */
	private static class SystemThread extends Thread {
		private static final Logger LOG = LoggerFactory.getLogger("UncaughtExceptions.SystemThread");
		private final SecurityContextManager contextManager;
		/* For autonumbering anonymous threads. */
		private static int threadInitNumber;

		private static synchronized int nextThreadNum() {
			return threadInitNumber++;
		}

		/**
		 * Instantiates a new system thread.
		 *
		 * @param contextManager the context manager
		 * @param runnable the runnable
		 */
		public SystemThread(SecurityContextManager contextManager, Runnable runnable) {
			this(null, contextManager, runnable);
		}

		/**
		 * Instantiates a new system thread with a custom name
		 *
		 * @param name the thread name prefix. If null is passed then the default name {@code SystemThread} will be used
		 * @param contextManager the context manager
		 * @param runnable the runnable
		 */
		public SystemThread(String name, SecurityContextManager contextManager, Runnable runnable) {
			super(runnable, Objects.toString(StringUtils.trimToNull(name), "SystemThread") + "-" + nextThreadNum());
			this.contextManager = contextManager;
		}

		@Override
		public void run() {
			contextManager.initializeExecutionAsSystemAdmin();
			try {
				super.run(); // NOSONAR
			} catch (RuntimeException ex) {
				LOG.error(ex.getMessage(), ex);
				throw ex;
			}
			// no need to clean the context the thread is going to be destroyed
			// anyway.
		}
	}
}
