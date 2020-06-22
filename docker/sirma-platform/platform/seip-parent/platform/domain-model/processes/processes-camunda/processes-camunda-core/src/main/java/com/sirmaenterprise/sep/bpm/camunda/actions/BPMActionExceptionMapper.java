package com.sirmaenterprise.sep.bpm.camunda.actions;

import java.lang.invoke.MethodHandles;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;

/**
 * {@link ExceptionMapper} for {@link BPMActionRuntimeException}.
 * 
 * @author Hristo Lungov
 */
@Provider
public class BPMActionExceptionMapper implements ExceptionMapper<BPMActionRuntimeException> {

	protected static final String LABEL_ID = "labelId";
	protected static final String MESSAGE = "message";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(BPMActionRuntimeException exception) {
		LOGGER.error("BPM action runtime exception", exception);
		JsonObjectBuilder error = Json.createObjectBuilder().add(MESSAGE, exception.getMessage()).add(LABEL_ID,
				exception.getLabelId());
		return ExceptionMapperUtil.buildJsonExceptionResponse(Status.INTERNAL_SERVER_ERROR, error.build());
	}

}
