package com.sirma.itt.emf.cls.validator.exception.mappers;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * Maps {@link CodeValidatorException} to JSON response.
 *
 * @author svetlozar.iliev
 */
@Provider
public class CodeValidatorExceptionMapper implements ExceptionMapper<CodeValidatorException> {

	@Override
	public javax.ws.rs.core.Response toResponse(CodeValidatorException exception) {
		JsonArrayBuilder messagesBuilder = Json.createArrayBuilder();
		exception.getErrors().forEach(messagesBuilder::add);
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder().add("message", messagesBuilder);

		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.BAD_REQUEST, responseBuilder.build());
	}
}