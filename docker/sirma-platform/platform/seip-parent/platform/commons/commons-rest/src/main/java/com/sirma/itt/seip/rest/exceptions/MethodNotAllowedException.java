package com.sirma.itt.seip.rest.exceptions;

import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * Exception thrown to trigger a 405 HTTP status error.
 * 
 * @author Svetlozar Iliev
 */
public class MethodNotAllowedException extends ResourceException {
	private static final long serialVersionUID = -3121205900710963019L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            error message to set in error data.
	 */
	public MethodNotAllowedException(String message) {
		this(new ErrorData().setMessage(message));
	}

	/**
	 * Constructor.
	 * 
	 * @param data
	 *            Error detail.
	 */
	public MethodNotAllowedException(ErrorData data) {
		this(data, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param data
	 *            Error detail.
	 * @param cause
	 *            Real cause.
	 */
	public MethodNotAllowedException(ErrorData data, Throwable cause) {
		super(Status.METHOD_NOT_ALLOWED, data, cause);
	}

}
