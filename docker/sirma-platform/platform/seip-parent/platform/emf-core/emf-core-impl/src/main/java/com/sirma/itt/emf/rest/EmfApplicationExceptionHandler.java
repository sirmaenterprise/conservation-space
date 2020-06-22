package com.sirma.itt.emf.rest;

import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Exception mapper for {@link EmfApplicationException}. Returns an object containing the exception message.
 *
 * @author Adrian Mitev
 * @author yasko
 */
@Provider
public class EmfApplicationExceptionHandler implements ExceptionMapper<EmfApplicationException> {
	private static final Logger LOG = LoggerFactory.getLogger(EmfApplicationExceptionHandler.class);

	@Override
	public Response toResponse(EmfApplicationException exception) {
		LOG.error("Exception occured in REST service", exception);

		if (exception instanceof RestServiceException) {
			return buildRestServiceExceptionResponse((RestServiceException) exception);
		}

		ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR);
		addDefaultResponseEntity(builder, exception);
		return builder.build();
	}

	private Response buildRestServiceExceptionResponse(RestServiceException exception) {
		ResponseBuilder builder = Response.status(exception.getStatus());
		addDefaultResponseEntity(builder, exception);
		return builder.build();
	}

	private void addDefaultResponseEntity(ResponseBuilder builder, EmfApplicationException exception) {
		JSONObject result = new JSONObject();
		JsonUtil.addToJson(result, "message", exception.getMessage());

		Map<String, String> messagesMap = exception.getMessages();
		if (messagesMap != null && !messagesMap.isEmpty()) {
			JSONObject messages = new JSONObject();
			for (Entry<String, String> entry : messagesMap.entrySet()) {
				JsonUtil.addToJson(messages, entry.getKey(), entry.getValue());
			}
			JsonUtil.addToJson(result, "messages", messages);
		}

		builder.type(MediaType.APPLICATION_JSON).entity(result.toString());
	}
}
