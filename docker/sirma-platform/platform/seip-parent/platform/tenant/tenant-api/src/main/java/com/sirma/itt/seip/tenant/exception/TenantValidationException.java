/**
 *
 */
package com.sirma.itt.seip.tenant.exception;

import com.sirma.itt.seip.exception.EmfException;

/**
 * Exception thrown when modifying tenant data and invalid data is passed.
 *
 * @author BBonev
 */
public class TenantValidationException extends EmfException {

	private static final long serialVersionUID = -471619501816763265L;

	/**
	 * Instantiates a new tenant validation exception.
	 */
	public TenantValidationException() {
		// implement me
	}

	/**
	 * Instantiates a new tenant validation exception.
	 *
	 * @param message
	 *            the message
	 */
	public TenantValidationException(String message) {
		super(message);
		// implement me
	}

	/**
	 * Instantiates a new tenant validation exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public TenantValidationException(Throwable causedBy) {
		super(causedBy);
		// implement me
	}

	/**
	 * Instantiates a new tenant validation exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public TenantValidationException(String message, Throwable causedBy) {
		super(message, causedBy);
		// implement me
	}

}
