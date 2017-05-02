/*
 *
 */
package com.sirma.itt.seip.definition.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DeletedDefinitionInfo;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.MutableDictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.serialization.convert.OutputFormat;
import com.sirma.itt.seip.serialization.xstream.XStreamConvertableWrapper;

/**
 * Rest service for working with definitions
 *
 * @author BBonev
 */
@Path("/definitions")
@Produces(MediaType.APPLICATION_JSON)
public class DefinitionRestService {

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private MutableDictionaryService mutableDictionaryService;

	@Inject
	private TypeConverter typeConverter;

	/**
	 * List all.
	 *
	 * @return the response
	 */
	@GET
	public Response listAll() {
		return RestUtil.buildDataResponse(collectSimpleDefinitionInfo(dictionaryService.getAllDefinitions()));
	}

	/**
	 * Gets the latest definition revision as json
	 *
	 * @param definitionId
	 *            the definition id
	 * @return the definition
	 */
	@GET
	@Path("{definitionId}")
	public Response getLastDefinition(@PathParam("definitionId") String definitionId) {
		return getDefinitionAs(definitionId, null, OutputFormat.JSON);
	}

	/**
	 * Gets specific definition revision as json
	 *
	 * @param definitionId
	 *            the definition id
	 * @param revision
	 *            the revision
	 * @return the definition
	 */
	@GET
	@Path("{definitionId}/{revision}")
	public Response getConcreteDefinition(@PathParam("definitionId") String definitionId,
			@PathParam("revision") Long revision) {
		return getDefinitionAs(definitionId, revision, OutputFormat.JSON);
	}

	/**
	 * Gets the latest definition revision as XML
	 *
	 * @param definitionId
	 *            the definition id
	 * @return the definition
	 */
	@GET
	@Path("{definitionId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getLastDefinitionAsXML(@PathParam("definitionId") String definitionId) {
		return getDefinitionAs(definitionId, null, OutputFormat.XML);
	}

	/**
	 * Gets specific definition revision as XML
	 *
	 * @param definitionId
	 *            the definition id
	 * @param revision
	 *            the revision
	 * @return the definition
	 */
	@GET
	@Path("{definitionId}/{revision}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getConcreteDefinitionAsXML(@PathParam("definitionId") String definitionId,
			@PathParam("revision") Long revision) {
		return getDefinitionAs(definitionId, revision, OutputFormat.XML);
	}

	/**
	 * Gets the definition as the specified format
	 *
	 * @param definitionId
	 *            the definition id
	 * @param revision
	 *            the revision
	 * @param outputFormat
	 *            the need output format
	 * @return the definition as
	 */
	private Response getDefinitionAs(String definitionId, Long revision, OutputFormat outputFormat) {

		DefinitionModel model = null;
		if (revision == null) {
			model = dictionaryService.find(definitionId);
		} else {
			model = dictionaryService.getDefinition(GenericDefinition.class, definitionId, revision);
		}

		if (model == null) {
			return RestUtil.buildErrorResponse(Status.NOT_FOUND,
					"Definition with id " + definitionId + " and revision " + revision + " was not found");
		}

		XStreamConvertableWrapper wrapper = new XStreamConvertableWrapper(outputFormat, model);
		if (outputFormat == OutputFormat.XML) {
			String xml = typeConverter.convert(String.class, wrapper);
			return RestUtil.buildResponse(Status.OK, xml);
		}
		JSONObject response = typeConverter.convert(JSONObject.class, wrapper);
		return RestUtil.buildDataResponse(response);
	}

	/**
	 * Collect simple definition info.
	 *
	 * @param allDefinitions
	 *            the all definitions
	 * @return the collection
	 */
	private static Collection<JSONObject> collectSimpleDefinitionInfo(Stream<DefinitionModel> allDefinitions) {
		return allDefinitions.map(definitionModel -> {
			JSONObject jsonObject = new JSONObject();
			JsonUtil.addToJson(jsonObject, "definitionId", definitionModel.getIdentifier());
			JsonUtil.addToJson(jsonObject, "definitionRevision", definitionModel.getRevision());
			JsonUtil.addToJson(jsonObject, "type", definitionModel.getType());
			return jsonObject;
		}).collect(Collectors.toList());
	}

	/**
	 * Delete definition.
	 *
	 * @param definitionId
	 *            the definition id
	 * @param revision
	 *            the revision
	 * @return the response
	 */
	@DELETE
	@Path("{definitionId}/{revision}")
	public Response deleteDefinition(@PathParam("definitionId") String definitionId,
			@PathParam("revision") Long revision) {

		if (revision == null) {
			return RestUtil.buildErrorResponse(Status.BAD_REQUEST, "Revision number should be specified.");
		}

		DeletedDefinitionInfo definitionInfo = mutableDictionaryService.deleteDefinition(GenericDefinition.class,
				definitionId, revision);
		return RestUtil
				.buildDataResponse(Collections.singletonList(typeConverter.convert(JSONObject.class, definitionInfo)));
	}

	/**
	 * Delete all definition revisions effectively removing the definition from the application
	 *
	 * @param definitionId
	 *            the definition id
	 * @return the response
	 */
	@DELETE
	@Path("{definitionId}/allRevisions")
	public Response deleteDefinitionAllRevisions(@PathParam("definitionId") String definitionId) {

		Collection<DeletedDefinitionInfo> definitionInfo = mutableDictionaryService
				.deleteAllDefinitionRevisions(GenericDefinition.class, definitionId);
		return RestUtil.buildDataResponse(typeConverter.convert(JSONObject.class, definitionInfo));
	}

	/**
	 * Delete all old definition revisions and leaving only the latest definition revision. Useful for definition
	 * cleanup
	 *
	 * @param definitionId
	 *            the definition id
	 * @return the response
	 */
	@DELETE
	@Path("{definitionId}/oldRevisions")
	public Response deleteDefinitionOldRevisions(@PathParam("definitionId") String definitionId) {
		Collection<DeletedDefinitionInfo> definitionInfo = mutableDictionaryService
				.deleteOldDefinitionRevisions(GenericDefinition.class, definitionId);
		return RestUtil.buildDataResponse(typeConverter.convert(JSONObject.class, definitionInfo));
	}

	/**
	 * Delete all old definition revisions and leaving only the latest definition revision. Useful for definition
	 * cleanup
	 *
	 * @param definitionId
	 *            the definition id
	 * @return the response
	 */
	@DELETE
	@Path("{definitionId}/lastRevision")
	public Response deleteLastDefinitionRevision(@PathParam("definitionId") String definitionId) {

		DeletedDefinitionInfo definitionInfo = mutableDictionaryService.deleteLastDefinition(GenericDefinition.class,
				definitionId);
		return RestUtil
				.buildDataResponse(Collections.singletonList(typeConverter.convert(JSONObject.class, definitionInfo)));
	}

}
