package com.sirmaenterprise.sep.bpm.camunda.listeners.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * Interface used to bridge {@link JavaDelegate} to CDI object. The interface has single method used as proxy to be
 * invoked with the execution context and the current {@link JavaDelegate}
 * 
 * @param <T>
 *            is specific listener as subtype of {@link JavaDelegate}
 * @author bbanchev
 */
public interface CDIJavaDelegate<T extends JavaDelegate> extends JavaDelegate {

	@Override
	default void execute(DelegateExecution execution) throws Exception {
		validateParameters();
		CDIBridgeExecutor.execute(execution, this);
	}

	/**
	 * Validates if all input parameters are valid. Throws an exception on check failure to prevent transaction
	 * completion.
	 */
	void validateParameters();

	/**
	 * CDI bridge method that handles an execution processing as a CDI managed.
	 * 
	 * @param delegateExecution
	 *            is the current context
	 * @param sourceListener
	 *            is the currently executed listener to be bridged as CDI listener
	 */
	void execute(DelegateExecution delegateExecution, T sourceListener);
}
