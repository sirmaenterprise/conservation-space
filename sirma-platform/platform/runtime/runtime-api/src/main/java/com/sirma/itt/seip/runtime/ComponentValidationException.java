package com.sirma.itt.seip.runtime;

/**
 * Exception thrown to indicate errors in component definition and/or configuration.
 *
 * @author BBonev
 */
public class ComponentValidationException extends RuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 669440744594547800L;

	/**
	 * Instantiates a new component validation exception.
	 */
	public ComponentValidationException() {
		// nothing to do
	}

	/**
	 * Instantiates a new component validation exception.
	 *
	 * @param message
	 *            the message
	 */
	public ComponentValidationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new component validation exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public ComponentValidationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new component validation exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public ComponentValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new component validation exception.
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
	public ComponentValidationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
