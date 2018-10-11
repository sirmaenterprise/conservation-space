package com.sirma.itt.seip.idp.exception;

import com.sirma.itt.seip.exception.EmfException;

/**
 * The IDPClientException represent general IDP exceptions
 *
 * @author bbanchev
 */
public class IDPClientException extends EmfException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1506184915217975182L;

	/**
	 * Instantiates a new IDP client exception.
	 */
	public IDPClientException() {
		super();
	}

	/**
	 * Instantiates a new IDP client exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public IDPClientException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new IDP client exception.
	 *
	 * @param message
	 *            the message
	 */
	public IDPClientException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new IDP client exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public IDPClientException(Throwable cause) {
		super(cause);
	}

}
