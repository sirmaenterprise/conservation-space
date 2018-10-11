package com.sirma.itt.seip.security.util;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.context.RuntimeContext.CurrentRuntimeConfiguration;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Base class for callable objects that can be used for asynchronous execution with initialized security. These objects
 * carry the security context in which they were created.
 *
 * @param <V>
 *            the value type
 * @author BBonev
 */
public abstract class SecureCallable<V> extends BaseSecureTask implements Callable<V> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/**
	 * Creates a {@link SecureCallable} that wraps the given {@link Supplier}. The returned instance manages the
	 * security context and calls the {@link Supplier#get()} in the {@link Callable#call()} method.
	 *
	 * @param <V>
	 *            the value type
	 * @param securityContextManager
	 *            the security context manager
	 * @param supplier
	 *            the supplier
	 * @return the secure callable
	 */
	public static <V> SecureCallable<V> wrap(SecurityContextManager securityContextManager,
			final Supplier<V> supplier) {
		return new SecureCallable<V>(securityContextManager) {
			@Override
			protected V doCall() {
				return supplier.get();
			}
		};
	}

	/**
	 * Creates {@link SecureCallable} that wraps the given {@link Executable}. The {@link Callable#call()} method always
	 * returns <code>null</code> .
	 *
	 * @param securityContextManager
	 *            the security context manager
	 * @param executable
	 *            the executable
	 * @return the secure callable
	 */
	public static SecureCallable<Void> wrap(SecurityContextManager securityContextManager,
			final Executable executable) {
		return new SecureCallable<Void>(securityContextManager) {

			@Override
			protected Void doCall() {
				executable.execute();
				return null;
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
	public SecureCallable(SecurityContextManager securityContextManager) {
		super(securityContextManager);
	}

	/**
	 * Methods that calls the {@link #doCall()} but before that wrap it to track the time and set the configuration
	 *
	 * @return the returned value
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public V call() throws Exception {
		if (isCanceled()) {
			throw new CancellationException();
		}
		// else just invoke the call method
		CurrentRuntimeConfiguration oldConfiguration = beforeCall();
		try {
			start();
			// set the new configurations this will transfer and security
			return doCall();
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		} finally {
			complete();
			LOGGER.trace("Generic async task: {} took: {} ms", this.getClass().getSimpleName(), executionTime());
			afterCall(oldConfiguration);
		}
	}

	/**
	 * This method is called on the new thread if any
	 *
	 * @return the v
	 */
	protected abstract V doCall();

}
