package com.sirma.itt.emf.cls.persister;

/**
 * Code list exception, that is thrown when illegal signs are set as code list data.
 *
 * @author SKostadinov
 */
public class PersisterException extends Exception {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Overrides super class constructor.
	 */
	public PersisterException() {
		super();
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param message
	 *            is the message of the exception
	 */
	public PersisterException(String message) {
		super(message);
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param cause
	 *            is the cause of the exception
	 */
	public PersisterException(Throwable cause) {
		super(cause);
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param message
	 *            is the message of the exception
	 * @param cause
	 *            is the cause of the exception
	 */
	public PersisterException(String message, Throwable cause) {
		super(message, cause);
	}
}