package com.sirma.itt.emf.exceptions;

/**
 * Thrown when authentication operations failed to perform or the required data
 * or state is invalid.
 *
 * @author BBonev
 */
public class AuthenticationException extends CmfSecurityException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2825192347049690381L;

	/**
	 * Instantiates a new cMF authentication exception.
	 */
	public AuthenticationException() {
		// nothing to do here
	}

	/**
	 * Instantiates a new cMF authentication exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public AuthenticationException(String arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new cMF authentication exception.
	 *
	 * @param arg0
	 *            the arg0
	 */
	public AuthenticationException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new cMF authentication exception.
	 *
	 * @param arg0
	 *            the arg0
	 * @param arg1
	 *            the arg1
	 */
	public AuthenticationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
