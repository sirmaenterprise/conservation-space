package com.sirma.itt.emf.rest;

import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.util.JsonUtil;

/**
 * Exception mapper for {@link EmfApplicationException}. Returns an object containing the exception
 * message.
 * 
 * @author Adrian Mitev
 */
@Provider
public class EmfApplicationExceptionHandler implements ExceptionMapper<EmfApplicationException> {

	private static final Logger LOG = LoggerFactory.getLogger(EmfApplicationExceptionHandler.class);

	@Override
	public Response toResponse(EmfApplicationException exception) {
		LOG.error("Exception occured in REST service", exception);

		JSONObject result = new JSONObject();
		JsonUtil.addToJson(result, "message", exception.getMessage());
		// append scoped messages if available
		if (!exception.getMessages().isEmpty()) {
			JSONObject messages = new JSONObject();
			for (Entry<String, String> entry : exception.getMessages().entrySet()) {
				JsonUtil.addToJson(messages, entry.getKey(), entry.getValue());
			}
			JsonUtil.addToJson(result, "messages", messages);
		}

		return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
				.entity(result.toString()).build();
	}
}
