package com.sirma.itt.seip.rest.cache;

import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriInfo;

/**
 * Responsible for checking whether a resource has changed and setting the
 * appropriate cache headers.
 *
 * @author yasko
 */
public interface CacheHandler {

	/**
	 * Called in response filter. This method should add the appropriate cache
	 * headers. Note that this method is called only on
	 * {@link Family#SUCCESSFUL} responses.
	 *
	 * @param info
	 *            {@link UriInfo} for the current request.
	 * @param responseContext
	 *            {@link ContainerResponseContext} for the current request.
	 */
	default void handleResponse(UriInfo info, ContainerResponseContext responseContext) {
		// does nothing by default
	}

	/**
	 * Should check if the requested resource has changed. If the resource
	 * hasn't changed a {@link ResponseBuilder} should be returned indicating
	 * that the resource hasn't changed. This could be achieved by using one of:
	 * <ul>
	 * <li>{@link Request#evaluatePreconditions(java.util.Date)}</li>
	 * <li>{@link Request#evaluatePreconditions(javax.ws.rs.core.EntityTag)}</li>
	 * <li>{@link Request#evaluatePreconditions(java.util.Date, javax.ws.rs.core.EntityTag)}</li>
	 * </ul>
	 *
	 * @param info
	 *            {@link UriInfo} for the current request.
	 * @param request
	 *            Current {@link Request} object.
	 * @return {@link ResponseBuilder} if the resource hasn't changed, or null
	 *         if the resource has change or never been requested before.
	 */
	default ResponseBuilder handleRequest(UriInfo info, Request request) {
		// resource is always fetched
		return null;
	}
}
