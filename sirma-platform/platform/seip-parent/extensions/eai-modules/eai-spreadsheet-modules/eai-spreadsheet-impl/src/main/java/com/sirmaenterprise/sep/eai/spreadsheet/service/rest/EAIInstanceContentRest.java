package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.instance.actions.save.SaveRestService;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
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
	private SecurityContextManager securityContextManager;
	@Inject
	private Actions actions;

	/**
	 * Reads a spreadsheet instance content, validates and returns a list of valid instances + validation report
	 * instance.
	 *
	 * @param request
	 *            is the read request as produced by {@link SpreadsheetReadRequestReader}
	 * @return instance of {@link SpreadsheetOperationReport} as json, serialized by {@link SpreadsheetOperationReportWriter}
	 * @throws EmfRuntimeException
	 *             on any critical error or {@link ResourceNotFoundException} if instance is not found or
	 *             {@link NoPermissionsException} if the user has no permissions
	 */
	@POST
	@Path("/read")
	@Consumes({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
	public SpreadsheetOperationReport preprocessSpreadsheet(SpreadSheetReadRequest request) {
		return (SpreadsheetOperationReport) actions.callSlowAction(request);
	}

	/**
	 * Reads a spreadsheet instance content and imports the requested instances.
	 *
	 * @param request
	 *            is the read request as produced by {@link SpreadsheetReadRequestReader}
	 * @return instance of {@link SpreadsheetOperationReport} as json, serialized by {@link SpreadsheetOperationReportWriter}
	 * @throws EmfRuntimeException
	 *             on any critical error or {@link ResourceNotFoundException} if instance is not found or
	 *             {@link NoPermissionsException} if the user has no permissions
	 */
	// no transaction here as the operation is slow and will timeout. The transactions are managed internally
	@POST
	@Path("/import")
	@Consumes({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
	public SpreadsheetOperationReport preprocessSpreadsheet(SpreadsheetDataIntegrataionRequest request) {
		return (SpreadsheetOperationReport) actions.callSlowAction(request);
	}

	/**
	 * Executes (by system) operations that creates or uploads an instance. Rest endpoint relays on existing service for
	 * {@link SaveRequest} processing. See {@link SaveRestService#save(SaveRequest)} for more details.
	 * <p>
	 * Typical request is
	 *
	 * <pre>
	 * <code>{
	 *   "userOperation" : "userOperationId",
	 *   "targetInstance" : {
	 *      "properties" : { ... }
	 *   }
	 * }</code>
	 * </pre>
	 *
	 * @deprecated use {@link #save(SaveRequest)} which is with different end point -> @Path("/actions/save")
	 *             [deprecated v2.20.0]
	 * @param request {@link SaveRequest} containing the information for the operation, like - user operation, context
	 *        path, placeholder, etc.
	 * @return the request result executed as system user. Throws exception of operations fails or if user has no write
	 *         access to instance
	 */
	@POST
	@Path("/actions/createOrUpdate")
	@Deprecated
	public Object saveOld(SaveRequest request) {
		return save(request);
	}

	/**
	 * Executes (by system) operations that creates or uploads an instance. Rest endpoint relays on existing service for
	 * {@link SaveRequest} processing. See {@link SaveRestService#save(SaveRequest)} for more details.
	 * <p>
	 * Typical request is
	 *
	 * <pre>
	 * <code>{
	 *   "userOperation" : "userOperationId",
	 *   "targetInstance" : {
	 *      "properties" : { ... }
	 *   }
	 * }</code>
	 * </pre>
	 *
	 * @param request {@link SaveRequest} containing the information for the operation, like - user operation, context
	 *        path, placeholder, etc.
	 * @return the request result executed as system user. Throws exception of operations fails or if user has no write
	 *         access to instance
	 */
	// TODO update the end point(the format is wrong and now it could be just save)
	@POST
	@Path("/actions/save")
	public Object save(SaveRequest request) {
		try {
			// probably should be added security check
			return securityContextManager.executeAsSystem().callable(() -> actions.callAction(request));
		} catch (Exception e) {
			throw new EAIRuntimeException("Failed during create or update operation!", e);
		}
	}
}