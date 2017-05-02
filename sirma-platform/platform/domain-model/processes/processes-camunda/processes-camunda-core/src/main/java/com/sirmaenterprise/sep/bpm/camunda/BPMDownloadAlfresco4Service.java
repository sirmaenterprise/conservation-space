package com.sirmaenterprise.sep.bpm.camunda;

import static com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants.KEY_DATA_ITEMS;
import static com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants.KEY_NODEREF;
import static com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants.KEY_PAGING;
import static com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants.KEY_PAGING_MAX;
import static com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants.KEY_PAGING_SIZE;
import static com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants.KEY_PAGING_SKIP;
import static com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants.KEY_QUERY;
import static com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants.KEY_ROOT_DATA;
import static com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants.KEY_SORT;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileWithNameDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.AdapterService;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Downloads the requested types of {@link CamundaModels} from the default location in DMS - Company
 * Home/Dictionary/Workflow Definitions.
 *
 * @author bbanchev
 */
@Singleton
public class BPMDownloadAlfresco4Service implements AdapterService {

	private static final long serialVersionUID = 5018345496436725823L;
	private static final int MAX_MODELS = 1000;
	@Inject
	private RESTClient restClient;

	/**
	 * Enum of models to be processed by the adapter during a specific request.
	 * 
	 * @author bbanchev
	 */
	public enum CamundaModels {
		/** BPMN model. */
		BPMN("name:\"*.bpmn\""),
		/** CMMN model. */
		CMMN("name:\"*.cmmn\""),
		/** DMN model. */
		DMN("name:\"*.dmn\"");

		String solrFilter;

		/**
		 * Instantiates a new camunda models.
		 *
		 * @param solrFilter
		 *            the solr filter
		 */
		CamundaModels(String solrFilter) {
			this.solrFilter = solrFilter;
		}
	}

	/**
	 * Prepare a list of {@link FileDescriptor} to models definitions to be downloaded from alfresco.
	 * 
	 * @param models
	 *            the models to retrieve
	 * @return a list of {@link FileDescriptor} to models definitions
	 * @throws DMSException
	 *             with describing message on any error during request
	 */
	public List<FileDescriptor> retrieveDefinitions(CamundaModels... models) throws DMSException {
		if (models == null || models.length == 0) {
			return Collections.emptyList();
		}
		final JsonObjectBuilder request = Json.createObjectBuilder();
		try {
			prepareRequest(request, models);
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.build().toString(), true);
			return readResponse(restClient.request(ServiceURIRegistry.CMF_SEARCH_SERVICE, createMethod));
		} catch (DMSException e) {
			throw e;
		} catch (Exception e) {
			throw new DMSException("Search in DMS failed for " + request.build().toString(), e);
		}

	}

	private static JsonObjectBuilder prepareRequest(JsonObjectBuilder request, CamundaModels[] models) {
		JsonObjectBuilder paging = Json.createObjectBuilder();
		paging.add(KEY_PAGING_SIZE, MAX_MODELS);
		paging.add(KEY_PAGING_SKIP, 0);
		paging.add(KEY_PAGING_MAX, MAX_MODELS);
		request.add(KEY_PAGING, paging);

		JsonArrayBuilder sorting = Json.createArrayBuilder();
		JsonObjectBuilder defaultSorter = Json.createObjectBuilder();
		defaultSorter.add("cm:modified", false);
		sorting.add(defaultSorter);
		request.add(KEY_SORT, sorting);

		StringJoiner modelsJoiner = new StringJoiner(" OR ");
		for (CamundaModels camundaModels : models) {
			modelsJoiner.add(camundaModels.solrFilter);
		}
		request.add(KEY_QUERY,
				"PATH:\"" + "/app:company_home/app:dictionary/app:workflow_defs/*\" AND TYPE:\"cm:content\" AND ("
						+ modelsJoiner.toString() + ")");
		return request;
	}

	private List<FileDescriptor> readResponse(String response) throws DMSException, DMSClientException, IOException {
		if (response == null) {
			throw new DMSException("DMS system does not respond properly to search!");
		}
		try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
			JsonObject result = jsonReader.readObject();
			if (!result.containsKey(KEY_ROOT_DATA)) {
				throw new DMSException("DMS response is incorrect! Recieved data: " + response);
			}
			// Gets the file name for each object and prepare the descriptor.
			JsonArray nodes = result.getJsonObject(KEY_ROOT_DATA).getJsonArray(KEY_DATA_ITEMS);
			List<FileDescriptor> results = new ArrayList<>(nodes.size());
			for (int i = 0; i < nodes.size(); i++) {
				JsonObject item = nodes.getJsonObject(i);
				populateDescriptor(results, item);
			}
			return results;

		}
	}

	private void populateDescriptor(List<FileDescriptor> results, JsonObject item)
			throws DMSClientException, UnsupportedEncodingException {
		String id = item.getString(KEY_NODEREF);
		String details = restClient.request(ServiceURIRegistry.NODE_DETAILS + id.replaceFirst("://", "/"),
				restClient.createMethod(new GetMethod(), (String) null, true));
		try (JsonReader detailsReader = Json.createReader(new StringReader(details))) {
			JsonObject nodeDetails = detailsReader.readObject().getJsonObject("item");
			if (nodeDetails.containsKey("fileName")) {
				results.add(
						new AlfrescoFileWithNameDescriptor(id, nodeDetails.getString("fileName"), null, restClient));
			}
		}
	}

}