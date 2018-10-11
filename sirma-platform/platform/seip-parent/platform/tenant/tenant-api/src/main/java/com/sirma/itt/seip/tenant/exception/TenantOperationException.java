package com.sirma.itt.seip.tenant.exception;

import javax.ejb.ApplicationException;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown when fail to execute a particular tenant management operation. For example when trying to notify
 * for tenant activation.
 *
 * @author BBonev
 */
@ApplicationException(rollback = true)
public class TenantOperationException extends EmfRuntimeException {
	private static final long serialVersionUID = -4441978592379170514L;

	/**
	 * Instantiates a new tenant operation not allowed.
	 */
	public TenantOperationException() {
		super();
	}

	/**
	 * Instantiates a new tenant operation not allowed.
	 *
	 * @param message
	 *            the message
	 */
	public TenantOperationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new tenant operation not allowed.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public TenantOperationException(Throwable causedBy) {
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
	public TenantOperationException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
