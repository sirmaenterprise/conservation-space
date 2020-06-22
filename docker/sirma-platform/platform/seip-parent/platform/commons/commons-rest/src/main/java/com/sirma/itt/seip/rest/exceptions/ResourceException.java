package com.sirma.itt.seip.rest.exceptions;

import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * Generic rest exception. Holding common data.
 *
 * @author yasko
 */
public class ResourceException extends RuntimeException {

	private static final long serialVersionUID = 6497146912256133821L;

	private final Status status;

	private final ErrorData data;

	/**
	 * Instantiates a new resource exception.
	 *
	 * @param status
	 *            HTTP status to be set in the response
	 * @param message
	 *            the error message to be set in the response
	 */
	public ResourceException(Status status, String message) {
		this(status, message, null);
	}

	/**
	 * Instantiates a new resource exception.
	 *
	 * @param status
	 *            HTTP status to be set in the response
	 * @param message
	 *            the error message to be set in the response
	 * @param cause
	 *            the real cause of the error
	 */
	public ResourceException(Status status, String message, Throwable cause) {
		this(status, new ErrorData(message), cause);
	}

	/**
	 * Constructor.
	 *
	 * @param status
	 *            HTTP status to be set in the response
	 * @param data
	 *            Error detail
	 * @param cause
	 *            Real cause
	 */
	public ResourceException(Status status, ErrorData data, Throwable cause) {
		super(cause);
		this.status = status;
		this.data = data;
	}

	public Status getStatus() {
		return status;
	}

	public ErrorData getData() {
		return data;
	}

}
