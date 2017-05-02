package com.sirma.cmf.web.object.relations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.LinkIterable;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.relation.RelationChangeEvent;
import com.sirma.itt.seip.instance.relation.RelationCreateEvent;
import com.sirma.itt.seip.instance.relation.RelationDeleteEvent;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Relations management services.
 *
 * @author svelikov
 */
@Path("/relations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Transactional(TxType.REQUIRED)
public class RelationsRestService extends EmfRestService {

	// ':'
	private static final int COLON_SIGN = 58;
	// '#'
	private static final int SHARP_SIGN = 35;

	public static final String MODE_INGOING = "ingoing";

	public static final String MODE_OUTGOING = "outgoing";

	public static final String MODE_ALL = "all";

	/** Pattern to match open and closing html tags. */
	private static final Pattern HTML_TAG = Pattern.compile("<(\\/)?[a-z][a-z0-9]*[^<>]*>", Pattern.CANON_EQ);

	private static final Pattern COMMA_REGEX = Pattern.compile("\\s*,\\s*");

	private static final String MISSING_REQUEST_DATA_FOR_CREATE_RELATION = "cmf.relations.create.missing.request.data";

	private static final String MISSING_EXPECTED_ITEM = "cmf.relations.expected.data.missing";

	private static final String RELATION_CREATE_ERROR = "cmf.relations.create.relation.internal.error";

	private static final String MISSING_EXPECTED_TYPE = "cmf.relations.expected.type.missing";

	private static final String FORBIDDEN_MENUAL_CREATION = "cmf.relations.forbidden.manual.relation";

	private static final String INVALID_TARGET_INSTANCE = "cmf.relations.invalid.target";

	private static final String INVALID_DESTINATION_INSTANCE = "cmf.relations.invalid.destination";

	@Inject
	private LinkService linkService;

	@Inject
	private Instance<SemanticDefinitionService> semanticDefinitionService;

	@Inject
	private EventService eventService;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	/**
	 * Load data.
	 *
	 * @param id
	 *            current instance id
	 * @param type
	 *            current instance type
	 * @param page
	 *            the page
	 * @param start
	 *            the start
	 * @param limit
	 *            the limit
	 * @param mode
	 *            the mode for the returned links. Possible values
	 *            <ul>
	 *            <li>outgoing - (default value)
	 *            <li>ingoing
	 *            <li>all
	 *            </ul>
	 * @param fields
	 *            - defines what fields from the instance to return. The fields are separated by comma
	 * @param linkId
	 *            - the specific linkId to be searched.
	 * @return Response which contains the built data store json
	 */
	@Path("loadData")
	@GET
	public Response loadData(@QueryParam("id") String id, @QueryParam("type") String type,
			@QueryParam("page") String page, @QueryParam("start") String start, @QueryParam("limit") String limit,
			@DefaultValue(MODE_OUTGOING) @QueryParam("mode") String mode, @QueryParam("fields") String fields,
			@QueryParam("linkId") String linkId) {
		if (debug) {
			LOG.debug("CMFWeb: ObjectsRelationsRestService.loadData relations for type[{}], id[{}]", type, id);
		}

		// no semantic present
		if (semanticDefinitionService.isUnsatisfied()) {
			return buildResponse(Status.NOT_FOUND, "Missing required dependency for this service! "
					+ "No implementation for " + SemanticDefinitionService.class);
		}

		// check required arguments
		if (StringUtils.isNullOrEmpty(id) || StringUtils.isNullOrEmpty(type)) {
			return buildBadRequestResponse("Missing required arguments!");
		}

		InstanceReference instance = getInstanceReference(id, type);
		JSONArray data = null;
		if (instance != null) {
			data = buildData(instance, mode, fields, org.apache.commons.lang3.StringUtils.trimToNull(linkId));
		} else {
			return buildResponse(Status.INTERNAL_SERVER_ERROR, "Can't find instance type=" + type + " with id=" + id);
		}

		return buildOkResponse(data.toString());
	}

	/**
	 * Creates relation between instances. The relation and instances are specified in the request data.
	 * <p>
	 * Request data example:
	 * </p>
	 * <code>
	 * <pre>
	 * {
	 *  relType       : 'emf:someRelation',
	 *  reverseRelType: 'emf:reverseSomeRelation',
	 *  operationId   : 'someOperation'(optional: if you want to log some operation in audit),
	 *  system        : true(optional: by default is false),
	 *  selectedItems : {
	 *                   someKey_1 : {
	 *                                targetId   : targetInstanceId,
	 *           		              targetType : targetInstanceType,
	 *           		              destId     : destInstanceId,
	 *           		              destType   : destInstanceType
	 *                               },
	 *                   someKey_2 : {...},
	 *                   ...}
	 * }
	 * </pre>
	 * </code>
	 *
	 * @param data
	 *            the request data
	 * @return response
	 */
	@Path("create")
	@POST
	public Response createRelation(String data) {
		return createRelationInternal(data, true);
	}

	/**
	 * Creates relation between instances. The relation and instances are specified in the request data.
	 *
	 * @param data
	 *            the request data
	 * @param fireEvent
	 *            indicates whether a {@link RelationCreateEvent} should be fired
	 * @return the response
	 */
	private Response createRelationInternal(String data, boolean fireEvent) {
		if (debug) {
			LOG.debug("CMFWeb: RelationsRestService.createRelation data: {}", data);
		}

		if (StringUtils.isNullOrEmpty(data)) {
			return buildBadRequestResponse(labelProvider.getValue(MISSING_REQUEST_DATA_FOR_CREATE_RELATION));
		}
		try {
			JSONObject jsonData = new JSONObject(data);
			String relationType = JsonUtil.getStringValue(jsonData, "relType");
			String selectedItemsString = JsonUtil.getStringValue(jsonData, "selectedItems");
			String parametersCheckResult = checkRequestParameters(relationType, selectedItemsString);
			if (StringUtils.isNotNull(parametersCheckResult)) {
				return buildBadRequestResponse(parametersCheckResult);
			}

			JSONObject selectedItems = new JSONObject(selectedItemsString);
			if (selectedItems.length() == 0) {
				return buildBadRequestResponse(labelProvider.getValue(MISSING_EXPECTED_ITEM));
			}

			return executeCreateRelation(fireEvent, jsonData, relationType, selectedItems);
		} catch (JSONException e) {
			LOG.error("", e);
			return buildResponse(Status.INTERNAL_SERVER_ERROR, "Server error!");
		}
	}

	/**
	 * Creates relations between the target instance and the instances that are pass in the request data property
	 * 'selectedItems', if there are more then one.
	 *
	 * @param fireEvent
	 *            indicates whether a {@link RelationCreateEvent} should be fired
	 * @param jsonData
	 *            the request data as JSON
	 * @param relationType
	 *            the relation type (the id of the relation)
	 * @param selectedItems
	 *            the instances to which the relation will be created
	 * @return response
	 */
	private Response executeCreateRelation(boolean fireEvent, JSONObject jsonData, String relationType,
			JSONObject selectedItems) {
		JSONObject response = new JSONObject();
		JSONArray messages = new JSONArray();
		JsonUtil.addToJson(response, "messages", messages);
		for (Iterator<String> iterator = selectedItems.keys(); iterator.hasNext();) {
			String key = iterator.next();
			JSONObject item = (JSONObject) JsonUtil.getValueOrNull(selectedItems, key);

			String targetId = JsonUtil.getStringValue(item, "targetId");
			String targetType = JsonUtil.getStringValue(item, "targetType");
			String destinationId = JsonUtil.getStringValue(item, "destId");
			String destinationType = JsonUtil.getStringValue(item, "destType");

			LOG.debug(
					"CMFWeb: RelationsRestService.createRelation relType[{}] "
							+ "for target type[{}] with id[{}] and destination type[{}] with id[{}]",
					relationType, targetType, targetId, destinationType, destinationId);

			InstanceReference targetReference = createInstanceReference(targetId, targetType);
			InstanceReference destinationReference = createInstanceReference(destinationId, destinationType);
			boolean canCreateLink = true;

			String instancesCheckResult = instancesNullCheck(targetReference, destinationReference);
			if (StringUtils.isNotNull(instancesCheckResult)) {
				canCreateLink = false;
				return buildBadRequestResponse(instancesCheckResult);
			}

			canCreateLink = checkIfSameInstance(messages, targetReference, destinationReference);
			if (!canCreateLink) {
				continue;
			}
			boolean createRelationResult = createRelation(fireEvent, jsonData, relationType, targetReference,
					destinationReference);
			if (!createRelationResult) {
				return buildResponse(Status.INTERNAL_SERVER_ERROR, labelProvider.getValue(RELATION_CREATE_ERROR));
			}
		}

		return buildOkResponse(response.toString());
	}

	/**
	 * Checks the the passed instances are same, if so the message is put in to messages array(used as notification) and
	 * false is returned. This is done, because it is forbidden to create relations, when the source and destinations
	 * are the same(CS-479).
	 *
	 * @param messages
	 *            store for messages, why can't create relation between the instances
	 * @param targetReference
	 *            the reference to the target instance
	 * @param destinationReference
	 *            the reference to the destination instance
	 * @return true if the instances are not same, false otherwise
	 */
	private boolean checkIfSameInstance(JSONArray messages, InstanceReference targetReference,
			InstanceReference destinationReference) {
		if (EqualsHelper.nullSafeEquals(targetReference, destinationReference)) {
			messages.put("Skipped creation of reference to current instance!");
			LOG.warn("CMFWeb: RelationsRestService.createRelation - Trying to create relation with itself:"
					+ " source and destinations are the same id[{}]. Ignoring relation!"
					+ targetReference.getIdentifier());
			return false;
		}
		return true;
	}

	/**
	 * Checks request parameters for create relations. Checks if there are passed relation type and destination
	 * instances. Also checks, if the relation is allowed to be created manually.
	 *
	 * @param relationType
	 *            the relation type
	 * @param selectedItemsString
	 *            the selected instances as string
	 * @return <b>null</b> if everything is correct, or message if there is some problem
	 */
	private String checkRequestParameters(String relationType, String selectedItemsString) {
		if (StringUtils.isNullOrEmpty(relationType)) {
			return labelProvider.getValue(MISSING_EXPECTED_TYPE);
		}

		if (isRelationDisallowed(relationType)) {
			return labelProvider.getValue(FORBIDDEN_MENUAL_CREATION);
		}

		if (StringUtils.isNullOrEmpty(selectedItemsString)) {
			return labelProvider.getValue(MISSING_EXPECTED_ITEM);
		}

		return null;
	}

	/**
	 * Checks passed instances for null.
	 *
	 * @param targetReference
	 *            reference to target instance
	 * @param destinationReference
	 *            reference to destination instance
	 * @return <b>null</b> if everything is correct, or message if there is a problem
	 */
	private String instancesNullCheck(InstanceReference targetReference, InstanceReference destinationReference) {
		if (targetReference == null) {
			LOG.debug("CMFWeb: RelationsRestService.createRelation - Can't find target instance");
			return labelProvider.getValue(INVALID_TARGET_INSTANCE);
		}

		if (destinationReference == null) {
			LOG.debug("CMFWeb: RelationsRestService.createRelation - Can't find destination instance ");
			return labelProvider.getValue(INVALID_DESTINATION_INSTANCE);
		}

		return null;
	}

	/**
	 * Creates the relation.
	 *
	 * @param fireEvent
	 *            the fire event
	 * @param jsonData
	 *            the json data
	 * @param system
	 *            the system
	 * @param currentUser
	 *            the current user
	 * @param relationType
	 *            the relation type
	 * @param targetReference
	 *            the target reference
	 * @param destinationReference
	 *            the destination reference
	 * @return the response
	 */
	private boolean createRelation(boolean fireEvent, JSONObject jsonData, String relationType,
			InstanceReference targetReference, InstanceReference destinationReference) {
		Boolean system = JsonUtil.getBooleanValue(jsonData, "system", Boolean.FALSE);
		String operationId = JsonUtil.getStringValue(jsonData, "operationId");
		// for system links we use the default
		Map<String, Serializable> properties = LinkConstants.getDefaultSystemProperties();
		if (Boolean.FALSE.equals(system)) {
			// for user created links we pass in the current user
			Serializable currentUser = getCurrentUser();
			properties = CollectionUtils.createHashMap(1);
			properties.put(DefaultProperties.CREATED_BY, currentUser);
			properties.put(DefaultProperties.CREATED_ON, new Date());
		}

		// if provided reverse link type, we use it
		String reverseLinkType = JsonUtil.getStringValue(jsonData, "reverseRelType");
		if (StringUtils.isNullOrEmpty(reverseLinkType)) {
			reverseLinkType = getReverseLinkType(relationType);
		}

		Pair<Serializable, Serializable> pair = linkService.link(targetReference, destinationReference, relationType,
				reverseLinkType, properties);
		if (pair.getFirst() == null) {
			LOG.debug(
					"CMFWeb: ObjectsRelationsRestService.createRelation - Can't create relation of type[{}] "
							+ "for target id[{}] and destination id[{}]",
					relationType, targetReference.getIdentifier(), destinationReference.getIdentifier());
			return false;
		}
		if (fireEvent) {
			fireRelationCreateEvent(relationType, targetReference, destinationReference, pair, operationId);
		}
		return true;
	}

	/**
	 * Fires RelationCreateEvent, if operation id is passed, it is passed and the operations will be logged in the audit
	 * log. If there is no operation id in the audit will be logged record with operation "create link".
	 *
	 * @param relationType
	 *            the relation type
	 * @param targetReference
	 *            the target instance reference
	 * @param destinationReference
	 *            the destination instance reference
	 * @param pair
	 *            pair of generated ids for the created links. The first entry from the pair corresponds to the first
	 *            link from -> to, while the second element of the pair for the relation to -> from
	 * @param operationId
	 *            the id on the operation
	 */
	private void fireRelationCreateEvent(String relationType, InstanceReference targetReference,
			InstanceReference destinationReference, Pair<Serializable, Serializable> pair, String operationId) {
		if (StringUtils.isNullOrEmpty(operationId)) {
			eventService.fire(new RelationCreateEvent(targetReference.getIdentifier(),
					destinationReference.getIdentifier(), relationType));
		} else {
			eventService.fire(new RelationCreateEvent(targetReference.getIdentifier(),
					destinationReference.getIdentifier(), relationType, operationId));
		}
	}

	/**
	 * Checks if given relation is disallowed for creation by end-user.
	 *
	 * @param relationType
	 *            the relation type
	 * @return true, if is relation allowed
	 */
	private boolean isRelationDisallowed(String relationType) {
		return semanticDefinitionService.get().isSystemRelation(relationType).booleanValue();
	}

	/**
	 * Update relation. At the moment this method simply removes old relation and creates a new one.
	 *
	 * @param relationId
	 *            the relation id which will be updated
	 * @param data
	 *            the data for the new relation. Same format as data send for create relation.
	 * @return the response
	 */
	@Path("update")
	@POST
	public Response updateRelation(@QueryParam("relationId") String relationId, String data) {

		if (debug) {
			LOG.debug("CMFWeb: ObjectsRelationsRestService.updateRelation relationId[{}]", relationId);
		}
		if (StringUtils.isNullOrEmpty(relationId)) {
			return buildResponse(Status.BAD_REQUEST, "Relation id is required!");
		}
		try {
			JSONObject jsonData = new JSONObject(data);
			String relationType = JsonUtil.getStringValue(jsonData, "relType");
			if (isRelationDisallowed(relationType)) {
				return buildResponse(Status.BAD_REQUEST, "The requested relation is forbidden!");
			}
			JSONObject selectedItems = jsonData.getJSONObject("selectedItems").getJSONObject("0");
			eventService.fire(new RelationChangeEvent(selectedItems.getString("targetId"),
					selectedItems.getString("destId"), relationType));
		} catch (JSONException e) {
			LOG.error("", e);
			return buildResponse(Status.INTERNAL_SERVER_ERROR, "Server error!");
		}
		linkService.removeLinkById(relationId);
		return createRelationInternal(data, false);
	}

	/**
	 * Deactivate a relation.
	 *
	 * @param relationId
	 *            the relation id
	 * @param relType
	 *            the rel type
	 * @param instanceId
	 *            the instance id
	 * @param toId
	 *            the to id
	 * @return the response
	 */
	@Path("deactivate")
	@GET
	public Response deactivateRelation(@QueryParam("relationId") String relationId,
			@QueryParam("relType") String relType, @QueryParam("instanceId") String instanceId,
			@QueryParam("toId") String toId) {
		if (debug) {
			LOG.debug("CMFWeb: ObjectsRelationsRestService.deactivateRelation relationId[{}]", relationId);
		}

		if (StringUtils.isNullOrEmpty(relationId)) {
			return buildResponse(Status.BAD_REQUEST, "Relation id is required!");
		}
		linkService.removeLinkById(relationId);
		eventService.fire(new RelationDeleteEvent(instanceId, toId, relType));
		return buildOkResponse("OK");
	}

	/**
	 * Deactivate a relation.
	 *
	 * @param data
	 *            the request data
	 * @return the response
	 */
	@Path("/")
	@DELETE
	public Response removeRelation(String data) {
		if (trace) {
			LOG.trace("CMFWeb: RelationsRestService.deactivateRelation: request: {}", data);
		}
		if (StringUtils.isNullOrEmpty(data)) {
			return buildResponse(Status.BAD_REQUEST,
					"There are missing or wrong request parameters for deactivate relation operation!");
		}
		return buildOkResponse("OK");
	}

	/**
	 * Gets the reverse link type. If no reverse link is defined in semantic model no reverse link will be created
	 *
	 * @param relationType
	 *            the relation type
	 * @return the reverse link type
	 */
	private String getReverseLinkType(String relationType) {
		PropertyInstance relation = semanticDefinitionService.get().getRelation(relationType);
		String inverse = null;
		if (relation != null) {
			Serializable serializable = relation.getProperties().get("inverseRelation");
			if (serializable != null) {
				inverse = serializable.toString();
			}
		}
		return inverse;
	}

	/**
	 * Creates the instance reference.
	 *
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @return the instance reference
	 */
	private InstanceReference createInstanceReference(String instanceId, String instanceType) {
		if (StringUtils.isNotNullOrEmpty(instanceId) && StringUtils.isNotNullOrEmpty(instanceType)) {
			DataTypeDefinition dataTypeDefinition = dictionaryService.getDataTypeDefinition(instanceType);
			return new LinkSourceId(instanceId, dataTypeDefinition);
		}
		return null;
	}

	/**
	 * Builds the data store to be returned to the client.
	 *
	 * @param instance
	 *            the instance
	 * @param mode
	 *            the mode
	 * @param fields
	 *            - defines what fields from the instance to return. The fields are separated by comma
	 * @return the jSON object representing the data store
	 */
	protected JSONArray buildData(InstanceReference instance, String mode, String fields, String linkId) {
		JSONArray data = new JSONArray();
		if (!isInstanceValid(instance)) {
			return data;
		}

		Resource currentUser = getCurrentUser();
		// no logged in user
		if (currentUser == null || semanticDefinitionService.isUnsatisfied()) {
			return data;
		}
		Map<String, PropertyInstance> relationsMap = semanticDefinitionService.get().getRelationsMap();
		Set<String> links = new HashSet<>();
		CollectionUtils.addNonNullValue(links, linkId);

		if (MODE_ALL.equalsIgnoreCase(mode) || MODE_OUTGOING.equalsIgnoreCase(mode)) {
			data = buildRelationData(linkService.getLinks(instance, links), relationsMap, currentUser, false, fields);
		}
		if (MODE_ALL.equalsIgnoreCase(mode) || MODE_INGOING.equalsIgnoreCase(mode)) {
			// returns the relations linked to the current instance - probably good idea to move it
			// to separate table
			data = buildRelationData(linkService.getLinksTo(instance, links), relationsMap, currentUser,
					MODE_ALL.equalsIgnoreCase(mode), fields);
		}

		return data;
	}

	/**
	 * Checks if the provided instance reference is valid.
	 *
	 * @param instance
	 *            - the provided instance reference
	 * @return true if the instance is not null and its identifier is also not null or false otherwise
	 */
	private static boolean isInstanceValid(InstanceReference instance) {
		if (instance == null) {
			return false;
		}
		if ("null".equals(instance.getIdentifier())) {
			return false;
		}
		return true;
	}

	/**
	 * Builds the relation data.
	 *
	 * @param links
	 *            the links
	 * @param relationsMap
	 *            the relations map
	 * @param currentUser
	 *            the current user
	 * @param isFrom
	 *            the is from
	 * @param fields
	 *            - defines what fields from the instance to return. The fields are separated by comma
	 * @return the JSON array
	 */
	private JSONArray buildRelationData(Collection<LinkReference> links, Map<String, PropertyInstance> relationsMap,
			Resource currentUser, boolean isFrom, String fields) {

		Collection<LinkInstance> instanceLinks = links
				.stream()
					.map(LinkReference::toLinkInstance)
					.filter(isLinkEndNonNull(isFrom))
					.collect(CollectionUtils.toList(-1));

		// here we collect all instances that are returned from getFrom() and getTo() methods because they should be
		// decorated so that permission evaluation could work properly
		Collection<com.sirma.itt.seip.domain.instance.Instance> instances = new ArrayList<>(instanceLinks.size() * 2);
		instances.addAll(LinkIterable.iterateFrom(instanceLinks));
		instances.addAll(LinkIterable.iterateTo(instanceLinks));
		instanceLoadDecorator.decorateResult(instances);

		Function<String, String> linkTitleProvider = linkId -> getLinkTitle(relationsMap, linkId);
		String currentUserId = currentUser.getName();
		Function<LinkInstance, Set<Action>> actionsProvider = link -> authorityService.getAllowedActions(currentUserId,
				link, "");

		Collection<JSONObject> converted = instanceLinks
				.stream()
					.filter(link -> isHeaderPresent(link, isFrom))
					.map(link -> linkToJson(linkTitleProvider, isFrom, fields, link, actionsProvider))
					.collect(CollectionUtils.toList(-1));

		return new JSONArray(converted);
	}

	private static Predicate<? super LinkInstance> isLinkEndNonNull(boolean isFrom) {
		return link -> Objects.nonNull(isFrom ? link.getFrom() : link.getTo());
	}

	private static JSONObject linkToJson(Function<String, String> linkTitleProvider, boolean isFrom, String fields,
			LinkInstance link, Function<LinkInstance, Set<Action>> actionsProvider) {
		com.sirma.itt.seip.domain.instance.Instance instance = isFrom ? link.getFrom() : link.getTo();

		String instanceHeader = instance.getString(DefaultProperties.HEADER_COMPACT);

		JSONObject linkData = new JSONObject();
		JsonUtil.addToJson(linkData, "linkId", link.getId());
		JsonUtil.addToJson(linkData, "linkType", link.getIdentifier());

		String linkTitle = linkTitleProvider.apply(link.getIdentifier());
		String plainText = HTML_TAG.matcher(instanceHeader).replaceAll("");

		if (isFrom) {
			JsonUtil.addToJson(linkData, "name", instanceHeader);
			JsonUtil.addToJson(linkData, "value", linkTitle);
			// we should have a field which doesn't contain html tags but only plain text
			JsonUtil.addToJson(linkData, "plainText", linkTitle);

			Action.addActions(linkData, Collections.<Action> emptySet());
			JsonUtil.addToJson(linkData, "editable", false);
		} else {
			JsonUtil.addToJson(linkData, "name", linkTitle);
			JsonUtil.addToJson(linkData, "value", instanceHeader);
			// we should have a field which doesn't contain html tags but only plain text
			// we should filter by plainText field because the value field can contain
			// html tags that may be invisible for the user
			JsonUtil.addToJson(linkData, "plainText", plainText);
			Set<Action> actions = actionsProvider.apply(link);
			Action.addActions(linkData, actions);
			JsonUtil.addToJson(linkData, "editable", link.getId() != null);
		}

		JsonUtil.addToJson(linkData, "editType", "text");

		JsonUtil.addToJson(linkData, "toType", instance.getClass().getSimpleName().toLowerCase());
		JsonUtil.addToJson(linkData, "toId", instance.getId());
		JsonUtil.addToJson(linkData, "cls", instance.getClass().getSimpleName().toLowerCase());

		// Retrieving any additional properties
		if (StringUtils.isNotNullOrEmpty(fields)) {
			for (String field : COMMA_REGEX.split(fields)) {
				String property = (String) instance.getProperties().get(field);
				JsonUtil.addToJson(linkData, field, property);
			}
		}
		return linkData;
	}

	private static boolean isHeaderPresent(LinkInstance link, boolean isFrom) {
		return (isFrom ? link.getFrom() : link.getTo()).isPropertyPresent(DefaultProperties.HEADER_COMPACT);
	}

	/**
	 * Gets the link title from relations cache.
	 *
	 * @param relationsMap
	 *            the relations map
	 * @param link
	 *            the link
	 * @return the link title
	 */
	private String getLinkTitle(Map<String, PropertyInstance> relationsMap, String link) {
		PropertyInstance propertyInstance = relationsMap.get(link);
		String title = getPropertyInstanceTitle(propertyInstance);
		if (title == null) {
			// should not enter here if everything is filled in the semantic model
			title = link;
			int indexOf = title.indexOf(SHARP_SIGN);
			if (indexOf > 0) {
				title = title.substring(indexOf + 1);
			} else {
				indexOf = title.indexOf(COLON_SIGN);
				if (indexOf > 0) {
					title = title.substring(indexOf + 1);
				}
			}
		}
		return title;
	}

	/**
	 * Getter for the human readable title of a property.
	 *
	 * @param propertyInstance
	 *            {@link PropertyInstance} to ge the title for.
	 * @return The human readable title of the property of {@code null} if propertyInstance is {@code null}.
	 */
	private String getPropertyInstanceTitle(PropertyInstance propertyInstance) {
		if (propertyInstance == null) {
			return null;
		}
		return propertyInstance.getLabel(userPreferences.getLanguage());
	}

}
