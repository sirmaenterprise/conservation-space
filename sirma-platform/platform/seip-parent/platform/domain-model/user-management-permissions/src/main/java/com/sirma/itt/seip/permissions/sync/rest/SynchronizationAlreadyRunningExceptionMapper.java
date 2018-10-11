package com.sirma.itt.seip.permissions.sync.rest;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.permissions.sync.SynchronizationAlreadyRunningException;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * Exception mapper for {@link SynchronizationAlreadyRunningException}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/07/2017
 */
@Provider
public class SynchronizationAlreadyRunningExceptionMapper
		implements ExceptionMapper<SynchronizationAlreadyRunningException> {
	@Override
	public Response toResponse(SynchronizationAlreadyRunningException exception) {
		JsonObjectBuilder error = Json.createObjectBuilder().add("message", exception.getMessage());

		return ExceptionMapperUtil.buildJsonExceptionResponse(Response.Status.PRECONDITION_FAILED, error.build());
	}
}
