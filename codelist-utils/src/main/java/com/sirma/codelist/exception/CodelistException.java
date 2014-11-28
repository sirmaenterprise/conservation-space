/**
 * Copyright (c) 2009 29.09.2009 , Sirma ITT.
 */
package com.sirma.codelist.exception;

/**
 * Thrown when the user requests information for invalid or non existing code
 * list number.
 * 
 * @author Borislav Bonev
 */
public class CodelistException extends RuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6921011726229497734L;

	/**
     * 
     */
	public CodelistException() {
		// nothing here
	}

	/**
	 * @param message
	 *            exception message
	 */
	public CodelistException(String message) {
		super(message);
	}

	/**
	 * @param message
	 *            exception message
	 * @param cause
	 *            previous exception that case this exception to be thrown
	 */
	public CodelistException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 *            previous exception that case this exception to be thrown
	 */
	public CodelistException(Throwable cause) {
		super(cause);
	}

}
