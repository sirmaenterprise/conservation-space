package com.sirma.itt.emf.cls.rest;

import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
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
 * Provides REST services for creating code lists and code values.
 * 
 * @author Vilizar Tsonev
 */
@Path("/codelists")
@Stateless
public class CreateService extends EmfRestService {
	@Inject
	private CodeListService codeListService;

	@Inject
	private Validator<CodeList> codeListValidator;

	@Inject
	private Validator<CodeValue> codeValueValidator;
	@Inject
	private static final Logger LOGGER = Logger.getLogger(CreateService.class);

	/**
	 * Creates a new code list.
	 * 
	 * @param codeList
	 *            is the request JSON object of the new code list
	 * @return response with status code and message in JSON
	 */
	@PUT
	@Consumes("application/json")
	@Produces(RestServiceConstants.APPLICATION_JSON_UTF_ENCODED)
	public Response createCodelist(CodeList codeList) {
		String response = "";
		Status responseStatus = Response.Status.OK;
		try {
			Map<String, String> errorsMap = codeListValidator.validate(codeList, false);
			if (errorsMap.isEmpty()) {
				codeListService.saveOrUpdateCodeList(codeList, false);
				response = "Code list successfully created.";
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
	 * Creates a new code value for the given code list.
	 * 
	 * @param codeValue
	 *            is the request JSON object of the new code value
	 * @return response with status code and message in JSON
	 */
	@PUT
	@Path("/{codeListID}/codevalues/")
	@Consumes("application/json")
	@Produces(RestServiceConstants.APPLICATION_JSON_UTF_ENCODED)
	public Response createCodeValue(CodeValue codeValue) {
		String response = "";
		Status responseStatus = Response.Status.OK;
		try {
			Map<String, String> errorsMap = codeValueValidator.validate(codeValue, false);
			if (errorsMap.isEmpty()) {
				codeListService.saveCodeValue(codeValue);
				response = "Code value successfully created.";
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
