package com.sirma.itt.seip.runtime.boot;

/**
 * Exception thrown by components on application start.
 *
 * @author BBonev
 */
public class StartupException extends FailedExecutionException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 5887954161145326528L;

	/**
	 * Instantiates a new startup exception.
	 */
	public StartupException() {
		// nothing to do
	}

	/**
	 * Instantiates a new startup exception.
	 *
	 * @param message
	 *            the message
	 */
	public StartupException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new startup exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public StartupException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new startup exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public StartupException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new startup exception.
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
	public StartupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
