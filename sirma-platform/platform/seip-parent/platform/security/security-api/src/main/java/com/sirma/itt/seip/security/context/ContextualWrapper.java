package com.sirma.itt.seip.security.context;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.sirma.itt.seip.Executable;

/**
 * Contextual wrapper provides means to building/wrapping functions so that they can be executed in specified context
 * later. This is mostly useful for passing functions that should be executed on other threads and the context should be
 * carried. Instances of this class can be obtained by the {@link SecurityContextManager#wrap()} method or
 * {@link ContextualExecutor#toWrapper()} instance.
 *
 * @author BBonev
 * @see ContextualExecutor
 */
public interface ContextualWrapper {

	/**
	 * Wraps a function in the predefined context
	 *
	 * @param <T>
	 *            the type of the function argument
	 * @param <R>
	 *            the type of the function result
	 * @param toWrap
	 *            function to wrap
	 * @return a function that wraps the original function. The returned function makes sure the original function to be
	 *         called in the correct context
	 */
	<T, R> Function<T, R> function(Function<T, R> toWrap);

	/**
	 * Wraps a function with two arguments in the predefined context
	 *
	 * @param <T>
	 *            the type of the first function argument
	 * @param <U>
	 *            the type of the second function argument
	 * @param <R>
	 *            the type of the function result
	 * @param toWrap
	 *            function to wrap
	 * @return a function that wraps the original function. The returned function makes sure the original function to be
	 *         called in the correct context
	 */
	<T, U, R> BiFunction<T, U, R> biFunction(BiFunction<T, U, R> toWrap);

	/**
	 * Wraps a consumer in the predefined context
	 *
	 * @param <T>
	 *            the type of the consumer argument
	 * @param toWrap
	 *            consumer to wrap
	 * @return a consumer that wraps the original consumer. The returned consumer makes sure the original consumer to be
	 *         called in the correct context
	 */
	<T> Consumer<T> consumer(Consumer<T> toWrap);

	/**
	 * Wraps a consumer in the predefined context
	 *
	 * @param <T>
	 *            the type of the first consumer argument
	 * @param <U>
	 *            the type of the second consumer argument
	 * @param toWrap
	 *            consumer to wrap
	 * @return a consumer that wraps the original consumer. The returned consumer makes sure the original consumer to be
	 *         called in the correct context
	 */
	<T, U> BiConsumer<T, U> biConsumer(BiConsumer<T, U> toWrap);

	/**
	 * Wraps a predicate in the predefined context
	 *
	 * @param <T>
	 *            the type of the predicate argument
	 * @param toWrap
	 *            predicate to wrap
	 * @return a predicate that wraps the original predicate. The returned predicate makes sure the original predicate
	 *         to be called in the correct context
	 */
	<T> Predicate<T> predicate(Predicate<T> toWrap);

	/**
	 * Wraps a predicate in the predefined context
	 *
	 * @param <T>
	 *            the type of the first predicate argument
	 * @param <U>
	 *            the type of the second predicate argument
	 * @param toWrap
	 *            predicate to wrap
	 * @return a predicate that wraps the original predicate. The returned predicate makes sure the original predicate
	 *         to be called in the correct context
	 */
	<T, U> BiPredicate<T, U> biPredicate(BiPredicate<T, U> toWrap);

	/**
	 * Wraps an executable in the predefined context
	 *
	 * @param toWrap
	 *            executable to wrap
	 * @return an executable that wraps the original executable. The returned executable makes sure the original
	 *         executable to be called in the correct context
	 */
	Executable executable(Executable toWrap);

	/**
	 * Wraps a callable in the predefined context
	 *
	 * @param <V>
	 *            the callable result type
	 * @param toWrap
	 *            callable to wrap
	 * @return a callable that wraps the original callable. The returned callable makes sure the original callable to be
	 *         called in the correct context
	 */
	<V> Callable<V> callable(Callable<V> toWrap);

	/**
	 * Wraps a supplier in the predefined context
	 *
	 * @param <T>
	 *            the supplier result type
	 * @param toWrap
	 *            supplier to wrap
	 * @return a supplier that wraps the original supplier. The returned supplier makes sure the original supplier to be
	 *         called in the correct context
	 */
	<T> Supplier<T> supplier(Supplier<T> toWrap);

	/**
	 * Converts the current wrapper instance to {@link ContextualExecutor} by keeping the same context
	 *
	 * @return contextual executor instance that is initialized from the current context
	 */
	ContextualExecutor toExecutor();

	/**
	 * {@link ContextualWrapper} implementation that does not use any context wrapping and just returns the arguments.
	 * <p>
	 * The method {@link ContextualWrapper#toExecutor()} will also return an instance that does nothing and just calls
	 * the function arguments
	 *
	 * @author BBonev
	 */
	class NoConextualWrapper implements ContextualWrapper {

		public static final ContextualWrapper INSTANCE = new NoConextualWrapper();

		@Override
		public <T, R> Function<T, R> function(Function<T, R> toWrap) {
			return toWrap;
		}

		@Override
		public <T, U, R> BiFunction<T, U, R> biFunction(BiFunction<T, U, R> toWrap) {
			return toWrap;
		}

		@Override
		public <T> Consumer<T> consumer(Consumer<T> toWrap) {
			return toWrap;
		}

		@Override
		public <T, U> BiConsumer<T, U> biConsumer(BiConsumer<T, U> toWrap) {
			return toWrap;
		}

		@Override
		public <T> Predicate<T> predicate(Predicate<T> toWrap) {
			return toWrap;
		}

		@Override
		public <T, U> BiPredicate<T, U> biPredicate(BiPredicate<T, U> toWrap) {
			return toWrap;
		}

		@Override
		public Executable executable(Executable toWrap) {
			return toWrap;
		}

		@Override
		public <V> Callable<V> callable(Callable<V> toWrap) {
			return toWrap;
		}

		@Override
		public <T> Supplier<T> supplier(Supplier<T> toWrap) {
			return toWrap;
		}

		@Override
		public ContextualExecutor toExecutor() {
			return ContextualExecutor.NoContextualExecutor.INSTANCE;
		}

	}
}
