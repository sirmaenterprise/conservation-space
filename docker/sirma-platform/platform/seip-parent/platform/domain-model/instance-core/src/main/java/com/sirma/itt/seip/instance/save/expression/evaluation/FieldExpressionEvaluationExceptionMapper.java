package com.sirma.itt.seip.instance.save.expression.evaluation;

import com.sirma.itt.seip.rest.exceptions.mappers.ExceptionMapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.lang.invoke.MethodHandles;

/**
 * Exception mapper for {@link FieldExpressionEvaluationException}.
 */
@Provider
public class FieldExpressionEvaluationExceptionMapper implements ExceptionMapper<FieldExpressionEvaluationException> {

	private static final String LABEL_ID = "labelId";
	private static final String MESSAGE = "message";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(FieldExpressionEvaluationException exception) {
		LOGGER.error("Error while trying to evaluate emf expression set in the definition", exception);
		JsonObjectBuilder error = Json.createObjectBuilder()
				.add(MESSAGE, exception.getMessage())
				.add(LABEL_ID, exception.getLabelId());
		return ExceptionMapperUtil.buildJsonExceptionResponse(Response.Status.INTERNAL_SERVER_ERROR, error.build());
	}

}

