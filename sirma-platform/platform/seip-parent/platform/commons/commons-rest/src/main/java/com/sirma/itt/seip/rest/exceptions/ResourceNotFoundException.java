package com.sirma.itt.seip.rest.exceptions;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * Generic exception for not found resources.
 *
 * @author A. Kunchev
 */
public class ResourceNotFoundException extends ResourceException {

	private static final long serialVersionUID = -6451750389665732161L;

	/**
	 * Default constructor.
	 *
	 * @param resourceId
	 *            the id of the resource
	 */
	public ResourceNotFoundException(String resourceId) {
		super(NOT_FOUND, new ErrorData("Could not find resource with id: " + resourceId), null);
	}

	/**
	 * Constructor with option for cause.
	 *
	 * @param cause
	 *            the cause led to this exception
	 */
	public ResourceNotFoundException(Throwable cause) {
		super(NOT_FOUND, new ErrorData(cause.getMessage()), cause);
	}

	/**
	 * Constructor with option for message.
	 *
	 * @param data
	 *            {@link ErrorData} containing the error message
	 */
	public ResourceNotFoundException(ErrorData data) {
		super(NOT_FOUND, data, null);
	}

	/**
	 * Constructor with full set of parameters.
	 *
	 * @param status
	 *            the error status
	 * @param data
	 *            {@link ErrorData} containing the error message
	 * @param cause
	 *            the cause for the exception
	 */
	public ResourceNotFoundException(Status status, ErrorData data, Throwable cause) {
		super(status, data, cause);
	}

}
