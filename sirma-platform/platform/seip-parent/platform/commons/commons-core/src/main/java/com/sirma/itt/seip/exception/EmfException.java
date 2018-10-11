package com.sirma.itt.seip.exception;

import javax.ejb.ApplicationException;

/**
 * Base checked application exception for the modules if they need to throw checked exceptions.
 *
 * @author BBonev
 */
@ApplicationException(rollback = true)
public class EmfException extends Exception {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8173179131273117779L;

	/**
	 * Instantiates a new emf exception.
	 */
	public EmfException() {
		// default constructor
	}

	/**
	 * Instantiates a new emf exception.
	 *
	 * @param message
	 *            the message
	 */
	public EmfException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new emf exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public EmfException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new emf exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public EmfException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new emf exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param enableSuppression
	 *            the enable suppression
	 * @param writableStackTrace
	 *            the writable stack trace
	 */
	public EmfException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
