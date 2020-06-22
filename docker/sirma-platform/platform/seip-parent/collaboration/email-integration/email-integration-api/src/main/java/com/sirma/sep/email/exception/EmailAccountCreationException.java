package com.sirma.sep.email.exception;

/**
 * For errors during mailbox creation.
 *
 * @author S.Djulgerova
 */
public class EmailAccountCreationException extends EmailIntegrationException {

	private static final long serialVersionUID = 1695232046056666123L;

	/**
	 * Instantiates a new EmailAccountCreationException exception.
	 */
	public EmailAccountCreationException() {
		super();
	}

	/**
	 * Instantiates a new EmailAccountCreationException exception.
	 *
	 * @param message
	 *            exception message
	 * @param cause
	 *            exception cause
	 */
	public EmailAccountCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new EmailAccountCreationException exception.
	 *
	 * @param message
	 *            exception message
	 */
	public EmailAccountCreationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new EmailAccountCreationException exception.
	 *
	 * @param cause
	 *            exception cause
	 */
	public EmailAccountCreationException(Throwable cause) {
		super(cause);
	}

}
