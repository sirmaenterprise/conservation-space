package com.sirma.itt.seip.security.util;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.context.RuntimeContext.CurrentRuntimeConfiguration;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Base class for {@link Runnable} objects that can be used for asynchronous execution with initialized security. These
 * objects carry the security context in which they were created.
 *
 * @author BBonev
 */
public abstract class SecureRunnable extends BaseSecureTask implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Creates {@link SecureRunnable} that wraps the given {@link Executable}. The {@link Callable#call()} method always
	 * returns <code>null</code> .
	 *
	 * @param securityContextManager
	 *            the security context manager
	 * @param executable
	 *            the executable
	 * @return the secure callable
	 */
	public static SecureRunnable wrap(SecurityContextManager securityContextManager, final Executable executable) {
		return new SecureRunnable(securityContextManager) {

			@Override
			protected void doRun() {
				executable.execute();
			}
		};
	}

	/**
	 * Instantiates a new secure callable from the given security context. That context will be used to wrap the
	 * invocation if any.
	 *
	 * @param securityContextManager
	 *            the security context manager
	 */
	public SecureRunnable(SecurityContextManager securityContextManager) {
		super(securityContextManager);
	}

	/**
	 * Methods that calls the {@link #doRun()} but before that wrap it to track the time and set the configuration
	 */
	@Override
	public void run() {
		if (isCanceled()) {
			// do not run if cancelled
			return;
		}
		// else just invoke the call method
		CurrentRuntimeConfiguration oldConfiguration = beforeCall();
		try {
			start();
			// set the new configurations this will transfer and security
			doRun();
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		} finally {
			complete();
			LOGGER.trace("Secure: {} took: {} ms", this.getClass().getSimpleName(), executionTime());
			afterCall(oldConfiguration);
		}
	}

	/**
	 * This method is called on the new thread if any
	 */
	protected abstract void doRun();

	/**
	 * Gets the set future as scheduled future. Note that if the set {@link Future} is instance of
	 * {@link ScheduledFuture} the method will throw a {@link ClassCastException}.
	 *
	 * @param <V> the future value type
	 * @return the scheduled future
	 */
	public <V> ScheduledFuture<V> getAsScheduledFuture() {
		return (ScheduledFuture<V>) getFuture();
	}
}
