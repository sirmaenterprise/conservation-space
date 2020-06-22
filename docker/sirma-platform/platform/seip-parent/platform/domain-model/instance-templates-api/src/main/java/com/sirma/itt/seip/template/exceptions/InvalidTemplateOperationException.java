package com.sirma.itt.seip.template.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Thrown when an invalid operation or action is performed on a template.
 * 
 * @author Vilizar Tsonev
 */
public class InvalidTemplateOperationException extends EmfRuntimeException {

	private static final long serialVersionUID = 1075942108201128150L;

	/**
	 * Nullary constructor
	 */
	public InvalidTemplateOperationException() {
		// nothing to do here
	}

	/**
	 * Constructs the exception with the provided message and cause.
	 * 
	 * @param message
	 *            the mesage
	 * @param cause
	 *            the cause
	 */
	public InvalidTemplateOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs the exception with the provided message.
	 * 
	 * @param message
	 *            the message
	 */
	public InvalidTemplateOperationException(String message) {
		super(message);
	}
}
