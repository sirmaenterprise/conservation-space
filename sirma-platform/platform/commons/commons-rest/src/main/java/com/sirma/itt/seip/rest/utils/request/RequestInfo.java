package com.sirma.itt.seip.rest.utils.request;

import javax.ws.rs.BeanParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 * Injects common request info (using {@link Context} annotation) and provides
 * utility methods. Intended to be used as {@link BeanParam}.
 * 
 * @author yasko
 */
public class RequestInfo {

	@Context
	private Request request;

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpHeaders headers;

	public String getMethod() {
		return request.getMethod();
	}

	public Request getRequest() {
		return request;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

}
