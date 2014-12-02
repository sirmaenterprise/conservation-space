/**
 * Copyright (c) 2013 17.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.idoc.web.document;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esotericsoftware.minlog.Log;
import com.sirma.cmf.web.SelectorItem;
import com.sirma.cmf.web.document.DocumentFileTypes;
import com.sirma.cmf.web.util.LabelBuilder;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.VersionInfo;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.NonPersistentProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.DraftService;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DefinitionUtil;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.Lockable;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.rest.EmfApplicationException;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.emf.rest.model.ViewInstance;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.sanitize.ContentSanitizer;
import com.sirma.itt.emf.web.util.DateUtil;

/**
 * REST services for working with intelligent document. Used mostly by the client-side to fetch and
 * store document data.
 * 
 * @author Adrian Mitev
 */
@Stateless
@Path("/intelligent-document")
@Produces(MediaType.APPLICATION_JSON)
public class IntelligentDocumentRestService {

	private static final Operation CREATE_OBJECT = new Operation(ActionTypeConstants.CREATE_OBJECT);

	/** The date util. */
	@Inject
	private DateUtil dateUtil;

	/** The label builder. */
	@Inject
	private LabelBuilder labelBuilder;

	/** The authentication service. */
	@Inject
	private AuthenticationService authenticationService;

	/** The idoc service. */
	@Inject
	private IntelligentDocumentService idocService;

	/** The document service. */
	@Inject
	private DocumentService documentService;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The service register. */
	@Inject
	private ServiceRegister serviceRegister;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The instance service. */
	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;

	@Inject
	private DocumentFileTypes documentFileTypes;

	@Inject
	private ContentSanitizer idocSanitizer;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private DraftService draftService;

	private static final Logger LOGGER = Logger.getLogger(IntelligentDocumentRestService.class);

	/** The Constant ESCAPED_PROPERTY_KEYS. */
	private static final Set<String> ESCAPED_PROPERTY_KEYS;

	static {
		ESCAPED_PROPERTY_KEYS = new HashSet<>();
		ESCAPED_PROPERTY_KEYS.add(DocumentProperties.CREATED_BY);
		ESCAPED_PROPERTY_KEYS.add(DocumentProperties.MODIFIED_BY);
		ESCAPED_PROPERTY_KEYS.add(DocumentProperties.TITLE);
	}

	/**
	 * Load a document and it's properties.
	 * 
	 * @param id
	 *            Instance id.
	 * @param type
	 *            Instance type.
	 * @param version
	 *            Version of the view to load.
	 * @return JSON representation of the instance.
	 */
	@GET
	@Path("/{id}")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public RestInstance load(@PathParam("id") String id, @QueryParam("type") String type,
			@QueryParam("version") String version) {
		Instance instance = loadInstanceInternal(type, id);
		if (instance != null) {
			if (StringUtils.isNotBlank(version)) {
				// putting this property, so the converters know that an old
				// version of the view should be loaded.
				instance.getProperties().put(NonPersistentProperties.LOAD_VIEW_VERSION, version);
			}
			RestInstance restInstance = convertInstanceToResponce(instance);

			initializeLockedByProperties(instance, restInstance.getProperties(), false);
			return restInstance;
		}
		// throw exception
		return null;
	}

	/**
	 * Fetches document properties for visualization.
	 * 
	 * @param documentId
	 *            of the document to fetch.
	 * @param type
	 *            the type
	 * @return fetched properties
	 */
	@GET
	@Path("/{documentId}/properties")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Object getProperties(@PathParam("documentId") String documentId,
			@QueryParam("type") String type) {
		InstanceReference reference = typeConverter.convert(InstanceReference.class, type);
		reference.setIdentifier(documentId);
		InitializedInstance instance = typeConverter.convert(InitializedInstance.class, reference);
		return getInstanceProperties(instance.getInstance()).toString();
	}

	/**
	 * Gets the properties for an instance in general.
	 * 
	 * @param instanceId
	 *            Instance id.
	 * @param type
	 *            Instance type
	 * @return String representing JSON array with the properties.
	 */
	@GET
	@Path("/instance/{id}/properties")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Object loadInstanceProperties(@PathParam("id") String instanceId,
			@QueryParam("type") String type) {
		Instance instance = loadInstanceInternal(type, instanceId);
		if (instance == null) {
			LOGGER.error("No instance found for parameters id=" + instanceId + ", type=" + type);
			return "[]";
		}
		return getInstanceProperties(instance).toString();
	}

	/**
	 * Retrieves the properties for a set of objects.
	 * 
	 * @param objects
	 *            Holds the objects type and id used for retrieving the properties.
	 * 
	 *            <pre>
	 * {
	 * 	"objects": [
	 * 		{ "type": "documentinstance", "dbId": "1",
	 * 		...
	 * 	]
	 * }
	 * </pre>
	 * @return The properties for the objects.
	 */
	@GET
	@Path("/properties")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String loadProperties(@QueryParam("objects") String objects) {
		JSONArray result = new JSONArray();
		try {
			JSONObject objectParams = new JSONObject(objects);
			if (objectParams.has("objects")) {
				JSONArray jsonArray = objectParams.getJSONArray("objects");
				int length = jsonArray.length();
				for (int i = 0; i < length; i++) {
					JSONObject object = jsonArray.getJSONObject(i);
					String type = object.getString("type");
					String id = object.getString("dbId");
					Instance instance = loadInstanceInternal(type, id);
					JSONArray instanceProperties = getInstanceProperties(instance);
					int instancePropertiesLength = instanceProperties.length();
					for (int j = 0; j < instancePropertiesLength; j++) {
						result.put(instanceProperties.get(j));
					}
				}
			}
		} catch (JSONException e) {
			LOGGER.error("Can not convert given string to json object", e);
		}
		return result.toString();
	}

	/**
	 * Updates the properties for an instance.
	 * 
	 * @param instanceId
	 *            Instance id.
	 * @param type
	 *            Instance type
	 * @param properties
	 *            MAp of properties to update
	 * @return the updated properties
	 */
	@POST
	@Path("/instance/{id}/properties")
	@Consumes(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String saveInstanceProperties(@PathParam("id") String instanceId,
			@QueryParam("type") String type, Map<String, ?> properties) {
		Instance instance = loadInstanceInternal(type, instanceId);
		if (instance == null) {
			LOGGER.error("Invalid instance id=" + instanceId + ", type" + type + ", properties="
					+ properties);
			return "{}";
		}
		DefinitionModel instanceDefinition = dictionaryService.getInstanceDefinition(instance);

		Map<String, Serializable> fromRest = propertiesService.convertToInternalModel(properties,
				instanceDefinition);
		properties.keySet().removeAll(fromRest.keySet());
		for (Entry<String, ?> entry : properties.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Serializable) {
				fromRest.put(entry.getKey(), (Serializable) value);
			}
		}
		instance.getProperties().putAll(fromRest);
		// save the instance
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		try {
			instanceService.save(instance, new Operation(ActionTypeConstants.EDIT_DETAILS));
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
		}
		return getInstanceProperties(instance).toString();
	}

	/**
	 * Searches for iDoc sections for given case.
	 * 
	 * @param caseId
	 *            the case id
	 * @return the object sections
	 */
	// TODO: move this in CaseInstanceRestService and merge with '/sections' getObjectSections
	@GET
	@Path("/documentsSections")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getiDocSections(@QueryParam("caseId") String caseId) {
		JSONObject result = new JSONObject();

		List<SectionInstance> caseObjectSections = null;
		CaseInstance caseInstance = (CaseInstance) serviceRegister.getInstanceService(
				CaseInstance.class).loadByDbId(caseId);
		if (caseInstance != null) {
			List<SectionInstance> sections = caseInstance.getSections();
			caseObjectSections = DefinitionUtil.filterByPurpose(sections, null);
		} else {
			LOGGER.error("IntelligentRestService: A case instance was not found!");
		}
		if (caseObjectSections != null) {
			JSONArray values = new JSONArray();
			for (Instance instance : caseObjectSections) {
				JSONObject item = convertInstanceToJSON(instance);
				if (item != null) {
					values.put(item);
				}
			}
			JsonUtil.addToJson(result, "values", values);
		}

		return result.toString();
	}

	/**
	 * Converts instance to json object in the format used for the basic search.
	 * 
	 * @param instance
	 *            the instance
	 * @return the jSON object
	 */
	private JSONObject convertInstanceToJSON(Instance instance) {
		JSONObject item = null;
		if (instance != null) {
			Map<String, Serializable> properties = instance.getProperties();
			if (properties == null) {
				// for some reason some of the objects does not have properties
				// this should not happen
				LOGGER.warn("Search returned an instance without properties: " + instance);
			} else {
				item = new JSONObject();
				try {
					item.put("identifier", properties.get(DefaultProperties.UNIQUE_IDENTIFIER));
					item.put("dbId", instance.getId());
					item.put("name", properties.get("uri"));
					item.put("title", properties.get(DefaultProperties.TITLE));
					// used to render the generic icons for every result item in
					// the web page
					item.put("type", instance.getClass().getSimpleName().toLowerCase());
					// as described in xml definition
					item.put(DefaultProperties.HEADER_DEFAULT,
							properties.get(DefaultProperties.HEADER_DEFAULT));
				} catch (JSONException e) {
					LOGGER.error("Can not convert given string to json object", e);
				}
			}
		}
		return item;
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
	private Instance loadInstanceInternal(String type, String id) {
		InstanceReference reference;
		try {
			reference = typeConverter.convert(InstanceReference.class, type);
		} catch (TypeConversionException e) {
			LOGGER.error("Error in type conversion", e);
			return null;
		}
		reference.setIdentifier(id);
		InitializedInstance instance = typeConverter.convert(InitializedInstance.class, reference);
		return instance.getInstance();
	}

	/**
	 * Gets the type class.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the type class
	 */
	@SuppressWarnings("unchecked")
	private <T extends Instance> Class<T> getTypeClass(String type) {
		if (type == null) {
			return null;
		}
		DataTypeDefinition selectedInstanceType = dictionaryService.getDataTypeDefinition(type);
		return (Class<T>) selectedInstanceType.getJavaClass();
	}

	/**
	 * Loads instance properties from it's definition.
	 * 
	 * @param instance
	 *            Instance to load properties for
	 * @return Json array java object with the properties.
	 */
	private JSONArray getInstanceProperties(Instance instance) {
		JSONArray result = new JSONArray();
		if (instance == null) {
			return result;
		}

		DefinitionModel instanceDefinition = dictionaryService.getInstanceDefinition(instance);
		DataTypeDefinition dataTypeDefinition = dictionaryService.getDataTypeDefinition(instance
				.getClass().getSimpleName().toLowerCase());

		Map<String, Object> convertProperties = convertProperties(instance);
		Set<String> processed = new LinkedHashSet<>();
		convertModelProperties(dataTypeDefinition, instanceDefinition, convertProperties, result,
				processed);
		if (instanceDefinition instanceof RegionDefinitionModel) {
			for (RegionDefinition regionDefinition : ((RegionDefinitionModel) instanceDefinition)
					.getRegions()) {
				convertModelProperties(dataTypeDefinition, regionDefinition, convertProperties,
						result, processed);
			}
		}
		return result;
	}

	/**
	 * Convert model properties.
	 * 
	 * @param dataTypeDefinition
	 *            Instance data type definition
	 * @param instanceDefinition
	 *            the instance definition
	 * @param convertProperties
	 *            the convert properties
	 * @param result
	 *            the resulted josn object
	 * @param processed
	 *            holds all properties processed against the definition model.
	 */
	private void convertModelProperties(DataTypeDefinition dataTypeDefinition,
			DefinitionModel instanceDefinition, Map<String, Object> convertProperties,
			JSONArray result, Set<String> processed) {

		String ownerDomainClass = dataTypeDefinition.getFirstUri();
		for (PropertyDefinition property : instanceDefinition.getFields()) {
			if ((property.getDisplayType() == DisplayType.EDITABLE)
					|| (property.getDisplayType() == DisplayType.READ_ONLY)) {
				JSONObject object = new JSONObject();
				try {
					object.put("domainClass", ownerDomainClass);
					object.put("name", property.getName());
					object.put("label", property.getLabel());
					object.put("value", convertProperties.get(property.getName()));

					if (property.getDisplayType() == DisplayType.EDITABLE) {
						object.put("readOnly", false);
						object.put("editable", true);
					} else {
						object.put("readOnly", true);
						object.put("editable", false);
					}
					object.put("type", property.getDataType().getName());
					if (property.getControlDefinition() != null) {
						object.put("control", property.getControlDefinition().getIdentifier());
					}
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
				result.put(object);
			}
			// remove system and hidden as well
			processed.add(property.getName());
		}
	}

	/**
	 * REVIEW: this should be replaced by the {@link #save(RestInstance)} method
	 * <p>
	 * Save document properties.
	 * 
	 * @param documentId
	 *            ID of the document to be saved.
	 * @param rest
	 *            document to be saved.
	 * @return fetched properties
	 */
	@POST
	@Path("/{documentId}/properties")
	@Consumes(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Object setProperties(@PathParam("documentId") String documentId, RestInstance rest) {

		Instance instance = null;
		if (SequenceEntityGenerator.isPersisted(rest) && (rest.getType() != null)) {
			instance = typeConverter.convert(getTypeClass(rest.getType()), rest);
			RuntimeConfiguration.enable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			try {
				instanceService.save(instance, new Operation(rest.getCurrentOperation()));
			} finally {
				RuntimeConfiguration.disable(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			}
		} else {
			instance = documentService.loadByDbId(documentId);
			if (instance != null) {
				DefinitionModel definitionModel = dictionaryService.getInstanceDefinition(instance);
				instance.getProperties().putAll(
						propertiesService.convertToInternalModel(rest.getProperties(),
								definitionModel));
				instance.getProperties().putAll(
						propertiesService.convertToInternalModel(rest.getViewProperties(),
								definitionModel));
				documentService.updateProperties((DocumentInstance) instance);
			}
		}

		return getInstanceProperties(instance).toString();
	}

	/**
	 * Initializes the default values of an instance.
	 * 
	 * @param type
	 *            document type
	 * @param definitionId
	 *            definition Id
	 * @return String representing JSON array with the properties.
	 */
	@GET
	@Path("/initInstance")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Object initializeNewInstance(@QueryParam("type") String type,
			@QueryParam("definitionId") String definitionId) {
		if (StringUtils.isBlank(type)) {
			return null;
		}
		Class<Instance> typeClass = getTypeClass(type);
		if (typeClass == null) {
			return null;
		}

		DefinitionModel definitionModel;
		InstanceService<Instance, DefinitionModel> service = serviceRegister
				.getInstanceService(typeClass);

		// for document instance for now the default instance creation is more
		// complicated...
		if (DocumentInstance.class.equals(typeClass)) {
			DocumentInstance documentInstance = new DocumentInstance();
			documentInstance.setIdentifier(definitionId);
			documentInstance.setStandalone(true);
			documentInstance.setRevision(0L);
			definitionModel = dictionaryService.getInstanceDefinition(documentInstance);
		} else {
			definitionModel = dictionaryService.getDefinition(service.getInstanceDefinitionClass(),
					definitionId);
		}

		Instance instance = service.createInstance(definitionModel, null, Operation.NO_OPERATION);

		// if (instance instanceof DocumentInstance) {
		// ((DocumentInstance) instance).setStandalone(true);
		// }

		return getInstanceProperties(instance).toString();
	}

	/**
	 * Creates a new intelligent document and saves it to a parent object.
	 * 
	 * @param rest
	 *            contains the data needed for document creation.
	 * @param request
	 *            http request containing url.
	 * @return an object containing the properties of the created object including the document
	 *         identified.
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public RestInstance create(@Context HttpServletRequest request, RestInstance rest) {
		Instance owningInstance = null;

		if (authenticationService.getCurrentUserId() == null) {
			return null;
		}

		// Document without a title should not be saved
		if ("".equals(rest.getProperties().get("title"))) {
			return null;
		}

		if (StringUtils.isNotBlank(rest.getOwningInstanceId())
				&& StringUtils.isNotBlank(rest.getOwningInstanceType())) {
			owningInstance = loadInstanceInternal(rest.getOwningInstanceType(), rest
					.getOwningInstanceId().toString());
		}

		if (owningInstance == null) {
			Log.warn("Creating idoc with owning instance[null], owningInstanceId["
					+ rest.getOwningInstanceId() + "], owningInstanceType["
					+ rest.getOwningInstanceType() + "]");
		}

		if (StringUtils.isNotBlank(rest.getContent())) {
			String origin = getOrigin(request);
			rest.setContent(idocSanitizer.sanitize(rest.getContent(), origin));
		}

		Class<Instance> typeClass = getTypeClass(rest.getType());
		Instance instance = null;
		if ((typeClass != null) && !DocumentInstance.class.isAssignableFrom(typeClass)
				&& (rest.getIdentifier() != null)) {
			instance = typeConverter.convert(typeClass, rest);
		}

		if ((owningInstance instanceof SectionInstance) && (typeClass != null)
				&& DocumentInstance.class.isAssignableFrom(typeClass)) {
			// TODO: refactor the create method of the idoc service
			DocumentInstance newDocumentInstance = documentService.createDocumentInstance(
					(SectionInstance) owningInstance, rest.getDefinitionId());

			if (newDocumentInstance == null) {
				// the document definition is probably not allowed in the
				// section we should create
				// from template
				DocumentDefinitionTemplate definition = dictionaryService.getDefinition(
						DocumentDefinitionTemplate.class, rest.getDefinitionId());
				// TODO: operation should be added if the document should be
				// initialized in a state
				// different from the default one
				newDocumentInstance = documentService.createInstance(
						new DocumentDefinitionRefProxy(definition), owningInstance);
				newDocumentInstance.setStandalone(true);
			}
			// copy properties and content
			Map<String, Serializable> properties = newDocumentInstance.getProperties();
			Map<String, Object> idocProperties = rest.getProperties();

			String fileName = UUID.randomUUID().toString();
			if (!fileName.toLowerCase().endsWith(".html")) {
				fileName += ".html";
			}

			properties.put(DocumentProperties.NAME, fileName);
			properties.put(DocumentProperties.DESCRIPTION,
					(Serializable) idocProperties.get(DocumentProperties.DESCRIPTION));
			properties.put(DocumentProperties.TITLE,
					(Serializable) idocProperties.get(DocumentProperties.TITLE));

			escapeStringProperties(properties);

			// StringEscapeUtils.escapeHtml(str) break cyrillic titles -
			// CMF-6044
			properties.put(DocumentProperties.TITLE,
					StringEscapeUtils.unescapeHtml((String) properties.get("title")));

			DocumentInstance result = idocService.save(newDocumentInstance, rest.getContent());
			// create parent-child relationship with sub-document
			linkToParent(rest.getParentId(), result);
			return convertInstanceToResponce(result);
		} else if ((owningInstance instanceof SectionInstance) && (instance != null)) {

			instance = instanceService.save(instance, CREATE_OBJECT);
			// TODO: this should be changed not to depend on this constant but
			// from the
			// rest.getCurrentOperation()
			instanceService.attach(owningInstance, CREATE_OBJECT, instance);
			return convertInstanceToResponce(instance);
		} else if (instance != null) {
			// set a parent instance
			if ((instance instanceof OwnedModel) && (owningInstance != null)) {
				((OwnedModel) instance).setOwningInstance(owningInstance);
			}
			// TODO: this should be changed not to depend on this constant but
			// from the
			// rest.getCurrentOperation()
			instanceService.save(instance, CREATE_OBJECT);
			return convertInstanceToResponce(instance);
		}

		return rest;
	}

	/**
	 * Updates a document content and saves the case.
	 * 
	 * @param rest
	 *            document to save
	 * @param request
	 *            http request containing url.
	 * @return document content after saving
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public RestInstance save(@Context HttpServletRequest request, RestInstance rest) {

		// TODO: remove and fix the security
		if (authenticationService.getCurrentUserId() == null) {
			return null;
		}

		Class<Instance> typeClass = getTypeClass(rest.getType());
		if (typeClass == null) {
			throw new EmfApplicationException("Type class is null");
		}

		// Document without a title should not be saved
		if (StringUtils.isBlank((String) rest.getProperties().get("title"))) {
			throw new EmfApplicationException("Document title is empty");
		}

		// clean the content before saving
		String origin = getOrigin(request);
		rest.setContent(idocSanitizer.sanitize(rest.getContent(), origin));

		// convert to real instance
		Instance instance = typeConverter.convert(typeClass, rest);
		if (instance == null) {
			return rest;
		}

		unlockInstance(instance);
		instanceService.save(instance, new Operation(rest.getCurrentOperation()));
		draftService.delete(instance, authenticationService.getCurrentUser());

		// return the changes back
		return convertInstanceToResponce(instance);
	}

	/**
	 * Loads the versions of the view for an object.
	 * 
	 * @param id
	 *            id of the object
	 * @param type
	 *            type of object
	 * @return json containing the versions
	 */
	@GET
	@Path("/{id}/versions")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getVersions(@PathParam("id") String id, @QueryParam("type") String type) {
		Instance instance = loadInstanceInternal(type, id);
		ViewInstance viewInstance = typeConverter.convert(ViewInstance.class, instance);
		if ((viewInstance == null) || (viewInstance.getViewReference() == null)) {
			// we will not have view reference if the object is not created in DMS, yet. This can
			// happen when object is imported in the system and not created in it.
			return "{}";
		}
		Instance view = viewInstance.getViewReference().toInstance();

		List<VersionInfo> versions = idocService.getVersions((DocumentInstance) view);

		JSONObject result = new JSONObject();
		try {
			JSONArray array = new JSONArray();

			ListIterator<VersionInfo> iterator = versions.listIterator(versions.size());

			// versions are returned in latest-first order that's way we iterate
			// in reverse order
			while (iterator.hasPrevious()) {
				VersionInfo version = iterator.previous();

				if (iterator.hasPrevious()) {
					JSONObject object = new JSONObject();
					object.put(DefaultProperties.CREATED_ON,
							convertDateTime(version.getVersionDate()));
					object.put(DefaultProperties.TITLE, version.getVersionLabel());
					object.put(DefaultProperties.CREATED_BY,
							getFullName(version.getVersionCreator()));

					array.put(object);
				}
			}

			result.put("versions", array);
		} catch (JSONException e) {
			LOGGER.error("Can not put result in json array", e);
		}

		return result.toString();
	}

	/**
	 * Loads a historic version of the document.
	 * 
	 * @param documentId
	 *            Id of the document for which we are retrieving a previous version.
	 * @param version
	 *            Version label e.g "1.3"
	 * @return A historic version of the document converted to {@link RestInstance} instance.
	 */
	@GET
	@Path("/{documentId}/versions/{version}")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public RestInstance retrievePreviousVersion(@PathParam("documentId") String documentId,
			@PathParam("version") String version) {
		DocumentInstance instance = idocService.loadVersion(documentId, version);
		return convertInstanceToResponce(instance);
	}

	/**
	 * Reverts the document/object view content to an older version.
	 * 
	 * @param id
	 *            Id of the document or object.
	 * @param type
	 *            Type of object (documentinstance/objectinstance).
	 * @param version
	 *            Version to revert to.
	 */
	@POST
	@Path("/{id}/versions/{version}/revert")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void revertToVersion(@PathParam("id") String id, @QueryParam("type") String type,
			@PathParam("version") String version) {
		Instance instance = loadInstanceInternal(type, id);
		ViewInstance viewInstance = typeConverter.convert(ViewInstance.class, instance);
		DocumentInstance view = (DocumentInstance) typeConverter.convert(InitializedInstance.class,
				viewInstance.getViewReference()).getInstance();
		if (!view.isLocked() || isLockedByMe(view)) {
			documentService.revertVersion(view, version);
		}
	}

	/**
	 * Locks the document for edit.
	 * 
	 * @param id
	 *            Document id.
	 * @param type
	 *            the type
	 * @return JSON object containing a single property ('lockedBy') if this property has a value it
	 *         means that a document is already locked, otherwise it was successfully locked.
	 */
	@POST
	@Path("/{id}/edit")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public RestInstance edit(@PathParam("id") String id, @QueryParam("type") String type) {
		Instance instance = loadInstanceInternal(type, id);
		if (instance == null) {
			return null;
		}

		initializeLockedByProperties(instance, instance.getProperties(), true);

		RestInstance restInstance = convertInstanceToResponce(instance);

		restInstance.getProperties().put(DocumentProperties.LOCKED_BY_MESSAGE,
				instance.getProperties().get(DocumentProperties.LOCKED_BY_MESSAGE));
		restInstance.getProperties().put(DefaultProperties.IS_LOCKED_BY_ME,
				instance.getProperties().get(DefaultProperties.IS_LOCKED_BY_ME));
		restInstance.getProperties().put("isLocked", instance.getProperties().get("isLocked"));
		return restInstance;
	}

	/**
	 * Checks is the instance (or the underlying view) is locked. If it's locked by somebody other
	 * than the current user - a message for the user is built and set in the provided map, so it
	 * could be shown to the user.
	 * 
	 * @param instance
	 *            Instance to check.
	 * @param properties
	 *            Properties map.
	 * @param edit
	 *            Indicates whether the document is being opened for edit or preview.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initializeLockedByProperties(Instance instance, Map properties, boolean edit) {
		ViewInstance viewInstance = typeConverter.convert(ViewInstance.class, instance);
		if (viewInstance == null) {
			return;
		}
		boolean isLocked = viewInstance.isLocked();
		boolean isLockedByMe = false;

		if (isLocked) {
			if (isLockedByMe(viewInstance)) {
				isLockedByMe = true;
			}
			properties.put(DocumentProperties.LOCKED_BY_MESSAGE,
					labelBuilder.getDocumentIsLockedMessage(viewInstance));
			properties.put(DocumentProperties.LOCKED_BY, viewInstance.getLockedBy());

			isLocked = true;
		} else if (edit) {
			InstanceReference reference = viewInstance.getViewReference();
			// if null then the default view was deleted for some reason
			if (reference != null) {
				Instance view = reference.toInstance();

				documentService.lock((DocumentInstance) view);

				if (view.getProperties() != null) {
					properties.put(DocumentProperties.LOCKED_BY,
							view.getProperties().get(DocumentProperties.LOCKED_BY));
				}
			} else {
				LOGGER.warn("No view for " + instance.getClass().getSimpleName() + " with id="
						+ instance.getId());
			}
			isLocked = false;
		}
		properties.put(DefaultProperties.IS_LOCKED_BY_ME, isLockedByMe);
		properties.put("isLocked", isLocked);
	}

	/**
	 * Deletes the document.
	 * 
	 * @param documentId
	 *            Document id.
	 * @param type
	 *            document type.
	 */
	@Secure
	@DELETE
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void deleteDocument(@QueryParam("documentId") String documentId,
			@QueryParam("type") String type) {
		Instance instance = loadInstanceInternal(type, documentId);
		ViewInstance viewInstance = typeConverter.convert(ViewInstance.class, instance);
		DocumentInstance view = (DocumentInstance) typeConverter.convert(InitializedInstance.class,
				viewInstance.getViewReference()).getInstance();

		if (!view.isLocked() || isLockedByMe(view)) {
			instanceService.delete(instance, new Operation(ActionTypeConstants.DELETE), true);
		}
	}

	/**
	 * Converts {@link Instance} to {@link RestInstance} by copying specific properties.
	 * 
	 * @param instance
	 *            the instance
	 * @return converted object {@link Instance} to convert.
	 */
	RestInstance convertInstanceToResponce(Instance instance) {
		return typeConverter.convert(RestInstance.class, instance);
	}

	/**
	 * Link sub-document to it's parent.
	 * 
	 * @param parentId
	 *            parent documentId
	 * @param to
	 *            sub-document instance
	 */
	private void linkToParent(String parentId, Instance to) {
		if (parentId != null) {
			Instance from = loadInstanceInternal(DocumentInstance.class.getName(), parentId);
			if ((from != null) && (to != null)) {
				linkService
						.link(from, to, LinkConstants.TREE_PARENT_TO_CHILD,
								LinkConstants.TREE_CHILD_TO_PARENT,
								LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
			}
		}
	}

	/**
	 * Converts object properties to strings TODO using the object definition.
	 * 
	 * @param instance
	 *            object which properties to extract.
	 * @return extracted properties.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> convertProperties(Instance instance) {
		DefinitionModel definitionModel = dictionaryService.getInstanceDefinition(instance);
		return (Map<String, Object>) propertiesService.convertToExternalModel(instance,
				definitionModel);
	}

	/**
	 * Fetches the available actions for a document. If documentId is {@code null} it is assumed
	 * that the user is creating a new document.
	 * 
	 * @param id
	 *            Object id.
	 * @param type
	 *            Type of object.
	 * @return The available actions.
	 */
	@GET
	@Path("/actions")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getAllowedActions(@QueryParam("id") String id, @QueryParam("type") String type) {

		if (id == null) {
			return null;
		}

		Instance instance = loadInstanceInternal(type, id);

		Set<Action> actions = authorityService.getAllowedActions(
				authenticationService.getCurrentUserId(), instance, "");
		JSONObject object = new JSONObject();
		JsonUtil.addActions(object, actions);

		return object.toString();
	}

	/**
	 * Get document URL
	 * 
	 * @param id
	 *            Object id.
	 * @param type
	 *            Type of object.
	 * @return The URL.
	 */
	@GET
	@Path("/getContentURI")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getContentURI(@QueryParam("id") String id, @QueryParam("type") String type) {

		if ((id != null) && (type != null)) {
			Instance instance = loadInstanceInternal(type, id);
			return documentService.getContentURI((DocumentInstance) instance);
		}

		return null;
	}

	/**
	 * Creates a clone of a given document. The clone is not saved but processed for edit.
	 * 
	 * @param documentId
	 *            of the document to clone.
	 * @param type
	 *            id of the section where the document is located.
	 * @param request
	 *            http request containing url.
	 * @return fetched document data
	 */
	@GET
	@Path("/{id}/clone")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public RestInstance clone(@Context HttpServletRequest request,
			@PathParam("id") String documentId, @QueryParam("type") String type) {
		Instance instance = loadInstanceInternal(type, documentId);

		RestInstance restInstance = convertInstanceToResponce(instance);
		Instance clonedInstance = instanceService.clone(instance, new Operation(
				ActionTypeConstants.CLONE));
		RestInstance restClonedInstance = convertInstanceToResponce(clonedInstance);

		String origin = getOrigin(request);
		String content = idocSanitizer.sanitizeBeforeClone(restInstance.getContent(), origin);
		restClonedInstance.setContent(content);
		restClonedInstance.setCurrentOperation(ActionTypeConstants.CLONE);

		return restClonedInstance;
	}

	/**
	 * FIXME: This method should be removed in caseDefinitionRestService Get all document types for
	 * a section.
	 * 
	 * @param type
	 *            Section type.
	 * @param id
	 *            Section id.
	 * @return document types
	 */
	@GET
	@Path("/documentTypes")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getDocumentTypes(@QueryParam("type") String type, @QueryParam("id") String id) {

		SectionInstance owningInstance = (SectionInstance) loadInstanceInternal(type, id);

		documentFileTypes.prepare(owningInstance);
		List<SelectorItem> types = documentFileTypes.getFileTypes();

		JSONArray array = new JSONArray();

		try {
			for (SelectorItem idocTypes : types) {
				JSONObject object = new JSONObject();
				object.put("id", idocTypes.getId());
				object.put("type", idocTypes.getType());
				object.put("description", idocTypes.getDescription());
				array.put(object);
			}

		} catch (JSONException e) {
			LOGGER.error("Can not put result in json array", e);
		}

		return array.toString();
	}

	/**
	 * Unlocks a document which is being locked for editing.
	 * 
	 * @param id
	 *            the id of the document
	 * @param type
	 *            the type
	 */
	@POST
	@Path("/{id}/unlock")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void unlock(@PathParam("id") String id, @QueryParam("type") String type) {
		Instance instance = loadInstanceInternal(type, id);
		unlockInstance(instance);
	}

	/**
	 * Converts {@link Date} object to a String representation using the CMF services.
	 * 
	 * @param date
	 *            the date
	 * @return converted date. {@link Date} to convert.
	 */
	private String convertDateTime(Object date) {
		if (date instanceof Date) {
			return dateUtil.getFormattedDateTime((Date) date);
		}
		return null;
	}

	/**
	 * Converts a username to a fullname.
	 * 
	 * @param username
	 *            username to convert.
	 * @return full name of the user
	 */
	private String getFullName(Object username) {
		if (username != null) {
			return labelBuilder.getDisplayNameForUser(username.toString());
		}
		return null;
	}

	/**
	 * Escapes document properties like title and createdBy names, so they cause js errors.
	 * 
	 * @param properties
	 *            Properties that may need escaping.
	 */
	private void escapeStringProperties(Map<String, Serializable> properties) {
		for (Entry<String, Serializable> entry : properties.entrySet()) {
			if (ESCAPED_PROPERTY_KEYS.contains(entry.getKey()) && (entry.getValue() != null)) {
				properties.put(entry.getKey(),
						StringEscapeUtils.escapeHtml(entry.getValue().toString()));
			}
		}
	}

	/**
	 * Checks if the lockedBy property of a {@link DocumentInstance} is the currently logged in
	 * user.
	 * 
	 * @param lockable
	 *            the document instance
	 * @return {@code true} if the document is locked by the current user, {@code false} otherwise.
	 *         {@link DocumentInstance} to check.
	 */
	private boolean isLockedByMe(Lockable lockable) {
		String currentUserId = authenticationService.getCurrentUserId();
		String lockedBy = lockable.getLockedBy();
		return StringUtils.isNotBlank(currentUserId) && currentUserId.equals(lockedBy);
	}

	/**
	 * Get part of the url.
	 * 
	 * @param request
	 *            http request containing url.
	 * @return part of url.
	 */
	private String getOrigin(HttpServletRequest request) {
		if (request != null) {
			String url = request.getRequestURL().toString();
			String uri = request.getRequestURI().toString();
			return url.replace(uri, "");
		}
		return "";
	}

	/**
	 * Unlocks instance in DMS.
	 * 
	 * @param instance
	 *            to be unlocked
	 */
	private void unlockInstance(Instance instance) {

		if (instance == null) {
			return;
		}

		ViewInstance viewInstance = typeConverter.convert(ViewInstance.class, instance);
		if (viewInstance == null) {
			LOGGER.error("Document view not found for: id=" + instance.getId() + ", type="
					+ instance.getIdentifier());
			return;
		}

		if (viewInstance.isLocked() && isLockedByMe(viewInstance)) {
			Instance view = viewInstance.getViewReference().toInstance();

			documentService.unlock((DocumentInstance) view);
		}
	}
}