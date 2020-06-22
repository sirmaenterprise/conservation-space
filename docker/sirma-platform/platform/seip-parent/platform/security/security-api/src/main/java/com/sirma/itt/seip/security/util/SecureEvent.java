/**
 *
 */
package com.sirma.itt.seip.security.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;

/**
 * Defines an event that can support transferring the security context and executing operations in that context. The
 * context transfer and execution should be provided via {@link SecureExecutor}. The secure executor may be initialized
 * at tenant creation. If not it may be initialized via {@link EventService} decorator. If no executor is defined the
 * operations will be run in the current context if any.
 *
 * @author BBonev
 */
public interface SecureEvent extends EmfEvent {

	/**
	 * Get secure executor.
	 *
	 * @return the secure executor or <code>null</code> if execution in special security context is not desired
	 */
	SecureExecutor getSecureExecutor();

	/**
	 * Sets the secure executor.
	 *
	 * @param executor
	 *            the new secure executor
	 */
	void setSecureExecutor(SecureExecutor executor);

	/**
	 * Execute in source security context.
	 *
	 * @param executable
	 *            the executable
	 */
	default void execute(Executable executable) {
		SecureExecutor secureExecutor = getSecureExecutor();
		if (secureExecutor != null) {
			secureExecutor.execute(executable);
		} else {
			executable.execute();
		}
	}

	/**
	 * Execute in source security context.
	 *
	 * @param <T>
	 *            the generic type
	 * @param callable
	 *            the supplier
	 * @return the result
	 * @throws Exception
	 *             the exception
	 */
	default <T> T call(Callable<T> callable) throws Exception { // NOSONAR
		SecureExecutor secureExecutor = getSecureExecutor();
		if (secureExecutor != null) {
			return secureExecutor.call(callable);
		}
		return callable.call();
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
	default <T> T execute(Supplier<T> supplier) {
		SecureExecutor secureExecutor = getSecureExecutor();
		if (secureExecutor != null) {
			return secureExecutor.execute(supplier);
		}
		return supplier.get();
	}

	/**
	 * Execute in source security context.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 * @param function
	 *            the supplier
	 * @param arg
	 *            the arg
	 * @return the result
	 */
	default <T, R> R execute(Function<T, R> function, T arg) {
		SecureExecutor secureExecutor = getSecureExecutor();
		if (secureExecutor != null) {
			return secureExecutor.execute(function, arg);
		}
		return function.apply(arg);
	}

	/**
	 * Execute in security context if present. The method invokes the given consumer by sending the given value in
	 * security context initialized by the {@link SecureExecutor}/
	 *
	 * @param <T>
	 *            the generic type
	 * @param consumer
	 *            the consumer
	 * @param value
	 *            the value to consume
	 */
	default <T> void execute(Consumer<T> consumer, T value) {
		SecureExecutor secureExecutor = getSecureExecutor();
		if (secureExecutor != null) {
			secureExecutor.execute(consumer, value);
		} else {
			consumer.accept(value);
		}
	}
}
