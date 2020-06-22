package com.sirma.itt.emf.instance;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import org.apache.commons.lang3.StringUtils;
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
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.revision.RevisionService;
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

	private static final String EMF_REST_INSTANCE_STATUS_CHECK_MISSING_REQUIRED_DATA = "emf.rest.instance.status.check.missing_required_data";
	private static final String EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_REQUIRED_ARGUMENTS = "emf.rest.instance.properties.load.missing_required_arguments";
	private static final String EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_DRAFT = "emf.rest.instance.properties.load.missing_draft";
	private static final String EMF_REST_INSTANCE_REVISION_LOAD_MISSING_REQUIRED_ARGUMENTS = "emf.rest.instance.revision.load.missing_required_arguments";
	private static final String EMF_REST_INSTANCE_INVALID_INSTANCE_TYPE = "emf.rest.instance.invalid_instance_type";
	private static final String STATUS = "status";
	private static final String IS_ASCENDING = "isAscending";
	private static final String PROPERTY = "property";

	@Inject
	private LinkService linkService;

	@Inject
	protected CodelistService codelistService;

	@Inject
	private RevisionService revisionService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Inject
	private InstanceContextService contextService;

	/**
	 * REST method for retrieving all revisions for specific instance. Based on their identifier and type.
	 *
	 * @param instanceId
	 *            current instance identifier
	 * @param ascending
	 *            define order flag
	 * @return response with revisions
	 * @throws JSONException
	 */
	@GET
	@Path("/revisions")
	public Response getRevisions(@QueryParam(INSTANCE_ID) String instanceId,
			@QueryParam(IS_ASCENDING) boolean ascending) {
		if (StringUtils.isBlank(instanceId)) {
			return buildBadRequestResponse(
					labelProvider.getValue(EMF_REST_INSTANCE_REVISION_LOAD_MISSING_REQUIRED_ARGUMENTS));
		}

		JSONObject response = new JSONObject();
		InstanceReference instanceReference = getInstanceReference(instanceId);
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
	 * @return the instance status
	 */
	@Path("status")
	@GET
	public Response getInstanceStatus(@QueryParam(INSTANCE_ID) String instanceId) {
		if (StringUtils.isBlank(instanceId)) {
			return buildResponse(Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_STATUS_CHECK_MISSING_REQUIRED_DATA));
		}
		Instance instance = fetchInstance(instanceId);
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
	 * @param properties
	 *            the properties to be retrieved
	 * @return the instance properties as a json object with property name as key and property values as value
	 */
	@Path("properties")
	@GET
	public Response getInstanceProperties(@QueryParam(INSTANCE_ID) String instanceId,
			@QueryParam(PROPERTY) List<String> properties) {
		if (StringUtils.isBlank(instanceId)) {
			return buildResponse(Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_REQUIRED_ARGUMENTS));
		}
		Instance instance = fetchInstance(instanceId);

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
	 * @param properties
	 *            the properties we want to retrieve for the given instance.
	 * @return the instance properties as a json object with property name as key and property values as value
	 */
	@Path("properties/original")
	@GET
	public Response getOriginalInstanceProperties(@QueryParam(INSTANCE_ID) String instanceId,
			@QueryParam(PROPERTY) List<String> properties) {
		if (StringUtils.isBlank(instanceId)) {
			return buildResponse(Status.BAD_REQUEST,
					labelProvider.getValue(EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_REQUIRED_ARGUMENTS));
		}
		Instance instance = fetchInstance(instanceId);
		Instance draft = revisionService.getRevisionOf(instance);

		JSONObject responseData = buildPropertiesForInstance(draft, properties);
		if (responseData != null) {
			return buildResponse(Status.OK, responseData.toString());
		}
		return buildResponse(Status.BAD_REQUEST,
				labelProvider.getValue(EMF_REST_INSTANCE_PROPERTIES_LOAD_MISSING_DRAFT));
	}

	private static JSONObject buildPropertiesForInstance(Instance instance, List<String> properties) {
		if (instance == null || properties == null) {
			return null;
		}

		JSONObject responseData = new JSONObject();
		for (String property : properties) {
			Object value = instance.getProperties().get(property);
			JsonUtil.addToJson(responseData, property, value);
		}
		return responseData;
	}

	/**
	 * Get the type of the owning instance of given instance.
	 *
	 * @param instanceId
	 *            - instance id
	 * @return the response - type of an owning instance
	 */
	@Path("parentInstanceType")
	@GET
	public Response getParentInstanceType(@QueryParam(INSTANCE_ID) String instanceId) {
		JSONObject response = new JSONObject();
		if (StringUtils.isBlank(instanceId)) {
			return buildResponse(Response.Status.BAD_REQUEST, "Request params are missing");
		}

		Instance instance = fetchInstance(instanceId);
		Instance context = contextService.getContext(instance).map(InstanceReference::toInstance).orElse(null);

		if (context == null) {
			return buildResponse(Response.Status.BAD_REQUEST, "Context instance is missing");
		}
		// TODO heavy refactor
		// Get owning instance type - RDF type if it's object or definition id
		String type = context.getString(DefaultProperties.TYPE);
		if (context.getClass().isAssignableFrom(getInstanceClass("objectinstance"))) {
			type = (String) context.getProperties().get(DefaultProperties.SEMANTIC_TYPE);
			ClassInstance clazz = semanticDefinitionService.getClassInstance(type);
			type = (String) clazz.getProperties().get("instance");
		}

		JsonUtil.addToJson(response, "sourceId", context.getId());
		JsonUtil.addToJson(response, "sourceType", context.type().getCategory());
		JsonUtil.addToJson(response, "subtype", type);

		return buildResponse(Response.Status.OK, response.toString());
	}

	/**
	 * Retrieves the allowed children for a specific instance according to it's definition.
	 *
	 * @param instanceId
	 *            Instance identifier.
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
			@QueryParam("childType") String childType) {

		Instance instance = fetchInstance(instanceId);
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

}
