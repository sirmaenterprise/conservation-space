/**
 * Copyright (c) 2010 13.02.2010 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.exceptions;

/**
 * Thrown if trying to modify an object which is marked as unmodifiable.
 * 
 * @author Hristo Iliev
 */
public class UnmodifiableException extends RuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4566334705425054922L;

	/**
	 * Default constructor.
	 */
	public UnmodifiableException() {
		// Default constructor.
	}

	/**
	 * Constructor with message.
	 * 
	 * @param message
	 *            {@link String}, the message
	 */
	public UnmodifiableException(final String message) {
		super(message);
	}

	/**
	 * Constructor with cause.
	 * 
	 * @param cause
	 *            {@link Throwable}, the cause
	 */
	public UnmodifiableException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with message and cause.
	 * 
	 * @param message
	 *            {@link String}, the message
	 * @param cause
	 *            {@link Throwable}, the cause
	 */
	public UnmodifiableException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
