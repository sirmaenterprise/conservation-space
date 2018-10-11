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
 * Contextual executor that can handle tenant changing. The implementation supports all methods.
 *
 * @author BBonev
 */
class TenantContextualExecutor implements ContextualExecutor {

	private final SecurityContextManager contextManager;
	private final String tenant;

	/**
	 * Instantiate executor and initialize the tenant
	 *
	 * @param contextManager
	 *            manager used for context switching
	 * @param tenant
	 *            the tenant identifier to use on context switching
	 */
	TenantContextualExecutor(SecurityContextManager contextManager, String tenant) {
		this.contextManager = Objects.requireNonNull(contextManager, "Manager is required argument");
		this.tenant = Objects.requireNonNull(tenant, "Tenant id is required argument");
	}

	@Override
	public <T, R> R function(Function<T, R> toCall, T arg) {
		contextManager.initializeTenantContext(tenant);
		try {
			return toCall.apply(arg);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T, U, R> R biFunction(BiFunction<T, U, R> toCall, T arg1, U arg2) {
		contextManager.initializeTenantContext(tenant);
		try {
			return toCall.apply(arg1, arg2);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T> void consumer(Consumer<T> toCall, T arg) {
		contextManager.initializeTenantContext(tenant);
		try {
			toCall.accept(arg);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T, U> void biConsumer(BiConsumer<T, U> toCall, T arg1, U arg2) {
		contextManager.initializeTenantContext(tenant);
		try {
			toCall.accept(arg1, arg2);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T> boolean predicate(Predicate<T> toCall, T arg) {
		contextManager.initializeTenantContext(tenant);
		try {
			return toCall.test(arg);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T, U> boolean biPredicate(BiPredicate<T, U> toCall, T arg1, U arg2) {
		contextManager.initializeTenantContext(tenant);
		try {
			return toCall.test(arg1, arg2);
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public void executable(Executable toCall) {
		contextManager.initializeTenantContext(tenant);
		try {
			toCall.execute();
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <V> V callable(Callable<V> toCall) throws Exception {
		contextManager.initializeTenantContext(tenant);
		try {
			return toCall.call();
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public <T> T supplier(Supplier<T> toCall) {
		contextManager.initializeTenantContext(tenant);
		try {
			return toCall.get();
		} finally {
			contextManager.endContextExecution();
		}
	}

	@Override
	public ContextualWrapper toWrapper() {
		return new TenantContextualWrapper(contextManager, tenant);
	}

}
