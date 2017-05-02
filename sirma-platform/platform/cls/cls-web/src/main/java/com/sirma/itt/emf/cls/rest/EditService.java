package com.sirma.itt.emf.cls.rest;

import java.util.Map;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
import com.sirma.itt.seip.rest.annotations.security.AdminResource;

/**
 * Provides REST services for editing/updating code lists and code values.
 *
 * @author Vilizar Tsonev
 */
@AdminResource
@Path("/codelists")
@Transactional(TxType.REQUIRED)
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateCodelist(CodeList codeList) {
		String responseMessage = "";
		Status responseStatus = Response.Status.OK;
		try {
			Map<String, String> errorsMap = codeListValidator.validate(codeList, true);
			if (errorsMap.isEmpty()) {
				codeListService.saveOrUpdateCodeList(codeList, true);
				responseMessage = "Code list successfully updated.";
			} else {
				responseStatus = Response.Status.BAD_REQUEST;
				responseMessage = new JSONObject(errorsMap).toString();
			}
		} catch (CodeListException e) {
			responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
			responseMessage = e.getMessage();
			LOGGER.error(e.getMessage(), e);
		}
		JSONObject jsonResponse = buildJsonResponse(responseMessage);
		return Response.status(responseStatus).entity(jsonResponse.toString()).build();
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateCodeValue(CodeValue codeValue) {
		String responseMessage = "";
		Status responseStatus = Response.Status.OK;
		try {
			Map<String, String> errorsMap = codeValueValidator.validate(codeValue, true);
			if (errorsMap.isEmpty()) {
				codeListService.updateCodeValue(codeValue);
				responseMessage = "Code value successfully updated.";
			} else {
				responseStatus = Response.Status.BAD_REQUEST;
				responseMessage = new JSONObject(errorsMap).toString();
			}
		} catch (CodeListException e) {
			responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
			responseMessage = e.getMessage();
			LOGGER.error(e.getMessage(), e);
		}
		JSONObject jsonResponse = buildJsonResponse(responseMessage);
		return Response.status(responseStatus).entity(jsonResponse.toString()).build();
	}

	/**
	 * Creates a JSON object with the given response message out of a string. If the string is null or a
	 * {@link JSONException} is thrown, the method returns null.
	 *
	 * @param responseMessage
	 *            the response message
	 * @return JSON object
	 */
	private JSONObject buildJsonResponse(String responseMessage) {
		if (responseMessage == null) {
			return null;
		}
		try {
			JSONObject responseJson = new JSONObject();
			responseJson.put("message", responseMessage);
			return responseJson;
		} catch (JSONException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}
}
