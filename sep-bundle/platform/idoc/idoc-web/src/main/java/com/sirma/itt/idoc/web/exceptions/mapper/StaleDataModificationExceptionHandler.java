package com.sirma.itt.idoc.web.exceptions.mapper;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

import com.sirma.itt.emf.exceptions.StaleDataModificationException;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Exception mapper for {@link StaleDataModificationException}. This exception
 * occurs when a newer version of the document/object has already been persisted
 * and the user is trying to overwrite with an older one.
 * 
 * @author yasko
 * 
 */
@Provider
public class StaleDataModificationExceptionHandler implements
		ExceptionMapper<StaleDataModificationException> {

	@Inject
	private LabelProvider labelProvider;

	@Override
	public Response toResponse(StaleDataModificationException exception) {
		JSONObject result = new JSONObject();
		
		JSONObject messages = new JSONObject();
		JsonUtil.addToJson(messages, "staleDataMessage", labelProvider.getValue("idoc.exception.staleDataModification.message"));
		JsonUtil.addToJson(result, "messages", messages);

		ResponseBuilder responseBuilder = Response.status(Status.CONFLICT);
		responseBuilder.type(MediaType.APPLICATION_JSON);
		responseBuilder.entity(result.toString());

		return responseBuilder.build();
	}

}
