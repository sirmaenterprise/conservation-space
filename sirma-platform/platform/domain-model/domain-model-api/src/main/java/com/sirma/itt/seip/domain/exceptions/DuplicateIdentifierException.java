package com.sirma.itt.seip.domain.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown when field uniqueness is required and the user didn't provide new unique id when needed.
 *
 * @author BBonev
 */
public class DuplicateIdentifierException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 926027604639027755L;

	/**
	 * Instantiates a new duplicate identifier exception.
	 */
	public DuplicateIdentifierException() {
	}

	/**
	 * Instantiates a new duplicate identifier exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public DuplicateIdentifierException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new duplicate identifier exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public DuplicateIdentifierException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new duplicate identifier exception.
	 *
	 * @param arg0
	 *            the arg0
	 * @param arg1
	 *            the arg1
	 */
	public DuplicateIdentifierException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
