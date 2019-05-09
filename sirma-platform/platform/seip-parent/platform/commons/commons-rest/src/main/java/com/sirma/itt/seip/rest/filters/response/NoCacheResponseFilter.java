package com.sirma.itt.seip.rest.filters.response;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;

/**
 * Filter that adds additional response headers for disabling caching in rest services.
 *
 * @author smustafov
 * @author yasko
 */
@Priority(Priorities.HEADER_DECORATOR)
public class NoCacheResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		responseContext.getHeaders().add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
		responseContext.getHeaders().add("Pragma", "no-cache");
		responseContext.getHeaders().add(HttpHeaders.EXPIRES, 0);
	}

}
