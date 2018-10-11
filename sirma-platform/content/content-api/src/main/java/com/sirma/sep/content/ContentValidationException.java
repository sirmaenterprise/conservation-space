/**
 *
 */
package com.sirma.sep.content;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown to indicate problem with the instance view structure or format.
 *
 * @author BBonev
 */
public class ContentValidationException extends EmfRuntimeException {

	private static final long serialVersionUID = 4331490992665491261L;

	/**
	 * Instantiates a new content validation exception.
	 */
	public ContentValidationException() {
		// nothing to do
	}

	/**
	 * Instantiates a new content validation exception.
	 *
	 * @param message
	 *            the message
	 */
	public ContentValidationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new content validation exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public ContentValidationException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * Instantiates a new content validation exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public ContentValidationException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
