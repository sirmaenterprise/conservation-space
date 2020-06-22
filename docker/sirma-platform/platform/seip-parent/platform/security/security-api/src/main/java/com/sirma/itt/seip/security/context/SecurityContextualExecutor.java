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
 * Default implementation of {@link ContextualExecutor} that uses a
 * {@link SecurityContextManager#initializeFromContext(SecurityContext)} to change the current context and
 * {@link SecurityContextManager#endContextExecution()} to clean after invocation
 *
 * @author BBonev
 */
class SecurityContextualExecutor implements ContextualExecutor {

	private final SecurityContextManager securityManager;
	private final SecurityContext targetContext;
	private ContextualWrapper wrapper;

	/**
	 * Instantiate executor using the provided security context manager and the transferable security context instance
	 *
	 * @param manager
	 *            the security context manager to use for context initializations
	 * @param targetContext
	 *            the target transferable context to use when initializing the context before function calls.
	 */
	public SecurityContextualExecutor(SecurityContextManager manager, SecurityContext targetContext) {
		securityManager = Objects.requireNonNull(manager, "Security context manager is required");
		this.targetContext = Objects.requireNonNull(targetContext, "Target security context is required");
	}

	@Override
	public <T, R> R function(Function<T, R> toCall, T arg) {
		Objects.requireNonNull(toCall, "Function is required argument");

		securityManager.initializeFromContext(targetContext);
		try {
			return toCall.apply(arg);
		} finally {
			securityManager.endContextExecution();
		}
	}

	@Override
	public <T, U, R> R biFunction(BiFunction<T, U, R> toCall, T arg1, U arg2) {
		Objects.requireNonNull(toCall, "BiFunction is required argument");

		securityManager.initializeFromContext(targetContext);
		try {
			return toCall.apply(arg1, arg2);
		} finally {
			securityManager.endContextExecution();
		}
	}

	@Override
	public <T> void consumer(Consumer<T> toCall, T arg) {
		Objects.requireNonNull(toCall, "Consumer is required argument");

		securityManager.initializeFromContext(targetContext);
		try {
			toCall.accept(arg);
		} finally {
			securityManager.endContextExecution();
		}
	}

	@Override
	public <T, U> void biConsumer(BiConsumer<T, U> toCall, T arg1, U arg2) {
		Objects.requireNonNull(toCall, "BiConsumer is required argument");

		securityManager.initializeFromContext(targetContext);
		try {
			toCall.accept(arg1, arg2);
		} finally {
			securityManager.endContextExecution();
		}
	}

	@Override
	public <T> boolean predicate(Predicate<T> toCall, T arg) {
		Objects.requireNonNull(toCall, "Predicate is required argument");

		securityManager.initializeFromContext(targetContext);
		try {
			return toCall.test(arg);
		} finally {
			securityManager.endContextExecution();
		}
	}

	@Override
	public <T, U> boolean biPredicate(BiPredicate<T, U> toCall, T arg1, U arg2) {
		Objects.requireNonNull(toCall, "BiPredicate is required argument");

		securityManager.initializeFromContext(targetContext);
		try {
			return toCall.test(arg1, arg2);
		} finally {
			securityManager.endContextExecution();
		}
	}

	@Override
	public void executable(Executable toCall) {
		Objects.requireNonNull(toCall, "Executable is required argument");

		securityManager.initializeFromContext(targetContext);
		try {
			toCall.execute();
		} finally {
			securityManager.endContextExecution();
		}
	}

	@Override
	public <V> V callable(Callable<V> toCall) throws Exception {
		Objects.requireNonNull(toCall, "Callable is required argument");

		securityManager.initializeFromContext(targetContext);
		try {
			return toCall.call();
		} finally {
			securityManager.endContextExecution();
		}
	}

	@Override
	public <T> T supplier(Supplier<T> toCall) {
		Objects.requireNonNull(toCall, "Supplier is required argument");

		securityManager.initializeFromContext(targetContext);
		try {
			return toCall.get();
		} finally {
			securityManager.endContextExecution();
		}
	}

	@Override
	public ContextualWrapper toWrapper() {
		if (wrapper == null) {
			wrapper = new SecurityContextualWrapper(securityManager, targetContext);
		}
		return wrapper;
	}
}
