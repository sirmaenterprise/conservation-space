package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirmaenterprise.sep.eai.spreadsheet.service.action.NoTx;
import com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io.SpreadsheetOperationReportWriter;
import com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io.SpreadsheetReadRequestReader;

/**
 * Rest service for data integration based on local instances in the system.
 * 
 * @author bbanchev
 */
@ApplicationScoped
@Path("/instance/{id}/integration")
@Produces(Versions.V2_JSON)
public class EAIInstanceContentRest {

	@Inject
	@NoTx
	private Actions actions;

	/**
	 * Reads a spreadsheet instance content, validates and returns a list of valid instances + validation report
	 * instance.
	 * 
	 * @param request
	 *            is the read request as produced by {@link SpreadsheetReadRequestReader}
	 * @return instance of {@link SpreadsheetReadReport} as json, serialized by {@link SpreadsheetOperationReportWriter}
	 * @throws EmfRuntimeException
	 *             on any critical error or {@link ResourceNotFoundException} if instance is not found or
	 *             {@link NoPermissionsException} if the user has no permissions
	 */
	@POST
	@Path("/read")
	@Consumes({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
	public SpreadsheetOperationReport preprocessSpreadsheet(SpreadSheetReadRequest request) {
		return (SpreadsheetOperationReport) actions.callAction(request);
	}

	/**
	 * Reads a spreadsheet instance content and imports the requested instances
	 * 
	 * @param request
	 *            is the read request as produced by {@link SpreadsheetReadRequestReader}
	 * @return instance of {@link SpreadsheetReadReport} as json, serialized by {@link SpreadsheetOperationReportWriter}
	 * @throws EmfRuntimeException
	 *             on any critical error or {@link ResourceNotFoundException} if instance is not found or
	 *             {@link NoPermissionsException} if the user has no permissions
	 */
	@POST
	@Path("/import")
	@Consumes({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
	public SpreadsheetOperationReport preprocessSpreadsheet(SpreadsheetDataIntegrataionRequest request) {
		return (SpreadsheetOperationReport) actions.callAction(request);
	}
}
