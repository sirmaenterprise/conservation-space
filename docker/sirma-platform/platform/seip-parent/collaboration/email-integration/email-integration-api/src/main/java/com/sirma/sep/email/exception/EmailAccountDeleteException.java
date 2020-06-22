package com.sirma.sep.email.exception;

/**
 * For errors during mailbox delete attempt.
 *
 * @author S.Djulgerova
 */
public class EmailAccountDeleteException extends EmailIntegrationException {

	private static final long serialVersionUID = 1695232046056666123L;

	/**
	 * Instantiates a new EmailAccountDeleteException exception.
	 */
	public EmailAccountDeleteException() {
		super();
	}

	/**
	 * Instantiates a new mailboxDeleteException exception.
	 *
	 * @param message
	 *            exception message
	 * @param cause
	 *            exception cause
	 */
	public EmailAccountDeleteException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new EmailAccountDeleteException exception.
	 *
	 * @param message
	 *            exception message
	 */
	public EmailAccountDeleteException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new EmailAccountDeleteException exception.
	 *
	 * @param cause
	 *            exception cause
	 */
	public EmailAccountDeleteException(Throwable cause) {
		super(cause);
	}

}