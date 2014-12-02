package com.sirma.itt.pm.web.project.members;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.cmf.web.entity.bookmark.BookmarkUtil;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.event.AuditableEvent;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.RoleService;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmSecurityModel;
import com.sirma.itt.pm.services.ProjectService;

/**
 * A controller for the PM resource manager functionality.
 * 
 * @author svelikov
 */
@Path("/pm/rm")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class ResourceManagerRestService extends EmfRestService {

	/** The Constant PROJECT_ID. */
	private static final String PROJECT_ID = "projectId";

	/** The Constant TYPE. */
	private static final String TYPE = "type";

	/** The Constant SORTING_FIELD. */
	private static final String SORTING_FIELD = "sortingField";

	/** The project service. */
	@Inject
	private ProjectService projectService;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The role service. */
	@Inject
	private RoleService roleService;

	/** The bookmark util. */
	@Inject
	private BookmarkUtil bookmarkUtil;

	@Inject
	private EventService eventService;

	/**
	 * Load all resources of given type.
	 * <p>
	 * 
	 * @REVIEW move the method to ResourceRestService in EMF
	 * @param type
	 *            the type
	 * @param sortingField
	 *            the sorting field
	 * @return response string formatted as json.
	 * @throws Exception
	 *             the exception
	 */
	@Path("loadItems")
	@GET
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response loadItems(@QueryParam(TYPE) String type,
			@QueryParam(SORTING_FIELD) String sortingField) throws Exception {
		if (debug) {
			log.debug("PMWeb: ResourceManagerController.loadItems: type[" + type + "]");
		}

		Response response = null;

		if (StringUtils.isNotNullOrEmpty(type)) {
			ResourceType resourceType = ResourceType.getByType(type);
			List<Resource> allResources;
			if (resourceType != null) {
				allResources = resourceService.getAllResources(resourceType, sortingField);
				if (allResources == null) {
					response = buildResponse(
							Response.Status.INTERNAL_SERVER_ERROR,
							labelProvider
									.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_IN_SERVICE));
				} else {
					response = buildResponse(Response.Status.OK, buildResourceJson(allResources));
				}
			}
		} else {
			response = buildResponse(Response.Status.BAD_REQUEST,
					labelProvider
							.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_TYPE_UNEXPECTED));
		}

		return response;
	}

	/**
	 * Load project items.
	 * 
	 * @param projectId
	 *            the project id
	 * @return the string
	 * @throws JSONException
	 *             the jSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Path("loadProjectItems")
	@GET
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response loadProjectItems(@QueryParam(PROJECT_ID) String projectId)
			throws JSONException, IOException {
		if (debug) {
			log.debug("PMWeb: ResourceManagerController.loadProjectItems for project id["
					+ projectId + "]");
		}

		Response response = null;

		JSONObject responseJson = new JSONObject();
		if (StringUtils.isNotNullOrEmpty(projectId)) {
			ProjectInstance projectInstance = projectService.loadByDbId(projectId);
			if (projectInstance != null) {
				responseJson.put("resources", getProjectResources(projectInstance));
				responseJson.put("roles", getActiveRoles());
				responseJson.put("projectManagerRole", PmSecurityModel.PmRoles.PROJECT_MANAGER);
				response = buildResponse(Response.Status.OK, responseJson.toString());
			} else {
				response = buildResponse(
						Response.Status.INTERNAL_SERVER_ERROR,
						labelProvider
								.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_PROJECT_NOT_FOUND));
			}
		} else {
			response = buildResponse(
					Response.Status.BAD_REQUEST,
					labelProvider
							.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_UNEXPECTED_PROJECTID));
		}

		return response;
	}

	/**
	 * Save selected resource items. <br>
	 * Expected format: {projectId:'1',assignedResources:{1:"projectmanager",2:"consumer"}}
	 * 
	 * @param data
	 *            the data
	 * @return the string
	 * @throws JSONException
	 *             the jSON exception
	 */
	@SuppressWarnings("unchecked")
	@Path("save")
	@POST
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Response saveItems(String data) throws JSONException {
		if (debug) {
			log.debug("PMWeb: ResourceManagerController.saveItems: " + data);
		}

		if (StringUtils.isNotNullOrEmpty(data)) {
			JSONObject request = new JSONObject(data);
			String projectId = JsonUtil.getStringValue(request, "projectId");
			ProjectInstance projectInstance = projectService.loadByDbId(projectId);
			if (projectInstance == null) {
				return buildResponse(
						Response.Status.BAD_REQUEST,
						labelProvider
								.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_PROJECT_NOT_FOUND));
			}

			JSONObject assignedResources = (JSONObject) JsonUtil.getValueOrNull(request,
					"assignedResources");
			if (assignedResources.length() == 0) {
				return buildResponse(
						Response.Status.BAD_REQUEST,
						labelProvider
								.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_REQUIRED_PM_RESOURCE));
			}

			Map<String, RoleIdentifier> resourcesMap = new HashMap<String, RoleIdentifier>();
			for (Iterator<String> iterator = assignedResources.keys(); iterator.hasNext();) {
				String resourceId = iterator.next();
				String rolename = (String) JsonUtil.getValueOrNull(assignedResources, resourceId);
				resourcesMap.put(resourceId, roleService.getRoleIdentifier(rolename));
			}

			resourceService.setResources(resourcesMap, projectInstance);
			JSONObject responseData = new JSONObject();
			String redirectUrl = bookmarkUtil.buildLink(projectInstance);
			responseData.put("redirectUrl", redirectUrl);

			eventService.fire(new AuditableEvent(projectInstance, "managedResources"));

			return buildResponse(Response.Status.OK, responseData.toString());
		}

		return buildResponse(
				Response.Status.BAD_REQUEST,
				labelProvider
						.getValue(ResourceManagerRestServiceLabels.PM_RM_ERROR_INVALID_REQUEST_DATA));
	}

	/**
	 * Gets the role.
	 * 
	 * @param label
	 *            the label
	 * @param value
	 *            the value
	 * @return the role
	 * @throws JSONException
	 *             the jSON exception
	 */
	protected JSONObject getRole(String label, String value) throws JSONException {
		JSONObject role = new JSONObject();
		role.put("label", label);
		role.put("value", value);
		return role;
	}

	/**
	 * Gets the project resources.
	 * 
	 * @param projectInstance
	 *            the project instance
	 * @return the project resources
	 * @throws JSONException
	 *             the jSON exception
	 */
	protected JSONArray getProjectResources(ProjectInstance projectInstance) throws JSONException {
		List<ResourceRole> resourceRoles = resourceService.getResourceRoles(projectInstance);
		JSONArray result = new JSONArray();
		for (ResourceRole resourceRole : resourceRoles) {
			JSONObject converted = typeConverter.convert(JSONObject.class, resourceRole);
			result.put(converted);
		}
		return result;
	}

	/**
	 * Gets the active roles.
	 * 
	 * @return the roles
	 */
	protected Collection<JSONObject> getActiveRoles() {
		List<RoleIdentifier> activeRoles = roleService.getActiveRoles();
		return typeConverter.convert(JSONObject.class, activeRoles);
	}

	/**
	 * Builds the resource json.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param resources
	 *            the resources
	 * @return the string
	 * @throws JSONException
	 *             the jSON exception
	 */
	protected <R extends Resource> String buildResourceJson(List<R> resources) throws JSONException {
		JSONArray result = new JSONArray();
		for (Resource resource : resources) {
			JSONObject converted = typeConverter.convert(JSONObject.class, resource);
			result.put(converted);
		}
		return result.toString();
	}

}
