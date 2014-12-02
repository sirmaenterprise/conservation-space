package com.sirma.cmf.web.object.relations;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
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

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.event.relation.RelationChangeEvent;
import com.sirma.itt.cmf.event.relation.RelationCreateEvent;
import com.sirma.itt.cmf.event.relation.RelationDeleteEvent;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.PropertyInstance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Relations management services.
 * 
 * @author svelikov
 */
@Secure
@Path("/relations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class RelationsRestService extends EmfRestService {

	public static final String MODE_INGOING = "ingoing";
	public static final String MODE_OUTGOING = "outgoing";
	public static final String MODE_ALL = "all";

	/** Pattern to match open and closing html tags. */
	private static final Pattern HTML_TAG = Pattern.compile("<(\\/)?[a-z][a-z0-9]*[^<>]*>",
			Pattern.CANON_EQ);

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The semantic definition service. */
	@Inject
	private Instance<SemanticDefinitionService> semanticDefinitionService;

	/** The authority service. */
	@Inject
	private AuthorityService authorityService;

	/** The event service. */
	@Inject
	private EventService eventService;

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
	 * @return Response which contains the built data store json
	 */
	@Path("loadData")
	@GET
	public Response loadData(@QueryParam("id") String id, @QueryParam("type") String type,
			@QueryParam("page") String page, @QueryParam("start") String start,
			@QueryParam("limit") String limit,
			@DefaultValue(MODE_OUTGOING) @QueryParam("mode") String mode) {
		if (debug) {
			log.debug(
					"CMFWeb: ObjectsRelationsRestService.loadData relations for type[{}], id[{}]",
					type, id);
		}

		// no semantic present
		if (semanticDefinitionService.isUnsatisfied()) {
			return Response
					.status(Response.Status.NOT_FOUND)
					.entity("Missing required dependency for this service! No implementation for "
							+ SemanticDefinitionService.class).build();
		}

		// check required arguments
		if (StringUtils.isNullOrEmpty(id) || StringUtils.isNullOrEmpty(type)) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Missing required arguments!").build();
		}

		InstanceReference instance = getInstanceReferense(id, type);
		JSONArray data = null;
		if (instance != null) {
			data = buildData(instance, mode);
		} else {
			return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "Can't find instance type="
					+ type + " with id=" + id);
		}

		return buildResponse(Response.Status.OK, data.toString());
	}

	/**
	 * Creates the relation.
	 * 
	 * @param data
	 *            the data
	 * @return the response
	 */

	@Path("create")
	@POST
	public Response createRelation(String data) {
		return createRelationInternal(data, true);
	}

	/**
	 * Creates the relation.
	 * 
	 * @param data
	 *            the data
	 * @param fireEvent
	 *            indicates whether a {@link RelationCreateEvent} should be fired
	 * @return the response
	 */
	@SuppressWarnings("unchecked")
	private Response createRelationInternal(String data, boolean fireEvent) {
		if (debug) {
			log.debug("CMFWeb: ObjectsRelationsRestService.createRelation data: {}", data);
		}

		if (StringUtils.isNullOrEmpty(data)) {
			return buildResponse(Response.Status.BAD_REQUEST,
					"There are missing or wrong request parameters for create relation operation!");
		}
		try {
			JSONObject jsonData = new JSONObject(data);

			Boolean system = JsonUtil.getBooleanValue(jsonData, "system");
			if (system == null) {
				system = Boolean.FALSE;
			}

			Serializable currentUser = getCurrentUser();
			if (currentUser == null) {
				return buildResponse(Response.Status.UNAUTHORIZED, "No logged in user!");
			}

			String relationType = JsonUtil.getStringValue(jsonData, "relType");
			// if no selected items, we can't create links
			if (StringUtils.isNullOrEmpty(relationType)) {
				return buildResponse(Response.Status.BAD_REQUEST,
						"Expected relation type is missing!");
			}

			// check if the relation is allowed to be created by user.
			if (isRelationAllowed(relationType)) {
				return buildResponse(
						Response.Status.BAD_REQUEST,
						"You cannot create manually relationships of this type. Please, use the respective system functions.");
			}

			String selectedItemsString = JsonUtil.getStringValue(jsonData, "selectedItems");
			// if no selected items, we can't create links
			if (StringUtils.isNullOrEmpty(selectedItemsString)) {
				return buildResponse(Response.Status.BAD_REQUEST,
						"Expected selected items data is missing!");
			}

			JSONObject selectedItems = new JSONObject(selectedItemsString);
			if (selectedItems.length() == 0) {
				return buildResponse(Response.Status.BAD_REQUEST,
						"Expected selected items data is missing!");
			}

			JSONObject okResponse = new JSONObject();
			JSONArray messages = new JSONArray();
			JsonUtil.addToJson(okResponse, "messages", messages);
			for (Iterator<String> iterator = selectedItems.keys(); iterator.hasNext();) {
				String key = iterator.next();
				JSONObject item = (JSONObject) JsonUtil.getValueOrNull(selectedItems, key);

				String targetId = JsonUtil.getStringValue(item, "targetId");
				String targetType = JsonUtil.getStringValue(item, "targetType");
				String destinationId = JsonUtil.getStringValue(item, "destId");
				String destinationType = JsonUtil.getStringValue(item, "destType");

				log.debug(
						"CMFWeb: ObjectsRelationsRestService.createRelation relType[{}] for target type[{}] with id[{}] and destination type[{}] with id[{}]",
						new Object[] { relationType, targetType, targetId, destinationType,
								destinationId });

				InstanceReference targetReference = createInstanceReference(targetId, targetType);
				boolean canCreateLink = true;
				if (targetReference == null) {
					canCreateLink = false;
					log.debug(
							"CMFWeb: ObjectsRelationsRestService.createRelation - Can't find target instance type[{}] with id[{}]",
							targetType, targetId);
					return buildResponse(Response.Status.BAD_REQUEST,
							"Expected target instance data is wrong or missing!");
				}

				InstanceReference destinationReference = createInstanceReference(destinationId,
						destinationType);
				if (destinationReference == null) {
					canCreateLink = false;
					log.debug(
							"CMFWeb: ObjectsRelationsRestService.createRelation - Can't find destination instance type[{}] with id[{}]",
							targetType, targetId);
					return buildResponse(Response.Status.BAD_REQUEST,
							"Expected destination instance data is wrong or missing!");
				}

				if (EqualsHelper.nullSafeEquals(targetReference, destinationReference)) {
					// CS-479: added check to forbid creating relation when the source and
					// destination are the same
					// add returning information message to user that one of the relations was
					// not created due to this limitation
					messages.put("Skipped creation of reference to current instance!");
					canCreateLink = false;
					log.warn("CMFWeb: ObjectsRelationsRestService.createRelation - Trying to create relation with itself: source and destinations are the same id[{}]. Ignoring relation!"
							+ targetReference.getIdentifier());
				}

				if (canCreateLink) {
					// for system links we use the default
					Map<String, Serializable> properties = LinkConstants.DEFAULT_SYSTEM_PROPERTIES;
					if (system.compareTo(Boolean.FALSE) == 0) {
						// for user created links we pass in the current user
						properties = CollectionUtils.createHashMap(1);
						properties.put(DefaultProperties.CREATED_BY, currentUser);
					}

					// if provided reverse link type, we use it
					String reverseLinkType = JsonUtil.getStringValue(jsonData, "reverseRelType");
					if (StringUtils.isNullOrEmpty(reverseLinkType)) {
						reverseLinkType = getReverseLinkType(relationType);
					}

					Pair<Serializable, Serializable> pair = linkService.link(targetReference,
							destinationReference, relationType, reverseLinkType, properties);
					if (pair.getFirst() == null) {
						log.debug(
								"CMFWeb: ObjectsRelationsRestService.createRelation - Can't create relation of type[{}] for target type[{}] with id[{}] and destination type[{}] with id[{}]",
								new Object[] { relationType, targetType, targetId, destinationType,
										destinationId });
						return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
								"Can't create relation due to internal error!");
					}
					if (fireEvent) {
						eventService.fire(new RelationCreateEvent(targetReference.getIdentifier(),
								destinationReference.getIdentifier(), relationType, pair.getFirst()
										.toString()));
					}

				}

			}

			return buildResponse(Status.OK, okResponse.toString());
		} catch (JSONException e) {
			log.error("", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error!")
					.build();
		}
	}

	/**
	 * Checks if is relation allowed for creation by the user.
	 * 
	 * @param relationType
	 *            the relation type
	 * @return true, if is relation allowed
	 */
	private boolean isRelationAllowed(String relationType) {
		// CS-378: forbid parent/child link creation
		return relationType.endsWith("hasChild") || relationType.endsWith("partOf")
				|| relationType.endsWith("hasThumbnail") || relationType.endsWith("isThumbnailOf")
				|| relationType.endsWith("hasPrimaryImage")
				|| relationType.endsWith("isPrimaryImageOf");
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
			log.debug("CMFWeb: ObjectsRelationsRestService.updateRelation relationId[{}]",
					relationId);
		}
		if (StringUtils.isNullOrEmpty(relationId)) {
			return buildResponse(Response.Status.BAD_REQUEST, "Relation id is required!");
		}
		JSONObject jsonData;
		JSONObject selectedItems;
		try {
			jsonData = new JSONObject(data);
			String relationType = JsonUtil.getStringValue(jsonData, "relType");
			if (isRelationAllowed(relationType)) {
				return buildResponse(Response.Status.BAD_REQUEST,
						"The requested relation is forbidden!");
			}
			selectedItems = jsonData.getJSONObject("selectedItems").getJSONObject("0");
			eventService.fire(new RelationChangeEvent(selectedItems.getString("targetId"),
					selectedItems.getString("destId"), relationType, relationId));
		} catch (JSONException e) {
			log.error("", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Server error!")
					.build();
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
			log.debug("CMFWeb: ObjectsRelationsRestService.deactivateRelation relationId[{}]",
					relationId);
		}

		if (StringUtils.isNullOrEmpty(relationId)) {
			return buildResponse(Response.Status.BAD_REQUEST, "Relation id is required!");
		}
		linkService.removeLinkById(relationId);
		eventService.fire(new RelationDeleteEvent(instanceId, toId, relType, relationId));
		return buildResponse(Response.Status.OK, "OK");
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
			log.trace("CMFWeb: RelationsRestService.deactivateRelation: request: {}", data);
		}
		if (StringUtils.isNullOrEmpty(data)) {
			return buildResponse(Response.Status.BAD_REQUEST,
					"There are missing or wrong request parameters for deactivate relation operation!");
		}
		return buildResponse(Response.Status.OK, "OK");
	}

	/**
	 * Gets the reverse link type. If no reverse link is defined in semantic model no reverse link
	 * will be created
	 * 
	 * @param relationType
	 *            the relation type
	 * @return the reverse link type
	 */
	private String getReverseLinkType(String relationType) {
		PropertyInstance relation = semanticDefinitionService.get().getRelation(relationType);
		String inverse = relationType;
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
			DataTypeDefinition dataTypeDefinition = dictionaryService
					.getDataTypeDefinition(instanceType);
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
	 * @return the jSON object representing the data store
	 */
	protected JSONArray buildData(InstanceReference instance, String mode) {
		JSONArray data = new JSONArray();

		if (instance == null) {
			return data;
		}

		if ("null".equals(instance.getIdentifier())) {
			return data;
		}
		Resource currentUser = getCurrentUser();
		// no logged in user
		if ((currentUser == null) || semanticDefinitionService.isUnsatisfied()) {
			return data;
		}
		Map<String, PropertyInstance> relationsMap = semanticDefinitionService.get()
				.getRelationsMap();

		if (MODE_ALL.equalsIgnoreCase(mode) || MODE_OUTGOING.equalsIgnoreCase(mode)) {
			buildRelationData(data,
					linkService.convertToLinkInstance(linkService.getLinks(instance), true),
					relationsMap, currentUser, false);
		}
		if (MODE_ALL.equalsIgnoreCase(mode) || MODE_INGOING.equalsIgnoreCase(mode)) {
			// returns the relations linked to the current instance - probably good idea to move it
			// to separate table
			buildRelationData(data,
					linkService.convertToLinkInstance(linkService.getLinksTo(instance), true),
					relationsMap, currentUser, MODE_ALL.equalsIgnoreCase(mode));
		}

		return data;
	}

	/**
	 * Builds the relation data.
	 * 
	 * @param target
	 *            the target
	 * @param links
	 *            the links
	 * @param relationsMap
	 *            the relations map
	 * @param currentUser
	 *            the current user
	 * @param isFrom
	 *            the is from
	 */
	private void buildRelationData(JSONArray target, Collection<LinkInstance> links,
			Map<String, PropertyInstance> relationsMap, Resource currentUser, boolean isFrom) {
		for (LinkInstance link : links) {
			// REVIEW: skip section instances to not be shown in relations
			// maybe later we will not get them at all
			if ((link.getTo() instanceof SectionInstance)
					|| (link.getFrom() instanceof SectionInstance)) {
				continue;
			}
			com.sirma.itt.emf.instance.model.Instance instance = isFrom ? link.getFrom() : link
					.getTo();

			// if there is no target instance, no need to cuntinue building result
			if (instance == null) {
				continue;
			}

			String instanceHeader = (String) instance.getProperties().get(
					DefaultProperties.HEADER_COMPACT);
			// does not support instances without headers
			if (instanceHeader == null) {
				continue;
			}

			JSONObject linkData = new JSONObject();
			JsonUtil.addToJson(linkData, "linkId", link.getId());
			JsonUtil.addToJson(linkData, "linkType", link.getIdentifier());

			String linkTitle = getLinkTitle(relationsMap, link.getIdentifier());
			String plainText = HTML_TAG.matcher(instanceHeader).replaceAll("");

			if (isFrom) {
				JsonUtil.addToJson(linkData, "name", instanceHeader);
				JsonUtil.addToJson(linkData, "value", linkTitle);
				// we should have a field which doesn't contain html tags but only plain text
				JsonUtil.addToJson(linkData, "plainText", linkTitle);

				JsonUtil.addActions(linkData, Collections.<Action> emptySet());
				JsonUtil.addToJson(linkData, "editable", false);
			} else {
				JsonUtil.addToJson(linkData, "name", linkTitle);
				JsonUtil.addToJson(linkData, "value", instanceHeader);
				// we should have a field which doesn't contain html tags but only plain text
				// we should filter by plainText field because the value field can contain
				// html tags that may be invisible for the user
				JsonUtil.addToJson(linkData, "plainText", plainText);
				Set<Action> actions = authorityService.getAllowedActions(
						currentUser.getIdentifier(), link, "");
				JsonUtil.addActions(linkData, actions);
				JsonUtil.addToJson(linkData, "editable", true);
			}

			JsonUtil.addToJson(linkData, "editType", "text");

			JsonUtil.addToJson(linkData, "toType", instance.getClass().getSimpleName()
					.toLowerCase());
			JsonUtil.addToJson(linkData, "toId", instance.getId());
			JsonUtil.addToJson(linkData, "cls", instance.getClass().getSimpleName().toLowerCase());

			target.put(linkData);
		}
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
			int indexOf = title.indexOf("#");
			if (indexOf > 0) {
				title = title.substring(indexOf + 1);
			} else {
				indexOf = title.indexOf(":");
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
	 * @return The human readable title of the property of {@code null} if propertyInstance is
	 *         {@code null}.
	 */
	private String getPropertyInstanceTitle(PropertyInstance propertyInstance) {
		if (propertyInstance == null) {
			return null;
		}
		return (String) propertyInstance.getProperties().get(DefaultProperties.TITLE);
	}

}
