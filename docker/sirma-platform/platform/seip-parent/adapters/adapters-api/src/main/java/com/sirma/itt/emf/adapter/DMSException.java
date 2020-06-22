/*
 * Copyright (c) 2012 12.09.2012 , Sirma ITT.
 */
package com.sirma.itt.emf.adapter;

/**
 * The Class DMSException for dms errors
 *
 * @author Borislav Banchev
 */
public class DMSException extends Exception {

	/** Comment for serialVersionUID. */
	private static final long serialVersionUID = 1695232046056666123L;

	/**
	 * Instantiates a new dMS exception.
	 */
	public DMSException() {
		super();
	}

	/**
	 * Instantiates a new dMS exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public DMSException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new dMS exception.
	 *
	 * @param message
	 *            the message
	 */
	public DMSException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new dMS exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public DMSException(Throwable cause) {
		super(cause);
	}

}
