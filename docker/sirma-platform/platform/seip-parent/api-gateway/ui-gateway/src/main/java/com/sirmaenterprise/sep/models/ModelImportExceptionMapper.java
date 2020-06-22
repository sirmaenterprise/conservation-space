package com.sirmaenterprise.sep.models;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * Maps {@link ModelImportException} to json response.
 *
 * @author Adrian Mitev
 */
@Provider
public class ModelImportExceptionMapper implements ExceptionMapper<ModelImportException> {

	@Override
	public javax.ws.rs.core.Response toResponse(ModelImportException exception) {
		JsonArrayBuilder messagesBuilder = Json.createArrayBuilder();
		exception.getMessages().forEach(messagesBuilder::add);

		JsonObjectBuilder responseBuilder = Json.createObjectBuilder().add("messages", messagesBuilder);

		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.BAD_REQUEST, responseBuilder.build());
	}
}