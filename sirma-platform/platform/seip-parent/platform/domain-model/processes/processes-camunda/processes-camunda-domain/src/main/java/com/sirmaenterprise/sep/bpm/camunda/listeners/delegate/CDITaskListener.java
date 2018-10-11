package com.sirmaenterprise.sep.bpm.camunda.listeners.delegate;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * Interface used to bridge {@link TaskListener} to CDI object. The interface has single method used as proxy to be
 * invoked with the execution context and the current {@link TaskListener}
 * 
 * @param <T>
 *            is specific listener as subtype of {@link TaskListener}
 * @author bbanchev
 */
public interface CDITaskListener<T extends TaskListener> extends TaskListener {

	@Override
	default void notify(DelegateTask delegateTask) {
		validateParameters();
		CDIBridgeExecutor.execute(delegateTask, this);
	}

	/**
	 * Validates if all input parameters are valid. Throws an exception on check failure to prevent transaction
	 * completion.
	 */
	void validateParameters();

	/**
	 * CDI bridge method that handles a task event processing as a CDI managed.
	 * 
	 * @param delegateTask
	 *            is the current task
	 * @param sourceListener
	 *            is the currently executed listener to be bridged as CDI listener
	 */
	void execute(DelegateTask delegateTask, T sourceListener);
}
