package com.sirmaenterprise.sep.exception.mapper;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirmaenterprise.sep.instance.validator.exceptions.InstanceValidationException;

/**
 * Exception mappper for {@link InstanceValidationException} so the response is 412 and not 500
 *
 * @author tdossev
 */
@Provider
public class InstanceValidationExceptionHandler implements ExceptionMapper<InstanceValidationException>{

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(InstanceValidationException exception) {
		LOGGER.warn(exception.getMessage());
		LOGGER.trace("", exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(Response.Status.PRECONDITION_FAILED, ExceptionMapperUtil
				.errorToJson(new ErrorData(Response.Status.PRECONDITION_FAILED.getStatusCode(), exception.getMessage())));
	}
}
