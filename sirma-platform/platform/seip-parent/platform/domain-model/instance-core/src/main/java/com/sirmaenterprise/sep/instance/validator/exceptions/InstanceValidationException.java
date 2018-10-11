package com.sirmaenterprise.sep.instance.validator.exceptions;

import java.util.Map;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;

/**
 * Base exception thrown during instance validation process.
 * 
 * @author tdossev
 *
 */
public class InstanceValidationException extends EmfApplicationException{

	private static final long serialVersionUID = 8646916891691738377L;

	/**
	 * Instantiates a new validation exception.
	 *
	 * @param message
	 *            the message
	 */
	public InstanceValidationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new validation exception.
	 *
	 * @param message
	 *            the message
	 * @param messages
	 *            the messages
	 */
	public InstanceValidationException(String message, Map<String, String> messages) {
		super(message, messages);
	}

	/**
	 * Instantiates a new validation exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public InstanceValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new validation exception.
	 *
	 * @param message
	 *            the message
	 * @param messages
	 *            the messages
	 * @param cause
	 *            the cause
	 */
	public InstanceValidationException(String message, Map<String, String> messages, Throwable cause) {
		super(message, messages, cause);
	}
}
