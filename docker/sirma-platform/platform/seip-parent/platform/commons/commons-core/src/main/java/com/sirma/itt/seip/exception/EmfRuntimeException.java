package com.sirma.itt.seip.exception;

/**
 * Base exception for EMF runtime exceptions.
 *
 * @author BBonev
 */
public class EmfRuntimeException extends RuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4426965311170782529L;

	/**
	 * Instantiates a new cmf runtime exception.
	 */
	public EmfRuntimeException() {
		// default constructor
	}

	/**
	 * Instantiates a new cmf runtime exception.
	 *
	 * @param message
	 *            the exception message
	 */
	public EmfRuntimeException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new cmf runtime exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public EmfRuntimeException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * Instantiates a new cmf runtime exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public EmfRuntimeException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
