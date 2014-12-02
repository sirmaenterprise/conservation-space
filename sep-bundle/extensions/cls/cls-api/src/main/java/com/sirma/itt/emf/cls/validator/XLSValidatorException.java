package com.sirma.itt.emf.cls.validator;

/**
 * Exception for XLS file validation.
 * 
 * @author Mihail Radkov
 */
public class XLSValidatorException extends Exception {

	/** Auto generated serial version UID. */
	private static final long serialVersionUID = 3320209427900719033L;

	/**
	 * Overrides super class constructor.
	 */
	public XLSValidatorException() {
	}

	/**
	 * Overrides super class constructor.
	 * 
	 * @param message
	 *            exception's message
	 */
	public XLSValidatorException(String message) {
		super(message);
	}

	/**
	 * Overrides super class constructor.
	 * 
	 * @param cause
	 *            exception's cause
	 */
	public XLSValidatorException(Throwable cause) {
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
	public XLSValidatorException(String message, Throwable cause) {
		super(message, cause);
	}

}
