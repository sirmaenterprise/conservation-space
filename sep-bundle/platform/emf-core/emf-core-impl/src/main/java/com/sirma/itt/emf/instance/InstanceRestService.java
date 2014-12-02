package com.sirma.itt.emf.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Instance service method implementations as rest services.
 *
 * @author svelikov
 */
@Secure
@Path("/instances")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class InstanceRestService extends EmfRestService {

	private static final String MISSING_REQUIRED_ARGUMENTS_FOR_MOVE_OPERATION = "Missing required arguments for move operation!";

	private static final String SOURCE_TYPE = "sourceType";

	private static final String SOURCE_ID = "sourceId";

	private static final String TARGET_TYPE = "targetType";

	private static final String TARGET_ID = "targetId";

	private static final String DETACH_DOCUMENT = "detachDocument";

	private static final String CONTEXT_ID = "contextId";

	private static final String CONTEXT_TYPE = "contextType";

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	/** Implementations of instance move operation. */
	@Inject
	private javax.enterprise.inject.Instance<InstanceMoveAction> instanceMoveAction;

	@Inject
	private javax.enterprise.inject.Instance<InstanceContextInitializer> instanceContextInitializer;

	@Inject
	private LinkService linkService;

	/**
	 * Gets the instance status.
	 *
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @return the instance status
	 */
	@Path("status")
	@GET
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response getInstanceStatus(@QueryParam("instanceId") String instanceId,
			@QueryParam("instanceType") String instanceType) {
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			return buildResponse(Status.BAD_REQUEST,
					"Missing required arguments for instance status check!");
		}
		Instance instance = fetchInstance(instanceId, instanceType);
		if (instance != null) {
			Serializable status = instance.getProperties().get(DefaultProperties.STATUS);
			JSONObject responseData = new JSONObject();
			JsonUtil.addToJson(responseData, "status", status);
			return buildResponse(Status.OK, responseData.toString());
		}
		return buildResponse(Status.INTERNAL_SERVER_ERROR, null);
	}

	/**
	 * Gets the instance properties.
	 *
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @param properties
	 *            the properties to be retrieved
	 * @return the instance properties as a json object with property name as key and property
	 *         values as value
	 */
	@Path("properties")
	@GET
	public Response getInstanceProperties(@QueryParam("instanceId") String instanceId,
			@QueryParam("instanceType") String instanceType,
			@QueryParam("property") String[] properties) {
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			return buildResponse(Status.BAD_REQUEST, "Missing required arguments!");
		}
		Instance instance = fetchInstance(instanceId, instanceType);
		if (instance != null) {
			JSONObject responseData = new JSONObject();
			for (int i = 0; i < properties.length; i++) {
				Serializable value = instance.getProperties().get(properties[i]);
				JsonUtil.addToJson(responseData, properties[i], value);
			}
			return buildResponse(Status.OK, responseData.toString());
		}
		return buildResponse(Status.INTERNAL_SERVER_ERROR, null);
	}

	/**
	 * Exposes detach method as rest service. This service can accept more than one linked instances
	 * that should be detached. If any request data for linked instance is not present or instance
	 * can not be found, then later is skipped from detach operation.
	 *
	 * @param data
	 *            Request data in json format: <code>
	 * {
	 *     targetId: '',
	 *     targetType: '',
	 *     linked: [
	 *         {
	 *             instanceId: '',
	 *             instanceType: ''
	 *         }
	 *     ]
	 * }</code>
	 * @return response
	 */
	@Path("detach")
	@POST
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Response detach(String data) {
		if (trace) {
			log.trace("Detach request: {}", data);
		}
		if (StringUtils.isNullOrEmpty(data)) {
			return buildResponse(Response.Status.BAD_REQUEST,
					"There are missing or wrong request parameters for deactivate relation operation!");
		}

		try {
			JSONObject request = new JSONObject(data);
			String targetInstanceId = JsonUtil.getStringValue(request, TARGET_ID);
			String targetInstanceType = JsonUtil.getStringValue(request, TARGET_TYPE);
			Instance targetInstance = fetchInstance(targetInstanceId, targetInstanceType);
			if (targetInstance == null) {
				return buildResponse(Status.BAD_REQUEST,
						"Missing or wrong target instance id or type for detach operation!");
			}

			JSONArray linkedInstances = JsonUtil.getJsonArray(request, "linked");
			if (linkedInstances == null) {
				return buildResponse(Status.BAD_REQUEST,
						"Missing linked instances array for detach operation!");
			}

			int length = linkedInstances.length();
			List<InstanceReference> items = new ArrayList<>();
			for (int i = 0; i < length; i++) {
				JSONObject current = (JSONObject) linkedInstances.get(i);
				// there is StringToLinkSourceConverter that converts json object
				// { instanceId: '', instanceType: ''} to instance reference
				InstanceReference reference = getTypeConverter().convert(InstanceReference.class,
						current.toString());
				if (reference != null) {
					items.add(reference);
				} else {
					log.debug("Can not detach linked instance [{}]", current);
				}
			}

			if (!items.isEmpty()) {
				Collection<Instance> values = loadInstances(items);
				instanceService.detach(targetInstance, new Operation(DETACH_DOCUMENT),
						values.toArray(new Instance[values.size()]));
			}

		} catch (JSONException e) {
			log.error("Error in parsing request data for detach operation!", e);
		}

		return buildResponse(Response.Status.OK, null);
	}

	/**
	 * Move instance from source to target instance.
	 *
	 * @param data
	 *            Request data in format:<code>
	 * {
	 * 	instanceId: instanceId,
	 * 	instanceType: instanceType,
	 * 	sourceId: sourceId,
	 * 	sourceType: sourceType,
	 * 	targetId: targetId,
	 * 	targetType: targetType
	 * }
	 * </code>
	 * @return the response
	 */
	@Path("move")
	@POST
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Response move(String data) {
		if (debug) {
			log.debug("Move operation request [{}]", data);
		}
		if (StringUtils.isNullOrEmpty(data)) {
			return buildResponse(Response.Status.BAD_REQUEST,
					MISSING_REQUIRED_ARGUMENTS_FOR_MOVE_OPERATION);
		}
		try {
			JSONObject request = new JSONObject(data);
			String instanceId = JsonUtil.getStringValue(request, INSTANCE_ID);
			String instanceType = JsonUtil.getStringValue(request, INSTANCE_TYPE);

			String contextId = JsonUtil.getStringValue(request, CONTEXT_ID);
			String contextType = JsonUtil.getStringValue(request, CONTEXT_TYPE);

			String sourceId = JsonUtil.getStringValue(request, SOURCE_ID);
			String sourceType = JsonUtil.getStringValue(request, SOURCE_TYPE);

			String targetId = JsonUtil.getStringValue(request, TARGET_ID);
			String targetType = JsonUtil.getStringValue(request, TARGET_TYPE);

			if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)
					|| StringUtils.isNullOrEmpty(targetId) || StringUtils.isNullOrEmpty(targetType)) {
				return buildResponse(Response.Status.BAD_REQUEST,
						MISSING_REQUIRED_ARGUMENTS_FOR_MOVE_OPERATION);
			}

			// if source instance is missing (like owning instance for document) we try to restore
			// the instance hierarchy in order to get the owning instance
			Instance sourceInstance = null;
			Instance instanceToMove = fetchInstance(instanceId, instanceType);
			if (StringUtils.isNullOrEmpty(sourceId) || StringUtils.isNullOrEmpty(sourceType)) {
				log.debug(
						"Missing source instance data sourceId[{}] or sourceType[{}]. Trying to restore the context for selected instance!",
						sourceId, sourceType);
				if (!instanceContextInitializer.isUnsatisfied() && (instanceToMove != null)) {
					Instance contextInstance = fetchInstance(contextId, contextType);
					if (contextInstance != null) {
						instanceContextInitializer.get().restoreHierarchy(instanceToMove,
								contextInstance);
						sourceInstance = ((OwnedModel) instanceToMove).getOwningInstance();
					}
				}
			} else {
				sourceInstance = fetchInstance(sourceId, sourceType);
			}

			Instance targetInstance = fetchInstance(targetId, targetType);
			if ((instanceToMove == null) || (targetInstance == null) || (sourceInstance == null)) {
				log.warn("Can't move instance [{}] from [{}] to [{}]!", new Object[] {
						instanceToMove, sourceInstance, targetInstance });
				buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
						"Instance to move, source target or destination target is null. Move operation rejected!");
			}

			// we should not allow moving into same instance
			if ((sourceInstance != null) && (targetInstance != null)
					&& sourceInstance.getId().equals(targetInstance.getId())) {
				return buildResponse(Response.Status.BAD_REQUEST, "Can't move in same section!");
			}

			Class<? extends Instance> instanceClass = getInstanceClass(instanceType);
			InstanceMoveAction moveAction = getInstanceMoveAction(instanceClass);
			if (moveAction != null) {
				boolean moved = moveAction.move(instanceToMove, sourceInstance, targetInstance);
				if (!moved) {
					buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
							"Can not move selected instance!");
				}
			}
		} catch (JSONException e) {
			log.error("Error in parsing request data for move operation!", e);
		}
		return buildResponse(Response.Status.OK, null);
	}

	/**
	 * Gets the instance move action implementation.
	 *
	 * @param instanceClass
	 *            the instance class
	 * @return the instance move action
	 */
	private InstanceMoveAction getInstanceMoveAction(Class<?> instanceClass) {
		for (InstanceMoveAction action : instanceMoveAction) {
			if (action.canHandle(instanceClass)) {
				return action;
			}
		}
		return null;
	}

	/**
	 * Delete.
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	@Path("delete")
	@POST
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Response delete(String data) {
		return buildResponse(Response.Status.OK, null);
	}

	/**
	 * Creates the instance.
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	@POST
	@Path("/")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createInstance(String data) {

		return buildResponse(Status.OK, data);
	}

	/**
	 * Save.
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	@PUT
	@Path("/")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Response save(String data) {

		return buildResponse(Status.OK, data);
	}

	/**
	 * Load.
	 *
	 * @param type
	 *            the type
	 * @param id
	 *            the id
	 * @return the response
	 */
	@GET
	@Path("{type}")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Response load(@PathParam("type") String type, @QueryParam("id") String id) {
		return buildResponse(Status.OK, "{type:" + type + ", id:" + id + "}");
	}

	/**
	 * Removes semantic links for given id. If the id is persisted, none links will be removed, else
	 * making attempt to remove all links, if there is any.
	 *
	 * @param instanceId
	 *            the id of the instance
	 * @param instanceType
	 *            the type of the instance
	 * @return <b>OK response - when</b> the id is persisted(none links removed),<br>
	 *         <b>when</b> the id is not persisted and links are removed<br>
	 *         and <b>when</b> the id is not persisted, but there isn't links.
	 *         <p>
	 *         <b>BAD REQUEST response </b> when any of the given id or type are null or empty.
	 */
	@Path("close")
	@GET
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Response removeSemanticLinks(@QueryParam("instanceId") String instanceId,
			@QueryParam("instanceType") String instanceType) {
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			log.debug("Empty or null instance id or type");
			return buildResponse(Status.BAD_REQUEST, "Missing required arguments.");
		}

		if (!InstanceUtil.isIdPersisted(instanceId)) {
			InstanceReference instanceReference = getInstanceReferense(instanceId, instanceType);
			boolean linkDeleted = linkService.removeLinksFor(instanceReference);
			// if at least one link is removed
			if (linkDeleted) {
				log.debug("All semantic link for id: {} are removed.", instanceId);
				return buildResponse(Status.OK, null);
			}
		}
		// persisted or no links
		log.debug("The given id: {} is persisted оr there is no links to/from it.", instanceId);
		return buildResponse(Status.OK, null);
	}

}
