/**
 *
 */
package com.sirma.itt.seip.security.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Special object that can be used to transfer current security context for asynchronous execution. The context is
 * initialized at the moment of object creation. If no security context is active then no context will be initialized
 * when executing the operations.
 *
 * @author BBonev
 */
public class SecureExecutor {

	private ContextualExecutor contextCaller;

	/**
	 * Instantiates a new secure executor
	 *
	 * @param securityContextManager
	 *            the security context manager, required
	 */
	public SecureExecutor(SecurityContextManager securityContextManager) {
		contextCaller = securityContextManager.executeAs();
	}

	/**
	 * Execute in source security context.
	 *
	 * @param executable
	 *            the executable
	 */
	public void execute(Executable executable) {
		contextCaller.executable(executable);
	}

	/**
	 * Execute in source security context.
	 *
	 * @param <T>
	 *            the generic type
	 * @param supplier
	 *            the supplier
	 * @return the result
	 */
	public <T> T execute(Supplier<T> supplier) {
		return contextCaller.supplier(supplier);
	}

	/**
	 * Execute in source security context.
	 *
	 * @param <T>
	 *            the argument type
	 * @param <R>
	 *            the result type
	 * @param function
	 *            the supplier
	 * @param arg
	 *            the arg
	 * @return the result
	 */
	public <T, R> R execute(Function<T, R> function, T arg) {
		return contextCaller.function(function, arg);
	}

	/**
	 * Execute in security context if present
	 *
	 * @param <T>
	 *            the generic type
	 * @param consumer
	 *            the consumer
	 * @param value
	 *            the value
	 */
	public <T> void execute(Consumer<T> consumer, T value) {
		contextCaller.consumer(consumer, value);
	}

	/**
	 * Execute.
	 *
	 * @param <T>
	 *            the generic type
	 * @param callable
	 *            the callable
	 * @return the t
	 * @throws Exception
	 *             the exception
	 */
	public <T> T call(Callable<T> callable) throws Exception { // NOSONAR
		return contextCaller.callable(callable);
	}
}
