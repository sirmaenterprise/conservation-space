package com.sirmaenterprise.sep.bpm.camunda.exception;

import com.sirmaenterprise.sep.bpm.exception.BPMRuntimeException;

/**
 * The @{link CamundaIntegrationException} indicates general runtime exception during operations on Camunda engine.
 *
 * @author Borislav Banchev
 */
public class CamundaIntegrationRuntimeException extends BPMRuntimeException {

	private static final long serialVersionUID = -8405112152637726226L;

	/**
	 * Instantiates a new Camunda integration exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public CamundaIntegrationRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new Camunda integration exception.
	 *
	 * @param message
	 *            the message
	 */
	public CamundaIntegrationRuntimeException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new Camunda integration exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public CamundaIntegrationRuntimeException(Throwable cause) {
		super(cause);
	}

}
