package com.sirma.itt.seip.rest.exceptions;

import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * Exception thrown to trigger a 400 HTTP status.
 *
 * @author yasko
 */
public class BadRequestException extends ResourceException {
	private static final long serialVersionUID = -3272705955710963019L;

	/**
	 * Constructor.
	 *
	 * @param message
	 *            error message to set in error data
	 */
	public BadRequestException(String message) {
		this(new ErrorData().setMessage(message));
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 *            error message to set in error data
	 * @param cause
	 *            Real cause
	 */
	public BadRequestException(String message, Throwable cause) {
		this(new ErrorData().setMessage(message), cause);
	}

	/**
	 * Constructor.
	 *
	 * @param data
	 *            Error detail
	 */
	public BadRequestException(ErrorData data) {
		this(data, null);
	}

	/**
	 * Constructor.
	 *
	 * @param data
	 *            Error detail
	 * @param cause
	 *            Real cause
	 */
	public BadRequestException(ErrorData data, Throwable cause) {
		super(Status.BAD_REQUEST, data, cause);
	}

}
