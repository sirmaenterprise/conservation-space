package com.sirma.itt.emf.authentication.sso.saml;

/**
 * Thrown when the SAML signature validation fails.
 *
 * @author Adrian Mitev
 */
public class SAMLMessageValidationException extends RuntimeException {

	private static final long serialVersionUID = -4848717487035980534L;

	/**
	 * Initializes the exception.
	 *
	 * @param message
	 *            exception message
	 */
	public SAMLMessageValidationException(String message) {
		super(message);
	}

	/**
	 * Initializes the exception.
	 *
	 * @param cause
	 *            {@link Throwable} caused this exception.
	 */
	public SAMLMessageValidationException(Throwable cause) {
		super(cause);
	}

}
