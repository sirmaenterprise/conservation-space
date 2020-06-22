/**
 *
 */
package com.sirma.itt.seip.mapping;

/**
 * Thrown to indicate problem during mapping.
 *
 * @author BBonev
 */
public class ObjectMappingException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1200179813677653696L;

	/**
	 * Instantiates a new object mapping exception.
	 */
	public ObjectMappingException() {
		// implement me
	}

	/**
	 * Instantiates a new object mapping exception.
	 *
	 * @param message
	 *            the message
	 */
	public ObjectMappingException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new object mapping exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public ObjectMappingException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new object mapping exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public ObjectMappingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new object mapping exception.
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
	public ObjectMappingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
