package com.sirma.sep.content;

/**
 * Thrown when specific content is not found or the system fails to retrieve it.
 *
 * @author A. Kunchev
 */
public class ContentNotFoundRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 4899565273899421442L;

	/**
	 * Instantiates a new content not found exception.
	 */
	public ContentNotFoundRuntimeException() {
		super();
	}

	/**
	 * Instantiates a new content not found exception.
	 *
	 * @param message
	 *            the exception message
	 */
	public ContentNotFoundRuntimeException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new content not found exception.
	 *
	 * @param message
	 *            the exception message
	 * @param cause
	 *            the cause for the exception
	 */
	public ContentNotFoundRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
