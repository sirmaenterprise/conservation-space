package com.sirma.itt.emf.rest;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.web.header.InstanceHeaderBuilder;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.BatchEntityLoader;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Base class for all rest service implementations. Wraps common logic usable in all implementations.
 *
 * @author svelikov
 */
public class EmfRestService {

	/** Pattern to match open and closing html 'a' tags. */
	private static final Pattern HTML_A_TAG = Pattern.compile("<(/?)(a[^>]*)>", Pattern.CANON_EQ);

	public static final String EMF_REST_INSTANCE_MISSING_REQUIRED_ARGUMENTS = "emf.rest.instance.missing_required_arguments";
	public static final String INSTANCE_ID = "instanceId";
	public static final String INSTANCE_TYPE = DefaultProperties.INSTANCE_TYPE;
	public static final String DEFINITION_ID = "definitionId";
	public static final String LIBRARY_ID = "libraryId";
	public static final String TYPE = "type";
	public static final String HEADER = "header";
	public static final String DATA = "data";


	protected Logger LOG;
	protected boolean trace;
	protected boolean debug;

	@Inject
	protected TypeMappingProvider allowedChildrenTypeProvider;

	@Inject
	protected InstanceService instanceService;

	@Inject
	protected AuthorityService authorityService;

	@Inject
	protected LabelProvider labelProvider;

	@Inject
	protected DictionaryService dictionaryService;

	@Inject
	protected TypeConverter typeConverter;

	@Inject
	protected ResourceService resourceService;

	@Inject
	protected ServiceRegistry serviceRegistry;

	@Inject
	protected TaskExecutor taskExecutor;

	@Inject
	private InstanceHeaderBuilder treeHeaderBuilder;

	@Inject
	protected SecurityContext securityContext;

	@Inject
	protected UserPreferences userPreferences;

	@Inject
	protected InstanceTypeResolver instanceTypeResolver;

	/**
	 * Instantiates a new emf rest service class.
	 */
	public EmfRestService() {
		LOG = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * Gets the instance class.
	 *
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the instance class
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Instance> Class<T> getInstanceClass(String type) {
		if (type == null) {
			return null;
		}
		DataTypeDefinition selectedInstanceType = dictionaryService.getDataTypeDefinition(type);
		return (Class<T>) selectedInstanceType.getJavaClass();
	}

	/**
	 * Fetch {@link InstanceReference} by type and id.
	 *
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @return the instance or null if any of required arguments is null.
	 */
	public InstanceReference getInstanceReference(String instanceId, String instanceType) {
		InstanceReference reference = null;
		if (StringUtils.isNotNullOrEmpty(instanceId)) {
			if (StringUtils.isNotNullOrEmpty(instanceType)) {
				reference = typeConverter.convert(InstanceReference.class, instanceType);
				reference.setIdentifier(instanceId);
			} else {
				reference = instanceTypeResolver.resolveReference(instanceId).orElse(null);
			}
		}
		return reference;
	}

	/**
	 * Fetch instance by id and type.
	 *
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @return the loaded instance.
	 * @throws BadRequestException
	 *             if either instance id or type are missing.
	 */
	public Instance fetchInstance(String instanceId, String instanceType) {
		boolean nullOrEmptyType = StringUtils.isNullOrEmpty(instanceType);
		boolean nullOrEmptyId = StringUtils.isNullOrEmpty(instanceId);
		if (!nullOrEmptyId && nullOrEmptyType) {
			return instanceTypeResolver.resolveReference(instanceId).map(InstanceReference::toInstance).orElse(null);
		}
		if (nullOrEmptyType || nullOrEmptyId) {
			BadRequestException e = new BadRequestException("Missing required parameters for instance load");
			if (nullOrEmptyId) {
				e.getMessages().put(INSTANCE_ID, INSTANCE_ID + " is required for loading an instance");
			}
			if (nullOrEmptyType) {
				e.getMessages().put(INSTANCE_TYPE, INSTANCE_TYPE + " is required for loading an instance");
			}

			throw e;
		}

		InstanceReference reference = getInstanceReference(instanceId, instanceType);
		return typeConverter.convert(InitializedInstance.class, reference).getInstance();
	}

	/**
	 * Loads an instance by dbId.
	 *
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type of the instance
	 * @param id
	 *            the dbId
	 * @return the instance
	 */
	// FIXME: it's not really internal if it's public, isn't it?
	public <T extends Instance> T loadInstanceInternal(Class<T> type, Serializable id) {
		InstanceService service = serviceRegistry.getInstanceService(type);
		try {
			return type.cast(service.loadByDbId(id));
		} catch (ClassCastException e) {
			LOG.trace("Invalid instance type. Expected {}", type, e);
			return null;
		}
	}

	/**
	 * Load instance internal.
	 *
	 * @param type
	 *            the type
	 * @param id
	 *            the id
	 * @return the t
	 */
	protected Instance loadInstanceInternal(String type, String id) {
		if (type == null) {
			return instanceTypeResolver.resolveReference(id).map(InstanceReference::toInstance).orElse(null);
		}
		InstanceReference reference;
		try {
			reference = typeConverter.convert(InstanceReference.class, type);
		} catch (TypeConversionException e) {
			LOG.error("Error in type conversion", e);
			return null;
		}
		reference.setIdentifier(id);
		InitializedInstance instance = typeConverter.convert(InitializedInstance.class, reference);
		return instance.getInstance();
	}

	/**
	 * Filter allowed children for given definition type.
	 *
	 * @param <I>
	 *            the generic type
	 * @param target
	 *            the target
	 * @param sectionInstances
	 *            the section instances
	 * @return the list
	 */
	protected <I extends Instance> List<I> filterAllowedChildren(Instance target, List<I> sectionInstances) {
		List<I> filteredSections = new LinkedList<>();
		String identifier = target.getIdentifier();
		String typeByInstance = allowedChildrenTypeProvider.getTypeByInstance(target.getClass());
		for (I sectionInstance : sectionInstances) {
			boolean childAllowed = instanceService.isChildAllowed(sectionInstance, typeByInstance, identifier);
			if (childAllowed) {
				filteredSections.add(sectionInstance);
			}
		}
		return filteredSections;
	}

	/**
	 * Load instances is separated for testability.
	 *
	 * @param itemsForLoad
	 *            the items for load
	 * @return the collection
	 */
	public Collection<Instance> loadInstances(List<InstanceReference> itemsForLoad) {
		Map<InstanceReference, Instance> items = BatchEntityLoader.loadAsMapFromReferences(itemsForLoad,
				serviceRegistry, taskExecutor);
		return items.values();
	}

	/**
	 * Converts instance to json object.
	 *
	 * @param instance
	 *            the instance
	 * @return the jSON object
	 */
	public JSONObject convertInstanceToJSON(Instance instance) {
		JSONObject item = new JSONObject();
		if (instance != null) {
			Map<String, Serializable> properties = instance.getProperties();
			if (properties == null) {
				// for some reason some of the objects does not have properties this should not
				// happen
				LOG.warn("Search returned an instance without properties: " + instance);
			} else {
				JsonUtil.addToJson(item, "identifier", properties.get(DefaultProperties.UNIQUE_IDENTIFIER));
				JsonUtil.addToJson(item, "dbId", instance.getId());
				JsonUtil.addToJson(item, "identifier", instance.getIdentifier());
				JsonUtil.addToJson(item, "name", properties.get("uri"));
				JsonUtil.addToJson(item, "title", properties.get(DefaultProperties.TITLE));
				// used to render the generic icons for every result item in the web page
				JsonUtil.addToJson(item, "type", instance.type().getCategory());
				JsonUtil.addToJson(item, "icon",
						treeHeaderBuilder.getIcon(instance, DefaultProperties.HEADER_DEFAULT, "bigger", false));
				// as described in xml definition
				JsonUtil.addToJson(item, DefaultProperties.HEADER_DEFAULT,
						properties.get(DefaultProperties.HEADER_DEFAULT));
			}
		}
		return item;
	}

	/**
	 * Build a response object.
	 *
	 * @param status
	 *            The response status code.
	 * @param entity
	 *            Entity object.
	 * @return Created response object.
	 */
	public Response buildResponse(Status status, Object entity) {
		if (status == null) {
			return null;
		}
		if (entity == null) {
			return Response.status(status).build();
		}
		return Response.status(status).entity(entity).build();
	}

	/**
	 * Builds the bad request response.
	 *
	 * @param entity
	 *            the entity
	 * @return the response
	 */
	public Response buildBadRequestResponse(Object entity) {
		return buildResponse(Status.BAD_REQUEST, entity);
	}

	/**
	 * Builds the error response.
	 *
	 * @param entity
	 *            the entity
	 * @return the response
	 */
	public Response buildErrorResponse(Object entity) {
		return buildResponse(Status.INTERNAL_SERVER_ERROR, entity);
	}

	/**
	 * Builds the ok response.
	 *
	 * @param entity
	 *            the entity
	 * @return the response
	 */
	public Response buildOkResponse(Object entity) {
		return buildResponse(Status.OK, entity);
	}

	/**
	 * Gets the current user or <code>null</code> if there is no logged in user.
	 *
	 * @return the current user
	 */
	// FIXME: rename to getFreshCurrentUser
	public Resource getCurrentUser() {
		// NOTE: there will be exception if user is not logged in or called without session
		com.sirma.itt.seip.security.User currentUser = securityContext.getAuthenticated();
		// return a copy of the logged in user, because...?
		return (Resource) resourceService.loadByDbId(currentUser.getSystemId());
	}

	/**
	 * Convert link to span.
	 *
	 * @param string
	 *            the string
	 * @return the string
	 */
	public String convertLinkToSpan(String string) {
		String result = string;
		Matcher matcher = HTML_A_TAG.matcher(result);
		result = matcher.replaceAll("<$1span>");
		return result;
	}

	/**
	 * Getter method for typeConverter.
	 *
	 * @return the typeConverter
	 */
	public TypeConverter getTypeConverter() {
		return typeConverter;
	}

	/**
	 * Gets the current logged user.
	 *
	 * @return the current logged user
	 */
	public User getCurrentLoggedUser() {
		return (User) securityContext.getAuthenticated();
	}

	/**
	 * Gets the language.
	 *
	 * @param language
	 *            the language
	 * @return the language
	 */
	protected String getlanguage(String language) {
		String currentLanguage = language;
		if (StringUtils.isNullOrEmpty(currentLanguage)) {
			currentLanguage = userPreferences.getLanguage();
		}
		return currentLanguage;
	}

	/**
	 * Checks the request data, tries to parse it as a JSON. If the id and the type for instance are available, builds
	 * instance reference with them.
	 *
	 * @param data
	 *            the request data, which should contain the id and the type of the instance, which will be processed
	 * @return instance reference, build from the type and the id of the instance, which is processed or <b>null</b> if
	 *         the data isn't in the correct JSON format and if some of the parameters are missing in the data
	 */
	public InstanceReference extractInstanceReference(String data) {
		if (StringUtils.isNotNullOrEmpty(data)) {
			return typeConverter.convert(InstanceReference.class, data);
		}
		return null;
	}

	/**
	 * Converts collection of instance references to JSONArray.
	 *
	 * @param references
	 *            collection of instance references, which will be converted
	 * @return passed instance references converted to JSONArray
	 */
	public JSONArray convertInstanceReferencesToJSON(Collection<InstanceReference> references) {
		Collection<JSONObject> convert = typeConverter.convert(JSONObject.class, references);
		return new JSONArray(convert);
	}

}
