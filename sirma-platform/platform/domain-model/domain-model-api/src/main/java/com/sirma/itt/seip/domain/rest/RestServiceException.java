package com.sirma.itt.seip.domain.rest;

import javax.ws.rs.core.Response.Status;

/**
 * Base exception class representing an error occured in a rest service.
 *
 * @author yasko
 */
public class RestServiceException extends EmfApplicationException {
	private static final long serialVersionUID = 301006047582976750L;

	private final Status status;
	private final Object entity;

	/**
	 * Constructor.
	 *
	 * @param message
	 *            Response message.
	 * @param status
	 *            HTTP status code.
	 */
	public RestServiceException(String message, Status status) {
		super(message);
		this.status = status;
		entity = null;
	}

	/**
	 * Constructor
	 *
	 * @param message
	 *            Response message.
	 * @param status
	 *            HTTP status code.
	 * @param entity
	 *            entity object
	 */
	public RestServiceException(String message, Status status, Object entity) {
		super(message);
		this.status = status;
		this.entity = entity;
	}

	/**
	 * Get status
	 *
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Get entity
	 *
	 * @return the entity
	 */
	public Object getEntity() {
		return entity;
	}

}
