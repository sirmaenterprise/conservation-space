package com.sirma.itt.seip.security.context;

import java.util.Objects;
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
 * Default implementation of {@link ContextualWrapper} that uses a
 * {@link SecurityContextManager#initializeFromContext(SecurityContext)} to change the current context and
 * {@link SecurityContextManager#endContextExecution()} to clean after invocation
 * @author BBonev
 */
class SecurityContextualWrapper implements ContextualWrapper {

	private final SecurityContextManager securityManager;
	private final SecurityContext targetContext;
	private ContextualExecutor caller;

	/**
	 * Instantiate wrapper using the provided security context manager and the transferable security context instance
	 *
	 * @param manager
	 *            the security context manager to use for context initializations
	 * @param targetContext
	 *            the target transferable context to use when initializing the context before function calls.
	 */
	public SecurityContextualWrapper(SecurityContextManager manager, SecurityContext targetContext) {
		securityManager = Objects.requireNonNull(manager, "Security context manager is required");
		this.targetContext = Objects.requireNonNull(targetContext, "Target security context is required");
	}

	@Override
	public <T, R> Function<T, R> function(Function<T, R> toWrap) {
		Objects.requireNonNull(toWrap, "Function is required argument");

		// do not leak the current instance into the lambda
		final SecurityContextManager manager = securityManager;
		final SecurityContext context = targetContext;

		return arg -> {
			manager.initializeFromContext(context);
			try {
				return toWrap.apply(arg);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T, U, R> BiFunction<T, U, R> biFunction(BiFunction<T, U, R> toWrap) {
		Objects.requireNonNull(toWrap, "BiFunction is required argument");

		// do not leak the current instance into the lambda
		final SecurityContextManager manager = securityManager;
		final SecurityContext context = targetContext;

		return (arg1, arg2) -> {
			manager.initializeFromContext(context);
			try {
				return toWrap.apply(arg1, arg2);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T> Consumer<T> consumer(Consumer<T> toWrap) {
		Objects.requireNonNull(toWrap, "Consumer is required argument");

		// do not leak the current instance into the lambda
		final SecurityContextManager manager = securityManager;
		final SecurityContext context = targetContext;

		return arg -> {
			manager.initializeFromContext(context);
			try {
				toWrap.accept(arg);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T, U> BiConsumer<T, U> biConsumer(BiConsumer<T, U> toWrap) {
		Objects.requireNonNull(toWrap, "BiConsumer is required argument");

		// do not leak the current instance into the lambda
		final SecurityContextManager manager = securityManager;
		final SecurityContext context = targetContext;

		return (arg1, arg2) -> {
			manager.initializeFromContext(context);
			try {
				toWrap.accept(arg1, arg2);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T> Predicate<T> predicate(Predicate<T> toWrap) {
		Objects.requireNonNull(toWrap, "Predicate is required argument");

		// do not leak the current instance into the lambda
		final SecurityContextManager manager = securityManager;
		final SecurityContext context = targetContext;

		return arg -> {
			manager.initializeFromContext(context);
			try {
				return toWrap.test(arg);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T, U> BiPredicate<T, U> biPredicate(BiPredicate<T, U> toWrap) {
		Objects.requireNonNull(toWrap, "BiPredicate is required argument");

		// do not leak the current instance into the lambda
		final SecurityContextManager manager = securityManager;
		final SecurityContext context = targetContext;

		return (arg1, arg2) -> {
			manager.initializeFromContext(context);
			try {
				return toWrap.test(arg1, arg2);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public Executable executable(Executable toWrap) {
		Objects.requireNonNull(toWrap, "Executable is required argument");

		// do not leak the current instance into the lambda
		final SecurityContextManager manager = securityManager;
		final SecurityContext context = targetContext;

		return () -> {
			manager.initializeFromContext(context);
			try {
				toWrap.execute();
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <V> Callable<V> callable(Callable<V> toWrap) {
		Objects.requireNonNull(toWrap, "Callable is required argument");

		// do not leak the current instance into the lambda
		final SecurityContextManager manager = securityManager;
		final SecurityContext context = targetContext;

		return () -> {
			manager.initializeFromContext(context);
			try {
				return toWrap.call();
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T> Supplier<T> supplier(Supplier<T> toWrap) {
		Objects.requireNonNull(toWrap, "Supplier is required argument");

		// do not leak the current instance into the lambda
		final SecurityContextManager manager = securityManager;
		final SecurityContext context = targetContext;

		return () -> {
			manager.initializeFromContext(context);
			try {
				return toWrap.get();
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public ContextualExecutor toExecutor() {
		if (caller == null) {
			return new SecurityContextualExecutor(securityManager, targetContext);
		}
		return caller;
	}
}
