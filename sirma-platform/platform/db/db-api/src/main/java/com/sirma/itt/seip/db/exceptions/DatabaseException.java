package com.sirma.itt.seip.db.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown to indicate a problem with execution of a database query/request. REVIEW Rename to Seip or Emf +
 * DatabaseException
 *
 * @author BBonev
 */
public class DatabaseException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1580146352763474196L;

	/**
	 * Instantiates a new cmf database exception.
	 */
	public DatabaseException() {
		super();
	}

	/**
	 * Instantiates a new cmf database exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new cmf database exception.
	 *
	 * @param message
	 *            the message
	 */
	public DatabaseException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new cmf database exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public DatabaseException(Throwable cause) {
		super(cause);
	}

}
