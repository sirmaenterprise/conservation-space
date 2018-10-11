package com.sirma.itt.emf.cls.validator.exception.mappers;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.emf.cls.validator.exception.SheetValidatorException;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * Maps {@link SheetValidatorException} to JSON response.
 *
 * @author svetlozar.iliev
 */
@Provider
public class SheetValidatorExceptionMapper implements ExceptionMapper<SheetValidatorException> {

	@Override
	public javax.ws.rs.core.Response toResponse(SheetValidatorException exception) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder().add("message", exception.getMessage());

		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.BAD_REQUEST, responseBuilder.build());
	}
}