package com.sirma.itt.seip.rest.exceptions.mappers;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Exception mapper for handling {@link AuthenticationException}s.
 *
 * @author yasko
 */
@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationExceptionMapper.class);

	@Override
	public Response toResponse(AuthenticationException exception) {
		LOGGER.error("Unauthorized access attempted: {}", exception.getMessage());
		LOGGER.trace("Unauthorized access attempted", exception);

		JsonObjectBuilder builder = Json.createObjectBuilder().add("message", "Unauthorized to access this resource");

		ExceptionMapperUtil.appendExceptionMessages(builder, exception);

		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.UNAUTHORIZED, builder.build());
	}

}
