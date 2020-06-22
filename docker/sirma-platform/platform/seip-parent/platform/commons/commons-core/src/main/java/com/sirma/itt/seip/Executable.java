/**
 *
 */
package com.sirma.itt.seip;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Functional interface to defines a single {@link #execute()} method. This is defined so not to use {@link Runnable}
 * because calling {@link Runnable#run()} is a static analysis rules violation.
 *
 * @author BBonev
 */
@FunctionalInterface
public interface Executable {

	/**
	 * Execute the task
	 */
	void execute();

	/**
	 * Converts the current executable as supplier that returns always null.
	 *
	 * @param <T>
	 *            the generic type
	 * @return the supplier
	 */
	default <T> Supplier<T> asSupplier() {
		return () -> {
			execute();
			return null;
		};
	}

	/**
	 * Converts the current executable as consumer that never reads the value.
	 *
	 * @return the consumer
	 */
	default Consumer<?> asConsumer() {
		return (a) -> execute();
	}
}
