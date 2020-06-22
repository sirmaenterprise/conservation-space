package com.sirma.itt.seip.runtime.boot;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception that indicates that a deployment error has occured and the deployment needs to be stopped no matter at what
 * phase it is.
 *
 * @author nvelkov
 */
public class DeploymentException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3202330872304891115L;

	/**
	 * Instantiates a new DeploymentException.
	 */
	public DeploymentException() {
		super();
	}

	/**
	 * Instantiates a new DeploymentException.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public DeploymentException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new DeploymentException.
	 *
	 * @param message
	 *            the message
	 */
	public DeploymentException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new DeploymentException.
	 *
	 * @param cause
	 *            the cause
	 */
	public DeploymentException(Throwable cause) {
		super(cause);
	}

}
