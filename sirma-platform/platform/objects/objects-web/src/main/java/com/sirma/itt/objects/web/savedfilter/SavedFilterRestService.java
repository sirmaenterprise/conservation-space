package com.sirma.itt.objects.web.savedfilter;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.resources.instances.InstanceResource;
import com.sirma.itt.seip.search.rest.SearchRest;

/**
 * A REST service for managing saved filters
 *
 * @deprecated use {@link SearchRest} to retrieve saved searches and {@link InstanceResource} to create and update new
 *             and existing ones.
 */
@Path("/savedfilters")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Deprecated
public class SavedFilterRestService extends EmfRestService {

	private static final String BASIC_SEARCH = "basic";

	// Saved Filter instance type
	private static final String SAVED_FILTER = "savedfilter";

	// Saved Filter properties
	private static final String FILTER_TYPE = "filterType";
	private static final String FILTER_CRITERIA = "filterCriteria";
	private static final String DESCRIPTION = "description";
	private static final String MUTABLE = "mutable";
	private static final String CAN_UPDATE = "canUpdate";

	// Criteria fields
	private static final String CREATED_BY = "createdBy";
	private static final String LOCATION = "location";

	@Inject
	private FieldValueRetrieverService fieldValueRetrieverService;

	/**
	 * Loads a saved filter by its URI. Also loads labels for autocomplete fields if basic search.
	 *
	 * @param savedFilterId
	 *            the saved filter URI
	 * @return the response
	 */
	@Path("/{savedFilterId}")
	@GET
	public Response load(@PathParam("savedFilterId") String savedFilterId) {
		if (StringUtils.isNullOrEmpty(savedFilterId)) {
			return buildResponse(Status.BAD_REQUEST, "Missing required arguments!");
		}

		Instance instance = fetchInstance(savedFilterId, SAVED_FILTER);
		if (instance == null) {
			return buildResponse(Status.BAD_REQUEST, "Unable to find saved filter with id: " + savedFilterId);
		}

		JSONObject responseData = new JSONObject();
		String filterType = String.valueOf(instance.getProperties().get(FILTER_TYPE));
		JsonUtil.addToJson(responseData, FILTER_TYPE, instance.getProperties().get(FILTER_TYPE));
		JsonUtil.addToJson(responseData, DESCRIPTION, instance.getProperties().get(DESCRIPTION));
		JsonUtil.addToJson(responseData, DefaultProperties.TITLE,
				instance.getProperties().get(DefaultProperties.TITLE));
		if (instance.getProperties().get(MUTABLE) != null) {
			JsonUtil.addToJson(responseData, MUTABLE, instance.getProperties().get(MUTABLE));
		} else {
			JsonUtil.addToJson(responseData, MUTABLE, Boolean.TRUE);
		}

		if (authorityService.isActionAllowed(instance, ActionTypeConstants.EDIT_DETAILS, null)) {
			JsonUtil.addToJson(responseData, CAN_UPDATE, Boolean.TRUE);
		} else {
			JsonUtil.addToJson(responseData, CAN_UPDATE, Boolean.FALSE);
		}

		JsonUtil.addToJson(responseData, DESCRIPTION, instance.getProperties().get(DESCRIPTION));
		Serializable criteria = instance.getProperties().get(FILTER_CRITERIA);
		// Append labels for some autocomplete fields
		if (criteria != null && BASIC_SEARCH.equals(filterType)) {
			JSONObject jsonCriteria = JsonUtil.toJsonObject(criteria);
			appendLabels(jsonCriteria, CREATED_BY, FieldId.USERNAME_BY_URI);
			appendLabels(jsonCriteria, LOCATION, FieldId.OBJECT);
			criteria = jsonCriteria.toString();
		}
		JsonUtil.addToJson(responseData, FILTER_CRITERIA, criteria);
		return buildResponse(Status.OK, responseData.toString());
	}

	/**
	 * Retrieve and append labels
	 *
	 * @param jsonCriteria
	 *            the json criteria
	 * @param criteriaField
	 *            the criteria field for which a labels should be appended. Labels will be appended to an array with the
	 *            same size called "&lt;criteriaField&gt;Value"
	 * @param retrieverField
	 *            the field to be used to retrieve the label. See {@link FieldId}
	 */
	private void appendLabels(JSONObject jsonCriteria, String criteriaField, String retrieverField) {
		String criteriaValueKey = criteriaField + "Value";
		JsonUtil.addToJson(jsonCriteria, criteriaValueKey, new JSONArray());
		JSONArray valuesArr = JsonUtil.getJsonArray(jsonCriteria, criteriaField);
		if (valuesArr != null) {
			for (int i = 0; i < valuesArr.length(); i++) {
				String value = JsonUtil.getStringFromArray(valuesArr, i);
				if (value == null) {
					continue;
				}
				JsonUtil.append(jsonCriteria, criteriaValueKey,
						fieldValueRetrieverService.getLabel(retrieverField, value));
			}
		}
	}

}
