package com.sirma.itt.emf.cls.validator.exception;

import com.sirma.itt.emf.cls.validator.SheetValidator;

/**
 * Exception for {@link SheetValidator}.
 *
 * @author svetlozar.iliev
 */
public class SheetValidatorException extends RuntimeException {

	/** Auto generated serial version UID. */
	private static final long serialVersionUID = 3320209427900719033L;

	/**
	 * Overrides super class constructor.
	 *
	 * @param message
	 *            exception's message
	 */
	public SheetValidatorException(String message) {
		super(message);
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param message
	 *            exception's message
	 * @param cause
	 *            exception's cause
	 */
	public SheetValidatorException(String message, Throwable cause) {
		super(message, cause);
	}

}
