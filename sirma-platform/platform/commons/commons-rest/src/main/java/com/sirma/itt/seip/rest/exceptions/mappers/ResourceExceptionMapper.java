package com.sirma.itt.seip.rest.exceptions.mappers;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.rest.exceptions.ResourceException;

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
		LOGGER.error("Resource exception: {}, {}", exception.status, exception.data, exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(exception.status,
															  ExceptionMapperUtil.errorToJson(exception.data));
	}
}
