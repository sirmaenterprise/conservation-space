package com.sirma.itt.seip.rest.models;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import com.sirma.itt.seip.rest.Request;

/**
 * {@link QueryParameters} represents a wrapper of the REST query parameters object. Can be used for injecting
 * parameters to rest service.
 *
 * @author BBonev
 */
public class QueryParameters extends Request {

	private static final long serialVersionUID = -8966196818226361520L;

	/**
	 * Instantiates a new query parameters.
	 */
	public QueryParameters() {
		super();
	}

	/**
	 * Instantiates a new query parameters.
	 *
	 * @param request the request
	 */
	public QueryParameters(Map<String, List<String>> request) {
		super(request);
	}

	/**
	 * Instantiates a new query parameters.
	 *
	 * @param info the info
	 */
	public QueryParameters(UriInfo info) {
		super(info.getQueryParameters());
	}
}
