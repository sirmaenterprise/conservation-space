package com.sirma.itt.seip.instance.lock.exception.mappers;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * {@link ExceptionMapper} for {@link LockException}.
 *
 * @author A. Kunchev
 */
@Provider
public class LockExceptionMapper implements ExceptionMapper<LockException> {

	@Override
	public Response toResponse(LockException exception) {
		String message = exception.getMessage();
		if (StringUtils.isBlank(message)) {
			message = "The resource is locked by " + exception.getLockInfo().getLockedBy();
		}

		JsonObjectBuilder builder = Json.createObjectBuilder().add("message", message);
		ExceptionMapperUtil.appendExceptionMessages(builder, exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.CONFLICT, builder.build());
	}

}
