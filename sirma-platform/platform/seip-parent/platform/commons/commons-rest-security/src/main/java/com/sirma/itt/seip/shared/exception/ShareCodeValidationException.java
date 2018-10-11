package com.sirma.itt.seip.shared.exception;

import com.sirma.itt.seip.exception.EmfException;

/**
 * Share code validation exception thrown when some of the parameters needed for the share code
 * construction/deconstruction are missing.
 * 
 * @author nvelkov
 */
public class ShareCodeValidationException extends EmfException {

	private static final long serialVersionUID = -1512125976010983411L;

	/**
	 * Create a {@link ShareCodeValidationException}.
	 */
	public ShareCodeValidationException() {
		super();
	}

	/**
	 * Create a {@link ShareCodeValidationException}.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param enableSuppression
	 *            the enableSuppression
	 * @param writableStackTrace
	 *            the writableStackTrace
	 */
	public ShareCodeValidationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Create a {@link ShareCodeValidationException}.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public ShareCodeValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a {@link ShareCodeValidationException}.
	 * 
	 * @param message
	 *            the message
	 */
	public ShareCodeValidationException(String message) {
		super(message);
	}

	/**
	 * Create a {@link ShareCodeValidationException}.
	 * 
	 * @param cause
	 *            the cause
	 */
	public ShareCodeValidationException(Throwable cause) {
		super(cause);
	}

}
