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
 * Contextual executor provides means of calling different functional objects in predefined security context. Instance
 * of this class can be obtained from the {@link SecurityContextManager} and more specifically from
 * <ul>
 * <li>{@link SecurityContextManager#executeAs()} - transferable executor initialized with the current active context
 * <li>{@link SecurityContextManager#executeWithPermissionsOf(com.sirma.itt.seip.security.User)
 * SecurityContextManager#executeAs(User)} - executor that will call from the name of the current user and the
 * permissions of the given user
 * <li>{@link SecurityContextManager#executeAsAdmin()} - executor that will call from the name of the admin user of the
 * tenant
 * <li>{@link SecurityContextManager#executeAsSystem()} - executor that will call from the name of the system user of
 * the tenant
 * <li>{@link SecurityContextManager#executeAsSystemAdmin()} - executor that will call from the system admin of the
 * application. Can be obtained only if in system tenant scope
 * <li>{@link SecurityContextManager#executeAsTenant(String)} - executor that will execute in the given tenant scope.
 * Can be obtained only if in system tenant scope
 * </ul>
 * It can also be obtained from an instance of {@link ContextualWrapper} by calling it's
 * {@link ContextualWrapper#toExecutor() toExecutor()} method.
 *
 * @author BBonev
 * @see ContextualWrapper
 */
public interface ContextualExecutor {

	/**
	 * Invokes the given function in the predefined context
	 *
	 * @param <T>
	 *            function argument type
	 * @param <R>
	 *            function result type
	 * @param toCall
	 *            the function to call
	 * @param arg
	 *            the argument to pass to the function
	 * @return the function result
	 */
	<T, R> R function(Function<T, R> toCall, T arg);

	/**
	 * Invokes the given function in the predefined context
	 *
	 * @param <T>
	 *            function first argument type
	 * @param <U>
	 *            function second argument type
	 * @param <R>
	 *            function result type
	 * @param toCall
	 *            the function to call
	 * @param arg1
	 *            the first argument to pass to the function
	 * @param arg2
	 *            the second argument to pass to the function
	 * @return the function result
	 */
	<T, U, R> R biFunction(BiFunction<T, U, R> toCall, T arg1, U arg2);

	/**
	 * Invokes the given consumer in the predefined context
	 *
	 * @param <T>
	 *            the consumer argument type
	 * @param toCall
	 *            the consumer to call
	 * @param arg
	 *            the argument to pass to the consumer
	 */
	<T> void consumer(Consumer<T> toCall, T arg);

	/**
	 * Invokes the given consumer in the predefined context
	 *
	 * @param <T>
	 *            the type of the first argument of the consumer
	 * @param <U>
	 *            the type of the second argument of the consumer
	 * @param toCall
	 *            the consumer to call
	 * @param arg1
	 *            the first argument to pass to the consumer
	 * @param arg2
	 *            the second argument to pass to the consumer
	 */
	<T, U> void biConsumer(BiConsumer<T, U> toCall, T arg1, U arg2);

	/**
	 * Invokes the given predicate in the predefined context
	 *
	 * @param <T>
	 *            the predicate argument type
	 * @param toCall
	 *            the predicate to call
	 * @param arg
	 *            the argument to pass to the predicate
	 * @return the predicate result
	 */
	<T> boolean predicate(Predicate<T> toCall, T arg);

	/**
	 * Invokes the given predicate in the predefined context
	 *
	 * @param <T>
	 *            the type of the first argument of the predicate
	 * @param <U>
	 *            the type of the second argument of the predicate
	 * @param toCall
	 *            the predicate to call
	 * @param arg1
	 *            the first argument to pass to the predicate
	 * @param arg2
	 *            the second argument to pass to the predicate
	 * @return the predicate result
	 */
	<T, U> boolean biPredicate(BiPredicate<T, U> toCall, T arg1, U arg2);

	/**
	 * Invokes the given executable in the predefined context
	 *
	 * @param toCall
	 *            the executable to invoke
	 */
	void executable(Executable toCall);

	/**
	 * Invokes the given callable in the predefined context. This method is added to allow invoking methods that throw
	 * exceptions
	 *
	 * @param <V>
	 *            the callable result
	 * @param toCall
	 *            the callable to invoke
	 * @return the callable result
	 * @throws Exception
	 *             any checked exception that occur during invocation
	 */
	<V> V callable(Callable<V> toCall) throws Exception; // NOSONAR

	/**
	 * Invokes the given supplier in the predefined context.
	 * 
	 * @param <T>
	 *            the supplier result
	 * @param toCall
	 *            the supplier to call
	 * @return the supplier result
	 */
	<T> T supplier(Supplier<T> toCall);

	/**
	 * Converts the current executor instance to {@link ContextualWrapper} instance. The returned wrapper will have the
	 * same context as this executor
	 *
	 * @return {@link ContextualWrapper} instance initialized with the same context as this executor
	 */
	ContextualWrapper toWrapper();

	/**
	 * {@link ContextualExecutor} implementation that does not use any context wrapping and just calls the functions.
	 * <p>
	 * The method {@link ContextualExecutor#toWrapper()} will also return an instance that does nothing and just returns
	 * it's arguments
	 *
	 * @author BBonev
	 */
	class NoContextualExecutor implements ContextualExecutor {

		public static final ContextualExecutor INSTANCE = new NoContextualExecutor();

		@Override
		public <T, R> R function(Function<T, R> toCall, T arg) {
			return toCall.apply(arg);
		}

		@Override
		public <T, U, R> R biFunction(BiFunction<T, U, R> toCall, T arg1, U arg2) {
			return toCall.apply(arg1, arg2);
		}

		@Override
		public <T> void consumer(Consumer<T> toCall, T arg) {
			toCall.accept(arg);
		}

		@Override
		public <T, U> void biConsumer(BiConsumer<T, U> toCall, T arg1, U arg2) {
			toCall.accept(arg1, arg2);
		}

		@Override
		public <T> boolean predicate(Predicate<T> toCall, T arg) {
			return toCall.test(arg);
		}

		@Override
		public <T, U> boolean biPredicate(BiPredicate<T, U> toCall, T arg1, U arg2) {
			return toCall.test(arg1, arg2);
		}

		@Override
		public void executable(Executable toCall) {
			toCall.execute();
		}

		@Override
		public <V> V callable(Callable<V> toCall) throws Exception {
			return toCall.call();
		}

		@Override
		public <T> T supplier(Supplier<T> toCall) {
			return toCall.get();
		}

		@Override
		public ContextualWrapper toWrapper() {
			return ContextualWrapper.NoConextualWrapper.INSTANCE;
		}
	}
}
