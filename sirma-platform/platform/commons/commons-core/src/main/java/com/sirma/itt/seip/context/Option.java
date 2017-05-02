package com.sirma.itt.seip.context;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sirma.itt.seip.Executable;

/**
 * Boolean option. An option could be active and inactive but not both at the same time.
 *
 * @author BBonev
 */
public interface Option {

	/**
	 * Enables the option.
	 */
	void enable();

	/**
	 * Disables the option.
	 */
	void disable();

	/**
	 * Checks if is enabled.
	 *
	 * @return <code>true</code>, if is enabled.
	 */
	boolean isEnabled();

	/**
	 * Wraps the given consumer invocation in active option.
	 *
	 * @param <T>
	 *            the generic type
	 * @param consumer
	 *            the consumer
	 * @return the consumer
	 */
	default <T> Consumer<T> wrap(Consumer<T> consumer) {
		return c -> {
			enable();
			try {
				consumer.accept(c);
			} finally {
				disable();
			}
		};
	}

	/**
	 * Wraps the given function invocation in active option.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <R>
	 *            the return type
	 * @param function
	 *            the function
	 * @return the function
	 */
	default <T, R> Function<T, R> wrapFunction(Function<T, R> function) {
		return c -> {
			enable();
			try {
				return function.apply(c);
			} finally {
				disable();
			}
		};
	}

	/**
	 * Wraps the given executable invocation in active option.
	 *
	 * @param executable
	 *            the executable to execute
	 * @return new executable that wrap the original one.
	 */
	default Executable wrap(Executable executable) {
		return () -> {
			enable();
			try {
				executable.execute();
			} finally {
				disable();
			}
		};
	}

	/**
	 * Wraps the given supplier invocation in active option.
	 * 
	 * @param <T>
	 *            the supplier result type
	 * @param supplier
	 *            the supplier to wrap
	 * @return new supplier that wrap the original one.
	 */
	default <T> Supplier<T> wrap(Supplier<T> supplier) {
		return () -> {
			enable();
			try {
				return supplier.get();
			} finally {
				disable();
			}
		};
	}

	/**
	 * Creates a conjunction with the given options.
	 *
	 * @param options
	 *            the options
	 * @return true, if all options are active
	 */
	static boolean and(Option... options) {
		if (options == null || options.length == 0) {
			return false;
		}
		for (int i = 0; i < options.length; i++) {
			if (!options[i].isEnabled()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Perform disjunction with the given options.
	 *
	 * @param options
	 *            the options
	 * @return true, if at least one option is active
	 */
	static boolean or(Option... options) {
		if (options == null || options.length == 0) {
			return false;
		}
		for (int i = 0; i < options.length; i++) {
			if (options[i].isEnabled()) {
				return true;
			}
		}
		return false;
	}
}