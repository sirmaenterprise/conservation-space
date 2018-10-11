package com.sirmaenterprise.sep.bpm.camunda.exception;

import com.sirmaenterprise.sep.bpm.exception.BPMException;

/**
 * The {@link CamundaIntegrationException} indicates general exception during operations on Camunda engine.
 *
 * @author bbanchev
 */
public class CamundaIntegrationException extends BPMException {

	private static final long serialVersionUID = -6342842316803069798L;

	/**
	 * Instantiates a new Camunda integration exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param enableSuppression
	 *            the enable suppression
	 * @param writableStackTrace
	 *            the writable stack trace
	 */
	public CamundaIntegrationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new Camunda integration exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public CamundaIntegrationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new Camunda integration exception.
	 *
	 * @param message
	 *            the message
	 */
	public CamundaIntegrationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new Camunda integration exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public CamundaIntegrationException(Throwable cause) {
		super(cause);
	}

}
