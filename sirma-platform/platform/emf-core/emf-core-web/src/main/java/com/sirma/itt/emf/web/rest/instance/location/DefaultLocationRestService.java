package com.sirma.itt.emf.web.rest.instance.location;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.location.InstanceDefaultLocationService;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Contains services for processing default locations instances for instances created with specific definition. There
 * are methods for creating, updating, removing and retrieving the default location for the given definition. For now
 * the default locations are only project instances. Most of the methods in this service can only be executed by user
 * with admin permissions.
 *
 * @author A. Kunchev
 */
@Path("/default-locations")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DefaultLocationRestService extends EmfRestService {

	private static final String PERMISSIONS_ERROR = "emf.rest.permissions.error";

	private static final String LOCATIONS = "locations";

	private static final String IS_DEFAULT_LOCATION = "isDefaultLocation";

	private static final String COMMON_INSTANCE = CommonInstance.class.getSimpleName().toLowerCase();

	@Inject
	private InstanceDefaultLocationService defaultLocationService;

	@Inject
	private DatabaseIdManager databaseIdManager;

	/**
	 * Sets default locations(default parent instance) for given definition. The data is passed as JSON array of objects
	 * containing the id of the definition, id and type of the default instance, which will be mapped to the definition.
	 * <p>
	 * Request data example:
	 *
	 * <pre>
	 * [{
	 * 	 definitionId   : document-definitionId,
	 * 	 instanceId     : locationId,
	 *   instanceType   : locationType
	 * },
	 * {
	 * 	 definitionId   : document-definitionId,
	 * 	 instanceId     : locationId,
	 *   instanceType   : locationType
	 * }, ...]
	 * </pre>
	 *
	 * @param data
	 *            contains the information about default locations mapping
	 * @return OK response, if the operations is executed successfully
	 * @throws RestServiceException
	 *             when the user is not admin or don't have admin permissions and when there is a problem with the
	 *             request data parsing
	 */
	@POST
	public Response setDefaultLocations(String data) {
		if (!authorityService.isAdminOrSystemUser()) {
			throw new RestServiceException(labelProvider.getValue(PERMISSIONS_ERROR), Status.UNAUTHORIZED);
		}

		try {
			Map<InstanceReference, InstanceReference> defaultLocations = getDefaultLocationFromRequest(data);
			defaultLocationService.addDefaultLocations(defaultLocations);
			return buildOkResponse(null);
		} catch (JSONException e) {
			throw new RestServiceException("Cannot parse data! " + e.getLocalizedMessage(), Status.BAD_REQUEST, e);
		}
	}

	/**
	 * Updates default location mapping. Accepts JSON array of objects, which contains definition id and instance
	 * location id and type, which will be mapped to the definition.
	 * <p>
	 * Request data example:
	 *
	 * <pre>
	 * [{
	 * 	 definitionId   : document-definitionId,
	 * 	 instanceId     : locationId,
	 *   instanceType   : locationType
	 * },
	 * {
	 * 	 definitionId   : document-definitionId,
	 * 	 instanceId     : locationId,
	 *   instanceType   : locationType
	 * }, ...]
	 * </pre>
	 *
	 * @param data
	 *            contains the information about default locations mapping
	 * @return OK response, if the operations is executed successfully. Note that this service will return OK, no matter
	 *         the success of the update operation
	 * @throws RestServiceException
	 *             when the user is not admin or don't have admin permissions and when there is a problem with the
	 *             request data parsing
	 */
	@PUT
	public Response updateDefaultLocations(String data) {
		if (!authorityService.isAdminOrSystemUser()) {
			throw new RestServiceException(labelProvider.getValue(PERMISSIONS_ERROR), Status.UNAUTHORIZED);
		}

		try {
			Map<InstanceReference, InstanceReference> defaultLocations = getDefaultLocationFromRequest(data);
			defaultLocationService.updateDefaultLocations(defaultLocations);
			return buildOkResponse(null);
		} catch (JSONException e) {
			throw new RestServiceException("Cannot parse data or the passed data is empty!", Status.BAD_REQUEST, e);
		}
	}

	/**
	 * Builds map with the default location and their corresponding instance from given JSONArray. The array contains
	 * object with definition id for the definition reference and id and type of the instance which will be default for
	 * the definition.
	 *
	 * @param data
	 *            JSON array as string, which contains the definitions and their default locations
	 * @return Map with the definition references keys and instance references and values
	 * @throws JSONException
	 *             when the passed String is null or empty or when the passed string is resolved to empty array.
	 */
	private Map<InstanceReference, InstanceReference> getDefaultLocationFromRequest(String data) throws JSONException {
		if (StringUtils.isNullOrEmpty(data)) {
			throw new JSONException("Empty or null data!");
		}

		JSONArray requestParams = new JSONArray(data);

		if (JsonUtil.isNullOrEmpty(requestParams)) {
			throw new JSONException("Passed empty array.");
		}

		return JsonUtil.toJsonObjectStream(requestParams).collect(
				Collectors.toMap(getDefinitionReferenceFromJSON(), getInstanceReferenceFromJSON()));
	}

	/**
	 * Builds function, when executed returns instance reference for the semantic definition (Example: instance
	 * reference with id:'document-OT210027' and type:'commoninstance').
	 *
	 * @return Function which returns as result instance reference for the semantic definition
	 */
	private Function<JSONObject, InstanceReference> getDefinitionReferenceFromJSON() {
		return json -> {
			String definitionId = JsonUtil.getStringValue(json, DEFINITION_ID);
			definitionId = (String) databaseIdManager.getValidId(definitionId);
			return getInstanceReference(definitionId, COMMON_INSTANCE);
		};
	}

	/**
	 * Builds function, when executed returns instance reference for the passed instance in the JSON. Uses instanceType
	 * and instanceId to build the reference.
	 *
	 * @return Function which returns as result instance reference for the passed instance id and type in the JSON
	 */
	private Function<JSONObject, InstanceReference> getInstanceReferenceFromJSON() {
		return json -> typeConverter.convert(InstanceReference.class, json);
	}

	/**
	 * Retrieves the default location(if any) for given definition id, plus project instances for which the user have
	 * permissions and can upload in them.
	 *
	 * <pre>
	 * Example URL:
	 * <code>
	 *  /default-locations/document-OT210027
	 * </code>
	 * </pre>
	 * <p>
	 * Response example:
	 *
	 * <pre>
	 * [{
	 * 	 instanceId     : document-locationId,
	 *   instanceType   : locationType,
	 *   header         : compactHeader
	 * },
	 * {
	 * 	 instanceId     : document-locationId,
	 *   instanceType   : locationType,
	 *   header         : compactHeader,
	 *   defaultLocation: definitionId
	 * }, ...]
	 * </pre>
	 *
	 * @param definitionId
	 *            the id of the definition for which will be extract default location
	 * @return OK response with default location(if any) plus all project to which the user have permissions
	 */
	@GET
	@Path("/{definitionId}")
	public Response retrieveLocations(@PathParam("definitionId") String definitionId) {
		if (StringUtils.isNullOrEmpty(definitionId)) {
			throw new RestServiceException("Required argument is missing!", Status.BAD_REQUEST);
		}

		String idWithPrefix = (String) databaseIdManager.getValidId(definitionId);

		Collection<? extends Instance> possibleLocations = defaultLocationService.retrieveLocations(idWithPrefix);

		if (possibleLocations.isEmpty()) {
			throw new RestServiceException("No possible locations found!", Status.BAD_REQUEST);
		}

		JSONObject result = buildDefaultLocationsResponse(possibleLocations, idWithPrefix);
		return buildOkResponse(result.toString());
	}

	/**
	 * Builds the response for the {@link #retrieveLocations(String)} method. Builds JSON object, which contains the
	 * projects, to which the current user have permissions. If there is project with default location property and it
	 * is equals to the passed default location, for this project will be added property in the JSON, which shows that
	 * this project is default location for this types of documents.
	 *
	 * @param instances
	 *            the instances, which will be put into the JSON
	 * @param definitionId,
	 *            the definitionId for which is searched default location
	 * @param defaultLocationIds
	 *            set of default location ids for the specific definition
	 * @return JSON object with the found result instances
	 */
	private static JSONObject buildDefaultLocationsResponse(Collection<? extends Instance> instances,
			String definitionId) {
		JSONArray possibleLocations = new JSONArray();
		for (Instance instance : instances) {
			JSONObject possibleLocation = new JSONObject();
			JsonUtil.addToJson(possibleLocation, INSTANCE_ID, instance.getId());
			JsonUtil.addToJson(possibleLocation, INSTANCE_TYPE, instance.type().getCategory());
			JsonUtil.addToJson(possibleLocation, HEADER, instance.getString(DefaultProperties.HEADER_COMPACT));
			if (instance.isPropertyPresent(IS_DEFAULT_LOCATION)) {
				JsonUtil.addToJson(possibleLocation, IS_DEFAULT_LOCATION, definitionId);
			}
			possibleLocations.put(possibleLocation);
		}

		JSONObject result = new JSONObject();
		JsonUtil.addToJson(result, LOCATIONS, possibleLocations);
		return result;
	}

	/**
	 * Removes default location for given definition ids.
	 *
	 * <pre>
	 * ["document-definitionId", "case-definitionId", "object-definitionId", ...]
	 * </pre>
	 *
	 * @param data
	 *            contains the definition ids for which the default locations will be removed
	 * @return OK response. Note, this service will return OK, no matter the internal operation success
	 */
	@DELETE
	public Response removeDefaultLocations(String data) {
		if (!authorityService.isAdminOrSystemUser()) {
			throw new RestServiceException(labelProvider.getValue(PERMISSIONS_ERROR), Status.UNAUTHORIZED);
		}

		if (StringUtils.isNullOrEmpty(data)) {
			throw new RestServiceException("Passed data is empty!", Status.BAD_REQUEST);
		}

		try {
			JSONArray definitionIds = new JSONArray(data);
			if (JsonUtil.isNullOrEmpty(definitionIds)) {
				throw new RestServiceException("Definition types array is empty!", Status.BAD_REQUEST);
			}

			Collection<InstanceReference> definitionInstanceReferences = JsonUtil
					.toStringStream(definitionIds)
						.map(v -> getInstanceReference(v, COMMON_INSTANCE))
						.collect(Collectors.toList());

			defaultLocationService.removeDefaultLocations(definitionInstanceReferences);
			return buildOkResponse(null);
		} catch (JSONException e) {
			throw new RestServiceException("Cannot parse data!", Status.BAD_REQUEST, e);
		}
	}

}
