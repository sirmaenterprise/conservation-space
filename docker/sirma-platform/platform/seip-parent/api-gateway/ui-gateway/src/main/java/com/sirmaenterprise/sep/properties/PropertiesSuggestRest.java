package com.sirmaenterprise.sep.properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.sirma.itt.seip.instance.properties.PropertiesSuggestService;
import com.sirma.itt.seip.json.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sirma.itt.seip.rest.utils.JsonKeys.TARGET_ID;

/**
 * Rest service for suggesting values for object properties on instance create.
 *
 * @author smustafov
 * @author svetlozar.iliev
 */
@ApplicationScoped
@Path("/properties/suggest")
public class PropertiesSuggestRest {

	private static final String MULTIVALUED = "multivalued";
	private static final String TYPE = "type";
	private static final String ID = "id";

	@Inject
	private PropertiesSuggestService propertiesSuggestService;

	/**
	 * Searches suggested ids for given context id, type and if its multivalued.
	 *
	 * @param contextId
	 *            the context id
	 * @param type
	 *            the type of the property
	 * @param multivalued
	 *            if the property allows multiple values
	 * @return list of the suggested values
	 */
	@GET
	public JsonArray suggest(@QueryParam(TARGET_ID) String contextId, @QueryParam(TYPE) String type,
			@QueryParam(MULTIVALUED) boolean multivalued) {
		List<Map<String, String>> resultList = propertiesSuggestService.suggestPropertiesIds(contextId, type, multivalued)
				.stream()
					.map(this::addToMap)
					.collect(Collectors.toList());

		return JSON.convertToJsonArray(resultList);
	}

	private Map<String, String> addToMap(String id){
		Map<String, String> result = new HashMap<>();
		result.put(ID, id);
		return result;
	}
}
