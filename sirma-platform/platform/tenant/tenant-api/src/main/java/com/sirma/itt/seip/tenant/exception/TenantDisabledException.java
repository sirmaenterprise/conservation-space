/**
 *
 */
package com.sirma.itt.seip.tenant.exception;

import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Exception thrown to indicate that operation related to a tenant could not be executed because the tenant is disabled.
 *
 * @author BBonev
 */
public class TenantDisabledException extends AuthenticationException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 815568555877096311L;

	/**
	 * Instantiates a new tenant disabled exception.
	 *
	 * @param failedIdentity
	 *            the failed identity
	 * @param message
	 *            the message
	 */
	public TenantDisabledException(String failedIdentity, String message) {
		super(failedIdentity, message);
	}

	/**
	 * Instantiates a new tenant disabled exception.
	 *
	 * @param failedIdentity
	 *            the failed identity
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public TenantDisabledException(String failedIdentity, String message, Throwable causedBy) {
		super(failedIdentity, message, causedBy);
	}

}
