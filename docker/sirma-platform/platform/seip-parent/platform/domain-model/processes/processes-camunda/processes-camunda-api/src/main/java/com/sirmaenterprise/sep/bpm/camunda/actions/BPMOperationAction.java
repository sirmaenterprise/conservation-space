package com.sirmaenterprise.sep.bpm.camunda.actions;


import org.apache.commons.lang.exception.ExceptionUtils;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.exception.BPMException;
import com.sirmaenterprise.sep.bpm.exception.BPMRuntimeException;

/**
 * Defines a basic action handler for {@link BPMActionRequest}. Each implementing handler should implement
 * {@link #executeBPMAction(BPMActionRequest)} which is invoked in transaction
 *
 * @author bbanchev
 * @param <T>
 *            is the specific type of {@link BPMActionRequest}
 */
public abstract class BPMOperationAction<T extends BPMActionRequest> implements Action<T> {

	@Override
	public Object perform(T request) {
		if (request == null || !(request.getTargetId() instanceof String)) {
			throw new CamundaIntegrationRuntimeException("Invalid request for action: " + request);
		}
		try {
			preProcessBPMAction(request);
			return executeBPMAction(request);
		} catch (BPMRuntimeException e) {
			throw e;
		} catch (Exception e) {
			Throwable rootCause = ExceptionUtils.getRootCause(e);
			Throwable actualRootCause = rootCause == null ? e : rootCause;
			throw new CamundaIntegrationRuntimeException("Error during execution of action: " + actualRootCause.getMessage(), e);
		} finally {
			postProcessBPMAction(request);
		}
	}

	/**
	 * Method would be executed immediately before {@link #executeBPMAction(BPMActionRequest)} to setup execution or to
	 * validate the request
	 * 
	 * @param request
	 *            is the specific {@link BPMActionRequest}
	 */
	protected abstract void preProcessBPMAction(T request);

	/**
	 * Executes the specific action and returns the result of it.
	 *
	 * @param request
	 *            is the specific {@link BPMActionRequest}
	 * @return arbitrary result for the specific action
	 * @throws BPMException
	 *             on any error during execution
	 */
	protected abstract Object executeBPMAction(T request) throws BPMException;

	/**
	 * Method would be executed always after {@link #executeBPMAction(BPMActionRequest)} to clean up resources or post
	 * process {@link BPMActionRequest}s
	 * 
	 * @param request
	 *            is the specific {@link BPMActionRequest}
	 */
	protected abstract void postProcessBPMAction(T request);
}
