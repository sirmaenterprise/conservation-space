package com.sirma.itt.emf.rest;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Exception mapper for all kind of exceptions. Returns an object containing the default EMF error message.
 *
 * @author Adrian Mitev
 */
// @Provider
public class RestExceptionHandler implements ExceptionMapper<Exception> {

	private static final Logger LOGGER = Logger.getLogger(RestExceptionHandler.class);

	@Inject
	private LabelProvider labelProvider;

	@Override
	public Response toResponse(Exception exception) {
		if (exception == null) {
			return Response.serverError().build();
		}
		LOGGER.trace("Building error responce for exception: " + exception.getMessage(), exception);

		JSONObject object = new JSONObject();

		JsonUtil.addToJson(object, "message", labelProvider.getValue("application.error"));

		return Response.serverError().type(MediaType.APPLICATION_JSON).entity(object.toString()).build();
	}

}
