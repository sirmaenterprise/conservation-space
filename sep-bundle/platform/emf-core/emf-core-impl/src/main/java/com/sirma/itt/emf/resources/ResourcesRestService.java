package com.sirma.itt.emf.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
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

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.resources.event.GroupSync;
import com.sirma.itt.emf.resources.event.ItemsFilterBinding;
import com.sirma.itt.emf.resources.event.LoadItemsEvent;
import com.sirma.itt.emf.resources.event.PeopleSync;
import com.sirma.itt.emf.resources.event.ResourceSynchronizationRequredEvent;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Rest service for accessing and managint resource instances via rest service
 *
 * @author BBonev
 */
@ApplicationScoped
@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public class ResourcesRestService extends EmfRestService {

	/** The event service. */
	@Inject
	private EventService eventService;

	@Inject
	private Event<LoadItemsEvent> loadItemsEvent;

	/**
	 * Load emf resources by type if provided or all resources instead. Optionally resources can be
	 * filtered by filter and keywords.
	 *
	 * @param type
	 *            the type
	 * @param filtername
	 *            the filtername
	 * @param keywords
	 *            the keywords
	 * @return the response
	 */
	@GET
	@Path("/{type}")
	public Response load(@PathParam("type") String type,
			@QueryParam("filtername") String filtername, @QueryParam("keywords") String keywords) {
		log.debug("Loading resources of type[{}] filter by[{}] with keywords[{}]", type,
				filtername, keywords);
		List<Resource> resources = new LinkedList<>();
		if (type == null) {
			return buildResponse(Status.OK, "[]");
		}
		JSONObject keywordsObject = JsonUtil.createObjectFromString(keywords);
		Map<String, Object> keywordsMap = null;
		if (keywordsObject == null) {
			keywordsMap = new HashMap<String, Object>();
		} else {
			keywordsMap = JsonUtil.jsonToMap(keywordsObject);
		}
		LoadItemsEvent event = fireEvent(filtername, keywordsMap);
		boolean loadAll = true;
		if ((event.getItems() != null) && event.isHandled()) {
			loadAll = false;
		}
		JSONArray resourcesJson = null;
		if (loadAll) {
			log.debug(
					"LoadItemsEvent was not handled or no filters were provided! All items of type[{}] will be loaded instead.",
					type);

			ResourceType resourceType = ResourceType.valueOf(type.toUpperCase());
			if (resourceType == ResourceType.ALL) {
				resources.addAll(resourceService.getAllResources(ResourceType.USER, null));
				resources.addAll(resourceService.getAllResources(ResourceType.GROUP, null));
			} else {
				resources = resourceService.getAllResources(resourceType, null);
			}
			Collection<JSONObject> collection = typeConverter.convert(JSONObject.class, resources);
			resourcesJson = new JSONArray(collection);
		} else {
			resourcesJson = new JSONArray(event.getItems());
			log.debug("Handled LoadItemsEvent");
		}
		log.debug("Resource items found [{}]", resourcesJson.length());
		return buildResponse(Status.OK, resourcesJson.toString());
	}

	/**
	 * Fire event for populating and filtering of the items.
	 *
	 * @param filterName
	 *            the filter name
	 * @param keywords
	 *            the keywords
	 * @return the load items event
	 */
	protected LoadItemsEvent fireEvent(String filterName, Map<String, Object> keywords) {
		LoadItemsEvent event = new LoadItemsEvent();
		if (StringUtils.isNotNullOrEmpty(filterName)) {
			if (keywords != null) {
				event.setKeywords(keywords);
			}
			ItemsFilterBinding itemsFilterBinding = new ItemsFilterBinding(filterName);
			loadItemsEvent.select(itemsFilterBinding).fire(event);
			log.debug("Fired LoadItemsEvent with filterName[{}] with keywords[{}]", filterName,
					keywords);
		}

		return event;
	}

	/**
	 * Refresh.
	 *
	 * @param force
	 *            the force
	 */
	@GET
	@Path("/refresh")
	@Secure(runAsSystem = true)
	public void refresh(@DefaultValue("false") @QueryParam("force") Boolean force) {
		eventService.fire(new ResourceSynchronizationRequredEvent(force));
	}

	/**
	 * Refresh users or groups from DMS the resources are also updated in semantic DB.
	 * 
	 * @param type
	 *            the type
	 */
	@GET
	@Path("/refresh/{type}")
	@Secure(runAsSystem = true)
	public void refreshFromProvider(@PathParam("type") String type) {
		if (StringUtils.isNullOrEmpty(type)) {
			return;
		}
		boolean all = "all".equalsIgnoreCase(type);
		if (all || "user".equalsIgnoreCase(type)) {
			eventService.fire(new PeopleSync());
		}
		if (all || "group".equalsIgnoreCase(type)) {
			eventService.fire(new GroupSync());
		}
	}

	/**
	 * Gets the resources.
	 *
	 * @param type
	 *            the type
	 * @return the resources
	 */
	@GET
	@Path("/get/{param}")
	public String getResources(@PathParam("param") String type) {
		List<Resource> result = null;
		if (type == null) {
			return "[]";
		}
		ResourceType resourceType = ResourceType.valueOf(type.toUpperCase());
		result = resourceService.getAllResources(resourceType, null);
		Collection<JSONObject> collection = typeConverter.convert(JSONObject.class, result);
		JSONArray array = new JSONArray(collection);
		return array.toString();
	}
}
