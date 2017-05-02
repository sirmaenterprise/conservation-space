package com.sirma.itt.emf.audit.exception;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception for missing operation id.
 *
 * @author Mihail Radkov
 */
public class MissingOperationIdException extends EmfRuntimeException {

	private static final long serialVersionUID = 419703556071690480L;

	/**
	 * Instantiates a new missing operation id exception.
	 */
	public MissingOperationIdException() {
		super();
	}

	/**
	 * Instantiates a new missing operation id exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public MissingOperationIdException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

	/**
	 * Instantiates a new missing operation id exception.
	 *
	 * @param message
	 *            the message
	 */
	public MissingOperationIdException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new missing operation id exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public MissingOperationIdException(Throwable causedBy) {
		super(causedBy);
	}

}
