package com.sirma.itt.seip.security.exception;

import java.io.Serializable;

/**
 * Thrown when the user don't have access to a particular entity.
 *
 * @author Adrian Mitev
 */
public class NoPermissionsException extends SecurityException {

	private static final long serialVersionUID = -339582817574552859L;

	private final Serializable id;

	/**
	 * Instantiates the class.
	 *
	 * @param id
	 *            the identifier of the entity that is being accessed and the user has no permissions for it.
	 */
	public NoPermissionsException(Serializable id) {
		super();
		this.id = id;
	}

	/**
	 * Instantiates the class.
	 *
	 * @param id
	 *            the identifier of the entity that is being accessed and the user has no permissions for it.
	 * @param message
	 *            the error message
	 */
	public NoPermissionsException(Serializable id, String message) {
		super(message);
		this.id = id;
	}

	/**
	 * Instantiates the class.
	 *
	 * @param id
	 *            the identifier of the entity that is being accessed and the user has no permissions for it.
	 * @param causedBy
	 *            the caused by exception
	 */
	public NoPermissionsException(Serializable id, Throwable causedBy) {
		super(causedBy);
		this.id = id;
	}

	/**
	 * Instantiates the class.
	 *
	 * @param id
	 *            the identifier of the entity that is being accessed and the user has no permissions for it.
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public NoPermissionsException(Serializable id, String message, Throwable causedBy) {
		super(message, causedBy);
		this.id = id;
	}

	/**
	 * Gets the identifier of the resource that is being accessed
	 *
	 * @return the id
	 */
	public Serializable getId() {
		return id;
	}
}
