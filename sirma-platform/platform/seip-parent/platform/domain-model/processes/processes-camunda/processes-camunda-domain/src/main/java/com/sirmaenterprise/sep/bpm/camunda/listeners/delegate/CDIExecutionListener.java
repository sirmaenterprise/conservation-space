package com.sirmaenterprise.sep.bpm.camunda.listeners.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

/**
 * {@link CDIExecutionListener} is a CDI event bridge listener. The interface has single method used as proxy to be
 * invoked with the execution context and the current {@link DelegateExecution}
 * 
 * @param <T>
 *            is specific listener as subtype of {@link ExecutionListener}
 * @author bbanchev
 */
public interface CDIExecutionListener<T extends ExecutionListener> extends ExecutionListener {

	@Override
	default void notify(DelegateExecution execution) throws Exception {
		validateParameters();
		CDIBridgeExecutor.notify(execution, this);
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
	 *            is the current execution context
	 * @param sourceListener
	 *            is the currently executed listener to be bridged as CDI listener
	 */
	void notify(DelegateExecution delegateExecution, T sourceListener);
}
