package com.sirma.itt.seip.resources;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.event.ItemsFilterBinding;
import com.sirma.itt.seip.domain.event.LoadItemsEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.event.ResourceSynchronizationRequredEvent;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.security.annotation.RunAsSystem;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Rest service for accessing and managing resource instances via rest service.
 *
 * @author BBonev
 */
@Api
@Transactional
@ApplicationScoped
@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public class ResourcesRestService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String KEYWORDS = "keywords";
	private static final String FILTERNAME = "filtername";
	private static final String SORTING_FIELD = "sortingField";

	@Inject
	private EventService eventService;

	@Inject
	private Event<LoadItemsEvent> loadItemsEvent;

	@Inject
	private ResourceService resourceService;

	@Inject
	private ResourceSorter resourceSorter;

	@Inject
	private TypeConverter typeConverter;

	/**
	 * Load emf resources by type if provided or all resources instead. Optionally resources can be filtered by filter
	 * and keywords.
	 *
	 * @param type
	 *            the type
	 * @param filtername
	 *            the filtername
	 * @param keywords
	 *            the keywords
	 * @param sortingField
	 *            is the field to sort the desired resources with
	 * @param active
	 *            shows if the resources should be filtered or not
	 * @return the response
	 */
	@GET
	@Path("/{type}")
	@ApiOperation(value = "Retrieves resources of specific type. Optionally, filtered and sorted.", response = Resource.class, responseContainer = "List")
	@ApiResponses(@ApiResponse(code = 400, message = "Unknown resource type"))
	@SuppressWarnings("unchecked")
	public Response load(
			@ApiParam(allowableValues = "user,group", value = "Type of the resources") @PathParam("type") String type,
			@QueryParam(FILTERNAME) String filtername, @QueryParam(KEYWORDS) String keywords,
			@QueryParam(SORTING_FIELD) String sortingField,
			@DefaultValue("false") @QueryParam("active") boolean active) {

		ResourceType resourceType = ResourceType.getByType(type);
		if (resourceType == ResourceType.UNKNOWN) {
			return RestUtil.buildResponse(Status.BAD_REQUEST, "[]");
		}

		List<Resource> resources = null;

		Map<String, Object> keywordsMap = getKeywords(keywords);
		if (StringUtils.isBlank(filtername) && keywordsMap.isEmpty()) {
			resources = fetchResources(resourceType, sortingField, active);
		} else {
			LoadItemsEvent event = fireEvent(filtername, keywordsMap, type, sortingField);
			if (event.isHandled()) {
				resources = (List<Resource>) event.getItems();
			} else {
				resources = fetchResources(resourceType, sortingField, active);
			}
		}
		JSONArray resourcesJson = resourcesToJsonArray(resources);
		return RestUtil.buildResponse(Status.OK, resourcesJson.toString());
	}

	/**
	 * Fetch resources by type.
	 *
	 * @param resourceType
	 *            the resource type
	 * @param sortingField
	 *            the sorting field
	 * @param onlyActive
	 *            shows if the resources should be filtered or not
	 * @return list of resources
	 */
	private List<Resource> fetchResources(ResourceType resourceType, String sortingField, boolean onlyActive) {
		if (onlyActive) {
			return getActiveResourcesByType(resourceType, sortingField);
		}
		return getResourcesByType(resourceType, sortingField);
	}

	/**
	 * Gets active resources. Primary used to filter the users, which are deactivated.
	 *
	 * @param resourceType
	 *            the type of the resource(user, group or all)
	 * @param sortingField
	 *            the sorting field
	 * @return list of active resources of the passed type, if [type=all] returns list of all active users and groups
	 */
	private List<Resource> getActiveResourcesByType(ResourceType resourceType, String sortingField) {
		if (resourceType == ResourceType.ALL) {
			List<Resource> resources = new LinkedList<>();

			List<Resource> users = resourceService.getAllActiveResources(ResourceType.USER, sortingField);
			resourceSorter.sort(users);
			List<Resource> groups = resourceService.getAllActiveResources(ResourceType.GROUP, sortingField);
			resourceSorter.sort(groups);

			resources.addAll(users);
			resources.addAll(groups);
			return resources;
		}
		List<Resource> resources = resourceService.getAllActiveResources(resourceType, sortingField);
		resourceSorter.sort(resources);
		return resources;
	}

	/**
	 * Gets resources for the system no matter if they are active or inactive(deactivated).
	 *
	 * @param resourceType
	 *            the type of the resource(user, group or all)
	 * @param sortingField
	 *            the sorting field
	 * @return list of resources of the passed type, if [type=all] returns list of all users and groups
	 */
	private List<Resource> getResourcesByType(ResourceType resourceType, String sortingField) {
		if (resourceType == ResourceType.ALL) {
			List<Resource> resources = new LinkedList<>();

			List<Resource> users = resourceService.getAllResources(ResourceType.USER, sortingField);
			resourceSorter.sort(users);
			List<Resource> groups = resourceService.getAllResources(ResourceType.GROUP, sortingField);
			resourceSorter.sort(groups);

			resources.addAll(users);
			resources.addAll(groups);
			return resources;
		}
		List<Resource> resources = resourceService.getAllResources(resourceType, sortingField);
		resourceSorter.sort(resources);
		return resources;
	}

	/**
	 * Converts resources list to json array.
	 *
	 * @param resources
	 *            the resources
	 * @return the jSON array
	 */
	private JSONArray resourcesToJsonArray(List<Resource> resources) {
		Collection<JSONObject> collection = typeConverter.convert(JSONObject.class, resources);
		return new JSONArray(collection);
	}

	/**
	 * Builds a keywords map for resource filtering if provided.
	 *
	 * @param keywordsJson
	 *            the keywords json
	 * @return the keywords
	 */
	private static Map<String, Object> getKeywords(String keywordsJson) {
		JSONObject keywordsObject = JsonUtil.createObjectFromString(keywordsJson);
		Map<String, Object> keywordsMap = Collections.emptyMap();
		if (keywordsObject != null) {
			keywordsMap = JsonUtil.jsonToMap(keywordsObject);
		}
		return keywordsMap;
	}

	/**
	 * Fire event for populating and filtering of the items.
	 *
	 * @param filterName
	 *            the filter name
	 * @param keywords
	 *            the keywords
	 * @param type
	 *            Resource type
	 * @param sortingField
	 *            by which field resources should be sorted
	 * @return the load items event
	 */
	protected LoadItemsEvent fireEvent(String filterName, Map<String, Object> keywords, String type,
			String sortingField) {
		LoadItemsEvent event = new LoadItemsEvent();
		if (StringUtils.isNotBlank(filterName)) {
			event.setType(type);
			event.setSortBy(sortingField);
			if (keywords != null) {
				event.setKeywords(keywords);
			}
			ItemsFilterBinding itemsFilterBinding = new ItemsFilterBinding(filterName);
			loadItemsEvent.select(itemsFilterBinding).fire(event);
			LOGGER.debug("Fired LoadItemsEvent with filterName[{}] with keywords[{}]", filterName, keywords);
		}
		return event;
	}

	/**
	 * Refresh.
	 *
	 * @param allowDelete
	 *            the allow delete
	 */
	@GET
	@Path("/refresh")
	@RunAsSystem
	public void refresh(@DefaultValue("false") @QueryParam("allowDelete") Boolean allowDelete) {
		eventService.fire(new ResourceSynchronizationRequredEvent(allowDelete));
	}

	/**
	 * Gets the resources.
	 *
	 * @param type
	 *            the type
	 * @param active
	 *            shows if the resources should be filtered or not
	 * @return the resources
	 */
	@GET
	@Path("/get/{param}")
	public String getResources(@PathParam("param") String type, @QueryParam("active") boolean active) {
		if (StringUtils.isBlank(type)) {
			return "[]";
		}
		ResourceType resourceType = ResourceType.valueOf(type.toUpperCase());
		List<Resource> result = fetchResources(resourceType, null, active);
		return resourcesToJsonArray(result).toString();
	}
}
