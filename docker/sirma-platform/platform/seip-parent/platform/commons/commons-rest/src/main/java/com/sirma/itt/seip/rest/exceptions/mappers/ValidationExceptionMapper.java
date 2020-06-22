package com.sirma.itt.seip.rest.exceptions.mappers;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.api.validation.ConstraintType.Type;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;

import com.sirma.itt.seip.rest.models.Error;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.itt.seip.rest.utils.ErrorCode;

/**
 * Exception handler for exception produced by the javax.validation api.
 *
 * @author yasko
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {

	@Override
	public Response toResponse(ResteasyViolationException exception) {
		List<ResteasyConstraintViolation> violations = exception.getViolations();
		ErrorData data = new ErrorData(ErrorCode.VALIDATION, exception.getMessage());

		for (ResteasyConstraintViolation violation : violations) {
			if (violation.getConstraintType() != Type.PARAMETER) {
				continue;
			}

			Error error = new Error();
			error.setType(violation.getConstraintType().name());
			error.setError("validation");
			error.setMessage(
					ValidationExceptionMapper.getParamViolationMessage(violation.getPath(), violation.getMessage()));

			// TODO: we should re-think the error response - at least the errors should be a list
			data.getErrors().put(UUID.randomUUID().toString(), error);
		}

		return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
				.entity(ExceptionMapperUtil.errorToJson(data)).build();
	}

	private static String getParamViolationMessage(String path, String message) {
		String[] split = path.split("\\.");
		return split[split.length - 1] + " " + message;
	}
}
