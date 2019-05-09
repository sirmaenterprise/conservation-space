package com.sirma.itt.emf.solr.services.properties;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.search.SearchablePropertiesService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * <p>
 * Implementation of {@link SearchablePropertiesServiceImpl}. Extracts properties from the system that are considered
 * searchable based on specific criteria and puts them into a map for later retrieval. The service populates the map on
 * initialization, definition reloading or if {@link #reset()} is invoked.
 * </p>
 * <p>
 * The service extracts the searchable properties from the definitions, but only those who have a range class in the
 * semantics. The range class is important because it's used in the WEB for creating auto suggests.
 * </p>
 *
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
@ApplicationScoped
public class SearchablePropertiesServiceImpl implements SearchablePropertiesService {
	private static final String OBJECT_TYPE = "object";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String TYPE_SEPARATOR = "_";

	@Inject
	private SolrConnector solrConnector;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private CodelistService codelistService;

	/**
	 * Used to store the parsed searchable properties information.
	 */
	@Inject
	private ContextualMap<String, List<SearchableProperty>> searchablePropertiesCache;

	/**
	 * Used to store the semantic class - definition id relation for domain objects. Used when retrieving domain objects
	 * definition fields, since they are requested by their semantic class, not definition id.
	 */
	@Inject
	private ContextualMap<String, String> semanticDefinitionMapping;

	/**
	 * Used when determining the type of the property. Used in the web layer as an indicator for the available values of
	 * each property.
	 */
	@Inject
	private ContextualMap<String, String> rangeClassMapping;

	/**
	 * Used to store the query response for solr's schema so we don't have to extract it on every request. Loaded only
	 * in the {@link SearchablePropertiesService#reset()}.
	 */
	@Inject
	private Contextual<LinkedHashMap<String, Object>> solrSchema;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private UserPreferences userPreferences;

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Observes the event fired after definition loading to trigger property reloading. This method is invoked
	 * <b>ONLY</b> if the service has been initialized to avoid double reloading.
	 *
	 * @param event
	 *            - the reload event
	 */
	public void onDefinitionReload(@Observes(notifyObserver = Reception.IF_EXISTS) DefinitionsChangedEvent event) {
		reset();
	}

	/**
	 * Initialize the service cache.
	 */
	@PostConstruct
	public void init() {
		searchablePropertiesCache.initializeWith(() -> new ConcurrentHashMap<>(512));
		solrSchema.initializeWith(this::getSolrSchema);
	}

	@Override
	public List<SearchableProperty> getSearchableSolrProperties(String forType, Boolean commonOnly, Boolean multiValued,
			Boolean skipObjectProperties) {
		Set<SearchableProperty> properties = getProperties(forType, Boolean.TRUE.equals(commonOnly));
		List<SearchableProperty> result = new ArrayList<>(properties.size());

		Map<String, String> solrFields = getSolrFields(Boolean.TRUE.equals(multiValued));

		for (SearchableProperty property : properties) {
			if (Boolean.TRUE.equals(skipObjectProperties) && OBJECT_TYPE.equals(property.getPropertyType())) {
				continue;
			}

			String solrFieldName = property.getSolrFieldName();

			String solrType = solrFields.get(solrFieldName);
			if (solrType == null) {
				solrFieldName = SolrQueryConstants.FIELD_NAME_FACET_PREFIX + solrFieldName;
				solrType = solrFields.get(solrFieldName);
			}
			if (solrType == null) {
				solrFieldName = property.getUri().substring(property.getUri().indexOf(':') + 1);
				solrType = solrFields.get(solrFieldName);
			}
			if (solrType != null) {
				property.setSolrType(solrType);
				property.setSolrFieldName(solrFieldName);

				result.add(property);
			}
		}
		return SearchablePropertiesUtils.sort(result, (p1, p2) -> p1.getText().compareTo(p2.getText()));
	}

	@Override
	public List<SearchableProperty> getSearchableSemanticProperties(String forType) {
		Set<SearchableProperty> properties = getProperties(forType, false);
		List<SearchableProperty> result = new ArrayList<>(properties.size());

		Map<String, String> solrFields = getSolrFields(true);

		for (SearchableProperty property : properties) {
			String identifier = property.getId();

			property.setSolrType(solrFields.get(identifier));
			// Removes the type property when there isn't selected a specific
			// object type. CMF-13261
			boolean shouldKeepType = !(StringUtils.isBlank(forType)
					&& DefaultProperties.TYPE.equals(property.getId()));

			if (shouldKeepType) {
				result.add(property);
			}

		}
		return SearchablePropertiesUtils.sort(result, (p1, p2) -> p1.getText().compareTo(p2.getText()));
	}

	@Override
	@RunAsAllTenantAdmins
	@OnTenantAdd
	@Startup(async = true)
	public void reset() {
		solrSchema.reset();
		rangeClassMapping.replaceContextValue(getRangeClassMapping());

		TimeTracker timeTracker = TimeTracker.createAndStart();
		Map<String, List<SearchableProperty>> searchablePropertiesLocal = new HashMap<>();
		Map<String, String> semanticDefinitionMappingLocal = new HashMap<>();
		for (ClassInstance instance : semanticDefinitionService.getSearchableClasses()) {
			Stream<DefinitionModel> allDefinitions = getAllDefinitionModels(instance);
			allDefinitions.forEach(definitionModel -> {
				PropertyDefinition rdfType = definitionModel.getField(DefaultProperties.SEMANTIC_TYPE).orElse(null);
				if (rdfType == null) {
					return;
				}
				List<SearchableProperty> properties = getSearchableProperties(definitionModel,
						instance.getId().toString());
				if (!properties.isEmpty()) {
					searchablePropertiesLocal.put(definitionModel.getIdentifier(), properties);
				}

				if (rdfType.getDefaultValue() != null) {
					String shortURI = namespaceRegistryService.getShortUri(rdfType.getDefaultValue());
					semanticDefinitionMappingLocal.put(shortURI, definitionModel.getIdentifier());
				}
			});
		}

		Map<String, List<SearchableProperty>> cache = getCache();
		cache.putAll(searchablePropertiesLocal);

		semanticDefinitionMapping.replaceContextValue(semanticDefinitionMappingLocal);
		LOGGER.info("Searchable properties and semantic-definition mapping took {} ms", timeTracker.stop());
	}

	@Override
	public Optional<SearchableProperty> getSearchableProperty(String forType, String propertyId) {
		return getSearchableSolrProperties(forType, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE)
				.stream()
					.filter(property -> propertyId.equals(property.getId()))
					.findFirst();
	}

	/**
	 * Reload the solr schema. Since sending a request to solr and waiting for a response is quite a slow operation, the
	 * response is stored for later retrieval and reloaded in the {@link SearchablePropertiesService#reset()} method.
	 *
	 * @return the solr schema from solr
	 */
	private LinkedHashMap<String, Object> getSolrSchema() {
		return new LinkedHashMap<>(solrConnector.retrieveSchema());
	}

	/**
	 * Retrieves the semantic properties from {@link SemanticDefinitionService} and creates a mapping between short URI
	 * and rangeClass property that is used when constructing {@link SearchableProperty}.
	 *
	 * @return a map between short URI and rangeClass
	 */
	private Map<String, String> getRangeClassMapping() {
		TimeTracker timeTracker = TimeTracker.createAndStart();
		List<PropertyInstance> semanticProperties = semanticDefinitionService.getProperties();
		Map<String, String> mapping = new HashMap<>();
		for (PropertyInstance property : semanticProperties) {
			// Faster for extraction later.
			String fullUri = property.getId().toString();
			String shortUri = namespaceRegistryService.getShortUri(fullUri);
			mapping.put(shortUri, property.getRangeClass());
		}
		LOGGER.info("Range class mapping initialized in {} ms", timeTracker.stop());
		return mapping;
	}

	/**
	 * Retrieves all {@link DefinitionModel} for given {@link ClassInstance}
	 *
	 * @param instance
	 *            the given {@link ClassInstance}
	 * @return a {@link List} of {@link DefinitionModel}
	 */
	private Stream<DefinitionModel> getAllDefinitionModels(ClassInstance instance) {
		return definitionService.getAllDefinitions(instance.type());
	}

	private Stream<DefinitionModel> getAllDefinitionModels(String classUri) {
		ClassInstance classInstance = semanticDefinitionService.getClassInstance(classUri);
		if (classInstance == null) {
			return Stream.empty();
		}
		return getAllDefinitionModels(classInstance);
	}

	/**
	 * Returns the searchable properties for specified types.
	 *
	 * @param forType
	 *            Types to retrieve literals for (optional). Parent semantic type and definition sub type must be
	 *            delimited by underscore
	 * @param commonOnly
	 *            If the service should return the intersection of the properties.
	 * @return list with the definition fields by type. Returns a map with key - the property id and value containing
	 *         the label and code list number for that property, or null if no code list
	 */
	private Set<SearchableProperty> getProperties(String forType, boolean commonOnly) {
		Set<SearchableProperty> properties = new HashSet<>();
		if (StringUtils.isNotBlank(forType)) {
			// Take each type's properties and do an intersection of them.
			for (String forTypeEntry : forType.split("\\s*,\\s*")) {
				String semanticType = forTypeEntry;
				String definitionType = null;

				int lastDashPos = forTypeEntry.lastIndexOf(TYPE_SEPARATOR);
				if (lastDashPos != -1) {
					semanticType = forTypeEntry.substring(0, lastDashPos);
					definitionType = forTypeEntry.substring(lastDashPos + 1);
				}

				Set<SearchableProperty> definitionProperties = getDefinitionPropertiesForType(definitionType,
						semanticType);
				SearchablePropertiesUtils.merge(properties, definitionProperties, this::mergeCodelists);
				SearchablePropertiesUtils.addOrRetain(properties, definitionProperties, commonOnly);
			}
		} else {
			Map<String, List<SearchableProperty>> cache = getOrReloadCache();
			for (Entry<String, List<SearchableProperty>> entry : cache.entrySet()) {
				Set<SearchableProperty> searchablePropertiesClone = SearchablePropertiesUtils.clone(entry.getValue());

				SearchablePropertiesUtils.merge(properties, searchablePropertiesClone, this::mergeCodelists);
				SearchablePropertiesUtils.addOrRetain(properties, searchablePropertiesClone, commonOnly);
				if (properties.isEmpty()) {
					LOGGER.info("All common searchable properties are filtered by: {}", entry.getKey());
					return properties;
				}
			}
		}
		return properties;
	}

	private Set<SearchableProperty> getDefinitionPropertiesForType(String definitionType, String semanticType) {
		Map<String, List<SearchableProperty>> cache = getOrReloadCache();
		Set<SearchableProperty> definitionProperties;

		if (StringUtils.isNotBlank(semanticType) && StringUtils.isBlank(definitionType)) {
			// If the given semantic type has a definition
			String definitionId = semanticDefinitionMapping.get(semanticType);
			if (definitionId != null) {
				return getDefinitionPropertiesForType(definitionId, semanticType);
			}
			definitionProperties = getAllSearchableProperties(semanticType);
		} else {
			definitionProperties = SearchablePropertiesUtils.clone(cache.get(definitionType));
			if (definitionProperties == null) {
				String semanticClass = semanticDefinitionMapping.get(definitionType);
				if (semanticClass != null) {
					definitionProperties = SearchablePropertiesUtils.clone(cache.get(semanticClass));
				} else {
					definitionProperties = getAllSearchableProperties(semanticType);
				}
			}

		}
		return definitionProperties;
	}

	/**
	 * Merge the codelists of two searchable properties.
	 *
	 * @param toBeModified
	 *            the searchable property whose codelists are going to be modified
	 * @param matchAgainst
	 *            the searchable property whose codelists are going to be added to the first one
	 */
	private void mergeCodelists(SearchableProperty toBeModified, SearchableProperty matchAgainst) {
		if (toBeModified.getCodelists() == null) {
			matchAgainst.setCodelists(new HashSet<>());
		}
		if (CollectionUtils.isNotEmpty(matchAgainst.getCodelists())) {
			toBeModified.getCodelists().addAll(matchAgainst.getCodelists());
		}
	}

	/**
	 * Gets the searchable properties based on the provided definition id and RDF type. There are three combinations of
	 * parameters that can be passed to this method. Used only when reloading the properties and repopulating the cache.
	 *
	 * @return the searchable properties as a list
	 */
	private List<SearchableProperty> getSearchableProperties(DefinitionModel definition, String rdfType) {
		// If the id is also specified, try to get it's fields from the
		// specified definition.
		// (ptop:DomainObject_EO10...)
		List<SearchableProperty> properties = getRelationProperties(rdfType)
				.filter(byDefinition(definition))
					.collect(Collectors.toList());
		properties.addAll(getSearchablePropertiesFromDefinition(definition));
		return properties;
	}

	private Predicate<SearchableProperty> byDefinition(DefinitionModel definition) {
		return property -> {
			Optional<PropertyDefinition> field = definition.getField(property.getId());
			if (!field.isPresent()) {
				field = definition.findField(PropertyDefinition.hasUri(property.getUri()));
			}
			return field.isPresent() && isPropertyDefinitionValid(field.get());
		};
	}

	/**
	 * Gets the searchable properties from all definitions with the specified semantic type.
	 *
	 * @param shortUri
	 *            the semantic type
	 * @return all definitions properties
	 */
	private Set<SearchableProperty> getAllSearchableProperties(String shortUri) {
		Set<SearchableProperty> properties = new HashSet<>();
		Map<String, List<SearchableProperty>> cache = getOrReloadCache();
		if (StringUtils.isNotBlank(shortUri)) {
			properties = getAllDefinitionModels(shortUri)
					.map(DefinitionModel::getIdentifier)
						.map(cache::get)
						.filter(Objects::nonNull)
						.map(SearchablePropertiesUtils::clone)
						.reduce(new HashSet<>(),
								(set1, set2) -> SearchablePropertiesUtils.combine(set1, set2, this::mergeCodelists));
		}
		Set<SearchableProperty> props = SearchablePropertiesUtils.clone(cache.get(shortUri));
		if (props != null) {
			properties.addAll(props);
		}
		return properties;
	}

	@Override
	public Map<String, List<PropertyDefinition>> getTypeFields(Instance instance, String type, String definitionId) {
		Map<String, List<PropertyDefinition>> typeFields = new HashMap<>();

		if (instance != null) {
			DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
			if (instanceDefinition == null) {
				return typeFields;
			}

			String domainClass = instance.type().getId().toString();
			typeFields.put(domainClass, getDefinitionFields(instanceDefinition));
			return typeFields;
		}
		return loadPropertiesByType(type, definitionId);
	}

	/**
	 * Load properties by forType and definitionId.
	 *
	 * @param type
	 *            - searchable type
	 * @param definitionId
	 *            - definition id
	 * @return all definitions fields for searchable type
	 */
	private Map<String, List<PropertyDefinition>> loadPropertiesByType(String type, String definitionId) {
		Map<String, List<PropertyDefinition>> typeFields = new HashMap<>();
		List<ClassInstance> searchableClasses = new LinkedList<>();
		String domainClass = null;
		if (definitionId != null) {
			searchableClasses.add(semanticDefinitionService.getClassInstance(definitionId));
		} else {
			searchableClasses = semanticDefinitionService.getSearchableClasses();
		}

		for (ClassInstance searchableClass : searchableClasses) {
			List<DefinitionModel> definitionModels = new LinkedList<>();
			List<PropertyDefinition> properties = new LinkedList<>();
			if (searchableClass == null) {
				if (typeConverter.convert(Uri.class, type) != null) {
					DefinitionModel model = getDefinitionModel(definitionId);
					definitionModels.add(model);
					PropertyDefinition property = definitionService.getProperty(DefaultProperties.TYPE, null,
							(PathElement) model);
					domainClass = codelistService.getDescription(property.getCodelist(), property.getDefaultValue());
				}
			} else {
				String semanticClass = semanticDefinitionMapping.get(searchableClass.getId().toString());

				if (semanticClass != null) {
					definitionModels.add(getDefinitionModel(semanticClass));
				} else {
					getAllDefinitionModels(searchableClass).forEach(definitionModels::add);
				}
				domainClass = namespaceRegistryService.buildFullUri((String) searchableClass.getId());
			}

			for (DefinitionModel definitionModel : definitionModels) {
				properties.addAll(getDefinitionFields(definitionModel));
			}
			typeFields.put(domainClass, properties);
		}
		return typeFields;
	}

	/**
	 * Gets all fields of given definition.
	 */
	private static List<PropertyDefinition> getDefinitionFields(DefinitionModel definition) {
		return definition.fieldsStream().collect(Collectors.toCollection(LinkedList::new));
	}

	/**
	 * Gets the searchable properties of a definition that are not hidden or system. Used only when reloading the
	 * properties and repopulating the cache.
	 *
	 * @return the searchable properties as a list
	 */
	private List<SearchableProperty> getSearchablePropertiesFromDefinition(DefinitionModel definition) {
		if (definition != null) {
			return definition
					.fieldsStream()
						.filter(this::isDataPropertyDefinitionValid)
						.map(this::createSearchableProperty)
						.collect(Collectors.toList());
		}
		return new LinkedList<>();
	}

	/**
	 * Checks if the provided property definition is valid so it can be added to the searchable properties. The
	 * definition must have a label & URI and its display type mustn't be system, otherwise it is not considered to be
	 * searchable.
	 *
	 * @return true if it's valid(searchable) or false otherwise
	 */
	private static boolean isPropertyDefinitionValid(PropertyDefinition propertyDefinition) {
		// What if PropertyDefinition extends something like Searchable ?!
		if (propertyDefinition.getLabel() == null) {
			return false;
		}
		if (propertyDefinition.getDisplayType() == DisplayType.SYSTEM) {
			return false;
		}
		return !PropertyDefinition.hasUri().negate().test(propertyDefinition);
	}

	private boolean isDataPropertyDefinitionValid(PropertyDefinition propertyDefinition) {
		return isPropertyDefinitionValid(propertyDefinition)
				&& rangeClassMapping.get(propertyDefinition.getUri()) != null;
	}

	/**
	 * Creates a {@link SearchableProperty} out of given {@link PropertyDefinition}. Sets the id and label of the
	 * searchable property and if the definition property has a code list number. Additionally gets the rangeClass of
	 * the property from {@link #rangeClassMapping} which is used in the WEB to recognize the type and create an
	 * autosuggest for the property.
	 *
	 * @return new {@link SearchableProperty}
	 */
	private SearchableProperty createSearchableProperty(PropertyDefinition propertyDefinition) {
		SearchableProperty property = new SearchableProperty();
		property.setId(propertyDefinition.getName());

		String labelId = propertyDefinition.getLabelId();
		property.setLabelId(() -> labelId);
		property.setLabelProvider(labelProvider.getLabelProvider());

		property.setUri(propertyDefinition.getUri());
		if (propertyDefinition.getCodelist() != null) {
			property.setCodelists(new HashSet<>(Collections.singletonList(propertyDefinition.getCodelist())));
		}
		String rangeClass = rangeClassMapping.get(propertyDefinition.getUri());
		property.setRangeClass(rangeClass);
		property.setPropertyType("definition");
		return property;
	}

	/**
	 * Gets the definition model by type and definition ID. The type must be a short URI.
	 *
	 * @param definitionId
	 *            the definition ID
	 * @return the definition model
	 */
	private DefinitionModel getDefinitionModel(String definitionId) {
		DefinitionModel definition = definitionService.find(definitionId);
		if (definition == null) {
			LOGGER.info("No definition model found for {}", definitionId);
		}
		return definition;
	}

	/**
	 * Gets the solr fields. This uses the fields from the already retrieved and stored solr schema request.
	 *
	 * @param allowTokenized
	 *            indicates whether tokenized fields should be returned.
	 * @return the solr fields
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> getSolrFields(boolean allowTokenized) {
		Map<String, String> fields = new HashMap<>();
		if (!solrSchema.isNotNull()) {
			return fields;
		}
		LinkedHashMap<String, Object> schema = solrSchema.getContextValue();
		List<SimpleOrderedMap<String>> fieldsList = (List<SimpleOrderedMap<String>>) schema.get("fields");
		Set<String> tokenizedFieldTypes = new HashSet<>();

		// If tokenized fields are not allowed, iterate over all field types,
		// find the ones that
		// have some sort of tokenizing on them and add them to the tokenized
		// fields map.
		if (!allowTokenized) {
			List<SimpleOrderedMap<Object>> fieldTypes = (List<SimpleOrderedMap<Object>>) schema.get("fieldTypes");
			for (SimpleOrderedMap<Object> fieldType : fieldTypes) {
				SimpleOrderedMap<Object> analyzer;
				analyzer = (SimpleOrderedMap<Object>) fieldType.get("analyzer");
				// If the analyzer is null, try to find an index analyzer.
				if (analyzer == null) {
					analyzer = (SimpleOrderedMap<Object>) fieldType.get("indexAnalyzer");
				}
				if (analyzer != null && analyzer.get("tokenizer") != null) {
					tokenizedFieldTypes.add(fieldType.get("name").toString());
				}
			}
		}

		for (SimpleOrderedMap<String> field : fieldsList) {
			String type = field.get("type");
			// If the type is in the tokenized fields map, we can't return it,
			// since it will add
			// a property that can be split during the faceting.
			if (!tokenizedFieldTypes.contains(type)) {
				fields.put(field.get("name"), field.get("type"));
			}
		}

		return fields;
	}

	/**
	 * Gets the relation properties for the given semantic type. Parses the solr connector and extracts extra
	 * information from the {@link SemanticDefinitionService}.
	 *
	 * @param classType
	 *            the semantic type
	 * @return the relation properties
	 */
	private Stream<SearchableProperty> getRelationProperties(String classType) {
		return semanticDefinitionService.getRelations(classType, null).stream().map(
				this::propertyInstanceToSearchableProperty);
	}

	private SearchableProperty propertyInstanceToSearchableProperty(PropertyInstance propertyInstance) {
		SearchableProperty searchableProperty = new SearchableProperty();
		searchableProperty.setRangeClass(propertyInstance.getRangeClass());
		searchableProperty.setUri(propertyInstance.getId().toString());

		UserPreferences localUserPreferences = userPreferences;
		searchableProperty.setLabelId(localUserPreferences::getLanguage);
		searchableProperty.setLabelProvider(propertyInstance.getLabelProvider());

		searchableProperty.setPropertyType(OBJECT_TYPE);
		String fullUri = namespaceRegistryService.buildFullUri(propertyInstance.getId().toString());
		searchableProperty.setId(fullUri);

		return searchableProperty;
	}

	/**
	 * Gets the searchable properties cache from the entity cache context.
	 */
	private Map<String, List<SearchableProperty>> getCache() {
		return searchablePropertiesCache;
	}

	/**
	 * Gets the searchable properties cache from the entity cache context. If the returned cache is empty, reload them
	 * first and then return it.
	 */
	private Map<String, List<SearchableProperty>> getOrReloadCache() {
		Map<String, List<SearchableProperty>> cache = getCache();
		if (cache.isEmpty()) {
			reset();
		}
		return cache;
	}
}
