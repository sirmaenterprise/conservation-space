package com.sirma.sep.model.management.exception;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.sep.model.management.DeploymentValidationReport;

import javax.ws.rs.core.Response;

/**
 * Exception thrown to indicate that the validation before update has failed in some way. This exception is mapped to a
 * response, containing the validation report.
 *
 * @author Radoslav Dimitrov
 */
public class UpdateModelFailed extends EmfRuntimeException {

	private final DeploymentValidationReport report;

	public UpdateModelFailed(DeploymentValidationReport report) {
		super();
		this.report = report;
	}

	public DeploymentValidationReport getValidationReport() {
		return report;
	}

	public Response.Status getStatus() {
		return Response.Status.BAD_REQUEST;
	}

}
