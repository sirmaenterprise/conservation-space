/**
 *
 */
package com.sirma.itt.seip.tenant.exception;

import javax.enterprise.context.ContextNotActiveException;

/**
 * Exception that is thrown when concrete tenant information is requested but there is no active tenant context.
 *
 * @author BBonev
 */
public class TenantNotActiveException extends ContextNotActiveException {
	private static final long serialVersionUID = 6370078896081153979L;

	/**
	 * Instantiates a new tenant not active exception.
	 */
	public TenantNotActiveException() {
		super();
	}

	/**
	 * Instantiates a new tenant not active exception.
	 *
	 * @param message
	 *            the message
	 */
	public TenantNotActiveException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new tenant not active exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public TenantNotActiveException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * Instantiates a new tenant not active exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public TenantNotActiveException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
