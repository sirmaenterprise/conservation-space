package com.sirma.itt.seip.rest.filters;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;

/**
 * Sets the Content-Length response header for {@link File} resources.
 *
 * @author yasko
 */
public class ContentLengthReponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		Object entity = responseContext.getEntity();
		if (entity instanceof File) {
			responseContext.getHeaders().addFirst(HttpHeaders.CONTENT_LENGTH, ((File) entity).length());
		}
	}

}
