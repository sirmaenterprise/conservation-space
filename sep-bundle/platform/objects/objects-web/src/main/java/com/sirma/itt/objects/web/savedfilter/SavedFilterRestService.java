package com.sirma.itt.objects.web.savedfilter;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * A REST service for managing saved filters
 */
@Secure
@Path("/savedfilters")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class SavedFilterRestService extends EmfRestService {
	
	private static final Logger LOGGER = Logger.getLogger(SavedFilterRestService.class);
	
	private static final String BASIC_SEARCH = "basic";
	
	// Saved Filter instance type
	private static final String SAVED_FILTER = "savedfilter";
	
	// Saved Filter properties
	private static final String FILTER_TYPE = "filterType";
	private static final String FILTER_CRITERIA = "filterCriteria";
	private static final String DESCRIPTION = "description";
	
	// Criteria fields
	private static final String CREATED_BY = "createdBy";
	private static final String LOCATION = "location";
	
	@Inject
	private FieldValueRetrieverService fieldValueRetrieverService;
	
	/**
	 * Loads a saved filter by its URI. Also loads labels for autocomplete fields if basic search.
	 *
	 * @param savedFilterId the saved filter URI
	 * @return the response
	 */
	@Path("/{savedFilterId}")
	@GET
	public Response load(@PathParam("savedFilterId")String savedFilterId) {
		if (StringUtils.isNullOrEmpty(savedFilterId)) {
			return buildResponse(Status.BAD_REQUEST,
					"Missing required arguments!");
		}

		Instance instance = fetchInstance(savedFilterId, SAVED_FILTER);
		if (instance != null) {
			JSONObject responseData = new JSONObject();
			String filterType = String.valueOf(instance.getProperties().get(FILTER_TYPE));
			JsonUtil.addToJson(responseData, FILTER_TYPE, instance.getProperties().get(FILTER_TYPE));
			JsonUtil.addToJson(responseData, DESCRIPTION, instance.getProperties().get(DESCRIPTION));

			Serializable criteria = instance.getProperties().get(FILTER_CRITERIA);
			// Append labels for some autocomplete fields
			if (criteria != null && BASIC_SEARCH.equals(filterType)) {
				JSONObject jsonCriteria = JsonUtil.toJsonObject(criteria);
				appendLabels(jsonCriteria, CREATED_BY, FieldId.USERNAME_BY_URI);
				appendLabels(jsonCriteria, LOCATION, FieldId.CONTEXT);
				criteria = jsonCriteria.toString();
			}
			JsonUtil.addToJson(responseData, FILTER_CRITERIA, criteria);
			return buildResponse(Status.OK, responseData.toString());
		}

		return buildResponse(Status.INTERNAL_SERVER_ERROR, null);
	}
	
	/**
	 * Retrieve and append labels
	 *
	 * @param jsonCriteria the json criteria
	 * @param criteriaField the criteria field for which a labels should be appended. Labels will be appended to an array with the same size called "&lt;criteriaField&gt;Value"
	 * @param retrieverField the field to be used to retrieve the label. See {@link FieldId}
	 */
	private void appendLabels(JSONObject jsonCriteria, String criteriaField, String retrieverField) {
		try {
			jsonCriteria.put(criteriaField + "Value", new JSONArray());
			JSONArray valuesArr = JsonUtil.getJsonArray(jsonCriteria, criteriaField);
			if (valuesArr != null) {
				for (int i = 0; i < valuesArr.length(); i++) {
					String value = valuesArr.getString(i);
					jsonCriteria.accumulate(criteriaField + "Value", fieldValueRetrieverService.getLabel(retrieverField, value));
				}
			}
		} catch (JSONException e) {
			LOGGER.error(e.getMessage());
		}
	}

}

