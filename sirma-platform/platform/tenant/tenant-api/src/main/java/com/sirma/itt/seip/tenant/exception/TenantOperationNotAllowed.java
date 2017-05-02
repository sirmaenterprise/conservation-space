package com.sirma.itt.seip.tenant.exception;

import javax.ejb.ApplicationException;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown when forbidden operation is performed. For example when trying to deactivate multitenant mode when
 * once activated.
 */
@ApplicationException(rollback = true)
public class TenantOperationNotAllowed extends EmfRuntimeException {
	private static final long serialVersionUID = -4441978592379170514L;

	/**
	 * Instantiates a new tenant operation not allowed.
	 */
	public TenantOperationNotAllowed() {
		super();
	}

	/**
	 * Instantiates a new tenant operation not allowed.
	 *
	 * @param message
	 *            the message
	 */
	public TenantOperationNotAllowed(String message) {
		super(message);
	}

	/**
	 * Instantiates a new tenant operation not allowed.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public TenantOperationNotAllowed(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * Instantiates a new tenant operation not allowed.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public TenantOperationNotAllowed(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
