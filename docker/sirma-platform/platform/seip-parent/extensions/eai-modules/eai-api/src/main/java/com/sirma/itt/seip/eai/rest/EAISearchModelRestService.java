package com.sirma.itt.seip.eai.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.definition.compile.TypeParser;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchFormCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchType;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;
import com.sirma.itt.seip.rest.utils.Versions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Rest service to provide search model configuration for given subsystem.
 *
 * @author bbanchev
 */
@Transactional
@Singleton
@Path("/integration")
@Produces(Versions.V2_JSON)
public class EAISearchModelRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String ERROR_MSG = "Failed to retrieve search model for '%s' system!";
	@Inject
	private ModelService modelService;

	/**
	 * Provides the search model for given system.
	 *
	 * @param systemId
	 *            the system id
	 * @return the search model configuration or throws exception on failure
	 */
	@GET
	@Path("/{systemId}/model/search")
	public SearchModelConfiguration provideSearchModel(@PathParam("systemId") String systemId) {
		try {
			return modelService.getSearchConfiguration(systemId);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new RestServiceException(String.format(ERROR_MSG, systemId), Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Provide search model types - all entity types that are supported in the external system.
	 *
	 * @param systemId
	 *            the system id to get types for
	 * @return the list of entity types
	 */
	@GET
	@Path("/{systemId}/model/search/types")
	public Response provideModelTypes(@PathParam("systemId") String systemId) {
		try {
			SearchModelConfiguration modelConfiguration = modelService.getSearchConfiguration(systemId);
			JsonArrayBuilder result = Json.createArrayBuilder();
			modelConfiguration
					.getTypesData()
						.stream()
						.map(EAISearchModelRestService::transformToSearchAPIObject)
						.forEach(result::add);
			return Response.ok(result.build()).build();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new RestServiceException(String.format(ERROR_MSG, systemId), Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Provide search model types' properties - all form data properties available in the external system
	 *
	 * @param systemId
	 *            the system id to get properties for
	 * @param typeId
	 *            is the type to get specific properties for - currently ignored
	 * @return the list of entity form criteria
	 */
	@GET
	@Path("/{systemId}/model/search/{typeId}/properties")
	public Response provideModelProperties(@PathParam("systemId") String systemId, @PathParam("typeId") String typeId) {
		try {
			SearchModelConfiguration modelConfiguration = modelService.getSearchConfiguration(systemId);
			JsonArrayBuilder result = Json.createArrayBuilder();
			modelConfiguration
					.getFormData()
						.stream()
						.filter(entry -> entry.isVisible())
						.map(entry -> transformToSearchAPIObject(entry, modelConfiguration))
						.forEach(result::add);
			return Response.ok(result.build()).build();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new RestServiceException(String.format(ERROR_MSG, systemId), Status.INTERNAL_SERVER_ERROR);
		}
	}

	private static JsonObjectBuilder transformToSearchAPIObject(EntitySearchType type) {
		JsonObjectBuilder result = Json.createObjectBuilder();
		result.add("id", type.getIdentifier());
		result.add("label", type.getTitle());
		result.add("type", type.getType());
		return result;
	}

	private static JsonObjectBuilder transformToSearchAPIObject(EntitySearchFormCriterion entry,
			SearchModelConfiguration modelConfiguration) {
		JsonObjectBuilder result = Json.createObjectBuilder();
		result.add("id", entry.getPropertyId());
		EntityProperty propertyByCriteration = modelConfiguration.getPropertyByCriteration(entry);
		if (propertyByCriteration != null) {
			result.add("text", propertyByCriteration.getTitle());
			result.add("type", detectType(propertyByCriteration));
		}
		JsonArrayBuilder operators = Json.createArrayBuilder();
		operators.add(entry.getOperator());
		result.add("operators", operators);
		return result;
	}

	private static String detectType(EntityProperty propertyByCriteration) {
		if (propertyByCriteration.getCodelist() != null) {
			return "codeList";
		}
		TypeParser type = TypeParser.parse(propertyByCriteration.getType());
		return type.getDataTypeDefinitionName();
	}

}
