package com.sirma.itt.seip.rest.filters;

import java.io.IOException;

import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.annotations.Cache;
import com.sirma.itt.seip.rest.cache.CacheHandler;
import com.sirma.itt.seip.util.CDI;

/**
 * Handles requests and responses to methods annotated with {@link Cache}. Runs
 * at {@link Priorities#HEADER_DECORATOR} priority.
 *
 * The filter checks if the requested resource has changed by calling the
 * provided {@link CacheHandler#handleRequest(UriInfo, Request)} method, if the
 * resource hasn't changed the request is aborted with the returned
 * {@link ResponseBuilder}. Otherwise the request continues.
 *
 * After a successful request the provided
 * {@link CacheHandler#handleResponse(UriInfo, ContainerResponseContext)} is
 * called to add the appropriate cache headers.
 *
 * @author yasko
 */
@Cache
@Provider
public class CacheFilter implements ContainerRequestFilter, ContainerResponseFilter {

	@Context
	private ResourceInfo resource;

	@Context
	private Request request;

	@Context
	private UriInfo info;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		Cache cache = resource.getResourceMethod().getAnnotation(Cache.class);
		CacheHandler validator = CDI.instantiate(cache.value(), CDI.getCachedBeanManager());
		ResponseBuilder builder = validator.handleRequest(info, request);
		if (builder != null) {
			requestContext.abortWith(builder.build());
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		StatusType status = responseContext.getStatusInfo();
		if (status == null || status.getFamily() != Family.SUCCESSFUL) {
			return;
		}

		Cache cache = resource.getResourceMethod().getAnnotation(Cache.class);
		CacheHandler validator = CDI.instantiate(cache.value(), CDI.getCachedBeanManager());
		validator.handleResponse(info, responseContext);
	}

}
