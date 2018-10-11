package com.sirma.itt.seip.rest.exceptions.mappers;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.exception.NoPermissionsException;

/**
 * Exception mapper for handling {@link NoPermissionsException}.
 *
 * @author Adrian Mitev
 */
@Provider
public class NoPermissionsExceptionMapper implements ExceptionMapper<NoPermissionsException> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NoPermissionsExceptionMapper.class);

	@Override
	public Response toResponse(NoPermissionsException exception) {
		LOGGER.error("Attempted access to forbidden object: {}, {}", exception.getId(), exception.getMessage());
		LOGGER.trace("Attempted access to forbidden object: {}", exception.getId(), exception);

		JsonObjectBuilder builder = Json.createObjectBuilder().add("message", "No access to this object");

		ExceptionMapperUtil.appendExceptionMessages(builder, exception);

		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.FORBIDDEN, builder.build());
	}

}
