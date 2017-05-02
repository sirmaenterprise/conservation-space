package com.sirmaenterprise.sep.bpm.camunda.listeners.delegate;

import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;

/**
 * CDI bridge executor of event received in Camunda Engine that is specific implementation of {@link CDIJavaDelegate} or
 * {@link CDITaskListener} or {@link CDIExecutionListener}
 * 
 * @author bbanchev
 */
public class CDIBridgeExecutor {
	private static final String MSG_ERR_INVALID_BEAN = "Recieved non CDI bridge event: ";
	private static final String MSG_ERR_INVALID_TYPE = "Recieved invalid CDI bridge event! Expected instanceof: ";

	private CDIBridgeExecutor() {
		// utility class for static usage
	}

	/**
	 * Executes a CDIJavaDelegate event as CDI bean by invoking
	 * {@link CDIJavaDelegate#execute(DelegateExecution, JavaDelegate)}. All exceptions are rethrown.
	 * 
	 * @param execution
	 *            is the current execution provided by the Camunda engine
	 * @param delegateListener
	 *            is the Camunda {@link CDIJavaDelegate} listener that the event is handled by, used as source value
	 */
	public static <T extends CDIJavaDelegate<?>> void execute(DelegateExecution execution, T delegateListener) {
		if (delegateListener == null || !CDIJavaDelegate.class.isAssignableFrom(delegateListener.getClass())) {
			throw new CamundaIntegrationRuntimeException(MSG_ERR_INVALID_TYPE + CDIJavaDelegate.class);
		}
		@SuppressWarnings("unchecked")
		CDIJavaDelegate<T> cdiDelegate = ProgrammaticBeanLookup.lookup(delegateListener.getClass());
		if (cdiDelegate == null) {
			throw new CamundaIntegrationRuntimeException(MSG_ERR_INVALID_BEAN + delegateListener.getClass());
		}
		cdiDelegate.execute(execution, delegateListener);
	}

	/**
	 * Executes a CDITaskListener event as CDI bean by invoking
	 * {@link CDITaskListener#execute(DelegateTask, TaskListener)}. All exceptions are rethrown.
	 * 
	 * @param task
	 *            is the current task provided by the Camunda engine
	 * @param delegateListener
	 *            is the Camunda {@link CDITaskListener} listener that the event is handled by, used as source value
	 */
	public static <T extends CDITaskListener<?>> void execute(DelegateTask task, T delegateListener) {
		if (delegateListener == null || !CDITaskListener.class.isAssignableFrom(delegateListener.getClass())) {
			throw new CamundaIntegrationRuntimeException(MSG_ERR_INVALID_TYPE + CDITaskListener.class);
		}
		@SuppressWarnings("unchecked")
		CDITaskListener<T> cdiDelegate = ProgrammaticBeanLookup.lookup(delegateListener.getClass());
		if (cdiDelegate == null) {
			throw new CamundaIntegrationRuntimeException(MSG_ERR_INVALID_BEAN + delegateListener.getClass());
		}
		cdiDelegate.execute(task, delegateListener);
	}

	/**
	 * Executes a CDIExecutionListener event as CDI bean by invoking
	 * {@link CDIExecutionListener#notify(DelegateExecution, ExecutionListener)}. All exceptions are rethrown.
	 * 
	 * @param execution
	 *            is the current {@link DelegateExecution} received from Camunda
	 * @param delegateListener
	 *            is the Camunda {@link CDIExecutionListener} listener that the event is handled by, used as source
	 *            value
	 */
	public static <T extends CDIExecutionListener<?>> void notify(DelegateExecution execution, T delegateListener) {
		if (delegateListener == null || !CDIExecutionListener.class.isAssignableFrom(delegateListener.getClass())) {
			throw new CamundaIntegrationRuntimeException(MSG_ERR_INVALID_TYPE + CDIExecutionListener.class);
		}
		@SuppressWarnings("unchecked")
		CDIExecutionListener<T> cdiDelegate = ProgrammaticBeanLookup.lookup(delegateListener.getClass());
		if (cdiDelegate == null) {
			throw new CamundaIntegrationRuntimeException(MSG_ERR_INVALID_BEAN + delegateListener.getClass());
		}
		cdiDelegate.notify(execution, delegateListener);
	}

}
