package com.sirma.itt.seip.instance.lock.exception.mappers;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.instance.lock.exception.UnlockException;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * {@link ExceptionMapper} for {@link UnlockException}.
 *
 * @author A. Kunchev
 */
@Provider
public class UnlockExceptionMapper implements ExceptionMapper<UnlockException> {

	@Override
	public Response toResponse(UnlockException exception) {
		String message = exception.getMessage();
		if (StringUtils.isBlank(message)) {
			message = "You cannot unlock this resource, it is locked by " + exception.getLockInfo().getLockedBy();
		}

		JsonObjectBuilder builder = Json.createObjectBuilder().add("message", message);
		ExceptionMapperUtil.appendExceptionMessages(builder, exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.CONFLICT, builder.build());
	}

}
