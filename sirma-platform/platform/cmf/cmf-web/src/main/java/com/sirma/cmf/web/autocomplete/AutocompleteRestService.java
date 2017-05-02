package com.sirma.cmf.web.autocomplete;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.emf.label.retrieve.RetrieveResponse;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.search.SearchConfiguration;

/**
 * A service for autocomplete or load combobox values from different resources.
 */
@Path("/autocomplete")
@ApplicationScoped
@Produces("application/json; charset=utf-8")
public class AutocompleteRestService extends EmfRestService {
	private static final String LABELS_NOT_FOUND_LABEL_ID = "autocomplete.labels.notfound";

	@Inject
	private FieldValueRetrieverService fieldValueRetrieverService;

	@Inject
	private SearchConfiguration searchConfiguration;

	/**
	 * Load value-label pairs for given field.
	 *
	 * @param fieldId
	 *            the id of the field
	 * @param uriInfo
	 *            object with URL parameters send with the request. Could contain common parameters as: <br />
	 *            <ul>
	 *            <li><b>q</b> - query term used to filter returned values</li>
	 *            <li><b>offset</b> - returned results offset</li>
	 *            <li><b>limit</b> - maximum number of returned results</li>
	 *            </ul>
	 *            and additional parameters to be used by the appropriate retrievers
	 * @return the {@link RetrieveResponse} to jSON string containing total number of results found and a request page
	 *         of results
	 * @throws JSONException
	 *             the jSON exception
	 */
	@GET
	@Path("/{field}")
	public String load(@PathParam("field") String fieldId, @Context UriInfo uriInfo) throws JSONException {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		SearchRequest request = new SearchRequest(queryParams);
		String queryTerm = request.getFirst("q");
		Integer offset = request.getFirstInteger("offset");
		Integer aLimit = request.getFirstInteger("limit");
		if (offset == null) {
			offset = 0;
		}
		if (aLimit == null) {
			aLimit = Integer.valueOf(searchConfiguration.getPagerPageSize());
		}

		RetrieveResponse result = fieldValueRetrieverService.getValues(fieldId, queryTerm, request, offset, aLimit);
		return result.toJSONString();
	}

	/**
	 * Load labels for the provided values.
	 *
	 * @param fieldId
	 *            the id of the field
	 * @param values
	 *            the values for which the labels will be retrieved
	 * @param uriInfo
	 *            contains query parameters used when construction the {@link SearchRequest}
	 * @return the labels for the provided values
	 */
	@GET
	@Path("/labels/{field}")
	public Response loadLabels(@PathParam("field") String fieldId, @QueryParam("values[]") List<String> values,
			@Context UriInfo uriInfo) {
		SearchRequest additionalParameters = new SearchRequest(uriInfo.getQueryParameters());
		Map<String, String> labels = fieldValueRetrieverService.getLabels(fieldId,
				values.toArray(new String[values.size()]), additionalParameters);
		if (CollectionUtils.isNotEmpty(labels)) {
			return buildOkResponse(constructLabelResponse(labels).toString());
		}
		return buildBadRequestResponse(labelProvider.getValue(LABELS_NOT_FOUND_LABEL_ID));
	}

	/**
	 * Construct the label response from the provided value-label mapping.
	 *
	 * @param valueToLabel
	 *            the value to label mapping
	 * @return a JSON array containing the value to label pairs
	 */
	private static JSONArray constructLabelResponse(Map<String, String> valueToLabel) {
		JSONArray results = new JSONArray();
		valueToLabel.entrySet().forEach(entry -> results.put(labelEntryToJSONObject(entry)));
		return results;
	}

	/**
	 * Convert a value-label entry to a {@link JSONObject}.
	 *
	 * @param entry
	 *            the value-label entry
	 * @return the JSON object
	 */
	private static JSONObject labelEntryToJSONObject(Entry<String, String> entry) {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "value", entry.getKey());
		JsonUtil.addToJson(object, "label", entry.getValue());
		return object;
	}
}
