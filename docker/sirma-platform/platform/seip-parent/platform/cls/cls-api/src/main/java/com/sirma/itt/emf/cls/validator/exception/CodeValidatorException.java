package com.sirma.itt.emf.cls.validator.exception;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.emf.cls.validator.CodeValidator;

/**
 * Exception for {@link CodeValidator}
 *
 * @author svetlozar.iliev
 */
public class CodeValidatorException extends RuntimeException {

	/** Auto generated serial version UID. */
	private static final long serialVersionUID = 3320209427900719033L;

	private final List<String> errors;

	/**
	 * Overrides super class constructor.
	 *
	 * @param message
	 *            exception's message
	 */
	public CodeValidatorException(String message) {
		super(message);
		this.errors = new ArrayList<>();
	}

	/**
	 * Custom class constructor specifying additional error messages.
	 *
	 * @param message
	 *            exception's message
	 * @param errors
	 *            a list of additional error messages
	 */
	public CodeValidatorException(String message, List<String> errors) {
		super(message);
		this.errors = errors;
	}

	/**
	 * Getter method for contained error messages
	 * 
	 * @return the error messages
	 */
	public List<String> getErrors() {
		return errors;
	}
}
