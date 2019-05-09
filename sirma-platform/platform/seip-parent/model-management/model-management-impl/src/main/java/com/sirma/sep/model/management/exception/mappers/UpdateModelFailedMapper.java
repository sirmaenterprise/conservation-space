package com.sirma.sep.model.management.exception.mappers;

import com.sirma.sep.model.management.DeploymentValidationReport;
import com.sirma.sep.model.management.exception.UpdateModelFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.lang.invoke.MethodHandles;

/**
 * {@link ExceptionMapper} for {@link UpdateModelFailed}.
 *
 * @author Radoslav Dimitrov
 */
@Provider
public class UpdateModelFailedMapper implements ExceptionMapper<UpdateModelFailed> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(UpdateModelFailed exception) {
		Response.Status status = exception.getStatus();
		DeploymentValidationReport report = exception.getValidationReport();
		LOGGER.error("{}: {}", status, report.getFailedEntries());
		LOGGER.trace("Detected UpdateModelFailed: {}, {}", status, report.getFailedEntries(), exception);
		return Response.status(status).entity(report).type(MediaType.APPLICATION_JSON).build();
	}
}
