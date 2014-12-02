package com.sirma.itt.emf.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author BBonev
 */
public class RestUtil {

	/**
	 * Build a response object.
	 * 
	 * @param status
	 *            The response status code.
	 * @param entity
	 *            Entity object.
	 * @return Created response object.
	 */
	public static Response buildResponse(Status status, Object entity) {
		if (status == null) {
			return null;
		}
		if (entity == null) {
			return Response.status(status).build();
		}
		return Response.status(status).entity(entity).build();
	}

	/**
	 * Builds the bad request response.
	 * 
	 * @param entity
	 *            the entity
	 * @return the response
	 */
	public static Response buildBadRequestResponse(Object entity) {
		return buildResponse(Status.BAD_REQUEST, entity);
	}

	/**
	 * Builds the error response.
	 * 
	 * @param entity
	 *            the entity
	 * @return the response
	 */
	public static Response buildErrorResponse(Object entity) {
		return buildResponse(Status.INTERNAL_SERVER_ERROR, entity);
	}
}
