package com.sirma.itt.seip.tenant.wizard.exception;

import javax.ejb.ApplicationException;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * TenantUpdateException indicates any error related to tenant update process
 * 
 * @author kirq4e
 */
@ApplicationException(rollback = true)
public class TenantUpdateException extends EmfRuntimeException {

	/** serialVersionUID. */
	private static final long serialVersionUID = 3337202846800191940L;

	/**
	 * Instantiates a new tenant update exception.
	 */
	public TenantUpdateException() {
		super();
	}

	/**
	 * Instantiates a new tenant update exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public TenantUpdateException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

	/**
	 * Instantiates a new tenant update exception.
	 *
	 * @param message
	 *            the message
	 */
	public TenantUpdateException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new tenant update exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public TenantUpdateException(Throwable causedBy) {
		super(causedBy);
	}

}
