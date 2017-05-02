package com.sirma.itt.seip.domain.exceptions;

import java.io.Serializable;
import java.util.Date;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;

/**
 * Exception thrown when the user tries to update data of an object/instance that already has been updated by him or
 * other user and he has a stale data.
 *
 * @author BBonev
 */
public class StaleDataModificationException extends EmfApplicationException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2120777308082997272L;

	/** The modified by. */
	private final Serializable modifiedBy;

	/** The last modified on. */
	private final Date lastModifiedOn;

	/**
	 * Instantiates a new stale data modification exception.
	 */
	public StaleDataModificationException() {
		this("Stale data modification exception", null, null);
	}

	/**
	 * Instantiates a new stale data modification exception.
	 *
	 * @param message
	 *            the message
	 */
	public StaleDataModificationException(String message) {
		this(message, null, null);
	}

	/**
	 * Instantiates a new stale data modification exception.
	 *
	 * @param message
	 *            the message
	 * @param modifiedBy
	 *            the modified by
	 * @param lastModifiedOn
	 *            the last modified on
	 */
	public StaleDataModificationException(String message, Serializable modifiedBy, Date lastModifiedOn) {
		super(message);
		this.modifiedBy = modifiedBy;
		this.lastModifiedOn = lastModifiedOn;
	}

	/**
	 * Returns the user/resource that modified the instance. Note the method could return String or
	 * {@code com.sirma.itt.seip.resources.Resource} or <code>null</code> if unknown.
	 *
	 * @return the modifiedBy
	 */
	public Serializable getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * Returns the last modified date.
	 *
	 * @return the lastModifiedOn
	 */
	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}

}
