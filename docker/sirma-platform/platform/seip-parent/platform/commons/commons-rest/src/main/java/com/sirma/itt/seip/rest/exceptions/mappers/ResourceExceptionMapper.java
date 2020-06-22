package com.sirma.itt.seip.rest.exceptions.mappers;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * {@link ExceptionMapper} for {@link ResourceException}.
 *
 * @author yasko
 */
@Provider
public class ResourceExceptionMapper implements ExceptionMapper<ResourceException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(ResourceException exception) {
		Status status = exception.getStatus();
		ErrorData data = exception.getData();
		LOGGER.error("Resource exception: {}, {}", status, data, exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(status, ExceptionMapperUtil.errorToJson(data));
	}

}
