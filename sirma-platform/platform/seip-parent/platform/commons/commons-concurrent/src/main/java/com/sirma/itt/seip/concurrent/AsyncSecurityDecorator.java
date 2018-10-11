package com.sirma.itt.seip.concurrent;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.context.RuntimeContext;
import com.sirma.itt.seip.context.RuntimeContext.CurrentRuntimeConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Defines that can be used to wrap invocations to {@link ExecutorService#submit(Runnable)},
 * {@link ExecutorService#submit(Runnable, Object)}, {@link ExecutorService#submit(java.util.concurrent.Callable)} and
 * {@link ExecutorService#execute(Runnable)}. <br>
 * All wrapped invocations that will happen in a new thread will be executed in proper security context and runtime
 * configurations.
 * <p>
 * Example:
 *
 * <pre>
 * <code>
 *
 * &#64;Inject
 * private AsyncSecurityDecorator decorator;
 * private ExecutorService executor = ...;
 * ...
 * Future&lt;String&gt; future = decorator.invokeSecure(executor::submit, this::executeAsync);
 * ...
 * private String executeAsync() {
 *     // do something in the other thread that requires security context present
 *     return "result";
 * }
 *
 * </code>
 * </pre>
 *
 * This should be used when the asynchronous tasks should have present a security context. The implementation takes care
 * of providing the correct security context in the other execution thread.
 *
 * @author BBonev
 */
@Singleton
public class AsyncSecurityDecorator {

	private static final String EXECUTABLE_SHOULD_NOT_BE_NULL = "Executable should not be null";
	private static final String ASYNC_INVOKE_FUNCTION_COULD_NOT_BE_NULL = "Async invoke function could not be null";
	/** The security context manager. */
	@Inject
	private SecurityContextManager securityContextManager;

	/**
	 * Invoke secure.
	 *
	 * @param <R>
	 *            the generic type
	 * @param asyncInvoke
	 *            the async invoke
	 * @param toCall
	 *            the to call
	 * @return the future
	 */
	public <R> Future<R> invokeSecure(Function<Callable<R>, Future<R>> asyncInvoke, Supplier<R> toCall) {
		Objects.requireNonNull(asyncInvoke, ASYNC_INVOKE_FUNCTION_COULD_NOT_BE_NULL);
		Objects.requireNonNull(toCall, EXECUTABLE_SHOULD_NOT_BE_NULL);

		SecurityContext transferableContext = securityContextManager.createTransferableContext();
		CurrentRuntimeConfiguration runtimeConfiguration = RuntimeContext.getCurrentConfiguration();
		return asyncInvoke.apply(() -> {
			CurrentRuntimeConfiguration oldConfiguration = RuntimeContext.replaceConfiguration(runtimeConfiguration);
			try {
				securityContextManager.initializeFromContext(transferableContext);
				return toCall.get();
			} finally {
				securityContextManager.endContextExecution();
				RuntimeContext.replaceConfiguration(oldConfiguration);
			}
		});
	}

	/**
	 * Invoke secure.
	 *
	 * @param <R>
	 *            the generic type
	 * @param asyncInvoke
	 *            the async invoke
	 * @param toCall
	 *            the to call
	 * @param result
	 *            the result
	 * @return the future
	 */
	public <R> Future<R> invokeSecure(BiFunction<Runnable, R, Future<R>> asyncInvoke, Executable toCall, R result) {
		Objects.requireNonNull(asyncInvoke, ASYNC_INVOKE_FUNCTION_COULD_NOT_BE_NULL);
		Objects.requireNonNull(toCall, EXECUTABLE_SHOULD_NOT_BE_NULL);

		SecurityContext transferableContext = securityContextManager.createTransferableContext();
		CurrentRuntimeConfiguration runtimeConfiguration = RuntimeContext.getCurrentConfiguration();
		return asyncInvoke.apply(() -> invokeSecureInternal(toCall, transferableContext, runtimeConfiguration), result);
	}

	/**
	 * Invoke secure.
	 *
	 * @param <F>
	 *            future type
	 * @param asyncInvoke
	 *            the async invoke
	 * @param toCall
	 *            the to call
	 * @return the future
	 */
	public <F> Future<F> invokeSecure(Function<Runnable, Future<F>> asyncInvoke, Executable toCall) {
		Objects.requireNonNull(asyncInvoke, ASYNC_INVOKE_FUNCTION_COULD_NOT_BE_NULL);
		Objects.requireNonNull(toCall, EXECUTABLE_SHOULD_NOT_BE_NULL);

		SecurityContext transferableContext = securityContextManager.createTransferableContext();
		CurrentRuntimeConfiguration runtimeConfiguration = RuntimeContext.getCurrentConfiguration();
		return asyncInvoke.apply(() -> invokeSecureInternal(toCall, transferableContext, runtimeConfiguration));
	}

	/**
	 * Invoke secure the given executable
	 *
	 * @param asyncInvoke
	 *            the async invoke
	 * @param toCall
	 *            the to call
	 */
	public void invokeSecureConsumer(Consumer<Runnable> asyncInvoke, Executable toCall) {
		Objects.requireNonNull(asyncInvoke, ASYNC_INVOKE_FUNCTION_COULD_NOT_BE_NULL);
		Objects.requireNonNull(toCall, EXECUTABLE_SHOULD_NOT_BE_NULL);

		SecurityContext transferableContext = securityContextManager.createTransferableContext();
		CurrentRuntimeConfiguration runtimeConfiguration = RuntimeContext.getCurrentConfiguration();
		asyncInvoke.accept(() -> invokeSecureInternal(toCall, transferableContext, runtimeConfiguration));
	}

	private void invokeSecureInternal(Executable toCall, SecurityContext transferableContext,
			CurrentRuntimeConfiguration runtimeConfiguration) {
		CurrentRuntimeConfiguration oldConfiguration = RuntimeContext.replaceConfiguration(runtimeConfiguration);
		try {
			securityContextManager.initializeFromContext(transferableContext);
			toCall.execute();
		} finally {
			securityContextManager.endContextExecution();
			RuntimeContext.replaceConfiguration(oldConfiguration);
		}
	}

}
