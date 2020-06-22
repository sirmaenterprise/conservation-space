package com.sirma.itt.seip.runtime.boot;

/**
 * Exception thrown to indicate errors in component execution.
 *
 * @author nvelkov
 */
public class FailedExecutionException extends RuntimeException {

	private static final long serialVersionUID = -8159195464085365789L;

	/**
	 * Instantiates a new failed execution exception.
	 */
	public FailedExecutionException() {
		// nothing to do
	}

	/**
	 * Instantiates a new failed execution exception.
	 *
	 * @param message
	 *            the message
	 */
	public FailedExecutionException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new failed execution exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public FailedExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new failed execution exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public FailedExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new failed execution exception.
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
	public FailedExecutionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}