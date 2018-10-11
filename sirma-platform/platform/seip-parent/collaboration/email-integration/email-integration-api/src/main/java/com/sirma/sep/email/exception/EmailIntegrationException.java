package com.sirma.sep.email.exception;

/**
 * The Class EmailIntegrationException for errors during email integration operations.
 *
 * @author S.Djulgerova
 */
public class EmailIntegrationException extends Exception {

	private static final long serialVersionUID = 1695232046056666123L;

	/**
	 * Instantiates a new emailIntegrationException exception.
	 */
	public EmailIntegrationException() {
		super();
	}

	/**
	 * Instantiates a new emailIntegrationException exception.
	 *
	 * @param message
	 *            exception message
	 * @param cause
	 *            exception cause
	 */
	public EmailIntegrationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new emailIntegrationException exception.
	 *
	 * @param message
	 *            exception message
	 */
	public EmailIntegrationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new emailIntegrationException exception.
	 *
	 * @param cause
	 *            exception cause
	 */
	public EmailIntegrationException(Throwable cause) {
		super(cause);
	}

}
