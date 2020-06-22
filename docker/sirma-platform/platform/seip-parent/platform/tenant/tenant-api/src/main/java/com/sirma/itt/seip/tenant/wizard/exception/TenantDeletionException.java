package com.sirma.itt.seip.tenant.wizard.exception;

import javax.ejb.ApplicationException;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * TenantDeletionException indicates that an error, related to tenant deletion process has occured.
 *
 * @author nvelkov
 */
@ApplicationException(rollback = true)
public class TenantDeletionException extends EmfRuntimeException {

	/** serialVersionUID. */
	private static final long serialVersionUID = -6001587887946044044L;

	/**
	 * Instantiates a new tenant deletion exception.
	 */
	public TenantDeletionException() {
		super();
	}

	/**
	 * Instantiates a new tenant deletion exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public TenantDeletionException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

	/**
	 * Instantiates a new tenant deletion exception.
	 *
	 * @param message
	 *            the message
	 */
	public TenantDeletionException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new tenant deletion exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public TenantDeletionException(Throwable causedBy) {
		super(causedBy);
	}

}