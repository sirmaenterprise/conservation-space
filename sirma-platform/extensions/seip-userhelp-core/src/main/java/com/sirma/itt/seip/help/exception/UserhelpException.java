package com.sirma.itt.seip.help.exception;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The UserhelpException indicates exceptions during help request.
 */
public class UserhelpException extends EmfRuntimeException {

	/** Comment for serialVersionUID. */
	private static final long serialVersionUID = -9061422019926675225L;

	/**
	 * Instantiates a new userhelp exception.
	 */
	public UserhelpException() {
		super();
	}

	/**
	 * Instantiates a new userhelp exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public UserhelpException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

	/**
	 * Instantiates a new userhelp exception.
	 *
	 * @param message
	 *            the message
	 */
	public UserhelpException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new userhelp exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public UserhelpException(Throwable causedBy) {
		super(causedBy);
	}

}
