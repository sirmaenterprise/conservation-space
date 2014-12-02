package com.sirma.itt.emf.web.rest;

import java.math.BigInteger;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * REST Service for retrieving specific codelists.
 *
 * @author yasko
 */
@ApplicationScoped
@Path("/codelist")
@Produces(MediaType.APPLICATION_JSON)
public class CodelistRestService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CodelistRestService.class);
	@Inject
	private CodelistService codelistService;

	/**
	 * Retrieve codelist values by specified codelist number and optional language for the
	 * description.
	 *
	 * @param codelist
	 *            Codelist number.
	 * @param language
	 *            Optional language. Defaults to 'en' if not specified.
	 * @return JSON object as string containing an array of codelist value code and description.
	 */
	@GET
	@Path("/{codelist}")
	public String retrieveValues(@PathParam("codelist") Integer codelist,
			@QueryParam("lang") String language) {

		if (codelist == null) {
			return null;
		}

		JSONArray result = new JSONArray();
		String currentLanguage = null;
		try {

			JSONObject value = null;
			currentLanguage = language;
			if (StringUtils.isBlank(language)) {
				currentLanguage = SecurityContextManager.getSystemLanguage();
			}

			Map<String, CodeValue> codeValues = codelistService.getCodeValues(codelist);
			for (Map.Entry<String, CodeValue> entry : codeValues.entrySet()) {
				value = new JSONObject();
				value.put("value", entry.getKey());
				value.put("label", entry.getValue().getProperties().get(currentLanguage));
				result.put(value);
			}

		} catch (JSONException e) {
			LOGGER.error("Codelist " + codelist + "@" + currentLanguage + "retrieval failed!", e);
		}
		return result.toString();
	}
	
	/**
	 * Retrieves a value of a code from a specific codelist.
	 * 
	 * @param codelist
	 *            Codelist number.
	 * @param code
	 *            Code from the codelist.
	 * @param language
	 *            Optional language of the value (defaults to the system
	 *            default)
	 * @return The value for a code from a codelist in the specified language,
	 *         or null if nothing matches the procided criteria.
	 */
	@GET
	@Path("/{codelist}/values/{code}")
	public String getCodeValue(@PathParam("codelist") Integer codelist, @PathParam("code") String code, @QueryParam("lang") String language) {
		String languageFilter = language;
		
		if (StringUtils.isBlank(languageFilter)) {
			languageFilter = SecurityContextManager.getSystemLanguage();
		}
		
		CodeValue value = codelistService.getCodeValue(codelist, code);
		if (value != null) {
			return value.getProperties().get(languageFilter).toString();
		}
		return null;
	}

	/**
	 * Retrieve all codelists.
	 *
	 * @return JSON object as string containing all codelists number and description
	 */
	@GET
	@Path("/codelists")
	public String getAllCodelists() {
		JSONArray result = new JSONArray();
		Map<BigInteger, String> codelists = codelistService.getAllCodelists();

		try {
			JSONObject value = null;

			for (Map.Entry<BigInteger, String> entry : codelists.entrySet()) {
				value = new JSONObject();
				value.put("value", entry.getKey());
				value.put("label", entry.getValue());
				result.put(value);
			}

		} catch (JSONException e) {
			LOGGER.error("All Codelist retrieval failed!", e);
		}
		return result.toString();
	}
}
