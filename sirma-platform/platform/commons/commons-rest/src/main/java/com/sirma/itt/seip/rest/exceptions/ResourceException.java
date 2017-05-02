package com.sirma.itt.seip.rest.exceptions;

import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * Generic rest exception. Holding common data.
 * @author yasko
 */
public class ResourceException extends RuntimeException {
	private static final long serialVersionUID = 6497146912256133821L;
	
	/** http status code **/
	public final Status status;
	
	/** error data **/
	public final ErrorData data;

	/**
	 * Constructor.
	 * @param status HTTP status to be set in the response.
	 * @param data Error detail.
	 * @param cause Real cause.
	 */
	public ResourceException(Status status, ErrorData data, Throwable cause) {
		super(cause);
		this.status = status;
		this.data = data;
	}

}
