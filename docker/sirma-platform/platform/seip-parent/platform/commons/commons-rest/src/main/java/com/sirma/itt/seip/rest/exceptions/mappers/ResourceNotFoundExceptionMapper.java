package com.sirma.itt.seip.rest.exceptions.mappers;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * {@link ExceptionMapper} for {@link ResourceNotFoundException}.
 *
 * @author BBonev
 */
@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(ResourceNotFoundException exception) {
		Status status = exception.getStatus();
		ErrorData data = exception.getData();
		LOGGER.error("{}: {}", status, data);
		LOGGER.trace("Resource exception: {}, {}", status, data, exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(status, ExceptionMapperUtil.errorToJson(data));
	}
}
