package com.sirma.itt.seip.template.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Thrown when an operation is attempted on a template that doesn't exist.
 * 
 * @author Vilizar Tsonev
 */
public class MissingTemplateException extends EmfRuntimeException {

	private static final long serialVersionUID = -1388436395897842396L;

	/**
	 * Nullary constructor.
	 */
	public MissingTemplateException() {
		// nothing to do here
	}

	/**
	 * Constructs the exception with the provided message and cause.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public MissingTemplateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs the exception with the provided message.
	 * 
	 * @param message
	 *            the message
	 */
	public MissingTemplateException(String message) {
		super(message);
	}

}
