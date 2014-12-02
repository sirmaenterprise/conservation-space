package com.sirma.itt.cmf.services.ws.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.event.LoadTemplates;
import com.sirma.itt.cmf.services.ws.ApplicationAdministration;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.codelist.event.LoadCodelists;
import com.sirma.itt.emf.codelist.event.ResetCodelistEvent;
import com.sirma.itt.emf.concurrent.GenericAsyncTask;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.event.LoadAllDefinitions;
import com.sirma.itt.emf.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.StaleDataModificationException;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.label.LabelService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.serialization.XStreamConvertableWrapper;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;

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
public class ApplicationAdministrationWS extends EmfRestService implements
		ApplicationAdministration {

	private static final Operation EDIT_DETAILS = new Operation(ActionTypeConstants.EDIT_DETAILS);

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationAdministrationWS.class);

	/** The event. */
	@Inject
	private EventService eventService;

	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The converter. */
	@Inject
	TypeConverter converter;
	/** The label service. */
	@Inject
	private LabelService labelService;

	/** The accessors. */
	@Inject
	private javax.enterprise.inject.Instance<DefinitionAccessor> accessors;

	/** The search service. */
	@Inject
	private SearchService searchService;

	/** The executor. */
	@Inject
	private TaskExecutor executor;

	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	@Inject
	private DbDao dbDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@GET
	@Path("reloadDefinitions")
	public void reloadDefinitions() {
		LOGGER.info("Called reloadDefinitions from WS port");
		eventService.fire(new LoadAllDefinitions(true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@GET
	@Path("reloadTemplates")
	public void reloadTemplates() {
		LOGGER.info("Called reloadTemplates from WS port");
		eventService.fire(new LoadTemplates());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@GET
	@Path("reloadSemanticDefinitions")
	public void reloadSemanticDefinitions() {
		LOGGER.info("Called reloadSemanticDefinitions from WS port");
		eventService.fire(new LoadSemanticDefinitions());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@GET
	@Path("resetCodelists")
	public void resetCodelists() {
		LOGGER.info("Called resetCodelists from WS port");
		eventService.fire(new ResetCodelistEvent());

		eventService.fire(new LoadCodelists());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@GET
	@Path("clearInternalCache")
	public void clearInternalCache() {
		LOGGER.info("Called clearInternalCache from WS port");
		Set<String> activeCaches = cacheContext.getActiveCaches();
		for (String cacheName : activeCaches) {
			if (cacheName.toLowerCase().contains("entity")) {
				LOGGER.info("Clearing " + cacheName);
				EntityLookupCache<Serializable, Object, Serializable> cache = cacheContext
						.getCache(cacheName);
				cache.clear();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@GET
	@Path("clearDefinitionsCache")
	public void clearDefinitionsCache() {
		LOGGER.info("Called clearDefinitionsCache from WS port");
		Set<String> activeCaches = cacheContext.getActiveCaches();
		for (String cacheName : activeCaches) {
			if (cacheName.toLowerCase().contains("definition")) {
				LOGGER.info("Clearing " + cacheName);
				EntityLookupCache<Serializable, Object, Serializable> cache = cacheContext
						.getCache(cacheName);
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
			return buildResponse(Status.NOT_FOUND, "{}");
		}
		EntityLookupCache<Serializable, Object, Serializable> cache = cacheContext
				.getCache(cacheName);
		JSONObject object = new JSONObject();
		buildCacheStatistics(object, cacheName, cache);
		if (cache != null) {
			cache.clear();
		}
		return buildResponse(Status.OK, object.toString());
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
			EntityLookupCache<Serializable, Object, Serializable> cache = cacheContext
					.getCache(cacheName);
			buildCacheStatistics(jsonObject, cacheName, cache);
		}
		return buildResponse(Status.OK, jsonObject.toString());
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
	private void buildCacheStatistics(JSONObject jsonObject, String cacheName,
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

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	@GET
	@Path("definition")
	@Produces({ "application/xml" })
	public String getDefinition(@QueryParam("type") String type, @QueryParam("id") String id) {
		if (StringUtils.isNullOrEmpty(type)) {
			return "<error>Missing required argument: type -> possible values: casedefinition, workflowdefinition, etc</error>";
		}

		DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(type
				.toLowerCase());
		if (typeDefinition == null) {
			return "<error>Invalid type: \"" + type
					+ "\". Possible values: casedefinition, workflowdefinition, etc</error>";
		}
		String className = typeDefinition.getJavaClassName();
		DefinitionModel model = null;
		try {
			Class<? extends DefinitionModel> forName = (Class<? extends DefinitionModel>) Class
					.forName(className);
			if (StringUtils.isNullOrEmpty(id)) {
				List<? extends DefinitionModel> definitions = dictionaryService
						.getAllDefinitions(forName);
				StringBuilder builder = new StringBuilder();
				builder.append("<definitions>");
				for (DefinitionModel definitionModel : definitions) {
					builder.append("\n\t<definition revision=\"")
							.append(definitionModel.getRevision()).append("\" >")
							.append(definitionModel.getIdentifier()).append("</definition>");
				}
				builder.append("</definitions>");
				return builder.toString();
			}

			model = dictionaryService.getDefinition(forName, id);
		} catch (Exception e) {
			return "<error>Failed to retrive definition with type: " + type + " and id " + id
					+ ": " + e.getMessage() + "</error>";
		}
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
					String type = JsonUtil.getStringValue(jsonObject, "type");
					String id = JsonUtil.getStringValue(jsonObject, "id");
					if (type == null) {
						JsonUtil.addToJson(jsonObject, "message", "Missing type");
						JsonUtil.addToJson(jsonObject, "exist", Boolean.FALSE);
						continue;
					}
					if (id == null) {
						JsonUtil.addToJson(jsonObject, "message", "Missing definition id");
						JsonUtil.addToJson(jsonObject, "exist", Boolean.FALSE);
						continue;
					}
					DataTypeDefinition typeDefinition = dictionaryService
							.getDataTypeDefinition(type);
					if (typeDefinition == null) {
						JsonUtil.addToJson(jsonObject, "message", "Invalid type");
						JsonUtil.addToJson(jsonObject, "exist", Boolean.FALSE);
						continue;
					}
					Class typeClass = typeDefinition.getJavaClass();
					if (typeClass == DocumentInstance.class) {
						typeClass = DocumentDefinitionTemplate.class;
					}
					DefinitionModel definition = dictionaryService.getDefinition(typeClass, id);
					JsonUtil.addToJson(jsonObject, "exist", definition != null);
				}
			} catch (JSONException e) {
				return Response.serverError().entity(e).build();
			}
		}

		return Response.ok(object.toString()).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@GET
	@Path("migrate")
	public Response migrateDefinitions(@DefaultValue("all") @QueryParam("type") String type,
			@DefaultValue("all") @QueryParam("definition") String definitionIds) {

		Class<?> targetClass = null;
		if (!"all".equals(type)) {
			DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(type
					.toLowerCase());
			if (definition == null) {
				return buildBadRequestResponse("Invalid object type: " + type);
			}
			targetClass = definition.getJavaClass();
		}

		String[] ids = new String[0];
		if (!"all".equals(definitionIds)) {
			ids = definitionIds.split("\\s*,\\s*");
		}
		int totalCount = 0;
		for (DefinitionAccessor accessor : accessors) {
			if ((targetClass == null) || accessor.getSupportedObjects().contains(targetClass)) {
				totalCount += accessor.updateDefinitionRevisionToMaxVersion(ids);
			}
		}

		if (totalCount > 0) {
			// clear all cache because the updated instances are out of date with database
			clearInternalCache();
		}

		JSONObject response = new JSONObject();
		JsonUtil.addToJson(response, "success", totalCount > 0);
		return buildResponse(Status.OK, response.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@GET
	@Path("migrate/instances")
	@Secure(runAsSystem = true)
	public Response migrateInstances(@QueryParam("type") String type, @QueryParam("id") String id,
			@QueryParam("threads") @DefaultValue("5") Integer threads,
			@DefaultValue("200") @QueryParam("batch") Integer batchSize,
			@QueryParam("query") String query,
			@DefaultValue(SearchDialects.SOLR) @QueryParam("dialect") String dialect) {

		Resource currentUser = getCurrentUser();
		if ((currentUser == null) || SecurityContextManager.isSystemUser((User) currentUser)) {
			JSONObject response = new JSONObject();
			JsonUtil.addToJson(response, "message",
					"Error: logged in user is required! Log in first before executing migration script.");
			return buildBadRequestResponse(response.toString());
		}

		List<Instance> instances = null;
		int processed = 0;
		int iteration = 0;
		Integer executionThreads = threads;
		List<Instance> failed = new Vector<Instance>();
		do {
			if (StringUtils.isNotNullOrEmpty(type) && StringUtils.isNotNullOrEmpty(id)) {
				instances = loadInstancesByIds(type, id);
				executionThreads = Integer.valueOf(1);
			} else if (StringUtils.isNotNullOrEmpty(query)) {
				instances = loadInstancesViaQuery(query, dialect, batchSize.intValue()
						* executionThreads.intValue(), ++iteration);
			} else if (StringUtils.isNotNullOrEmpty(type)) {
				String typeQuery = getQueryForType(type);
				instances = loadInstancesViaQuery(typeQuery, dialect, batchSize.intValue()
						* executionThreads.intValue(), ++iteration);
			}

			processed += updateInstances(instances, executionThreads, failed);
			if (id != null) {
				break;
			}
		} while ((instances != null) && !instances.isEmpty());

		JSONObject response = new JSONObject();
		// try to process the failed instances again after refresh
		if (!failed.isEmpty()) {
			LOGGER.info(
					"Failed migration of {} instances due to StaleDataModificationException. Will try to process them again. ",
					failed.size());
			List<Instance> secondPassFailed = new LinkedList<Instance>();
			updateInstances(reloadInstances(failed), secondPassFailed);

			if (!secondPassFailed.isEmpty()) {
				LOGGER.warn("{} instances still failing", secondPassFailed.size());
				JSONArray array = new JSONArray();
				for (Instance instance : secondPassFailed) {
					JSONObject object = new JSONObject();
					JsonUtil.addToJson(object, "id", instance.getId());
					JsonUtil.addToJson(object, "type", instance.getClass().getSimpleName()
							.toLowerCase());
					array.put(object);
				}
				JsonUtil.addToJson(response, "failed", array);
			}
		}

		JsonUtil.addToJson(response, "updated", processed);
		return buildResponse(Status.OK, response.toString());
	}

	/**
	 * Gets the query for type.
	 * 
	 * @param type
	 *            the type
	 * @return the query for type
	 */
	private String getQueryForType(String type) {
		DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(type);
		if (definition != null && definition.getFirstUri() != null) {
			return "rdfType:\"" + definition.getFirstUri() + "\"";
		} else {
			LOGGER.warn("Type {} not supported!", type);
		}
		return null;
	}

	/**
	 * Update instances.
	 * 
	 * @param instances
	 *            the instances
	 * @param threads
	 *            the threads
	 * @param failed
	 *            the failed
	 * @return the int
	 */
	private int updateInstances(List<Instance> instances, Integer threads,
			final List<Instance> failed) {
		if ((instances == null) || instances.isEmpty()) {
			return 0;
		}
		if (threads.intValue() <= 1) {
			updateInstances(instances, failed);
		} else {

			List<GenericAsyncTask> tasks = new ArrayList<GenericAsyncTask>(threads);
			int batch = instances.size() / threads;
			for (int i = 0; i < threads; i++) {
				final List<Instance> toProcess = instances.subList(i * batch,
						Math.min((i + 1) * batch, instances.size()));
				tasks.add(new GenericAsyncTask() {
					/**
					 * Comment for serialVersionUID.
					 */
					private static final long serialVersionUID = 8555579642152065117L;

					@Override
					protected boolean executeTask() throws Exception {
						updateInstances(toProcess, failed);
						return true;
					}
				});
			}
			executor.execute(tasks);
		}

		return instances.size();
	}

	/**
	 * Update instances.
	 * 
	 * @param instances
	 *            the instances
	 * @param failed
	 *            the failed
	 */
	private void updateInstances(final List<Instance> instances, final List<Instance> failed) {
		dbDao.invokeInNewTx(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
				RuntimeConfiguration.enable(RuntimeConfigurationProperties.DISABLE_AUDIT_LOG);
				RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
				RuntimeConfiguration.enable(RuntimeConfigurationProperties.OVERRIDE_MODIFIER_INFO);

				try {
					for (Instance instance : instances) {
						try {
							instanceService.save(instance, EDIT_DETAILS);
						} catch (StaleDataModificationException e) {
							failed.add(instance);
							String string = "Failed to save instance of type [{}] and id [{}]";
							LOGGER.warn(string, instance.getClass().getSimpleName().toLowerCase(),
									instance.getId());
							LOGGER.trace(string, instance.getClass().getSimpleName().toLowerCase(),
									instance.getId(), e);
						}
					}
				} finally {
					RuntimeConfiguration
							.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
					RuntimeConfiguration.disable(RuntimeConfigurationProperties.DISABLE_AUDIT_LOG);
					RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
					RuntimeConfiguration
							.disable(RuntimeConfigurationProperties.OVERRIDE_MODIFIER_INFO);
				}
				return null;
			}
		});

	}

	/**
	 * Load instances via query.
	 * 
	 * @param query
	 *            the query
	 * @param dialect
	 *            the dialect
	 * @param batchSize
	 *            the batch size
	 * @param iteration
	 *            the iteration
	 * @return the list
	 */
	private List<Instance> loadInstancesViaQuery(String query, String dialect, int batchSize,
			int iteration) {
		if (query == null) {
			return Collections.emptyList();
		}
		SearchArguments<Instance> arguments = new SearchArguments<Instance>();
		arguments.setDialect(dialect);
		arguments.setStringQuery(query);
		arguments.setSparqlQuery(SearchDialects.SPARQL.equals(dialect.toLowerCase()));
		arguments.setPageSize(batchSize);
		arguments.setPageNumber(iteration);

		LOGGER.debug("Executing instance search with {} query {}", dialect, query);
		searchService.search(SearchInstance.class, arguments);

		List<Instance> list = arguments.getResult();
		if ((list == null) || list.isEmpty()) {
			LOGGER.debug("{} query does not return anything: {}", dialect, query);
			return Collections.emptyList();
		}

		return reloadInstances(list);
	}

	/**
	 * Reload instances.
	 * 
	 * @param list
	 *            the list
	 * @return the list
	 */
	private List<Instance> reloadInstances(List<Instance> list) {
		// load the actual instances from the proper db source
		List<InstanceReference> references = new ArrayList<InstanceReference>(list.size());
		for (Instance instance : list) {
			references.add(instance.toReference());
		}

		return BatchEntityLoader.loadFromReferences(references, serviceRegister, executor);
	}

	/**
	 * Load instances by ids.
	 * 
	 * @param type
	 *            the type
	 * @param ids
	 *            the ids
	 * @return the list
	 */
	private List<Instance> loadInstancesByIds(String type, String ids) {
		DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(type);
		if (typeDefinition == null) {
			LOGGER.warn("Invalid type [{}] passed", type);
			return Collections.emptyList();
		}


		InstanceService<Instance, DefinitionModel> service = serviceRegister
				.getInstanceService(typeDefinition.getJavaClass());
		if (service == null) {
			LOGGER.warn("Instance type [{}] not supported. No Instance service found!", type);
			return Collections.emptyList();
		}
		String[] split = ids.split(",|;");
		LOGGER.debug("Loading instances for type [{}] and ids [{}]", type, ids);
		return service.loadByDbId(Arrays.asList(split));
	}

}
