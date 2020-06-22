package com.sirma.sep.model.management.exception.mappers;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.sep.model.management.exception.ChangeSetCollisionException;

/**
 * {@link ExceptionMapper} for {@link ChangeSetCollisionException}.
 *
 * @author Radoslav Dimitrov
 */
@Provider
public class ChangeSetCollisionExceptionMapper implements ExceptionMapper<ChangeSetCollisionException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(ChangeSetCollisionException exception) {
		Response.Status status = exception.getStatus();
		ErrorData data = exception.getData();
		LOGGER.error("{}: {}", status, data);
		LOGGER.trace("Detected ChangeSetCollisionException: {}, {}", status, data, exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(status, ExceptionMapperUtil.errorToJson(data));
	}
}
