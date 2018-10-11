package com.sirmaenterprise.sep.exception.mapper;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;
import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * Exception mappper for {@link InstanceNotFoundException}s so the response is 404 and not 500
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/05/2017
 */
@Provider
public class InstanceNotFoundExceptionHandler implements ExceptionMapper<InstanceNotFoundException> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(InstanceNotFoundException exception) {
		LOGGER.warn(exception.getMessage());
		LOGGER.trace("", exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(Response.Status.NOT_FOUND,
				ExceptionMapperUtil.errorToJson(new ErrorData(Response.Status.NOT_FOUND.getStatusCode(), exception
						.getMessage())));
	}
}
