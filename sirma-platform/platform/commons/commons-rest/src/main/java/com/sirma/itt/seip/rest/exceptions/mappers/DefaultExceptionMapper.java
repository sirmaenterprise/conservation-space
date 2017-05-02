package com.sirma.itt.seip.rest.exceptions.mappers;

import java.lang.invoke.MethodHandles;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception mapper for handling all other exceptions that are not mapped by other specific mappers.
 *
 * @author BBonev
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(Throwable exception) {
		LOGGER.error("Generic exception", exception);

		JsonObjectBuilder error = Json.createObjectBuilder().add("message",
				"Unexpected exception occur in the system. Please contact your system administrator.");

		ExceptionMapperUtil.appendExceptionMessages(error, exception);

		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.INTERNAL_SERVER_ERROR, error.build());
	}
}
