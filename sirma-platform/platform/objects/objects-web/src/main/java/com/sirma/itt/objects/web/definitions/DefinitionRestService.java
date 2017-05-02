/**
 * Copyright (c) 2013 22.11.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.objects.web.definitions;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashSet;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.rest.DefinitionHelperOld;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.web.util.InstancePropertyComparator;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionHelper;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.properties.PropertiesConverter;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.search.SearchablePropertiesService;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Provides information about the definitions of the models used in the application.
 *
 * @author Adrian Mitev
 */
@ApplicationScoped
@Path("/definition")
public class DefinitionRestService extends EmfRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	// WILL BE REMOVED
	private static final String PARENT_INSTANCE_ID = "parentInstanceId";
	private static final String PARENT_INSTANCE_TYPE = "parentInstanceType";
	private static final String OPERATION = "operation";
	private static final String MANDATORY = "mandatory";

	private static final String DOMAIN_CLASS = "domainClass";
	private static final String NAME = "name";
	private static final String SUB_TYPE = "subType";
	private static final String OBJECT_TYPE = "objectType";
	private static final String TITLE = "title";
	private static final String INSTANCE = "instance";
	private static final String NULLABLE_VALUE = "null";
	private static final String PROPERTIES = "properties";
	private static final String CODELIST_DESCRIPTIONS = "cldescription";
	private static final String LABEL = "label";
	private static final String FIELDS = "fields";
	private static final String HEADERS = "headers";

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private CodelistService codelistService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private TypeMappingProvider typeProvider;

	@Inject
	private PropertiesConverter propertyConverter;

	@Inject
	private DefinitionHelperOld definitionHelperOld;

	@Inject
	private DefinitionHelper definitionHelper;

	@Inject
	private SearchablePropertiesService searchablePropertiesService;

	@Inject
	private HeadersService headersService;

	/**
	 * Provides an array with the classes that are eligible for search ordered by title.
	 *
	 * @return array containing the searchable types ordered by title.
	 */
	@GET
	@Path("/searchable-types")
	public String getSearchableTypes() {
		List<ClassInstance> classes = semanticDefinitionService.getSearchableClasses();

		JSONArray result = toJsonArray(classes, DefaultProperties.TITLE,
				new StringPair(DefaultProperties.NAME, INSTANCE), new StringPair(DefaultProperties.TITLE, TITLE));
		return result.toString();
	}

	/**
	 * Provides an array with all searchable types and their sub types inluding codelist types.
	 *
	 * @deprecated @see {@link DefinitionsRestService}
	 * @param addFullURI
	 *            if true the full uri is added to the response
	 * @param classFilter
	 *            List URIs to filter the classes by. Classes that don't have one of these as a parent are removed.
	 * @param skipDefinitionTypes
	 *            the skip definition types
	 * @return array containing the searchable types and sub types ordered by title.
	 */
	@GET
	@Path("/all-types")
	@Deprecated
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllTypes(@QueryParam("addFullURI") boolean addFullURI,
			@QueryParam("classFilter") List<String> classFilter,
			@QueryParam("skipDefinitionTypes") boolean skipDefinitionTypes) {

		List<ClassInstance> seed = new LinkedList<>();
		if (CollectionUtils.isNotEmpty(classFilter)) {
			for (String id : classFilter) {
				ClassInstance clazz = semanticDefinitionService.getClassInstance(id);
				seed.add(clazz);
			}
			Collections.sort(seed, InstancePropertyComparator.BY_TITLE_COMPARATOR);
		} else {
			seed.add(semanticDefinitionService.getRootClass());
		}

		Set<ClassInstance> history = new HashSet<>();
		JSONArray result = new JSONArray();
		Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.enable();
		try {
			for (ClassInstance clazz : seed) {
				addTypeHierarchy(clazz, result, history, addFullURI, skipDefinitionTypes, false);
			}
		} finally {
			Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.disable();
		}
		return result.toString();
	}

	/**
	 * Apply searchable subtypes based on current type.
	 *
	 * @param clazz
	 *            class instance
	 * @param result
	 *            holder for all searchable types
	 * @param history
	 *            already managed data holder
	 * @param fullURI
	 *            apply full URI
	 * @param definitions
	 *            apply definition
	 * @param subClass
	 *            apply sub class
	 */
	private void addTypeHierarchy(ClassInstance clazz, JSONArray result, Set<ClassInstance> history, boolean fullURI,
			boolean definitions, boolean subClass) {
		if (clazz == null) {
			LOGGER.error("Invalid/null class is provided to get hierarchy for. Currect model is: " + history);
			return;
		}
		if (!history.add(clazz)) {
			return;
		}

		boolean searchable = isSearchable(clazz);
		if (searchable && !isForbiddenLibrary(clazz)) {
			addTypeInfo(result, clazz, fullURI, subClass, definitions);
		}

		List<ClassInstance> subClasses = new LinkedList<>(clazz.getSubClasses().values());
		Collections.sort(subClasses, InstancePropertyComparator.BY_TITLE_COMPARATOR);
		for (ClassInstance sub : subClasses) {
			if (trace) {
				LOGGER.trace("Check subclass " + sub + " of " + clazz);
			}
			// technically it's a sub class but if the parent is not searchable we threat it as top level
			addTypeHierarchy(sub, result, history, fullURI, definitions, searchable);
		}
	}

	/**
	 * Checks if is forbidden library (if it part of the library set and it not part of allowed libraries for user).
	 *
	 * @param allObjectLibrary
	 *            the all object library
	 * @param allowedLibraries
	 *            the allowed libraries
	 * @param clazz
	 *            the clazz
	 * @return true, if is forbidden library
	 */
	private boolean isForbiddenLibrary(ClassInstance clazz) {
		return !authorityService.isActionAllowed(clazz, ActionTypeConstants.VIEW_DETAILS, null)
				&& clazz.type().isPartOflibrary();
	}

	/**
	 * Check if the instance is searcheable.
	 *
	 * @param classInstance
	 *            the class to check
	 * @return true if is searcheable
	 */
	private static boolean isSearchable(ClassInstance classInstance) {
		return classInstance.type().isSearchable();
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
	 *            if the class is a sub type. If not will try to add the codelist elements to the list
	 * @param skipDefinitionTypes
	 *            Do not add subtypes from definitions.
	 */
	private void addTypeInfo(JSONArray result, ClassInstance types, boolean addFullURI, boolean subType,
			boolean skipDefinitionTypes) {
		JSONObject value = new JSONObject();
		JsonUtil.addToJson(value, DefaultProperties.NAME, types.getProperties().get(INSTANCE));
		String fullUri = namespaceRegistryService.buildFullUri(types.getProperties().get(INSTANCE).toString());
		if (addFullURI) {
			JsonUtil.addToJson(value, "uri", fullUri);
		}
		JsonUtil.addToJson(value, DefaultProperties.TITLE, types.getLabel(getlanguage(null)));
		if (subType) {
			JsonUtil.addToJson(value, OBJECT_TYPE, "item");
			JsonUtil.addToJson(value, SUB_TYPE, subType);
			JsonUtil.addToJson(value, "subClass", Boolean.TRUE);
		} else {
			JsonUtil.addToJson(value, OBJECT_TYPE, "category");
		}
		result.put(value);

		if (!subType && !skipDefinitionTypes) {
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
		Class<?> javaClass = typeDefinition.getJavaClass();
		if (javaClass == null || ObjectInstance.class.isAssignableFrom(javaClass)) {
			return;
		}
		if (!fullUri.equals(typeDefinition.getFirstUri())) {
			return;
		}

		InstanceService service = serviceRegistry.getInstanceService(typeDefinition.getJavaClass());
		if (service == null) {
			return;
		}

		String language = getlanguage(null);

		List<DefinitionModel> allDefinitions = dictionaryService.getAllDefinitions(GenericDefinition.class);
		for (DefinitionModel model : allDefinitions) {
			if (model instanceof GenericDefinition && !EqualsHelper.nullSafeEquals(typeDefinition.getName(),
					typeProvider.getDataTypeName(((GenericDefinition) model).getType()), true)) {
				continue;
			}
			// TODO: This check is only for the demo of Huvefarma and it
			// must be refactored
			PropertyDefinition property = PathHelper.findProperty(model, (PathElement) model, DefaultProperties.TYPE);
			String uriValue = dictionaryService.getDefinitionIdentifier(model);
			ClassInstance definitionClass = semanticDefinitionService.getClassInstance(uriValue);
			if (definitionClass == null || !isForbiddenLibrary(definitionClass)) {
				buildDefinitionTypeData(result, property, fullUri, language);
			}
		}
	}

	/**
	 * Builds the definition type data.
	 *
	 * @param result
	 *            the result
	 * @param property
	 *            the property
	 * @param fullUri
	 *            the full uri
	 * @param language
	 *            the language
	 */
	private void buildDefinitionTypeData(JSONArray result, PropertyDefinition property, String fullUri,
			String language) {
		if (property != null && property.getCodelist() != null && property.getCodelist() > 0) {
			CodeValue codeValue = codelistService.getCodeValue(property.getCodelist(), property.getDefaultValue());
			if (codeValue != null) {
				JSONObject value = new JSONObject();
				String extractRootPath = PathHelper.extractLastElementInPath(property.getParentPath());
				JsonUtil.addToJson(value, DefaultProperties.NAME, extractRootPath);
				JsonUtil.addToJson(value, DefaultProperties.TITLE, codeValue.get(language));
				JsonUtil.addToJson(value, OBJECT_TYPE, "item");
				JsonUtil.addToJson(value, SUB_TYPE, Boolean.TRUE);
				JsonUtil.addToJson(value, "parent", fullUri);
				result.put(value);
			}
		}
	}

	/**
	 * Provides an array with the relationship types that can be created from the "from" types.
	 *
	 * @param forType
	 *            CSV string describing type(s) of the object which relationships to fetch. If null, the relationship
	 *            types of all objects will be provided
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
			relations.addAll(semanticDefinitionService.getSearchableRelations());
		}

		return buildRelationshipsOrPropertiesResult(relations, null).toString();
	}

	/**
	 * Provides an array with the relationship types that can be created from the given object type.
	 *
	 * @param forType
	 *            the object type
	 * @param id
	 *            for object with id
	 * @param definitionId
	 *            the object definition id
	 * @param rdfType
	 *            the object rdf type
	 * @return array with the available relationship types.
	 */
	@GET
	@Path("relations-by-type")
	public String getRelationsByInstanceType(@QueryParam("forType") String forType, @QueryParam("id") String id,
			@QueryParam("definitionId") String definitionId, @QueryParam("rdfType") String rdfType) {
		String data = "[]";

		Collection<PropertyInstance> relations = new ArrayList<>();
		if (!"null".equals(id) && StringUtils.isNotNullOrEmpty(id) && StringUtils.isNotNullOrEmpty(forType)) {
			relations = getRelationsForInstanceType(id, forType, relations);
		} else if (!"null".equals(rdfType) && StringUtils.isNotNullOrEmpty(rdfType)) {
			relations = getRelationsByRdfType(rdfType, relations);
		} else if (StringUtils.isNotNullOrEmpty(forType)) {
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
	 * @param rdfType
	 *            RDF Type
	 * @param toType
	 *            the to type
	 * @param toId
	 *            the to id
	 * @return the relations intersection for the two given instances
	 */
	@GET
	@Path("relations-by-type-intersect")
	public String getRelationsByInstanceTypesIntersect(@QueryParam("fromType") String fromType,
			@QueryParam("fromId") String fromId, @QueryParam("rdfType") String rdfType,
			@QueryParam("toType") String toType, @QueryParam("toId") String toId) {
		if (debug) {
			LOG.debug("DefinitionRestService.getRelationsByInstanceTypesIntersect for from type [" + fromType
					+ "] with id [" + fromId + "] and to type [" + toType + "] with id [" + toId + "]");
		}

		String data = "[]";

		Collection<PropertyInstance> fromRelations = new ArrayList<>();
		if (StringUtils.isNotNullOrEmpty(fromId) && StringUtils.isNotNullOrEmpty(fromType)) {
			fromRelations = getRelationsForInstanceType(fromId, fromType, fromRelations);
		} else if (StringUtils.isNotNullOrEmpty(rdfType)) {
			fromRelations = getRelationsByRdfType(rdfType, fromRelations);
		} else if (StringUtils.isNotNullOrEmpty(fromType)) {
			fromRelations = getRelationsByDefinitionType(fromType, fromRelations);
		}

		Collection<PropertyInstance> toRelations = new ArrayList<>();
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
			String semClassType = instance.type().getId().toString();
			relations.addAll(semanticDefinitionService.getRelations(semClassType, null));
			// https://ittruse.ittbg.com/jira/browse/CMF-7515
		} else {
			// REVIEW: Why do we do this? If we can't load the instance here,
			// chances are we won't be able to load it durring link creation
			getRelationsByDefinitionType(forType, relations);
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
			relations.addAll(semanticDefinitionService
					.getRelations(namespaceRegistryService.buildFullUri(definition.getFirstUri()), null));
		}
		return relations;
	}

	/**
	 * Loads available relations by RDF type.
	 *
	 * @param rdfType
	 *            the for type
	 * @param relations
	 *            Relations list to append the relations by the given RDF type
	 * @return the relations by RDF type
	 */
	protected Collection<PropertyInstance> getRelationsByRdfType(String rdfType,
			Collection<PropertyInstance> relations) {
		relations.addAll(semanticDefinitionService.getRelations(namespaceRegistryService.buildFullUri(rdfType), null));
		return relations;
	}

	/**
	 * Builds a JSON array from a list of instances by copying specific instance properties and sorts the array by a
	 * specified property.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instances
	 *            list with instances to turn into array.
	 * @param sortBy
	 *            object (in the resulting array) property to sort by
	 * @param elements
	 *            mapping between instance properties and a
	 * @return constructed array.
	 */
	private static <I extends Instance> JSONArray toJsonArray(Collection<I> instances, String sortBy,
			StringPair... elements) {
		Map<String, JSONObject> sortedMap = new TreeMap<>();

		for (I instance : instances) {
			JSONObject object = new JSONObject();
			Map<String, Serializable> properties = instance.getProperties();

			for (StringPair element : elements) {
				Serializable value = properties.get(element.getSecond());
				JsonUtil.addToJson(object, element.getFirst(), value);
				if (element.getFirst().equals(DefaultProperties.NAME) && value != null) {
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
				ClassInstance classInstance = semanticDefinitionService.getClassInstance(string.trim());
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
			properties.addAll(0, semanticDefinitionService.getOwnProperties(classInstance.getId().toString()));
		}

		return buildRelationshipsOrPropertiesResult(properties, null).toString();
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
	public String loadLiteralsForInstance(@QueryParam("forType") String type, @QueryParam("id") String id,
			@QueryParam("definitionId") String definitionId, @QueryParam("unsetOnly") boolean unsetOnly,
			@QueryParam("excludeSystem") boolean excludeSystem, @QueryParam("displayEmpty") boolean displayEmpty) {

		Instance instance = null;

		if (id != null) {
			instance = fetchInstance(id, type);
		}
		JSONArray result = new JSONArray();

		Map<String, List<PropertyDefinition>> typeFields = searchablePropertiesService.getTypeFields(instance, type,
				definitionId);

		Set<String> fieldHistorySet = new HashSet<>();

		for (Entry<String, List<PropertyDefinition>> entrySet : typeFields.entrySet()) {

			String domainClass = entrySet.getKey();
			List<PropertyDefinition> fields = entrySet.getValue();

			for (PropertyDefinition field : fields) {
				addPropertyFromDefinitionToResult(result, field, instance, unsetOnly, fieldHistorySet, domainClass,
						excludeSystem, displayEmpty);
			}

		}

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
	private static boolean addPropertyFromDefinitionToResult(JSONArray result, PropertyDefinition definition,
			Instance ownerInstance, boolean unsetOnly, Set<String> fieldHistorySet, String domainClass,
			boolean excludeSystem, boolean displayEmpty) {
		DisplayType displayType = definition.getDisplayType();

		if (displayType == DisplayType.SYSTEM) {
			return false;
		}

		if (displayEmpty) {
			return false;
		}

		if (excludeSystem && (displayType == DisplayType.HIDDEN || displayType == DisplayType.SYSTEM)) {
			return false;
		}

		if (ownerInstance != null) {
			Object value = ownerInstance.getProperties().get(definition.getName());

			if (!(value instanceof List)) {
				if (unsetOnly && ownerInstance.getProperties().get(definition.getName()) != null) {
					return false;
				}
			} else {
				List<?> multiValueList = (List<?>) value;
				if (!multiValueList.isEmpty()) {
					return false;
				}
			}
		}

		String name = definition.getName();
		String label = definition.getLabel();
		if (org.apache.commons.lang.StringUtils.isBlank(name) || org.apache.commons.lang.StringUtils.isBlank(label)
				|| name.contains("header")) {
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
			jsonObject.put("editable", !(hidden || readOnly || system));
		} catch (JSONException e) {
			LOGGER.error("Can not convert given string to json object", e);
		}
		String uri = definition.getUri();
		if (org.apache.commons.lang.StringUtils.isNotBlank(uri)) {
			fieldHistorySet.add(uri);
		}
		JsonUtil.addToJson(jsonObject, DOMAIN_CLASS, domainClass);
		JsonUtil.addToJson(jsonObject, "isMultiValued", definition.isMultiValued());
		result.put(jsonObject);
		return true;
	}

	/**
	 * Builds a Json array from the specified list of {@link PropertyInstance}s, optionally filtered by type of the
	 * entity.
	 *
	 * @param collection
	 *            List of {@link PropertyInstance}
	 * @param types
	 *            the types hierarchy
	 * @return A filtered Json array object.
	 */
	private JSONArray buildRelationshipsOrPropertiesResult(Collection<PropertyInstance> collection,
			List<String> types) {
		Collection<PropertyInstance> local = collection;
		if (types != null) {
			// filter the returned items according to from value(s)
			List<PropertyInstance> filtered = new ArrayList<>();
			for (PropertyInstance relation : collection) {
				if (types.contains(relation.getProperties().get(DOMAIN_CLASS))) {
					filtered.add(relation);
				}
			}
			local = filtered;
		}

		JSONArray result = new JSONArray();
		JSONObject jsonObject = null;

		for (PropertyInstance propertyInstance : local) {
			jsonObject = new JSONObject();
			JsonUtil.addToJson(jsonObject, DefaultProperties.NAME, propertyInstance.getProperties().get(INSTANCE));
			JsonUtil.addToJson(jsonObject, DefaultProperties.TITLE, propertyInstance.getLabel(getlanguage(null)));
			JsonUtil.addToJson(jsonObject, DOMAIN_CLASS, propertyInstance.getProperties().get(DOMAIN_CLASS));
			JsonUtil.addToJson(jsonObject, "type", propertyInstance.getProperties().get("rangeClass"));
			JsonUtil.addToJson(jsonObject, DefaultProperties.DESCRIPTION,
					propertyInstance.getProperties().get("definition"));
			result.put(jsonObject);
		}

		return result;
	}

	/**
	 * Retrieve the definition fields(with regions). The logic loads the definition by two ways, because the instance
	 * can be new or persisted. After the definition is loaded will be triggered value extraction by code list(if any)
	 * and external model that holds code list keys and/or field value(evaluated). At the end of the logic is expected
	 * JSON model with fields, regions, code list values and evaluated values from definition.
	 * <p>
	 * The logic will be terminate if the entry parameters for new and persisted instance are unavailable.
	 * <p>
	 * The logic will be terminated if the model is not available.
	 *
	 * @param definitionId
	 *            definition identifier
	 * @param currentInstanceType
	 *            current instance type
	 * @param currentInstanceId
	 *            current instance identifier
	 * @param parentInstanceType
	 *            parent(owning) instance type
	 * @param parentInstanceId
	 *            parent(owning) instance identifier
	 * @return JSON model tree with definition fields and fields parameters
	 */
	@GET
	@Path("/{definitionId}/fields")
	@Produces(MediaType.APPLICATION_JSON)
	public Response retrieveFields(@PathParam("definitionId") String definitionId,
			@QueryParam("currentInstanceType") String currentInstanceType,
			@QueryParam("currentInstanceId") String currentInstanceId,
			@QueryParam("parentInstanceType") String parentInstanceType,
			@QueryParam("parentInstanceId") String parentInstanceId) {

		boolean isInstancePersisted = validateRequestParams(currentInstanceId, currentInstanceType);
		boolean isInstanceNew = validateRequestParams(definitionId, parentInstanceId, parentInstanceType,
				currentInstanceType);
		if (!isInstancePersisted && !isInstanceNew) {
			return RestUtil.buildBadRequestResponse("Missing required parameters for retrieving definition fields!");
		}
		DefinitionModel definition = null;
		Instance instance = null;
		Map<String, String> codelistValues = null;
		Map<String, String> properties = null;

		if (isInstancePersisted) {
			instance = fetchInstance(currentInstanceId, currentInstanceType);
			definition = dictionaryService.find(instance.getIdentifier());
		} else {
			definition = loadDefinition(definitionId, currentInstanceType);
			Instance parentInstance = fetchInstance(parentInstanceId, parentInstanceType);
			instance = instanceService.createInstance(definition, parentInstance);
		}

		JSONObject jsonDefinition = convertDefinitionToJson(definition);

		properties = getExternalModel(instance, definition);
		codelistValues = extractCodelistDescriptions(properties, definition);

		JsonUtil.addToJson(JsonUtil.toJsonObject(properties), CODELIST_DESCRIPTIONS, codelistValues);
		if (JsonUtil.isNullOrEmpty(jsonDefinition)) {
			return RestUtil.buildErrorResponse("Can't load definition model fields!");
		}
		JsonUtil.addToJson(jsonDefinition, PROPERTIES, properties);
		return RestUtil.buildResponse(Response.Status.OK, jsonDefinition.toString());
	}

	/**
	 * Retrieves system or semantic definition fields as is without modifications. Allows batch of definitions to be
	 * retrieved. If no identifiers are requested then return all main searchable (semantic) types
	 *
	 * @param data
	 *            definition identifiers or semantic URIs
	 * @return {@link Response} representing JSON model tree with definition fields and fields parameters
	 */
	@POST
	@Path("/fields")
	@Produces(MediaType.APPLICATION_JSON)
	public Response retrieveDefinitionFields(String data) {
		JSONObject jsonData = JsonUtil.createObjectFromString(data);
		JSONArray jsonArray = JsonUtil.getJsonArray(jsonData, "identifiers");
		List<String> identifiers = (List<String>) JsonUtil.jsonArrayToList(jsonArray);

		JSONArray result = new JSONArray();
		if (identifiers == null) {
			identifiers = new ArrayList<>();
		}
		if (identifiers.isEmpty()) {
			identifiers.addAll(getMainSearchableTypes());
		}

		for (String identifier : identifiers) {
			DefinitionModel definition = dictionaryService.find(identifier);
			if (definition != null) {
				JSONObject convert = typeConverter.convert(JSONObject.class, definition);
				JsonUtil.addToJson(convert, LABEL, definitionHelper.getDefinitionLabel(definition));
				JsonUtil.addToJson(result, convert);
			} else {
				String fullURI = namespaceRegistryService.buildFullUri(identifier);
				DataTypeDefinition typeDef = dictionaryService.getDataTypeDefinition(fullURI);
				if (typeDef != null) {
					List<DefinitionModel> allDefinitions = dictionaryService.getAllDefinitions(GenericDefinition.class);

					ClassInstance classInstance = semanticDefinitionService.getClassInstance(fullURI);
					Set<String> rdfTypes = createHashSet(1 + classInstance.getSubClasses().size());
					rdfTypes.add(fullURI);
					// for non top level classes collect their children
					classInstance.getSubClasses().keySet().stream().map(namespaceRegistryService::buildFullUri).forEach(
							rdfTypes::add);

					final TypeConverter converter = typeConverter;
					JSONArray props = new JSONArray(allDefinitions
							.stream()
								.filter(filterByDefinitionType(typeDef).and(filterByRdfType(rdfTypes)))
								.flatMap(DefinitionModel::fieldsStream)
								.distinct()
								.map(prop -> converter.convert(JSONObject.class, prop))
								.collect(Collectors.toList()));

					JSONObject obj = new JSONObject();
					JsonUtil.addToJson(obj, DefaultProperties.UNIQUE_IDENTIFIER, identifier);
					JsonUtil.addToJson(obj, LABEL, classInstance.getLabel(getlanguage(null)));
					JsonUtil.addToJson(obj, FIELDS, props);
					JsonUtil.addToJson(result, obj);
					LOGGER.trace("Adding {} fields for {}", props.length(), identifier);
				} else {
					LOGGER.info("No datatype definition found for " + identifier);
				}
			}
		}
		return RestUtil.buildResponse(Response.Status.OK, result.toString());
	}

	/**
	 * Filter definition models if they are of the generic definitions so that they match the type represented by the
	 * given data type definition
	 */
	private Predicate<DefinitionModel> filterByDefinitionType(DataTypeDefinition typeDefinition) {
		return model -> !(model instanceof GenericDefinition)
				|| nullSafeEquals(typeDefinition.getName(), typeProvider.getDataTypeName(model.getType()), true);
	}

	/**
	 * filter definitions that have semantic that match the requested or does not have such field at all (this is for
	 * cases/WF, etc)
	 */
	private static Predicate<DefinitionModel> filterByRdfType(Set<String> types) {
		return model -> {
			Optional<PropertyDefinition> type = model.getField(DefaultProperties.SEMANTIC_TYPE);

			return !type.isPresent() || type.get().getDefaultValue() == null
					|| types.contains(type.get().getDefaultValue());
		};
	}

	/**
	 * @return main searchable types URIs
	 */
	private List<String> getMainSearchableTypes() {
		return dictionaryService
				.getAllDefinitions()
				.flatMap(toSemanticDefinitions())
				.filter(searchableSemanticDefinition())
				.sorted(sortByLabel())
				.flatMap(toURI())
				.distinct()
				.collect(Collectors.toList());
	}

	private Function<DefinitionModel, Stream<ClassInstance>> toSemanticDefinitions() {
		return model -> {
			Optional<PropertyDefinition> field = model.getField(SEMANTIC_TYPE);
			if (field.isPresent()) {
				String semanticType = field.get().getDefaultValue();
				ClassInstance classInstance = null;
				if(org.apache.commons.lang3.StringUtils.isNotBlank(semanticType) &&
						(classInstance = semanticDefinitionService.getClassInstance(semanticType)) != null) {
					Stream<ClassInstance> superClasses = classInstance.getSuperClasses().stream();
					return Stream.concat(Stream.of(classInstance), superClasses);
				}
			}
			return Stream.empty();
		};
	}

	private static Predicate<ClassInstance> searchableSemanticDefinition() {
		return classInstance -> classInstance.type().isSearchable();
	}

	private static Function<ClassInstance, Stream<String>> toURI() {
		return classInstance -> Stream.of(classInstance.getId().toString());
	}

	private Comparator<? super ClassInstance> sortByLabel() {
		Collator collator = Collator.getInstance(Locale.forLanguageTag(userPreferences.getLanguage()));
		return (m1, m2) -> collator.compare(m1.getLabel(), m2.getLabel());
	}

	/**
	 * Getter for external model.
	 *
	 * @param instance
	 *            current instance
	 * @param definition
	 *            current definition model
	 * @return external model
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, String> getExternalModel(Instance instance, DefinitionModel definition) {
		return (Map<String, String>) propertyConverter.convertToExternalModel(instance, definition);
	}

	/**
	 * Extract code list descriptions by given external and definition model.
	 *
	 * @param model
	 *            external model
	 * @param definition
	 *            definition model
	 * @return code list descriptions
	 */
	private Map<String, String> extractCodelistDescriptions(Map<String, String> model, DefinitionModel definition) {
		return definition
				.fieldsStream()
					.filter(propDef -> model.containsKey(propDef.getIdentifier()))
					.filter(propDef -> propDef.getCodelist() != null)
					.map(propDef -> getCodelistDescription(propDef.getCodelist(), model.get(propDef.getIdentifier()),
							propDef.getIdentifier()))
					.filter(Objects::nonNull)
					.collect(Collectors.toConcurrentMap(Pair::getFirst, Pair::getSecond));
	}

	/**
	 * Getter for code list descriptions.
	 *
	 * @param codelist
	 *            current code list number
	 * @param value
	 *            code list value
	 * @param propKey
	 *            field identifier
	 * @return code list descriptions
	 */
	private Pair<String, String> getCodelistDescription(Integer codelist, String value, String propKey) {
		CodeValue codeValue = codelistService.getCodeValue(codelist, value);
		if (codeValue == null || !CollectionUtils.isNotEmpty(codeValue.getProperties())) {
			return null;
		}
		return new Pair<>(propKey, (String) codeValue.getProperties().get(userPreferences.getLanguage()));
	}

	/**
	 * Validate specific number of parameters received from AJAX request.
	 *
	 * @param parameters
	 *            parameters received from the request
	 * @return true if they are valid
	 */
	private static boolean validateRequestParams(String... parameters) {
		for (String parameter : parameters) {
			if (org.apache.commons.lang.StringUtils.isBlank(parameter) || NULLABLE_VALUE.equals(parameter)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Convert definition model to JSON format.
	 *
	 * @param definition
	 *            definition model
	 * @return JSON object
	 */
	protected JSONObject convertDefinitionToJson(DefinitionModel definition) {
		return typeConverter.convert(JSONObject.class, definition);
	}

	/**
	 * Load definition by instance type and definition id.
	 *
	 * @param definitionId
	 *            the definition id
	 * @param instanceType
	 *            the instance type
	 * @return the definition model
	 */
	protected DefinitionModel loadDefinition(String definitionId, String instanceType) {
		return dictionaryService.find(definitionId);
	}

	protected void addHeadersToModels(Instance instance, JSONObject instanceModels) {
		JSONObject headers = new JSONObject();
		JsonUtil.addToJson(headers, DefaultProperties.HEADER_DEFAULT,
				headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_DEFAULT));
		JsonUtil.addToJson(headers, DefaultProperties.HEADER_COMPACT,
				headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_COMPACT));
		JsonUtil.addToJson(headers, DefaultProperties.HEADER_BREADCRUMB,
				headersService.generateInstanceHeader(instance, DefaultProperties.HEADER_BREADCRUMB));
		JsonUtil.addToJson(instanceModels, HEADERS, headers);
	}

	/**
	 * Retrieve an instance definition model using the actual instance to get the property values.
	 *
	 * @param definitionId
	 *            the definition id
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @param parentInstanceId
	 *            the parent instance id
	 * @param parentInstanceType
	 *            the parent instance type
	 * @param mandatory
	 *            if only field with attribute mandatory=true should be returned
	 * @param operation
	 *            the instance operation is needed because the required fields defined in state transitions can be
	 *            matched using the operation id
	 * @return the definition model
	 */
	@GET
	@Path("{definitionId}/properties")
	public Response propertiesValues(@PathParam(DEFINITION_ID) String definitionId,
			@QueryParam(INSTANCE_ID) String instanceId, @QueryParam(INSTANCE_TYPE) String instanceType,
			@QueryParam(PARENT_INSTANCE_ID) String parentInstanceId,
			@QueryParam(PARENT_INSTANCE_TYPE) String parentInstanceType, @QueryParam(MANDATORY) boolean mandatory,
			@QueryParam(OPERATION) String operation) {

		DefinitionModel definitionModel;
		Instance instance;
		Instance parentInstance = null;

		boolean isNewInstance = StringUtils.isNullOrEmpty(instanceId);
		if (isNewInstance) {
			definitionModel = dictionaryService.find(definitionId);
			if (parentInstanceId != null) {
				parentInstance = fetchInstance(parentInstanceId, parentInstanceType);
			}
			instance = instanceService.createInstance(definitionModel, parentInstance);
		} else {
			instance = fetchInstance(instanceId, instanceType);
			definitionModel = dictionaryService.getInstanceDefinition(instance);
		}

		Map<String, ?> externalModel = propertyConverter.convertToExternalModel(instance, definitionModel);
		Set<String> mandatoryFieldIds = definitionHelperOld.getMandatoryFieldIds(definitionModel, instance, operation);
		List<Ordinal> sortedFields;
		if (mandatory) {
			sortedFields = definitionHelperOld.collectMandatoryFields(definitionModel, mandatoryFieldIds);
		} else {
			sortedFields = definitionHelperOld.collectAllFields(definitionModel);
		}

		JSONObject models = definitionHelperOld.toJsonModel(externalModel, sortedFields, labelProvider,
				mandatoryFieldIds, isNewInstance);
		addHeadersToModels(instance, models);
		JsonUtil.addToJson(models, "path", getInstancePath(instance));
		JsonUtil.addToJson(models, "definitionId", definitionModel.getIdentifier());
		JsonUtil.addToJson(models, "definitionLabel", definitionHelper.getDefinitionLabel(definitionModel));
		JsonUtil.addToJson(models, "instanceType", instance.type().getCategory());

		return RestUtil.buildResponse(Status.OK, models.toString());
	}

	static JSONArray getInstancePath(Instance instance) {
		JSONArray result = new JSONArray();

		for (Instance current : InstanceUtil.getParentPath(instance, true)) {

			JSONObject element = new JSONObject();
			JsonUtil.addToJson(element, "id", current.getId());
			JsonUtil.addToJson(element, "type", instance.type().getCategory());
			JsonUtil.addToJson(element, "compactHeader", current.get(DefaultProperties.HEADER_COMPACT));

			result.put(element);
		}

		return result;
	}

}
