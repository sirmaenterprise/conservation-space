package com.sirma.itt.seip.domain.rest;

import javax.ws.rs.core.Response.Status;

/**
 * REST service exception representing internal server error (HTTP 500)
 *
 * @author yasko
 */
public class InternalServerErrorException extends RestServiceException {
	private static final long serialVersionUID = -8915860565886410430L;

	/**
	 * Constructor.
	 *
	 * @param message
	 *            Error message
	 */
	public InternalServerErrorException(String message) {
		super(message, Status.INTERNAL_SERVER_ERROR);
	}

}
