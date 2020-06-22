package com.sirma.sep.keycloak.events.exception;

/**
 * Generic runtime exception thrown when some operation fails.
 *
 * @author smustafov
 */
public class JmsEventListenerException extends RuntimeException {

	/**
	 * Instantiates new runtime exception.
	 */
	public JmsEventListenerException() {
		// default
	}

	/**
	 * Instantiates new runtime exception with the given message.
	 *
	 * @param message the exception message
	 */
	public JmsEventListenerException(String message) {
		super(message);
	}

	/**
	 * Instantiates new runtime exception with the given message and caused exception.
	 *
	 * @param message the exception message
	 * @param cause   the cause
	 */
	public JmsEventListenerException(String message, Throwable cause) {
		super(message, cause);
	}
}
