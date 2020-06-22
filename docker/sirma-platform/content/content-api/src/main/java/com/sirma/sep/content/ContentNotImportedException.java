package com.sirma.sep.content;

/**
 * Thrown when specific content fails to import in to the system.
 *
 * @author A. Kunchev
 */
public class ContentNotImportedException extends RuntimeException {

	private static final long serialVersionUID = 5294325661879318582L;

	/**
	 * Instantiates a new content not imported exception.
	 */
	public ContentNotImportedException() {
		super();
	}

	/**
	 * Instantiates a new content not imported exception.
	 *
	 * @param message
	 *            the exception message
	 */
	public ContentNotImportedException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new content not imported exception.
	 *
	 * @param message
	 *            the exception message
	 * @param cause
	 *            the cause for the exception
	 */
	public ContentNotImportedException(String message, Throwable cause) {
		super(message, cause);
	}

}
