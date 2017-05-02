package com.sirma.cmf.web.document;

/**
 * Exception that is used in {@link DocumentSearchAction}. Will be thrown if in mapping container(with code-list values)
 * occurs duplicate element.
 *
 * @author cdimitrov
 */
public class DuplicateDocumentSearchArgumentException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor for the exception. Instantiating new exception.
	 */
	public DuplicateDocumentSearchArgumentException() {
		super();
	}

	/**
	 * Instantiating new exception with specific message.
	 *
	 * @param message
	 *            exception message
	 */
	public DuplicateDocumentSearchArgumentException(String message) {
		super(message);
	}

	/**
	 * Instantiating new exception with specific message and exception details.
	 *
	 * @param message
	 *            exception message
	 * @param cause
	 *            exception details
	 */
	public DuplicateDocumentSearchArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
}
