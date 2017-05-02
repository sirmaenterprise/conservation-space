/**
 *
 */
package com.sirma.itt.seip.security.exception;

/**
 * Exception thrown when trying to access context information when there is no active context.
 *
 * @author BBonev
 */
public class ContextNotActiveException extends RuntimeException {

	private static final long serialVersionUID = -339582817574552859L;

	/**
	 * Instantiates a new context not active exception.
	 */
	public ContextNotActiveException() {
		super();
	}

	/**
	 * Instantiates a new context not active exception.
	 *
	 * @param message
	 *            the message
	 */
	public ContextNotActiveException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new context not active exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public ContextNotActiveException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * Instantiates a new context not active exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public ContextNotActiveException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
