package com.sirma.itt.emf.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.notification.MessageLevel;
import com.sirma.itt.seip.instance.notification.NotificationMessage;
import com.sirma.itt.seip.instance.notification.NotificationSupport;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Instance service method implementations as rest services.
 *
 * @author svelikov
 */
@Path("/instances")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InstanceRestService extends EmfRestService {

	private static final String IS_MULTY_FIELD_NAME = "multy";
	private static final String EMF_REST_INSTANCE_DETACH_MISSING_LINKED_INSTANCES = "emf.rest.instance.detach.missing_linked_instances";
	private static final String EMF_REST_INSTANCE_DETACH_MISSING_TARGET_ID_OR_TYPE = "emf.rest.instance.detach.missing_target_id_or_type";
	private static final String EMF_REST_INSTANCE_DETACH_MISSING_REQUIRED_ARGUMENTS = "emf.rest.instance.detach.missing_required_arguments";
	private static final String EMF_REST_INSTANCE_STATUS_CHECK_MISSING_REQUIRED_DATA = "emf.rest.instance.status.check.missing_required_data";
	private static final String EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_REQUIRED_ARGUMENTS = "emf.rest.instance.properties.load.missing_required_arguments";
	private static final String EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_DRAFT = "emf.rest.instance.properties.load.missing_draft";
	private static final String EMF_REST_INSTANCE_REVISION_LOAD_MISSING_REQUIRED_ARGUMENTS = "emf.rest.instance.revision.load.missing_required_arguments";
	private static final String EMF_REST_INSTANCE_INVALID_INSTANCE_TYPE = "emf.rest.instance.invalid_instance_type";
	private static final String CMF_ACTION_DOCUMENT_SUCCESSFUL_PUBLISH = "cmf.action.document.successful_publish";
	private static final String EMF_ACTION_OBJECT_SUCCESSFUL_PUBLISH = "emf.action.object.successful_publish";
	private static final String TARGET_TYPE = "targetType";
	private static final String TARGET_ID = "targetId";
	private static final String STATUS = "status";
	private static final String IS_ASCENDING = "isAscending";
	private static final String PROPERTY = "property";
	private static final String OPERATION_ID = "operationId";
	private static final String DOCUMENT_INSTANCE_TYPE = "documentinstance";

	private static final Operation OP_PUBLISH = new Operation(ActionTypeConstants.PUBLISH);

	@Inject
	private LinkService linkService;

	@Inject
	private NotificationSupport notificationSupport;

	@Inject
	protected CodelistService codelistService;

	@Inject
	private RevisionService revisionService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	/**
	 * Publish a revision. Accepts data in format:
	 *
	 * <pre>
	 * {
	 * 	id: "emf:e6db3b7c-4061-4a8b-bee8-2c318e99551d",
	 * 	type: "documentinstance"
	 * }
	 * </pre>
	 *
	 * @param payload
	 *            Instance representation to publish.
	 * @return the published instance
	 */
	@POST
	@Path("/revisions")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(TxType.REQUIRED)
	public Response publishRevision(String payload) {
		// TODO: this is crap - pass the id as path param, type as query (can
		// be removed when we
		// load instances by uri)
		JSONObject payloadObject = JsonUtil.toJsonObject(payload);
		if (payloadObject == null) {
			throw new BadRequestException("Request payload, containing instanceId and instanceType, is required.");
		}

		String type = JsonUtil.getStringValue(payloadObject, INSTANCE_TYPE);
		Instance instance = fetchInstance(JsonUtil.getStringValue(payloadObject, INSTANCE_ID), type);

		Instance published = instanceService.publish(instance, OP_PUBLISH);
		if (published == null) {
			// TODO: publish fails and we return bad request, is this correct?
			// 5xx maybe?
			throw new BadRequestException(labelProvider.getValue("Publish can not be executed!"));
		}

		String successMessage = EMF_ACTION_OBJECT_SUCCESSFUL_PUBLISH;
		if (DOCUMENT_INSTANCE_TYPE.equalsIgnoreCase(type)) {
			successMessage = CMF_ACTION_DOCUMENT_SUCCESSFUL_PUBLISH;
		}

		Serializable compactHeader = published.getProperties().get(DefaultProperties.HEADER_COMPACT);

		String publishSuccessMessage = labelProvider.getValue(successMessage) + compactHeader;
		notificationSupport.addMessage(new NotificationMessage(publishSuccessMessage, MessageLevel.INFO));

		JSONObject response = convertInstanceToJSON(published);
		return buildOkResponse(response.toString());
	}

	/**
	 * REST method for retrieving all revisions for specific instance. Based on their identifier and type.
	 *
	 * @param instanceId
	 *            current instance identifier
	 * @param instanceType
	 *            current instance type
	 * @param ascending
	 *            define order flag
	 * @return response with revisions
	 * @throws JSONException
	 */
	@GET
	@Path("/revisions")
	public Response getRevisions(@QueryParam(INSTANCE_ID) String instanceId,
			@QueryParam(INSTANCE_TYPE) String instanceType, @QueryParam(IS_ASCENDING) boolean ascending) {
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			return buildBadRequestResponse(
					labelProvider.getValue(EMF_REST_INSTANCE_REVISION_LOAD_MISSING_REQUIRED_ARGUMENTS));
		}

		JSONObject response = new JSONObject();

		InstanceReference instanceReference = getInstanceReference(instanceId, instanceType);
		if (instanceReference == null) {
			return buildBadRequestResponse(labelProvider.getValue(EMF_REST_INSTANCE_INVALID_INSTANCE_TYPE));
		}

		Collection<Instance> revisions = revisionService.getRevisions(instanceReference, ascending);
		instanceLoadDecorator.decorateResult(revisions);
		for (Instance instance : revisions) {
			JSONObject jsonObject = instance.toJson(DefaultProperties.HEADER_COMPACT, DefaultProperties.STATUS);
			JsonUtil.append(response, DATA, jsonObject);
		}

		return buildResponse(Status.OK, response.toString());
	}

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
	public Response getInstanceStatus(@QueryParam(INSTANCE_ID) String instanceId,
			@QueryParam(INSTANCE_TYPE) String instanceType) {
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			return buildResponse(Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_STATUS_CHECK_MISSING_REQUIRED_DATA));
		}
		Instance instance = fetchInstance(instanceId, instanceType);
		if (instance != null) {
			Serializable status = instance.getProperties().get(DefaultProperties.STATUS);
			JSONObject responseData = new JSONObject();
			JsonUtil.addToJson(responseData, STATUS, status);
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
	 * @return the instance properties as a json object with property name as key and property values as value
	 */
	@Path("properties")
	@GET
	public Response getInstanceProperties(@QueryParam(INSTANCE_ID) String instanceId,
			@QueryParam(INSTANCE_TYPE) String instanceType, @QueryParam(PROPERTY) String[] properties) {
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			return buildResponse(Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_REQUIRED_ARGUMENTS));
		}
		Instance instance = fetchInstance(instanceId, instanceType);

		JSONObject responseData = buildPropertiesForInstance(instance, properties);
		if (responseData != null) {
			return buildResponse(Status.OK, responseData.toString());
		}
		return buildResponse(Status.NOT_FOUND, null);
	}

	/**
	 * Retrieves properties for the original instance (this is used if we pass a revision).
	 *
	 * @param instanceId
	 *            the id of the instance we want to retrieve properties.
	 * @param instanceType
	 *            the type of instance.
	 * @param properties
	 *            the properties we want to retrieve for the given instance.
	 * @return the instance properties as a json object with property name as key and property values as value
	 */
	@Path("properties/original")
	@GET
	public Response getOriginalInstanceProperties(@QueryParam(INSTANCE_ID) String instanceId,
			@QueryParam(INSTANCE_TYPE) String instanceType, @QueryParam(PROPERTY) String[] properties) {
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			return buildResponse(Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_REQUIRED_ARGUMENTS));
		}
		Instance instance = fetchInstance(instanceId, instanceType);
		Instance draft = revisionService.getRevisionOf(instance);

		JSONObject responseData = buildPropertiesForInstance(draft, properties);
		if (responseData != null) {
			return buildResponse(Status.OK, responseData.toString());
		}
		return buildResponse(Status.BAD_REQUEST,
				labelProvider.getValue(EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_DRAFT));
	}

	/**
	 * @param instance
	 * @param properties
	 * @return
	 */
	private static JSONObject buildPropertiesForInstance(Instance instance, String[] properties) {
		if (instance == null || properties == null) {
			return null;
		}

		JSONObject responseData = new JSONObject();
		for (int i = 0; i < properties.length; i++) {
			Object value = instance.getProperties().get(properties[i]);
			JsonUtil.addToJson(responseData, properties[i], value);
		}
		return responseData;
	}

	/**
	 * Exposes detach method as rest service. This service can accept more than one linked instances that should be
	 * detached. If any request data for linked instance is not present or instance can not be found, then later is
	 * skipped from detach operation.
	 *
	 * @param data
	 *            Request data in json format: <code>
	 * {
	 *     targetId: '',
	 *     targetType: '',
	 *     operationId: '',
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
	@Transactional(TxType.REQUIRED)
	public Response detach(String data) {
		if (trace) {
			LOG.trace("Detach request: {}", data);
		}
		if (StringUtils.isNullOrEmpty(data)) {
			return buildResponse(Response.Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_DETACH_MISSING_REQUIRED_ARGUMENTS));
		}

		JSONObject request = JsonUtil.createObjectFromString(data);
		String targetInstanceId = JsonUtil.getStringValue(request, TARGET_ID);
		String targetInstanceType = JsonUtil.getStringValue(request, TARGET_TYPE);
		String operationId = JsonUtil.getStringValue(request, OPERATION_ID);
		Instance targetInstance = fetchInstance(targetInstanceId, targetInstanceType);
		if (targetInstance == null) {
			return buildResponse(Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_DETACH_MISSING_TARGET_ID_OR_TYPE));
		}

		JSONArray linkedInstances = JsonUtil.getJsonArray(request, "linked");
		if (linkedInstances == null) {
			return buildResponse(Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_DETACH_MISSING_LINKED_INSTANCES));
		}

		int length = linkedInstances.length();
		List<InstanceReference> items = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			JSONObject current = (JSONObject) JsonUtil.getFromArray(linkedInstances, i);
			// there is StringToLinkSourceConverter that converts json object
			// { instanceId: '', instanceType: ''} to instance reference
			InstanceReference reference = getTypeConverter().convert(InstanceReference.class, current.toString());
			if (reference != null) {
				items.add(reference);
			} else {
				LOG.debug("Can not detach linked instance [{}]", current);
			}
		}

		if (!items.isEmpty() && StringUtils.isNotNullOrEmpty(operationId)) {
			Operation operation = new Operation(operationId);
			Collection<Instance> values = loadInstances(items);
			instanceService.detach(targetInstance, operation, values.toArray(new Instance[values.size()]));
		} else {
			return buildResponse(Response.Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_DETACH_MISSING_REQUIRED_ARGUMENTS));
		}

		return buildResponse(Response.Status.OK, null);
	}

	/**
	 * Get the type of the owning instance of given instance.
	 *
	 * @param instanceId
	 *            - instance id
	 * @param instanceType
	 *            - instance type
	 * @return the response - type of an owning instance
	 */
	@Path("parentInstanceType")
	@GET
	public Response getParentInstanceType(@QueryParam(INSTANCE_ID) String instanceId,
			@QueryParam(INSTANCE_TYPE) String instanceType) {

		JSONObject response = new JSONObject();
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			return buildResponse(Response.Status.BAD_REQUEST, "Request params are missing");
		}
		Instance instance = fetchInstance(instanceId, instanceType);
		Instance owningInstance = ((OwnedModel) instance).getOwningInstance();

		if (owningInstance == null) {
			return buildResponse(Response.Status.BAD_REQUEST, "Owning instance is null");
		}

		// Get owning instance type - RDF type if it's object or definition id
		String type = (String) owningInstance.getProperties().get(DefaultProperties.TYPE);
		if (owningInstance.getClass().isAssignableFrom(getInstanceClass("sectioninstance"))) {
			Instance caseInstance = InstanceUtil.getParentPath(instance).get(1);
			type = (String) caseInstance.getProperties().get(DefaultProperties.TYPE);
		}
		if (owningInstance.getClass().isAssignableFrom(getInstanceClass("objectinstance"))) {
			type = (String) owningInstance.getProperties().get(DefaultProperties.SEMANTIC_TYPE);
			ClassInstance clazz = semanticDefinitionService.getClassInstance(type);
			type = (String) clazz.getProperties().get("instance");
		}

		JsonUtil.addToJson(response, "sourceId", owningInstance.getId());
		JsonUtil.addToJson(response, "sourceType", owningInstance.type().getCategory());
		JsonUtil.addToJson(response, "subtype", type);

		return buildResponse(Response.Status.OK, response.toString());
	}

	/**
	 * Removes links for given instance. If the instance is persisted, none links will be removed, else tries to remove
	 * all links, if there are any.
	 *
	 * @param instanceId
	 *            the id of the instance
	 * @param instanceType
	 *            the type of the instance
	 * @return <b>OK response - when</b> the instance is persisted(none links removed),<br>
	 *         <b>when</b> the instance is not persisted and links are removed <br>
	 *         and <b>when</b> the id is not persisted, but there aren't any links.
	 *         <p>
	 *         <b>BAD REQUEST response </b> when any of the given id or type are null/empty.
	 */
	@Path("removeLinks")
	@GET
	@Transactional(TxType.REQUIRED)
	public Response removeInstanceLinks(@QueryParam("instanceId") String instanceId,
			@QueryParam("instanceType") String instanceType) {
		if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
			LOG.debug("Empty/null instance id or type");
			return buildResponse(Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_MISSING_REQUIRED_ARGUMENTS));
		}

		if (!InstanceUtil.isIdPersisted(instanceId)) {
			InstanceReference instanceReference = getInstanceReference(instanceId, instanceType);
			boolean linkDeleted = linkService.removeLinksFor(instanceReference);
			// if at least one link is removed
			if (linkDeleted) {
				LOG.debug("All semantic link for id: {} are removed.", instanceId);
				return buildResponse(Status.OK, null);
			}
		}
		// persisted or no links
		LOG.debug("The given id: {} is persisted or there is no links to/from it.", instanceId);
		return buildResponse(Status.OK, null);
	}

	/**
	 * Fetches the available tasks for a instance (project or case).
	 *
	 * @param id
	 *            instance id.
	 * @param type
	 *            instance type.
	 * @return The available tasks.
	 */
	@GET
	@Path("/tasks")
	public String getAllowedTasks(@QueryParam("id") String id, @QueryParam("type") String type) {

		JSONArray result = new JSONArray();
		List<DefinitionModel> definitions = fetchDefinitions(type, id);

		for (DefinitionModel definition : definitions) {
			PropertyDefinition typeProperty = (PropertyDefinition) definition.getChild(DefaultProperties.TYPE);
			Integer codelist = null;
			if (typeProperty != null) {
				codelist = typeProperty.getCodelist();
			}

			String definitionId = definition.getIdentifier();

			JSONObject data = new JSONObject();

			List<PropertyDefinition> properties = definition.getFields();
			for (PropertyDefinition property : properties) {
				if ("assignee".equals(property.getName())) {
					JsonUtil.addToJson(data, "user", "user");
					JsonUtil.addToJson(data, IS_MULTY_FIELD_NAME, false);
				}
				if ("assignees".equals(property.getName())) {
					JsonUtil.addToJson(data, "user", "user");
					JsonUtil.addToJson(data, IS_MULTY_FIELD_NAME, true);
				}
				if ("groupAssignee".equals(property.getName())) {
					JsonUtil.addToJson(data, "user", "group");
					JsonUtil.addToJson(data, IS_MULTY_FIELD_NAME, false);
				}
				if ("multiAssignees".equals(property.getName())) {
					JsonUtil.addToJson(data, "user", "all");
					JsonUtil.addToJson(data, IS_MULTY_FIELD_NAME, true);
				}
			}

			String descr = definitionId;
			if (codelist != null) {
				descr = codelistService.getDescription(codelist, definitionId);
				if (StringUtils.isNotNullOrEmpty(descr)) {
					descr += " (" + definitionId + ")";
				}
				if (StringUtils.isNullOrEmpty(descr)) {
					descr = definitionId;
				}
			}

			JsonUtil.addToJson(data, "value", definition.getIdentifier());
			JsonUtil.addToJson(data, "label", descr);
			JsonUtil.addToJson(data, "currentUser", getCurrentLoggedUser().getId());
			result.put(data);
		}

		return result.toString();
	}

	/**
	 * Retrieves the allowed children for a specific instance according to it's definition.
	 *
	 * @param instanceId
	 *            Instance identifier.
	 * @param instanceType
	 *            Instance type.
	 * @param childType
	 *            Type of the allowed children.
	 * @return JSON object in format
	 *
	 *         <pre>
	 * <code>
	 * {
	 *     children: [
	 *         {
	 *             identifier: '',
	 *             description: ''
	 *         }
	 *     ]
	 * }
	 * </code>
	 *         </pre>
	 */
	@GET
	@Path("/{" + INSTANCE_ID + "}/allowedChildren")
	public Response getAllowedChildren(@PathParam(INSTANCE_ID) String instanceId,
			@QueryParam(INSTANCE_TYPE) String instanceType, @QueryParam("childType") String childType) {

		Instance instance = fetchInstance(instanceId, instanceType);
		List<DefinitionModel> allowedChildren = instanceService.getAllowedChildren(instance, childType);

		JSONObject response = new JSONObject();
		JSONArray children = new JSONArray();
		JsonUtil.addToJson(response, "children", children);
		for (DefinitionModel model : allowedChildren) {
			PropertyDefinition property = PathHelper.findProperty(model, (PathElement) model, DefaultProperties.TYPE);
			if (property == null) {
				continue;
			}
			JSONObject child = new JSONObject();

			String identifier = model.getIdentifier();
			Integer codelistNumber = property.getCodelist();
			Object description = null;
			if (codelistNumber != null) {
				description = codelistService.getDescription(codelistNumber, identifier);
			}
			if (description == null) {
				description = identifier;
			}
			JsonUtil.addToJson(child, "description", description);
			JsonUtil.addToJson(child, "identifier", identifier);

			children.put(child);
		}
		return buildOkResponse(response.toString());
	}

	/**
	 * Load definitions of given type.
	 *
	 * @param id
	 *            instance id.
	 * @param type
	 *            instance type.
	 * @return all possible definitions.
	 */
	private List<DefinitionModel> fetchDefinitions(String type, String id) {
		Instance contextInstance = fetchInstance(id, type);
		// for constant reference see ObtectTypesCmf.STANDALONE_TASK
		// REVIEW: this should be passed as argument
		return instanceService.getAllowedChildren(contextInstance, "task");
	}

}
