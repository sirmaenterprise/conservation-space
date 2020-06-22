package com.sirma.itt.seip.security.exception;

import javax.ejb.ApplicationException;

/**
 * Exception thrown when trying to access context information when there is no active context.
 *
 * @author BBonev
 */
@ApplicationException(rollback = true)
public class SecurityException extends RuntimeException {

	private static final long serialVersionUID = -339582817574552859L;

	/**
	 * Instantiates a new context not active exception.
	 */
	public SecurityException() {
		super();
	}

	/**
	 * Instantiates a new context not active exception.
	 *
	 * @param message
	 *            the message
	 */
	public SecurityException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new context not active exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public SecurityException(Throwable causedBy) {
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
	public SecurityException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
