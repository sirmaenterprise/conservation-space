package com.sirma.itt.emf.rest;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.web.header.InstanceHeaderBuilder;

/**
 * Base class for all rest service implementations. Wraps common logic usable in all
 * implementations.
 * 
 * @author svelikov
 */
public class EmfRestService {

	/** Pattern to match open and closing html 'a' tags. */
	private static final Pattern HTML_A_TAG = Pattern.compile("<(/?)(a[^>]*)>", Pattern.CANON_EQ);

	public static final String INSTANCE_TYPE = "instanceType";
	public static final String INSTANCE_ID = "instanceId";
	public static final String HEADER = "header";

	protected Logger log;
	protected boolean trace;
	protected boolean debug;

	@Inject
	protected LabelProvider labelProvider;

	@Inject
	protected DictionaryService dictionaryService;

	@Inject
	protected TypeConverter typeConverter;

	@Inject
	protected javax.enterprise.inject.Instance<AuthenticationService> authenticationService;

	@Inject
	protected ResourceService resourceService;

	@Inject
	protected ServiceRegister serviceRegister;

	@Inject
	protected TaskExecutor taskExecutor;

	@Inject
	private InstanceHeaderBuilder treeHeaderBuilder;

	/**
	 * Instantiates a new emf rest service class.
	 */
	public EmfRestService() {
		log = LoggerFactory.getLogger(this.getClass());
		trace = log.isTraceEnabled();
		debug = log.isDebugEnabled();
	}

	/**
	 * Retrieves the java class for given instance type.
	 * 
	 * @param type
	 *            the instance type ("caseinstance" for example.)
	 * @return the instance class
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends Instance> getInstanceClass(String type) {
		DataTypeDefinition selectedInstanceType = dictionaryService.getDataTypeDefinition(type);
		return (Class<? extends Instance>) selectedInstanceType.getJavaClass();
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
	public InstanceReference getInstanceReferense(String instanceId, String instanceType) {
		InstanceReference reference = null;
		if (StringUtils.isNotNullOrEmpty(instanceId) && StringUtils.isNotNullOrEmpty(instanceType)) {
			reference = typeConverter.convert(InstanceReference.class, instanceType);
			reference.setIdentifier(instanceId);
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
	 * @return the instance or null if any of required arguments is null.
	 */
	public Instance fetchInstance(String instanceId, String instanceType) {
		Instance instance = null;
		if (StringUtils.isNotNullOrEmpty(instanceId) && StringUtils.isNotNullOrEmpty(instanceType)) {
			InstanceReference reference;
			try {
				reference = typeConverter.convert(InstanceReference.class, instanceType);
			} catch (TypeConversionException e) {
				log.error("", e);
				return null;
			}
			reference.setIdentifier(instanceId);
			instance = typeConverter.convert(InitializedInstance.class, reference).getInstance();
		}
		return instance;
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
	public <T extends Instance> T loadInstanceInternal(Class<T> type, Serializable id) {
		InstanceService<Instance, DefinitionModel> service = serviceRegister
				.getInstanceService(type);
		try {
			return type.cast(service.loadByDbId(id));
		} catch (ClassCastException e) {
			return null;
		}
	}

	/**
	 * Load instances is separated for testability.
	 * 
	 * @param itemsForLoad
	 *            the items for load
	 * @return the collection
	 */
	public Collection<Instance> loadInstances(List<InstanceReference> itemsForLoad) {
		Map<InstanceReference, Instance> items = BatchEntityLoader.loadAsMapFromReferences(
				itemsForLoad, serviceRegister, taskExecutor);
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
		JSONObject item = null;
		if (instance != null) {
			Map<String, Serializable> properties = instance.getProperties();
			if (properties == null) {
				// for some reason some of the objects does not have properties
				// this should not happen
				log.warn("Search returned an instance without properties: " + instance);
			} else {
				item = new JSONObject();
				JsonUtil.addToJson(item, "identifier",
						properties.get(DefaultProperties.UNIQUE_IDENTIFIER));
				JsonUtil.addToJson(item, "dbId", instance.getId());
				JsonUtil.addToJson(item, "name", properties.get("uri"));
				JsonUtil.addToJson(item, "title", properties.get(DefaultProperties.TITLE));
				// used to render the generic icons for every result item in
				// the web page
				JsonUtil.addToJson(item, "type", instance.getClass().getSimpleName().toLowerCase());
				JsonUtil.addToJson(item, "icon", treeHeaderBuilder.getIcon(instance,
						DefaultProperties.HEADER_DEFAULT, "bigger", false));
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
	 * Gets the current user or <code>null</code> if there is no logged in user.
	 * 
	 * @return the current user
	 */
	public Resource getCurrentUser() {
		try {
			// NOTE: there will be exception if user is not logged in or called without session
			User currentUser = authenticationService.get().getCurrentUser();
			if (currentUser != null) {
				// return a copy of the logged in user, because...?
				return resourceService.loadByDbId(currentUser.getId());
			}
		} catch (ContextNotActiveException e) {
			log.warn("No active context found to determine the current user!");
		}
		return null;
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
}
