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
 * Contextual executor that can handle system admin context initialization. The implementation supports all methods.
 *
 * @author BBonev
 */
class SystemAdminContextualExecutor implements ContextualExecutor {

	private final SecurityContextManager contextManager;

	/**
	 * Instantiate executor with the given security manager
	 *
	 * @param contextManager
	 *            manager used for context switching
	 */
	SystemAdminContextualExecutor(SecurityContextManager contextManager) {
		this.contextManager = Objects.requireNonNull(contextManager, "Manager is required argument");
	}

	@Override
	public <T, R> R function(Function<T, R> toCall, T arg) {
		contextManager.initializeExecutionAsSystemAdmin();
		try {
			return toCall.apply(arg);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T, U, R> R biFunction(BiFunction<T, U, R> toCall, T arg1, U arg2) {
		contextManager.initializeExecutionAsSystemAdmin();
		try {
			return toCall.apply(arg1, arg2);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T> void consumer(Consumer<T> toCall, T arg) {
		contextManager.initializeExecutionAsSystemAdmin();
		try {
			toCall.accept(arg);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T, U> void biConsumer(BiConsumer<T, U> toCall, T arg1, U arg2) {
		contextManager.initializeExecutionAsSystemAdmin();
		try {
			toCall.accept(arg1, arg2);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T> boolean predicate(Predicate<T> toCall, T arg) {
		contextManager.initializeExecutionAsSystemAdmin();
		try {
			return toCall.test(arg);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T, U> boolean biPredicate(BiPredicate<T, U> toCall, T arg1, U arg2) {
		contextManager.initializeExecutionAsSystemAdmin();
		try {
			return toCall.test(arg1, arg2);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public void executable(Executable toCall) {
		contextManager.initializeExecutionAsSystemAdmin();
		try {
			toCall.execute();
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <V> V callable(Callable<V> toCall) throws Exception {
		contextManager.initializeExecutionAsSystemAdmin();
		try {
			return toCall.call();
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T> T supplier(Supplier<T> toCall) {
		contextManager.initializeExecutionAsSystemAdmin();
		try {
			return toCall.get();
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public ContextualWrapper toWrapper() {
		return new SystemAdminContextualWrapper(contextManager);
	}

}
