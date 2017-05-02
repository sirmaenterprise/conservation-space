package com.sirma.itt.emf.cls.service;

/**
 * Exception for illegal operations upon code list and values.
 *
 * @author Mihail Radkov
 */
public class CodeListException extends Exception {

	/** Auto generated serial version UID. */
	private static final long serialVersionUID = -3016674173856495400L;

	/**
	 * Overrides super class constructor.
	 */
	public CodeListException() {
		// default constructor
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param message
	 *            exception's message
	 */
	public CodeListException(String message) {
		super(message);
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param cause
	 *            exception's cause
	 */
	public CodeListException(Throwable cause) {
		super(cause);
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param message
	 *            exception's message
	 * @param cause
	 *            exception's cause
	 */
	public CodeListException(String message, Throwable cause) {
		super(message, cause);
	}

}
