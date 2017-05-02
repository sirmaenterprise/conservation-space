package com.sirma.itt.seip.domain.exceptions;

import java.util.Map;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;

/**
 * Application exception thrown if a service method is invoked on a deleted instance or non existent such
 *
 * @author BBonev
 */
public class InstanceDeletedException extends EmfApplicationException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7809830714727563318L;

	/**
	 * Instantiates a new instance deleted exception.
	 *
	 * @param message
	 *            the message
	 */
	public InstanceDeletedException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new instance deleted exception.
	 *
	 * @param message
	 *            the message
	 * @param messages
	 *            the messages
	 */
	public InstanceDeletedException(String message, Map<String, String> messages) {
		super(message, messages);
	}

	/**
	 * Instantiates a new instance deleted exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public InstanceDeletedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new instance deleted exception.
	 *
	 * @param message
	 *            the message
	 * @param messages
	 *            the messages
	 * @param cause
	 *            the cause
	 */
	public InstanceDeletedException(String message, Map<String, String> messages, Throwable cause) {
		super(message, messages, cause);
	}

}
