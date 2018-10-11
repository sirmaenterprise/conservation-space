package com.sirma.sep.export;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;
import com.sirma.sep.export.ContentExportException;

/**
 * {@link ContentExportExceptionMapper} is used to map {@link ContentExportException} to jax-rs {@link Response}
 * 
 * @author bbanchev
 */
@Provider
public class ContentExportExceptionMapper implements ExceptionMapper<ContentExportException> {

	@Override
	public Response toResponse(ContentExportException exception) {
		String message = exception.getMessage();
		if (StringUtils.isBlank(message)) {
			message = "Fail to process content export request!";
		}
		JsonObjectBuilder builder = Json.createObjectBuilder().add("message", message);
		ExceptionMapperUtil.appendExceptionMessages(builder, exception);
		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.INTERNAL_SERVER_ERROR, builder.build());
	}
}
