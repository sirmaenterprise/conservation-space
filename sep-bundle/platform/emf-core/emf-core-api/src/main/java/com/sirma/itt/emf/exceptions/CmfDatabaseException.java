package com.sirma.itt.emf.exceptions;

/**
 * Exception thrown to indicate a problem with execution of a database query/request.
 * 
 * REVIEW Rename to Seip or Emf + DatabaseException
 * 
 * @author BBonev
 */
public class CmfDatabaseException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1580146352763474196L;

	/**
	 * Instantiates a new cmf database exception.
	 */
	public CmfDatabaseException() {
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
	public CmfDatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new cmf database exception.
	 * 
	 * @param message
	 *            the message
	 */
	public CmfDatabaseException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new cmf database exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public CmfDatabaseException(Throwable cause) {
		super(cause);
	}

}
