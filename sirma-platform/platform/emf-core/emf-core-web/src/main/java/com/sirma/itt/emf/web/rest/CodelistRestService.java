package com.sirma.itt.emf.web.rest;

import java.io.Serializable;
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

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * REST Service for retrieving specific codelists.
 *
 * @author yasko
 */
@ApplicationScoped
@Path("/codelist")
@Produces(MediaType.APPLICATION_JSON)
public class CodelistRestService extends EmfRestService {
	private static final String LABEL = "label";
	private static final String VALUE = "value";

	@Inject
	private CodelistService codelistService;

	@Inject
	private SystemConfiguration systemConfiguration;

	/**
	 * Retrieve codelist values by specified codelist number and optional language for the description. The codevalues
	 * result can be filtered by providing additional arguments:<br>
	 * <ul>
	 * <li>If filterBy and filterSource arguments are passed, then the result is filtered by property/ies found in
	 * filterSource column in codelists.xls</li>
	 * <li>If customFilters argument is provided, then the filters are executed on codevalues from the given codelist
	 * and the result is retained with the codevalues extracted and (optionally filtered on previous step).</li>
	 * </ul>
	 *
	 * @param codelist
	 *            Codelist number.
	 * @param filterBy
	 *            the keyword to be used for filtering
	 * @param filterSource
	 *            against which description property to apply filter
	 * @param inclusive
	 *            if the filter should be inclusive or not
	 * @param customFilters
	 *            custom filters list is taken from the property definition and if provided, then the filters are
	 *            executed and the values returned are retained with the once returned by other service calls
	 * @param mapped
	 *            if the response should be returned as array or as mapped objects by codelist key
	 * @param language
	 *            optional language. Defaults to 'en' if not specified.
	 * @param query
	 *            string that represent codelist value prefix.
	 * @return JSON object as string containing an array of codelist value code and description.
	 */
	@GET
	@Path("/{codelist}")
	@SuppressWarnings("squid:S00107")
	public String retrieveCodeValues(@PathParam("codelist") Integer codelist, @QueryParam("filterBy") String filterBy,
			@QueryParam("filterSource") String filterSource, @QueryParam("inclusive") boolean inclusive,
			@QueryParam("customFilters[]") String[] customFilters, @QueryParam("mapped") boolean mapped,
			@QueryParam("lang") String language, @QueryParam("q") String query) {

		if (codelist == null) {
			return null;
		}
		boolean mappedResult = false;
		if (mapped) {
			mappedResult = true;
		}
		LOG.debug("codelist[{}], filter[{}], filterSource[{}], inclusive[{}], customFilters:[{}], mapped[{}]", codelist,
				filterBy, filterSource, inclusive, customFilters, mappedResult);

		Map<String, CodeValue> codeValues = null;
		if (StringUtils.isNotEmpty(filterBy) && StringUtils.isNotEmpty(filterSource)) {
			codeValues = codelistService.filterCodeValues(codelist, inclusive, filterSource, filterBy);
		} else {
			codeValues = codelistService.getCodeValues(codelist);
		}

		// if custom filters are provided, then retain the result values from both service
		// invocations
		if (customFilters != null && customFilters.length > 0) {
			Map<String, CodeValue> filteredCodeValues = codelistService.getFilteredCodeValues(codelist, customFilters);
			codeValues.entrySet().retainAll(filteredCodeValues.entrySet());
		}

		Object result = codevaluesToJsonArray(language, codeValues, mappedResult, query);

		return result.toString();
	}

	/**
	 * Get a codevalue object as json.
	 *
	 * @param codelist
	 *            the codelist
	 * @param code
	 *            the code from given codelist
	 * @param language
	 *            the language
	 * @return the codevalue
	 */
	@GET
	@Path("/{codelist}/{code}")
	public String getCodevalue(@PathParam("codelist") Integer codelist, @PathParam("code") String code,
			@QueryParam("lang") String language) {
		String languageFilter = getlanguage(language);

		CodeValue value = codelistService.getCodeValue(codelist, code);
		if (value != null) {
			JSONObject cvJson = value.toJSONObject();
			JsonUtil.addToJson(cvJson, "locale", languageFilter);
			JsonUtil.addToJson(cvJson, "localizedDescription",
					JsonUtil.getStringValue(JsonUtil.getJsonObject(cvJson, "descriptions"), languageFilter));
			return cvJson.toString();
		}
		return null;
	}

	/**
	 * Retrieves a value of a code from a specific codelist.
	 *
	 * @param codelist
	 *            Codelist number.
	 * @param code
	 *            Code from the codelist.
	 * @param language
	 *            Optional language of the value (defaults to the system default)
	 * @return The value for a code from a codelist in the specified language, or null if nothing matches the procided
	 *         criteria.
	 */
	@GET
	@Path("/{codelist}/values/{code}")
	public String getCodeValueLabel(@PathParam("codelist") Integer codelist, @PathParam("code") String code,
			@QueryParam("lang") String language) {
		String languageFilter = language;

		if (StringUtils.isBlank(languageFilter)) {
			languageFilter = systemConfiguration.getSystemLanguage();
		}

		String label = null;
		CodeValue value = codelistService.getCodeValue(codelist, code);
		if (value != null) {
			Serializable serializible = value.getProperties().get(languageFilter);
			if (serializible != null) {
				label = serializible.toString();
			}
		}
		return label;
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

		JSONObject value = null;
		for (Map.Entry<BigInteger, String> entry : codelists.entrySet()) {
			value = new JSONObject();

			JsonUtil.addToJson(value, VALUE, entry.getKey());
			JsonUtil.addToJson(value, LABEL, entry.getValue());
			result.put(value);
		}
		return result.toString();
	}

	/**
	 * Codevalues to json array or object according to mapped parameter.
	 *
	 * @param language
	 *            the language
	 * @param codeValues
	 *            the code values
	 * @param mapped
	 *            the mapped
	 * @return the object
	 * @throws JSONException
	 */
	protected Object codevaluesToJsonArray(String language, Map<String, CodeValue> codeValues, boolean mapped,
			String query) {
		String calculatedLanguage = getlanguage(language);
		if (mapped) {
			JSONObject result = new JSONObject();
			JSONObject value = null;
			for (Map.Entry<String, CodeValue> entry : codeValues.entrySet()) {
				value = codevalueAsJson(calculatedLanguage, entry);
				JsonUtil.addToJson(result, entry.getKey(), value);
			}
			return result;
		}

		JSONArray result = new JSONArray();
		JSONObject value = null;
		for (Map.Entry<String, CodeValue> entry : codeValues.entrySet()) {
			value = codevalueAsJson(calculatedLanguage, entry);
			value = findValueByQuery(value, query);
			if (!JsonUtil.isNullOrEmpty(value)) {
				result.put(value);
			}
		}
		return result;
	}

	/**
	 * Search for codelist value based on different parts of it.
	 *
	 * @param codeValue
	 *            codelist value
	 * @param query
	 *            value part
	 * @return codelist value if is supported
	 */
	private JSONObject findValueByQuery(JSONObject codeValue, String query) {
		if (StringUtils.isBlank(query)) {
			return codeValue;
		}
		String label = JsonUtil.getStringValue(codeValue, LABEL);
		if (StringUtils.containsIgnoreCase(label, query)) {
			return codeValue;
		}
		return null;
	}

	/**
	 * Codevalue as json.
	 *
	 * @param calculatedLanguage
	 *            the calculated language
	 * @param entry
	 *            the entry
	 * @return the jSON object
	 */
	protected JSONObject codevalueAsJson(String calculatedLanguage, Map.Entry<String, CodeValue> entry) {
		JSONObject value;
		value = entry.getValue().toJSONObject();
		JsonUtil.addToJson(value, "ln", calculatedLanguage);
		JsonUtil.addToJson(value, VALUE, entry.getKey());
		JsonUtil.addToJson(value, LABEL, entry.getValue().getProperties().get(calculatedLanguage));
		return value;
	}
}
