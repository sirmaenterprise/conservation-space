/**
 * Copyright (c) 2013 22.11.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.objects.web.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.StringPair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.PropertyInstance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Provides information about the definitions of the models used in the
 * application.
 *
 * @author Adrian Mitev
 */
@ApplicationScoped
@Path("/definition")
public class DefinitionRestService extends EmfRestService {

	private static final String NAME = "name";
	private static final String RDF_TYPE = "rdf:type";
	private static final String SUB_TYPE = "subType";
	private static final String OBJECT_TYPE = "objectType";
	private static final String TITLE = "title";
	private static final String INSTANCE = "instance";

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private CodelistService codelistService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private ServiceRegister serviceRegister;
	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationService;

	@Inject
	private AllowedChildrenTypeProvider typeProvider;

	private static final Logger LOGGER = Logger.getLogger(DefinitionRestService.class);

	/**
	 * Provides an array with the classes that are eligible for search ordered
	 * by title.
	 *
	 * @return array containing the searchable types ordered by title.
	 */
	@GET
	@Path("/searchable-types")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getSearchableTypes() {
		List<ClassInstance> classes = semanticDefinitionService.getSearchableClasses();

		JSONArray result = toJsonArray((List) classes, DefaultProperties.TITLE, new StringPair(
				DefaultProperties.NAME, INSTANCE), new StringPair(DefaultProperties.TITLE, TITLE));
		return result.toString();
	}

	/**
	 * Generate a list of classes with specific parents.
	 *
	 * @param filter
	 *            Parents that we want children for.
	 * @return JSON array containing the object types that match the filter.
	 */
	@GET
	@Path("/types")
	public String getTypes(@QueryParam("filter") List<String> filter) {

		JSONArray result = new JSONArray();
		if ((filter != null) && !filter.isEmpty()) {
			List<ClassInstance> classes = semanticDefinitionService.getSearchableClasses();

			for (ClassInstance classInstance : classes) {
				if (!hasParent(filter, classInstance)) {
					continue;
				}

				JSONObject value = new JSONObject();
				JsonUtil.addToJson(value, DefaultProperties.NAME, classInstance.getProperties()
						.get(INSTANCE));
				JsonUtil.addToJson(value, DefaultProperties.TITLE, classInstance.getProperties()
						.get(TITLE));
				JsonUtil.addToJson(value, OBJECT_TYPE, "category");
				result.put(value);
			}
		}
		return result.toString();
	}

	/**
	 * Recursively checks the parents of a class to see if one of them is in a
	 * list of parents.
	 *
	 * @param parents
	 *            List of parents to look for.
	 * @param clazz
	 *            Class that we are checking.
	 * @return {@code true} if one of the parents of the class is in the list of
	 *         filtered classes, {@code false} otherwise.
	 */
	private boolean hasParent(List<String> parents, ClassInstance clazz) {
		if (clazz == null) {
			return false;
		}

		String clazzId = clazz.getId().toString();
		if (parents.contains(clazzId)) {
			return true;
		}

		HashSet<Serializable> superClasses = (HashSet<Serializable>) clazz.getProperties().get("superClasses");

		for (Serializable superClass : superClasses) {
			ClassInstance owningInstance =  semanticDefinitionService.getClassInstance(superClass.toString());
			if(hasParent(parents, owningInstance)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Provides an array with all searchable types and their sub types.
	 *
	 * @param addFullURI
	 *            if true the full uri is added to the response
	 * @return array containing the searchable types and sub types ordered by
	 *         title.
	 */
	@GET
	@Path("/all-types")
	public String getAllTypes(@DefaultValue("false") @QueryParam("addFullURI") Boolean addFullURI) {
		List<ClassInstance> classes = semanticDefinitionService.getClasses();
		Set<ClassInstance> processed = CollectionUtils.createLinkedHashSet(classes.size());

		JSONArray result = new JSONArray();

		// Get all searcheable objects
		for (ClassInstance classInstance : classes) {
			if (isSearchable(classInstance) && !processed.contains(classInstance)) {
				processed.add(classInstance);

				addTypeInfo(result, classInstance, addFullURI, false);
				if (!classInstance.getSubClasses().isEmpty()) {
					for (ClassInstance subInstance : classInstance.getSubClasses().values()) {
						if (isSearchable(subInstance)) {
							processed.add(subInstance);
							addTypeInfo(result, subInstance, addFullURI, true);
						}
					}
				}
			}
		}

		return result.toString();
	}

	/**
	 * Check if the instance is searcheable
	 *
	 * @param classInstance
	 *            the class to check
	 * @return true if is searcheable
	 */
	private boolean isSearchable(ClassInstance classInstance) {
		return Boolean.TRUE.equals(classInstance.getProperties().get("searchable"));
	}

	/**
	 * Add class type info to the result Json array.
	 *
	 * @param result
	 *            the result
	 * @param types
	 *            the types
	 * @param addFullURI
	 *            the add full uri
	 * @param subType
	 *            if the class is a sub type. If not will try to add the
	 *            codelist elements to the list
	 */
	private void addTypeInfo(JSONArray result, ClassInstance types, Boolean addFullURI,
			boolean subType) {
		JSONObject value;
		value = new JSONObject();
		JsonUtil.addToJson(value, DefaultProperties.NAME, types.getProperties().get(INSTANCE));
		String fullUri = namespaceRegistryService.buildFullUri(types.getProperties().get(INSTANCE)
				.toString());
		if (addFullURI) {
			JsonUtil.addToJson(value, DefaultProperties.URI, fullUri);
		}
		JsonUtil.addToJson(value, DefaultProperties.TITLE, types.getProperties().get(TITLE));
		if (subType) {
			JsonUtil.addToJson(value, OBJECT_TYPE, "item");
			JsonUtil.addToJson(value, SUB_TYPE, subType);
		} else {
			JsonUtil.addToJson(value, OBJECT_TYPE, "category");
		}
		result.put(value);

		if (!subType) {
			addDefinitionsForType(result, fullUri);
		}
	}

	/**
	 * Adds the definitions for the given type class.
	 *
	 * @param result
	 *            the result
	 * @param fullUri
	 *            the full uri
	 */
	private void addDefinitionsForType(JSONArray result, String fullUri) {

		DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(fullUri);
		if (typeDefinition == null) {
			return;
		}
		if (ObjectInstance.class.isAssignableFrom(typeDefinition.getJavaClass())) {
			return;
		}
		if (!fullUri.equals(typeDefinition.getFirstUri())) {
			return;
		}

		InstanceService<Instance, DefinitionModel> instanceService = serviceRegister
				.getInstanceService(typeDefinition.getJavaClass());
		if (instanceService == null) {
			return;
		}

		String language = getCurrentUserLanguage();

		JSONObject value;
		List<DefinitionModel> allDefinitions = dictionaryService.getAllDefinitions(instanceService
				.getInstanceDefinitionClass());
		for (DefinitionModel model : allDefinitions) {
			if (model instanceof GenericDefinition) {
				if (!EqualsHelper.nullSafeEquals(typeDefinition.getName(),
						typeProvider.getDataTypeName(((GenericDefinition) model).getType()), true)) {
					continue;
				}
			}
			PropertyDefinition property = PathHelper.findProperty(model, (PathElement) model,
					DefaultProperties.TYPE);
			if (property != null) {
				if ((property.getCodelist() != null) && (property.getCodelist() > 0)) {
					CodeValue codeValue = codelistService.getCodeValue(property.getCodelist(),
							property.getDefaultValue());
					if (codeValue != null) {

						value = new JSONObject();
						JsonUtil.addToJson(value, DefaultProperties.NAME, codeValue.getValue());
						// use the cu
						JsonUtil.addToJson(value, DefaultProperties.TITLE, codeValue
								.getProperties().get(language));
						JsonUtil.addToJson(value, OBJECT_TYPE, "item");
						JsonUtil.addToJson(value, SUB_TYPE, Boolean.TRUE);
						JsonUtil.addToJson(value, "parent", fullUri);
						result.put(value);
					}
				}
			}
		}
	}

	/**
	 * Gets the current user language.
	 *
	 * @return the current user language
	 */
	private String getCurrentUserLanguage() {
		try {
			AuthenticationService service = authenticationService.get();
			return SecurityContextManager.getUserLanguage(service.getCurrentUser());
		} catch (ContextNotActiveException e) {
			return SecurityContextManager.getSystemLanguage();
		}
	}

	/**
	 * Provides an array with the relationship types that can be created from
	 * the "from" types.
	 *
	 * @param forType
	 *            CSV string describing type(s) of the object which
	 *            relationships to fetch. If null, the relationship types of all
	 *            objects will be provided
	 * @return array with the available relationship types.
	 */
	@GET
	@Path("/relationship-types")
	public String getRelations(@QueryParam("forType") String forType) {
		Collection<PropertyInstance> relations = new HashSet<>();

		if (StringUtils.isNotNullOrEmpty(forType)) {
			String[] split = forType.split(",");

			for (String type : split) {
				relations.addAll(semanticDefinitionService.getRelations(type, null));
			}
		} else {
			relations.addAll(semanticDefinitionService.getRelations());
		}

		return buildRelationshipsOrPropertiesResult(relations, null).toString();
	}

	/**
	 * Provides an array with the relationship types that can be created from
	 * the given object type.
	 *
	 * @param forType
	 *            the object type
	 * @param id
	 *            the id
	 * @return array with the available relationship types.
	 */
	@GET
	@Path("relations-by-type")
	public String getRelationsByInstanceType(@QueryParam("forType") String forType,
			@QueryParam("id") String id) {
		String data = "[]";

		Collection<PropertyInstance> relations = new ArrayList<PropertyInstance>();
		Instance instance = null;
		if (StringUtils.isNotNullOrEmpty(id) && StringUtils.isNotNullOrEmpty(forType)) {
			instance = fetchInstance(id, forType);
			relations = getRelationsForInstanceType(id, forType, relations);
		}

		if ((instance == null) && StringUtils.isNotNullOrEmpty(forType)) {
			relations = getRelationsByDefinitionType(forType, relations);
		}

		if (relations != null) {
			data = buildRelationshipsOrPropertiesResult(relations, null).toString();
		}

		return data;
	}

	/**
	 * Gets possible relations types intersection between two instances.
	 *
	 * @param fromType
	 *            the from type
	 * @param fromId
	 *            the from id
	 * @param toType
	 *            the to type
	 * @param toId
	 *            the to id
	 * @return the relations intersection for the two given instances
	 */
	@GET
	@Path("relations-by-type-intersect")
	public String getRelationsByInstanceTypesIntersect(@QueryParam("fromType") String fromType,
			@QueryParam("fromId") String fromId, @QueryParam("toType") String toType,
			@QueryParam("toId") String toId) {
		if (debug) {
			log.debug("DefinitionRestService.getRelationsByInstanceTypesIntersect for from type ["
					+ fromType + "] with id [" + fromId + "] and to type [" + toType
					+ "] with id [" + toId + "]");
		}

		String data = "[]";

		Collection<PropertyInstance> fromRelations = new ArrayList<PropertyInstance>();
		if (StringUtils.isNotNullOrEmpty(fromId) && StringUtils.isNotNullOrEmpty(fromType)) {
			fromRelations = getRelationsForInstanceType(fromId, fromType, fromRelations);
		} else if (StringUtils.isNotNullOrEmpty(fromType)) {
			fromRelations = getRelationsByDefinitionType(fromType, fromRelations);
		}

		Collection<PropertyInstance> toRelations = new ArrayList<PropertyInstance>();
		if (StringUtils.isNotNullOrEmpty(toId) && StringUtils.isNotNullOrEmpty(toType)) {
			toRelations = getRelationsForInstanceType(toId, toType, toRelations);
		} else if (StringUtils.isNotNullOrEmpty(toType)) {
			toRelations = getRelationsByDefinitionType(toType, toRelations);
		}

		if (fromRelations != null) {
			fromRelations.retainAll(toRelations);
			data = buildRelationshipsOrPropertiesResult(fromRelations, null).toString();
		}

		return data;
	}

	/**
	 * Loads available relation types for instance type.
	 *
	 * @param id
	 *            the id
	 * @param forType
	 *            the for type
	 * @param relations
	 *            the relations
	 * @return the relations for instance type
	 */
	protected Collection<PropertyInstance> getRelationsForInstanceType(String id, String forType,
			Collection<PropertyInstance> relations) {
		Instance instance = fetchInstance(id, forType);
		if (instance != null) {
			// QVISRV-381
			String semClassType = typeConverter.convert(String.class,
					instance.getProperties().get(RDF_TYPE));

			if (StringUtils.isNullOrEmpty(semClassType)) {
				DataTypeDefinition dataTypeDefinition = dictionaryService
						.getDataTypeDefinition(instance.getClass().getName());
				semClassType = dataTypeDefinition.getFirstUri();
			}

			if (StringUtils.isNotNullOrEmpty(semClassType)) {
				relations.addAll(semanticDefinitionService.getRelations(semClassType, null));
			} else {
				relations.addAll(getRelationsByDefinitionType(forType, relations));
			}
			// https://ittruse.ittbg.com/jira/browse/CMF-7515
		} else {
			// REVIEW: Why do we do this? If we can't load the instance here,
			// chances are we won't be able to load it durring link creation
			relations.addAll(getRelationsByDefinitionType(forType, relations));
		}
		return relations;
	}

	/**
	 * Loads available relation types by data definition type.
	 *
	 * @param forType
	 *            the for type
	 * @param relations
	 *            the relations
	 * @return the relations by definition type
	 */
	protected Collection<PropertyInstance> getRelationsByDefinitionType(String forType,
			Collection<PropertyInstance> relations) {
		DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(forType);
		if (definition != null) {
			relations.addAll(semanticDefinitionService.getRelations(
					namespaceRegistryService.buildFullUri(definition.getFirstUri()), null));
		}
		return relations;
	}

	/**
	 * Builds a JSON array from a list of instances by copying specific instance
	 * properties and sorts the array by a specified property.
	 *
	 * @param instances
	 *            list with instances to turn into array.
	 * @param sortBy
	 *            object (in the resulting array) property to sort by
	 * @param elements
	 *            mapping between instance properties and a
	 * @return constructed array.
	 */
	private JSONArray toJsonArray(Collection<Instance> instances, String sortBy,
			StringPair... elements) {
		Map<String, JSONObject> sortedMap = new TreeMap<>();

		for (Instance instance : instances) {
			JSONObject object = new JSONObject();
			Map<String, Serializable> properties = instance.getProperties();

			for (StringPair element : elements) {
				Serializable value = properties.get(element.getSecond());
				JsonUtil.addToJson(object, element.getFirst(), value);
				if (element.getFirst().equals(DefaultProperties.NAME) && (value != null)) {
					JsonUtil.addToJson(object, "shortName", value.toString());
				}
			}

			sortedMap.put(JsonUtil.getStringValue(object, sortBy), object);
		}

		JSONArray result = new JSONArray();
		for (Entry<String, JSONObject> entry : sortedMap.entrySet()) {
			result.put(entry.getValue());
		}

		return result;
	}

	/**
	 * Returns the properties (literals) for specified types.
	 *
	 * @param forType
	 *            Types to retrieve literals for (optional)
	 * @return Json array as string containing the literals.
	 */
	@GET
	@Path("/properties")
	public String getPropertiesOfTypes(@QueryParam("forType") String forType) {
		List<ClassInstance> classes = null;
		if (StringUtils.isNotNullOrEmpty(forType)) {
			classes = new LinkedList<>();
			String[] split = forType.split(",");
			for (String string : split) {
				ClassInstance classInstance = semanticDefinitionService.getClassInstance(string
						.trim());
				if (classInstance != null) {
					classes.add(classInstance);
				}
			}
		} else {
			classes = semanticDefinitionService.getClasses();
		}
		List<ClassInstance> flatClassTree = new LinkedList<>();
		for (ClassInstance classInstance : classes) {
			if (!flatClassTree.contains(classInstance)) {
				flatClassTree.add(0, classInstance);
			}
			ClassInstance parent = (ClassInstance) classInstance.getOwningInstance();
			while (parent != null) {
				if (!flatClassTree.contains(parent)) {
					flatClassTree.add(0, parent);
				}
				parent = (ClassInstance) parent.getOwningInstance();
			}
		}
		List<PropertyInstance> properties = new LinkedList<>();
		Iterator<ClassInstance> iterator = flatClassTree.iterator();
		while (iterator.hasNext()) {
			ClassInstance classInstance = iterator.next();
			properties.addAll(0,
					semanticDefinitionService.getOwnProperties(classInstance.getId().toString()));
		}

		return buildRelationshipsOrPropertiesResult(properties, null).toString();
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
	 * Loads literals (properties) for an object.
	 *
	 * @param type
	 *            Object type.
	 * @param id
	 *            Object identifier.
	 * @param definitionId
	 *            The definition id
	 * @param unsetOnly
	 *            Load only properties w/o value.
	 * @param excludeSystem
	 *            whether or to include system and hidden properties.
	 * @param displayEmpty
	 *            show or hide empty properties values
	 * @return JSON array with available properties.
	 */
	@GET
	@Path("/literals")
	public String loadLiteralsForInstance(@QueryParam("forType") String type,
			@QueryParam("id") String id, @QueryParam("definitionId") String definitionId,
			@QueryParam("unsetOnly") boolean unsetOnly,
			@QueryParam("excludeSystem") boolean excludeSystem,
			@QueryParam("displayEmpty") boolean displayEmpty) {
		Instance instance = fetchInstance(id, type);
		DefinitionModel instanceDefinition;
		if (instance != null) {

			// TODO: at some point we should get the properties from a single
			// place i.e semantic db
			instanceDefinition = dictionaryService.getInstanceDefinition(instance);
			if (instanceDefinition == null) {
				return "[]";
			}
		} else {
			Class<Instance> typeClass = getTypeClass(type);
			if (typeClass == null) {
				return null;
			}
			InstanceService<Instance, DefinitionModel> instanceService = serviceRegister
					.getInstanceService(typeClass);

			// for document instance for now the default instance creation is
			// more
			// complicated...
			if (DocumentInstance.class.equals(typeClass)) {
				DocumentInstance documentInstance = new DocumentInstance();
				documentInstance.setIdentifier(definitionId);
				documentInstance.setStandalone(true);
				documentInstance.setRevision(0L);
				instanceDefinition = dictionaryService.getInstanceDefinition(documentInstance);
			} else {
				instanceDefinition = dictionaryService.getDefinition(
						instanceService.getInstanceDefinitionClass(), definitionId);
			}

			instance = instanceService.createInstance(instanceDefinition, null, new Operation(""));
		}

		String domainClass = typeConverter.convert(String.class,
				instance.getProperties().get(RDF_TYPE));
		if (org.apache.commons.lang.StringUtils.isBlank(domainClass)) {
			DataTypeDefinition dataTypeDefinition = dictionaryService.getDataTypeDefinition(type);
			domainClass = dataTypeDefinition.getFirstUri();
		}

		JSONArray result = new JSONArray();
		Set<String> fieldHistorySet = new HashSet<>();

		// get properties defined in the base object definition
		List<PropertyDefinition> fields = instanceDefinition.getFields();
		for (PropertyDefinition propertyDefinition : fields) {
			addPropertyFromDefinitionToResult(result, propertyDefinition, instance, unsetOnly,
					fieldHistorySet, domainClass, excludeSystem, displayEmpty);
		}

		// if it's a region definition get the regions and the properties
		// defined in each region
		if (instanceDefinition instanceof RegionDefinitionModel) {
			List<RegionDefinition> regions = ((RegionDefinitionModel) instanceDefinition)
					.getRegions();
			for (RegionDefinition region : regions) {
				fields = region.getFields();
				for (PropertyDefinition propertyDefinition : fields) {
					addPropertyFromDefinitionToResult(result, propertyDefinition, instance,
							unsetOnly, fieldHistorySet, domainClass, excludeSystem, displayEmpty);
				}
			}
		}
		// Semantic properties will be included later on
		// List<PropertyInstance> properties =
		// semanticDefinitionService.getProperties(domainClass);
		// for (PropertyInstance propertyInstance : properties) {
		// addPropertyFromOntologyToResult(result, propertyInstance, instance,
		// unsetOnly,
		// fieldHistorySet, domainClass);
		// }
		return result.toString();
	}

	/**
	 * Adds a property from the object definition to a result JSON array.
	 *
	 * @param result
	 *            JSON array to add to.
	 * @param definition
	 *            Property definition.
	 * @param ownerInstance
	 *            Instance containing the property.
	 * @param unsetOnly
	 *            Don't add if property has value.
	 * @param fieldHistorySet
	 *            History set.
	 * @param domainClass
	 *            Owning instance domain class (uri)
	 * @param excludeSystem
	 *            whether or to include system and hidden properties.
	 * @param displayEmpty
	 *            show or hide empty properties values
	 * @return true if the property was added.
	 */
	private boolean addPropertyFromDefinitionToResult(JSONArray result,
			PropertyDefinition definition, Instance ownerInstance, boolean unsetOnly,
			Set<String> fieldHistorySet, String domainClass, boolean excludeSystem,
			boolean displayEmpty) {
		DisplayType displayType = definition.getDisplayType();

		if (displayEmpty) {
			return false;
		}

		if (excludeSystem
				&& ((displayType == DisplayType.HIDDEN) || (displayType == DisplayType.SYSTEM))) {
			return false;
		}

		Object value = ownerInstance.getProperties().get(definition.getName());

		if (!(value instanceof List)) {
			if (unsetOnly && (ownerInstance.getProperties().get(definition.getName()) != null)) {
				return false;
			}
		} else {
			List<Serializable> multiValueList = (List<Serializable>) value;
			if (multiValueList.size() != 0) {
				return false;
			}
		}
		String name = definition.getName();
		String label = definition.getLabel();
		if (org.apache.commons.lang.StringUtils.isBlank(name)
				|| org.apache.commons.lang.StringUtils.isBlank(label) || name.contains("header")) {
			return false;
		}

		JSONObject jsonObject = new JSONObject();
		JsonUtil.addToJson(jsonObject, NAME, name);
		JsonUtil.addToJson(jsonObject, TITLE, label);

		if (definition.getCodelist() != null) {
			JsonUtil.addToJson(jsonObject, "codelistNumber", definition.getCodelist());
			// FIXME: constant?
			JsonUtil.addToJson(jsonObject, "editType", "codelist");
		} else {
			JsonUtil.addToJson(jsonObject, "editType", definition.getDataType().getName());
		}
		try {
			boolean hidden = displayType == DisplayType.HIDDEN;
			boolean readOnly = displayType == DisplayType.READ_ONLY;
			boolean system = displayType == DisplayType.SYSTEM;
			if (readOnly) {
				// return false;
			}
			jsonObject.put("editable", !((hidden && readOnly) || system));
		} catch (JSONException e) {
			LOGGER.error("Can not convert given string to json object", e);
		}
		String uri = definition.getUri();
		if (org.apache.commons.lang.StringUtils.isNotBlank(uri)) {
			fieldHistorySet.add(uri);
		}
		JsonUtil.addToJson(jsonObject, "domainClass", domainClass);
		JsonUtil.addToJson(jsonObject, "isMultiValued", definition.isMultiValued());
		result.put(jsonObject);
		return true;
	}

	// TODO: not used - to be deleted
	// /**
	// * Adds a property from the semantic db to a result JSON array.
	// *
	// * @param result
	// * JSON array to add to.
	// * @param definition
	// * Property definition.
	// * @param ownerInstance
	// * Instance containing the property.
	// * @param unsetOnly
	// * Don't add if property has value.
	// * @param fieldHistorySet
	// * History set.
	// * @param domainClass
	// * Owning instance domain class (uri)
	// * @return true if the property was added.
	// */
	// private boolean addPropertyFromOntologyToResult(JSONArray result,
	// PropertyInstance
	// definition,
	// Instance ownerInstance, boolean unsetOnly, Set<String> fieldHistorySet,
	// String domainClass) {
	// String uri = definition.getId().toString();
	// if (unsetOnly && (ownerInstance.getProperties().get(uri) != null)) {
	// return false;
	// }
	//
	// JSONObject jsonObject = new JSONObject();
	// // TODO FIXME: ExtJs doesn't like the colon for some reason, find out why
	// and fix
	// // the opposite is done in ObjectRestService#updateObject(...)
	// JsonUtil.addToJson(jsonObject, "name", uri.replace(':', '_'));
	// JsonUtil.addToJson(jsonObject, "title",
	// definition.getProperties().get(DefaultProperties.TITLE));
	//
	// JsonUtil.addToJson(jsonObject, "editType",
	// definition.getProperties().get("rangeClass"));
	// JsonUtil.addToJson(jsonObject, "linkId", uri);
	// JsonUtil.addToJson(jsonObject, "domainClass",
	// definition.getProperties().get("domainClass"));
	// JsonUtil.addToJson(jsonObject, "isMultiValued",
	// definition.getProperties().get("isMultiValued"));
	//
	// result.put(jsonObject);
	// fieldHistorySet.add(uri);
	// return true;
	// }

	/**
	 * Builds a Json array from the specified list of {@link PropertyInstance}s,
	 * optionally filtered by type of the entity.
	 *
	 * @param list
	 *            List of {@link PropertyInstance}
	 * @param types
	 *            the types hierarchy
	 * @return A filtered Json array object.
	 */
	private JSONArray buildRelationshipsOrPropertiesResult(Collection<PropertyInstance> list,
			List<String> types) {
		if (types != null) {
			// filter the returned items according to from value(s)
			List<PropertyInstance> filtered = new ArrayList<>();
			for (PropertyInstance relation : list) {
				if (types.contains(relation.getProperties().get("domainClass"))) {
					filtered.add(relation);
				}
			}

			list = filtered;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		JSONArray result = toJsonArray((Collection) list, DefaultProperties.TITLE, new StringPair(
				DefaultProperties.NAME, INSTANCE), new StringPair(DefaultProperties.TITLE,
				DefaultProperties.TITLE), new StringPair("domainClass", "domainClass"),
				new StringPair("type", "rangeClass"), new StringPair(DefaultProperties.DESCRIPTION,
						"definition"));
		return result;
	}
}
