package com.sirma.itt.emf.cls.rest;

import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.emf.cls.entity.CodeList;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.cls.service.CodeListException;
import com.sirma.itt.emf.cls.service.CodeListService;
import com.sirma.itt.emf.cls.web.validation.Validator;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.rest.RestServiceConstants;

/**
 * Provides REST services for editing/updating code lists and code values.
 * 
 * @author Vilizar Tsonev
 */
@Path("/codelists")
@Stateless
public class EditService extends EmfRestService {

	@Inject
	private Validator<CodeList> codeListValidator;

	@Inject
	private Validator<CodeValue> codeValueValidator;

	@Inject
	private CodeListService codeListService;

	@Inject
	private static final Logger LOGGER = Logger.getLogger(EditService.class);

	/**
	 * Updates the data for the given code list.
	 * 
	 * @param codeList
	 *            is the request JSON object of the updated code list
	 * @return response with status code and message in JSON
	 */
	@POST
	@Path("/{codeListID}")
	@Consumes("application/json")
	@Produces(RestServiceConstants.APPLICATION_JSON_UTF_ENCODED)
	public Response updateCodelist(CodeList codeList) {
		String response = "";
		Status responseStatus = Response.Status.OK;
		try {
			Map<String, String> errorsMap = codeListValidator.validate(codeList, true);
			if (errorsMap.isEmpty()) {
				codeListService.saveOrUpdateCodeList(codeList, true);
				response = "Code list successfully updated.";
			} else {
				responseStatus = Response.Status.BAD_REQUEST;
				response = new JSONObject(errorsMap).toString();
			}
		} catch (CodeListException e) {
			responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
			response = e.getMessage();
			LOGGER.error(e.getMessage(), e);
		}

		return buildResponse(responseStatus, buildJSON(response));
	}

	/**
	 * Updates the data for the given code value by creating a new code value version.
	 * 
	 * @param codeValue
	 *            the request JSON object of the updated code value
	 * @return response with status code and message in JSON
	 */
	@POST
	@Path("codevalue/{codeValue}")
	@Consumes("application/json")
	@Produces(RestServiceConstants.APPLICATION_JSON_UTF_ENCODED)
	public Response updateCodeValue(CodeValue codeValue) {
		String response = "";
		Status responseStatus = Response.Status.OK;
		try {
			Map<String, String> errorsMap = codeValueValidator.validate(codeValue, true);
			if (errorsMap.isEmpty()) {
				codeListService.updateCodeValue(codeValue);
				response = "Code value successfully updated.";
			} else {
				responseStatus = Response.Status.BAD_REQUEST;
				response = new JSONObject(errorsMap).toString();
			}
		} catch (CodeListException e) {
			responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
			response = e.getMessage();
			LOGGER.error(e.getMessage(), e);
		}
		return buildResponse(responseStatus, buildJSON(response));
	}

	/**
	 * Creates a JSON object out of a string. If the string is null or a {@link JSONException} is
	 * thrown, the method returns null.
	 * 
	 * @param content
	 *            the string
	 * @return JSON object
	 */
	private JSONObject buildJSON(String content) {
		if (content == null) {
			return null;
		}
		try {
			return new JSONObject(content);
		} catch (JSONException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}
}
