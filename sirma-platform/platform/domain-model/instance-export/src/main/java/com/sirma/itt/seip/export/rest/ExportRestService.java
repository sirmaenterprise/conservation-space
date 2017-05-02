package com.sirma.itt.seip.export.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Used for exporting web page to PDF/Word. The URL of the page that should be exported is passed in the action request.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@ApplicationScoped
@Consumes(Versions.V2_JSON)
public class ExportRestService {

	@Inject
	private Actions actions;

	/**
	 * Executes export to PDF action. This service returns download link for the generated file. The file is saved as
	 * content for the instance.
	 *
	 * @param request
	 *            {@link ExportPDFRequest} containing all the information to execute the action correctly
	 * @return download link for the generated PDF
	 */
	@POST
	@Path("/{id}/actions/export-pdf")
	public String exportToPDF(ExportPDFRequest request) {
		return (String) actions.callAction(request);
	}

	/**
	 * Executes export to word action. This service returns download link for the generated file. The file is saved as
	 * content for the instance.
	 *
	 * @param request
	 *            {@link ExportWordRequest} containing all the information to execute the action correctly
	 * @return download link for the generated word document
	 */
	@POST
	@Path("/{id}/actions/export-word")
	public String exportToWord(ExportWordRequest request) {
		return (String) actions.callAction(request);
	}

	/**
	 * Executes export to xlsx action.
	 *
	 * @param request
	 *            {@link ExportListDataXlsxRequest} containing all the information to execute the action correctly
	 * @return download link for the generated xlsx document
	 */
	@Path("/{id}/actions/export-xlsx")
	@POST
	public String exportToXlsx(ExportListDataXlsxRequest request) {
		return (String) actions.callAction(request);
	}
}
