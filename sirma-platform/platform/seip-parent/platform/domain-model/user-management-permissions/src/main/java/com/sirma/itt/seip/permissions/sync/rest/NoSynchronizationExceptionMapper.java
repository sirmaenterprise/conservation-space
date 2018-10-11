package com.sirma.itt.seip.permissions.sync.rest;

import javax.json.Json;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.permissions.sync.NoSynchronizationException;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * Exception mapper for {@link NoSynchronizationException}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/07/2017
 */
@Provider
public class NoSynchronizationExceptionMapper implements ExceptionMapper<NoSynchronizationException> {

	@Override
	public Response toResponse(NoSynchronizationException exception) {
		return ExceptionMapperUtil.buildJsonExceptionResponse(Response.Status.PRECONDITION_FAILED,
				Json.createObjectBuilder().add("message", exception.getMessage()).build());
	}
}
