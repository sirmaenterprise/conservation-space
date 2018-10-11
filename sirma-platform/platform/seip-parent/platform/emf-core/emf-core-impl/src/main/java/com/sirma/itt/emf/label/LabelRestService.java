package com.sirma.itt.emf.label;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverParameters;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.definition.label.SystemCode;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.rest.utils.JSON;

/**
 * Rest service that provides access to internal labels.
 *
 * @author BBonev
 * @author Vilizar Tsonev
 */
@Path("/label")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class LabelRestService extends EmfRestService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(LabelRestService.class);

	/** The field value retriever **/
	@Inject
	private FieldValueRetrieverService fieldValueRetrievalService;

	/**
	 * Retrieve a label using the {@link LabelService}.
	 *
	 * @param labelId
	 *            the label id
	 * @param lang
	 *            the language
	 * @return the label
	 */
	@GET
	@Path("/{labelId}")
	public String getLabel(@PathParam("labelId") String labelId, @QueryParam("lang") String lang) {
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		if (StringUtils.isBlank(lang)) {
			objectBuilder.add(labelId, labelProvider.getLabel(labelId));
		} else {
			objectBuilder.add(labelId, labelProvider.getLabel(labelId, lang));
		}
		return objectBuilder.build().toString();
	}

	/**
	 * Retrieve a label from the label bundes. If the label is not found in the bundles, it will be retrieved using the
	 * {@link LabelService}.
	 *
	 * @param labelId
	 *            the label id
	 * @return the label
	 */
	@GET
	@Path("/bundle/{labelId}")
	public String getBundleLabel(@PathParam("labelId") String labelId) {
		return Json.createObjectBuilder().add(labelId, labelProvider.getValue(labelId)).build().toString();
	}

	/**
	 * Retrieve a label from the label bundes. If the label is not found in the bundles, it will be retrieved using the
	 * {@link LabelService}.
	 *
	 * @param data
	 *            json array containing the label ids.
	 * @return the labels
	 */
	@POST
	@Path("/multi")
	public String getLabels(String data) {
		if (StringUtils.isBlank(data)) {
			return "{}";
		}
		return JSON.readArray(data, array -> {
			Map<String, String> mappings = array.stream()
					.map(JSON::readJsonValue)
					.map(Object::toString)
					.collect(Collectors.toMap(Function.identity(), labelProvider::getLabel, (s, s2) -> s));
			return JSON.convertToJsonObject(mappings).toString();
		});
	}

	/**
	 * Retrieve a label from the label bundes.
	 *
	 * @param data
	 *            json array containing the label ids.
	 * @return the labels
	 */
	@POST
	@Path("/bundle/multi")
	public String getBundleLabels(String data) {
		if (StringUtils.isBlank(data)) {
			return "{}";
		}

		return JSON.readArray(data, array -> {
			Map<String, String> mappings = array.stream()
					.map(JSON::readJsonValue)
					.map(Object::toString)
					.collect(Collectors.toMap(Function.identity(), labelProvider::getBundleValue, (s, s2) -> s));
			return JSON.convertToJsonObject(mappings).toString();
		});
	}

	/**
	 * Gets the labels for the provided system codes, retrieving them from code lists or semantics.
	 *
	 * @param systemCodes
	 *            is the collection of system code objects to retrieve the labels for. Each one has its (system) code,
	 *            its indicator, which can be any value defined in {@link FieldId}, and its indicatorValue, which can be
	 *            the code list number for the given code, or 0 if user display name is retrieved<br>
	 *            Example:<br>
	 *            [{ "code":"0006-000084", "indicator": "codelist", "indicatorValue": "208" }]<br>
	 *            Result:<br>
	 *            [{label: "Medium" code: "0006-000084" }]
	 * @return the JSON array where each system code is mapped with its label
	 */
	@Documentation("Gets the labels for the provided system codes, retrieving them from code lists or semantics.")
	@Path("/search")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public String getLabels(List<SystemCode> systemCodes) {
		JsonArrayBuilder codeLabels = Json.createArrayBuilder();
		try {
			for (SystemCode systemCode : systemCodes) {
				JsonObjectBuilder codeLabel = Json.createObjectBuilder();
				Map<String, List<String>> requestMap = new HashMap<>(8);
				SearchRequest request = new SearchRequest(requestMap);
				request.add("field", systemCode.getField());
				request.add(FieldValueRetrieverParameters.CODE_LIST_ID, systemCode.getIndicatorValue());
				String processedCode = systemCode.getCode();
				// the UsernameByURIFieldValueRetriever supports only short URIs, so a conversion is
				// needed. The processedCode contains the converted URI
				if (systemCode.getIndicator().equals(FieldId.USERNAME_BY_URI)) {
					processedCode = convertToShortUri(systemCode.getCode());
				}
				String label = fieldValueRetrievalService.getLabel(systemCode.getIndicator(), processedCode, request);
				codeLabel.add("code", systemCode.getCode());
				codeLabel.add("label", label);
				codeLabels.add(codeLabel);
			}
		} catch (JsonException e) {
			LOGGER.error("Error while parsing the system codes collection: " + e.getMessage(), e);
		}
		return codeLabels.build().toString();
	}

	/**
	 * Converts the given full URI into a short URI.
	 *
	 * @param fullUri
	 *            is the full URI
	 * @return the short URI
	 */
	private String convertToShortUri(String fullUri) {
		ShortUri shortUri = typeConverter.convert(ShortUri.class, fullUri);
		if (shortUri != null) {
			return shortUri.toString();
		}
		return fullUri;
	}
}
