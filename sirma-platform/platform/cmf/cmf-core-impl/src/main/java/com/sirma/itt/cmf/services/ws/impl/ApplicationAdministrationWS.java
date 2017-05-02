package com.sirma.itt.cmf.services.ws.impl;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.ws.soap.MTOM;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.services.ws.ApplicationAdministration;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.cache.SimpleCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.event.LoadAllDefinitions;
import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.codelist.event.ResetCodelistEvent;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.serialization.xstream.XStreamConvertableWrapper;
import com.sirma.itt.seip.template.LoadTemplates;

/**
 * Web service for remote administration of the application.
 *
 * @author BBonev
 */
@ApplicationScoped
@Path("/administration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@MTOM
public class ApplicationAdministrationWS implements ApplicationAdministration {

	private static final String MESSAGE = "message";
	private static final String EXIST = "exist";

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationAdministrationWS.class);

	@Inject
	private EventService eventService;

	@Inject
	private EntityLookupCacheContext cacheContext;

	@Inject
	private TypeConverter converter;
	@Inject
	private LabelService labelService;
	@Inject
	private DictionaryService dictionaryService;

	@Override
	@GET
	@Path("reloadDefinitions")
	public void reloadDefinitions() {
		LOGGER.info("Called reloadDefinitions from WS port");
		eventService.fire(new LoadAllDefinitions());
	}

	@Override
	@GET
	@Path("reloadTemplates")
	public void reloadTemplates() {
		LOGGER.info("Called reloadTemplates from WS port");
		eventService.fire(new LoadTemplates());
	}

	@Override
	@GET
	@Path("reloadSemanticDefinitions")
	public void reloadSemanticDefinitions() {
		LOGGER.info("Called reloadSemanticDefinitions from WS port");
		eventService.fire(new LoadSemanticDefinitions());
	}

	@Override
	@GET
	@Path("resetCodelists")
	public void resetCodelists() {
		LOGGER.info("Called resetCodelists from WS port");
		eventService.fire(new ResetCodelistEvent());
	}

	@Override
	@GET
	@Path("clearInternalCache")
	public void clearInternalCache() {
		LOGGER.info("Called clearInternalCache from WS port");
		Set<String> activeCaches = cacheContext.getActiveCaches();
		for (String cacheName : activeCaches) {
			if (cacheName.toLowerCase().contains("entity")) {
				LOGGER.info("Clearing " + cacheName);
				SimpleCache<Serializable, Object> cache = cacheContext.getCache(cacheName, false);
				cache.clear();
			}
		}
	}

	@Override
	@GET
	@Path("clearDefinitionsCache")
	public void clearDefinitionsCache() {
		LOGGER.info("Called clearDefinitionsCache from WS port");
		Set<String> activeCaches = cacheContext.getActiveCaches();
		for (String cacheName : activeCaches) {
			if (cacheName.toLowerCase().contains("definition")) {
				LOGGER.info("Clearing " + cacheName);
				SimpleCache<Serializable, Object> cache = cacheContext.getCache(cacheName, false);
				cache.clear();
			}
		}
	}

	/**
	 * Clear cache.
	 */
	@GET
	@Path("clear-label-cache")
	public void clearLabelCache() {
		labelService.clearCache();
	}

	/**
	 * Clear cache.
	 *
	 * @param cacheName
	 *            the cache name
	 * @return the response
	 */
	@GET
	@Path("{cacheId}/clear")
	public Response clearCache(@PathParam("cacheId") String cacheName) {
		if (StringUtils.isNullOrEmpty(cacheName)) {
			return RestUtil.buildResponse(Status.NOT_FOUND, "{}");
		}
		EntityLookupCache<Serializable, Object, Serializable> cache = cacheContext.getCache(cacheName);
		JSONObject object = new JSONObject();
		buildCacheStatistics(object, cacheName, cache);
		if (cache != null) {
			cache.clear();
		}
		return RestUtil.buildResponse(Status.OK, object.toString());
	}

	/**
	 * Gets the cache statistics.
	 *
	 * @return the cache statistics
	 */
	@GET
	@Path("cacheStatistics")
	public Response getCacheStatistics() {
		Set<String> activeCaches = cacheContext.getActiveCaches();
		JSONObject jsonObject = new JSONObject();
		for (String cacheName : activeCaches) {
			EntityLookupCache<Serializable, Object, Serializable> cache = cacheContext.getCache(cacheName);
			buildCacheStatistics(jsonObject, cacheName, cache);
		}
		return RestUtil.buildResponse(Status.OK, jsonObject.toString());
	}

	/**
	 * Builds the cache statistics.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param cacheName
	 *            the cache name
	 * @param cache
	 *            the cache
	 */
	private static void buildCacheStatistics(JSONObject jsonObject, String cacheName,
			EntityLookupCache<Serializable, Object, Serializable> cache) {
		if (cache == null) {
			JsonUtil.addToJson(jsonObject, cacheName, JSONObject.NULL);
		} else {
			JSONObject values = new JSONObject();
			JsonUtil.addToJson(values, "primary", Integer.toString(cache.primaryKeys().size()));
			JsonUtil.addToJson(values, "secondary", Integer.toString(cache.secondaryKeys().size()));
			JsonUtil.addToJson(jsonObject, cacheName, values);
		}
	}

	@GET
	@Override
	@Path("definition")
	@Produces({ "application/xml" })
	public String getDefinition(@QueryParam("id") String id) {
		DefinitionModel model = null;
		if (StringUtils.isNullOrEmpty(id)) {
			return dictionaryService
					.getAllDefinitions()
						.map(def -> "\n\t<definition revision=\"" + def.getRevision() + "\" >" + def.getIdentifier()
								+ "</definition>")
						.collect(Collectors.joining("", "<definitions>", "</definitions>"));
		}

		model = dictionaryService.find(id);
		if (model == null) {
			return "<error>Definition \"" + id + "\" not found</error>";
		}
		return converter.convert(String.class, new XStreamConvertableWrapper(model));
	}

	/**
	 * Check definitions.
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	@POST
	@Path("definition")
	public Response checkDefinitions(String data) {
		JSONObject object = JsonUtil.createObjectFromString(data);
		JSONArray array = JsonUtil.getJsonArray(object, "data");
		for (int i = 0; i < array.length(); i++) {
			try {
				Object element = array.get(i);
				if (element instanceof JSONObject) {
					JSONObject jsonObject = (JSONObject) element;
					String id = JsonUtil.getStringValue(jsonObject, "id");
					if (id == null) {
						JsonUtil.addToJson(jsonObject, MESSAGE, "Missing definition id");
						JsonUtil.addToJson(jsonObject, EXIST, Boolean.FALSE);
						continue;
					}
					DefinitionModel definition = dictionaryService.find(id);
					JsonUtil.addToJson(jsonObject, EXIST, definition != null);
				}
			} catch (JSONException e) {
				return Response.serverError().entity(e).build();
			}
		}

		return Response.ok(object.toString()).build();
	}
}