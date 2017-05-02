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
 * Contextual wrapper that can handle system admin context initialization. The implementation supports all methods.
 *
 * @author BBonev
 */
class SystemAdminContextualWrapper implements ContextualWrapper {

	private final SecurityContextManager contextManager;

	/**
	 * Instantiate wrapper with the given security manager
	 *
	 * @param contextManager
	 *            manager used for context switching
	 */
	SystemAdminContextualWrapper(SecurityContextManager contextManager) {
		this.contextManager = Objects.requireNonNull(contextManager, "Manager is required argument");
	}

	@Override
	public <T, R> Function<T, R> function(Function<T, R> toWrap) {
		SecurityContextManager manager = contextManager;
		return arg -> {
			manager.initializeExecutionAsSystemAdmin();
			try {
				return toWrap.apply(arg);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T, U, R> BiFunction<T, U, R> biFunction(BiFunction<T, U, R> toWrap) {
		SecurityContextManager manager = contextManager;
		return (arg1, arg2) -> {
			manager.initializeExecutionAsSystemAdmin();
			try {
				return toWrap.apply(arg1, arg2);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T> Consumer<T> consumer(Consumer<T> toWrap) {
		SecurityContextManager manager = contextManager;
		return arg -> {
			manager.initializeExecutionAsSystemAdmin();
			try {
				toWrap.accept(arg);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T, U> BiConsumer<T, U> biConsumer(BiConsumer<T, U> toWrap) {
		SecurityContextManager manager = contextManager;
		return (arg1, arg2) -> {
			manager.initializeExecutionAsSystemAdmin();
			try {
				toWrap.accept(arg1, arg2);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T> Predicate<T> predicate(Predicate<T> toWrap) {
		SecurityContextManager manager = contextManager;
		return arg -> {
			manager.initializeExecutionAsSystemAdmin();
			try {
				return toWrap.test(arg);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T, U> BiPredicate<T, U> biPredicate(BiPredicate<T, U> toWrap) {
		SecurityContextManager manager = contextManager;
		return (arg1, arg2) -> {
			manager.initializeExecutionAsSystemAdmin();
			try {
				return toWrap.test(arg1, arg2);
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public Executable executable(Executable toWrap) {
		SecurityContextManager manager = contextManager;
		return () -> {
			manager.initializeExecutionAsSystemAdmin();
			try {
				toWrap.execute();
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <V> Callable<V> callable(Callable<V> toWrap) {
		SecurityContextManager manager = contextManager;
		return () -> {
			manager.initializeExecutionAsSystemAdmin();
			try {
				return toWrap.call();
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public <T> Supplier<T> supplier(Supplier<T> toWrap) {
		SecurityContextManager manager = contextManager;
		return () -> {
			manager.initializeExecutionAsSystemAdmin();
			try {
				return toWrap.get();
			} finally {
				manager.endContextExecution();
			}
		};
	}

	@Override
	public ContextualExecutor toExecutor() {
		return new SystemAdminContextualExecutor(contextManager);
	}

}
