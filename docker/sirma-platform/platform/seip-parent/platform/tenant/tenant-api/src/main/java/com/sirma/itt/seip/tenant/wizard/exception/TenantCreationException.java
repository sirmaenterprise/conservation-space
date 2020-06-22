package com.sirma.itt.seip.tenant.wizard.exception;

import javax.ejb.ApplicationException;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * TenantCreationException indicates any error related to tenant creation process
 *
 * @author bbanchev
 */
@ApplicationException(rollback = true)
public class TenantCreationException extends EmfRuntimeException {

	/** serialVersionUID. */
	private static final long serialVersionUID = -6001587887946044044L;

	/**
	 * Instantiates a new tenant creation exception.
	 */
	public TenantCreationException() {
		super();
	}

	/**
	 * Instantiates a new tenant creation exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public TenantCreationException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

	/**
	 * Instantiates a new tenant creation exception.
	 *
	 * @param message
	 *            the message
	 */
	public TenantCreationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new tenant creation exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public TenantCreationException(Throwable causedBy) {
		super(causedBy);
	}

}
