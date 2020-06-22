package com.sirma.itt.seip.domain.rest;

import javax.ws.rs.core.Response.Status;

/**
 * Rest service exception that will be mapped to a bad request response.
 *
 * @deprecated use the exception from commons-rest
 * @author yasko
 * @deprecated Use the com.sirma.itt.seip.rest.exceptions.BadRequestException
 */
@Deprecated
public class BadRequestException extends RestServiceException {
	private static final long serialVersionUID = 6868501223173113849L;

	/**
	 * Constructor.
	 *
	 * @param message
	 *            Exception message.
	 */
	public BadRequestException(String message) {
		super(message, Status.BAD_REQUEST);
	}

}
