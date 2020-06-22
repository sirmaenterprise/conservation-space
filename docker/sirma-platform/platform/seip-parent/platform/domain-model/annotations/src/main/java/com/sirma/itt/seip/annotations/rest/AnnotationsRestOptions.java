package com.sirma.itt.seip.annotations.rest;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.HEAD;
import static javax.ws.rs.HttpMethod.OPTIONS;
import static javax.ws.rs.HttpMethod.POST;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.rest.annotations.security.PublicResource;

/**
 * Rest definition that contains the options methods supported by {@link AnnotationsRestService} service.
 *
 * @author BBonev
 */
@Singleton
@Path("/annotations")
@Produces(MediaType.APPLICATION_JSON)
public class AnnotationsRestOptions {
	/** The HTTP {@code Access-Control-Allow-Headers} header field name. */
	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	/** The HTTP {@code Access-Control-Allow-Methods} header field name. */
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
	/** The HTTP {@code Access-Control-Allow-Origin} header field name. */
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	public static final String ALLOWED_HEADERS = "X-Requested-With,Content-Type";

	public static final String PUT = "PUT";
	public static final String TRACE = "TRACE";
	public static final String DELETE = "DELETE";

	private static final String ALLOWED_METHODS = GET + "," + PUT + "," + POST + "," + DELETE;

	private static final String WILDCARD = "*";

	/**
	 * Handles OPTIONS request for the operation createAnnotation
	 *
	 * @return Response with the needed headers so the client to invoke the real operation
	 */
	@OPTIONS
	@Path("/create")
	@PublicResource
	@SuppressWarnings("static-method")
	public Response handleCreateAnnotationOptions() {
		return handleOptionsRequest(POST, TRACE, OPTIONS);
	}

	/**
	 * Handles OPTIONS request for create annotations method
	 *
	 * @return options response with supported methods
	 */
	@OPTIONS
	@PublicResource
	@Consumes(MediaType.WILDCARD)
	@SuppressWarnings("static-method")
	public Response handleCreateMultipleAnnotationOptions() {
		return handleOptionsRequest(POST, TRACE, OPTIONS);
	}

	/**
	 * Handles OPTIONS request for the operation updateAnnotation
	 *
	 * @return Response with the needed headers so the client to invoke the real operation
	 */
	@OPTIONS
	@Path("/update/{id}")
	@PublicResource
	@SuppressWarnings("static-method")
	public Response handleUpdateAnnotationOptions() {
		return handleOptionsRequest(POST, TRACE, OPTIONS);
	}

	/**
	 * Handles OPTIONS request for the operation deleteAnnotation
	 *
	 * @return Response with the needed headers so the client to invoke the real operation
	 */
	@OPTIONS
	@Path("/destroy")
	@PublicResource
	@SuppressWarnings("static-method")
	public Response handleDeleteAnnotationOptions() {
		return handleOptionsRequest(POST, TRACE, OPTIONS);
	}

	/**
	 * Handles OPTIONS request for methods that accept an annotation id
	 *
	 * @return Response with the needed headers so the client to invoke the real operation
	 */
	@OPTIONS
	@Path("/{id}")
	@PublicResource
	@SuppressWarnings("static-method")
	public Response handleIdOptions() {
		return handleOptionsRequest(GET, POST, PUT, DELETE, TRACE, OPTIONS);
	}

	/**
	 * Handles OPTIONS request for the operation performSearch
	 *
	 * @return Response with the needed headers so the client to invoke the real operation
	 */
	@OPTIONS
	@Path("/search")
	@PublicResource
	@SuppressWarnings("static-method")
	public Response handleSearchAnnotationOptions() {
		return handleOptionsRequest(GET, OPTIONS, TRACE, HEAD);
	}

	private static ResponseBuilder buildDefaultResponse() {
		return Response
				.status(Status.OK)
					.header(ACCESS_CONTROL_ALLOW_ORIGIN, WILDCARD)
					.header(ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS)
					.header(ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
	}

	private static Response handleOptionsRequest(String... allowedRequests) {
		return buildDefaultResponse().allow(allowedRequests).build();
	}
}
