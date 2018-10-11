package com.sirma.itt.emf.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
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

	public static final String INSTANCE_ID = "instanceId";
	public static final String INSTANCE_TYPE = DefaultProperties.INSTANCE_TYPE;
	public static final String DEFINITION_ID = "definitionId";
	public static final String TYPE = "type";
	public static final String HEADER = "header";
	public static final String DATA = "data";

	protected Logger LOG;
	protected boolean trace;
	protected boolean debug;

	@Inject
	protected InstanceService instanceService;

	@Inject
	protected AuthorityService authorityService;

	@Inject
	protected LabelProvider labelProvider;

	@Inject
	protected DefinitionService definitionService;

	@Inject
	protected TypeConverter typeConverter;

	@Inject
	protected ResourceService resourceService;

	@Inject
	protected TaskExecutor taskExecutor;

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
		DataTypeDefinition selectedInstanceType = definitionService.getDataTypeDefinition(type);
		return (Class<T>) selectedInstanceType.getJavaClass();
	}

	/**
	 * Fetch {@link InstanceReference} by type and id.
	 *
	 * @param instanceId
	 *            the instance id
	 * @return the instance or null if any of required arguments is null.
	 */
	public InstanceReference getInstanceReference(String instanceId) {
		InstanceReference reference = null;
		if (StringUtils.isNotBlank(instanceId)) {
			reference = instanceTypeResolver.resolveReference(instanceId).orElse(null);
		}
		return reference;
	}

	/**
	 * Fetch instance by id and type.
	 *
	 * @param instanceId
	 *            the instance id
	 * @return the loaded instance.
	 * @throws BadRequestException
	 *             if either instance id or type are missing.
	 */
	public Instance fetchInstance(String instanceId) {
		if (StringUtils.isBlank(instanceId)) {
			BadRequestException e = new BadRequestException("Missing required parameters for instance load");
			e.getMessages().put(INSTANCE_ID, INSTANCE_ID + " is required for loading an instance");
			throw e;
		}
		InstanceReference reference = getInstanceReference(instanceId);
		if (reference == null) {
			return null;
		}
		return reference.toInstance();
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
	public Resource getFreshCurrentUser() {
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
		if (StringUtils.isBlank(currentLanguage)) {
			currentLanguage = userPreferences.getLanguage();
		}
		return currentLanguage;
	}
}
