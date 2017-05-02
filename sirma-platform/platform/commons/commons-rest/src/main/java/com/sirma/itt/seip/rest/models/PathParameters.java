package com.sirma.itt.seip.rest.models;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import com.sirma.itt.seip.rest.Request;

/**
 * {@link PathParameters} represents a wrapper of the REST path parameters object. Can be used for injecting the path
 * parameters to rest service.
 *
 * @author BBonev
 */
public class PathParameters extends Request {

	private static final long serialVersionUID = 3960942271063898371L;

	/**
	 * Instantiates a new path parameters.
	 */
	public PathParameters() {
		super();
	}

	/**
	 * Instantiates a new path parameters.
	 *
	 * @param request the request
	 */
	public PathParameters(Map<String, List<String>> request) {
		super(request);
	}

	/**
	 * Instantiates a new path parameters.
	 *
	 * @param info
	 *            the info
	 */
	public PathParameters(UriInfo info) {
		super(info.getPathParameters());
	}

}
