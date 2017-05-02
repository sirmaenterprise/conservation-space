package com.sirma.itt.seip.domain.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The SearchException indicates problem during search.
 */
public class SearchException extends EmfRuntimeException {

	/**
	 * Instantiates a new search exception.
	 */
	public SearchException() {
		super();
	}

	/**
	 * Instantiates a new search exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new search exception.
	 *
	 * @param message
	 *            the message
	 */
	public SearchException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new search exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public SearchException(Throwable cause) {
		super(cause);
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2948037477452396446L;

}
