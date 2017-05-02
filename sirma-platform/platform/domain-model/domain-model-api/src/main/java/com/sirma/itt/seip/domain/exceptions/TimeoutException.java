package com.sirma.itt.seip.domain.exceptions;

import java.util.Map;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;

/**
 * Exception thrown by methods that wait time for some condition to occur but the condition is never met and they cannot
 * continue correctly their execution or wait indefinitely.
 *
 * @author BBonev
 */
public class TimeoutException extends EmfApplicationException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -310520824007753320L;

	/**
	 * Instantiates a new timeout exception.
	 *
	 * @param message
	 *            the message
	 */
	public TimeoutException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new timeout exception.
	 *
	 * @param message
	 *            the message
	 * @param messages
	 *            the messages
	 */
	public TimeoutException(String message, Map<String, String> messages) {
		super(message, messages);
	}

	/**
	 * Instantiates a new timeout exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new timeout exception.
	 *
	 * @param message
	 *            the message
	 * @param messages
	 *            the messages
	 * @param cause
	 *            the cause
	 */
	public TimeoutException(String message, Map<String, String> messages, Throwable cause) {
		super(message, messages, cause);
	}

}
