package com.sirma.itt.emf.semantic;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashSet;
import static com.sirma.itt.seip.collections.CollectionUtils.createLinkedHashMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.InverseRelationProvider;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.definition.event.SemanticDefinitionsReloaded;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.SecureObserver;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.search.SemanticQueries;
import com.sirma.seip.semantic.events.SemanticModelUpdatedEvent;

/**
 * Implementation of SemanticDefinitionsService interface
 *
 * @author kirq4e
 */
@ApplicationScoped
public class SemanticDefinitionServiceImpl implements SemanticDefinitionService {

	private static final String SUPER_CLASS = "superClass";
	private static final String RANGE_CLASS = "rangeClass";
	private static final String DOMAIN_CLASS = "domainClass";
	private static final String SUPER_CLASSES = "superClasses";
	private static final String TITLE_LANGUAGE = "titleLanguage";
	private static final String TITLE = "title";
	private static final String DEFINITIONS = "definitions";
	private static final String DEFINITION_ID = "definitionId";
	private static final String ONTOLOGY = "ontology";
	private static final String PROPERTY_TYPE = "propertyType";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SearchService searchService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	private Contextual<SemanticDefinitionCache> cache;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private SemanticConfiguration semanticConfiguration;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private EventService eventService;

	@PostConstruct
	protected void initialize() {
		cache.initializeWith(() -> new SemanticDefinitionCache(searchService, definitionService,
				namespaceRegistryService));
	}

	/**
	 * Forces cache reload
	 */
	@RunAsAllTenantAdmins
	@OnTenantAdd(order = -800)
	@Startup(phase = StartupPhase.BEFORE_APP_START, async = true)
	void initializeCache() {
		getCache().reload();
		eventService.fire(new SemanticDefinitionsReloaded());
	}

	private SemanticDefinitionCache getCache() {
		return cache.getContextValue();
	}

	@Override
	public List<ClassInstance> getClasses() {
		return getCache().getAllClasses();
	}

	@Override
	public List<ClassInstance> getDataTypes() {
		return getCache().getAllDataTypes();
	}

	@Override
	public List<String> getHierarchy(String classType) {
		if (StringUtils.isBlank(classType)) {
			return CollectionUtils.emptyList();
		}

		Set<String> hierarchy = new LinkedHashSet<>();
		ClassInstance classInstance = getCache().getClassesCache().get(
				namespaceRegistryService.buildFullUri(classType));
		if (classInstance != null) {
			getHierarchy(classInstance, hierarchy);
			hierarchy.add(classInstance.getId().toString());
		}

		return new ArrayList<>(hierarchy);
	}

	@Override
	public Set<ClassInstance> collectSubclasses(String id) {
		if (!StringUtils.isNotBlank(id)) {
			return Collections.emptySet();
		}

		ClassInstance classInstance = getClassInstance(id);

		Set<ClassInstance> subclasses = new LinkedHashSet<>();
		SemanticDefinitionServiceImpl.collectSubclasses(classInstance, subclasses);
		return subclasses;
	}

	@Override
	@Transactional
	public void modelUpdated() {
		// uses 2 events to reload the model as the operations from the first event should be executed and completed
		// before the second event is fired
		// triggers class metadata generation before updating the internal cache
		transactionSupport.invokeInNewTx(() -> eventService.fire(new SemanticModelUpdatedEvent()));
		// reloads the internal cache
		eventService.fire(new LoadSemanticDefinitions());
	}

	private static void collectSubclasses(ClassInstance clazz, Set<ClassInstance> all) {
		if (all.contains(clazz)) {
			return;
		}

		all.add(clazz);
		for (ClassInstance classInstance : clazz.getSubClasses().values()) {
			collectSubclasses(classInstance, all);
		}
	}

	/**
	 * Fills recursively the list of classes of the hierarchy
	 *
	 * @param classInstance
	 *            Current class instance
	 * @param hierarchy
	 *            The hierarchy list
	 */
	private void getHierarchy(ClassInstance classInstance, Set<String> hierarchy) {
		List<ClassInstance> superClasses = classInstance.getSuperClasses();
		for (ClassInstance superClass : superClasses) {
			getHierarchy(superClass, hierarchy);
			hierarchy.add(superClass.getId().toString());
		}
	}

	@Override
	public List<PropertyInstance> getProperties() {
		return getCache().getAllProperties();
	}

	@Override
	public List<PropertyInstance> getProperties(String classType) {
		if (StringUtils.isBlank(classType)) {
			return new ArrayList<>(0);
		}

		Map<String, ClassInstance> classesCache = getCache().getClassesCache();
		String type = namespaceRegistryService.buildFullUri(classType);
		if (!classesCache.containsKey(type)) {
			return new ArrayList<>(0);
		}

		List<String> hierarchy = getHierarchy(classType);

		List<PropertyInstance> result = new ArrayList<>();
		for (String classId : hierarchy) {
			result.addAll(getClassInstance(classId).getFields().values());
		}
		return result;
	}

	@Override
	public List<PropertyInstance> getRelations() {
		return getCache().getAllRelations();
	}

	@Override
	public List<PropertyInstance> getSearchableRelations() {
		return getCache().getAllSearchableRelations();
	}

	@Override
	public PropertyInstance getRelation(String relationUri) {
		// TODO : Remove conversion to short URI and define short URI in types.xml
		// and avoid using namespaceRegistryService.getShortUri(fromClass) on
		// the following line
		return getCache().getRelationsCache().get(namespaceRegistryService.getShortUri(relationUri));
	}

	@Override
	@Produces
	public InverseRelationProvider getInverseRelationProvider() {
		return relationId -> {
			if (relationId == null) {
				return null;
			}
			PropertyInstance relation = getRelation(relationId);
			if (relation != null) {
				return relation.getInverseRelation();
			}
			return null;
		};
	}

	@Override
	public Boolean isSystemRelation(String propertyUri) {
		if (getCache().getRelationsCache().get(propertyUri) == null) {
			LOGGER.warn("Unknown relation [{}]", propertyUri);
			return Boolean.FALSE;
		}

		Serializable serializable = getCache().getRelationsCache().get(propertyUri).get(
				EMF.IS_SYSTEM_PROPERTY.getLocalName());
		return Boolean.TRUE.equals(serializable);
	}

	@Override
	public List<PropertyInstance> getRelations(String fromClass, String toClass) {
		List<String> fromClassList = CollectionUtils.emptyList();
		if (StringUtils.isNotBlank(fromClass)) {
			fromClassList = Collections.singletonList(fromClass);
		}
		List<String> toClasslist = CollectionUtils.emptyList();
		if (StringUtils.isNotBlank(toClass)) {
			toClasslist = Collections.singletonList(toClass);
		}
		return getRelations(fromClassList, toClasslist);
	}

	@Override
	public List<PropertyInstance> getRelations(Collection<String> fromClasses, Collection<String> toClasses) {
		if (isEmpty(fromClasses) && isEmpty(toClasses)) {
			return getSearchableRelations();
		}

		Set<String> domains = getHierarchy(fromClasses);
		Set<String> ranges = getHierarchy(toClasses);

		Set<PropertyInstance> relations = new HashSet<>(getSearchableRelations());
		List<PropertyInstance> filteredRelations = new ArrayList<>();
		for (PropertyInstance relation : relations) {
			String relationshipDomain = relation.getDomainClass();
			String relationshipRange = relation.getRangeClass();

			boolean domainMatches = isEmpty(domains)
					|| domains.contains(namespaceRegistryService.buildFullUri(relationshipDomain));
			boolean rangeMatches = isEmpty(ranges)
					|| ranges.contains(namespaceRegistryService.buildFullUri(relationshipRange));
			if (domainMatches && rangeMatches) {
				filteredRelations.add(relation);
			}
		}

		return filteredRelations;
	}

	private Set<String> getHierarchy(Collection<String> classIds) {
		if (isEmpty(classIds)) {
			return CollectionUtils.emptySet();
		}

		Set<String> hierarchy = new HashSet<>(classIds.size() + 10);
		classIds.forEach(element -> hierarchy.addAll(getHierarchy(element)));
		return hierarchy;
	}

	@Override
	public Map<String, PropertyInstance> getRelationsMap() {
		return getCache().getRelationsCache();
	}

	@Override
	public List<ClassInstance> getSearchableClasses() {
		return getCache().getSearchableTypes();
	}

	@Override
	public ClassInstance getClassInstance(String identifier) {
		LOGGER.trace("Loading class [{}]", identifier);
		if (StringUtils.isBlank(identifier)) {
			return null;
		}
		String fullUri = namespaceRegistryService.buildFullUri(identifier);
		return getCache().getClassesCache().get(fullUri);
	}

	@Override
	public List<ClassInstance> getClassesForOntology(String ontologyId) {
		if (StringUtils.isBlank(ontologyId)) {
			return CollectionUtils.emptyList();
		}
		return getCache()
				.getAllClasses()
					.stream()
					.filter(classInstance -> ontologyId.equals(classInstance.get(ONTOLOGY)))
					.collect(Collectors.toList());
	}

	@Override
	public ClassInstance getDataType(String identifier) {
		LOGGER.trace("Loading data type [{}]", identifier);
		if (StringUtils.isBlank(identifier)) {
			return null;
		}
		String fullUri = namespaceRegistryService.buildFullUri(identifier);
		return getCache().getDataTypesCache().get(fullUri);
	}

	@Override
	public List<PropertyInstance> getOwnProperties(String classType) {
		ClassInstance classInstance = getCache().getClassesCache().get(classType);
		return new ArrayList<>(classInstance.getFields().values());
	}

	@Override
	public List<ClassInstance> getLibrary(String libraryId) {
		if (!getCache().getLibraries().containsKey(libraryId)) {
			return Collections.emptyList();
		}
		return getCache().getLibraries().get(libraryId);
	}

	@Override
	@SecureObserver
	public void observeReloadDefinitionEvent(@Observes LoadSemanticDefinitions event) {
		SemanticOperationLogger.saveLog();
		getCache().reload();
		eventService.fire(new SemanticDefinitionsReloaded());
	}

	@Override
	public Map<String, PropertyInstance> getPropertiesMap() {
		return getCache().getAllPropertiesMap();
	}

	@Override
	public PropertyInstance getProperty(String uri) {
		String shortUri = namespaceRegistryService.getShortUri(uri);
		return getCache().getAllPropertiesMap().get(shortUri);
	}

	@Override
	public Supplier<Collection<String>> getTopLevelTypes() {
		return getCache().getTopLevelTypes();
	}

	@Override
	public ClassInstance getRootClass() {
		return getClassInstance(semanticConfiguration.getRootClassName().get());
	}

	@Override
	public String getMostConcreteClass(Collection<String> collection) {
		if (isEmpty(collection)) {
			return null;
		}
		List<String> fullUri = collection.stream().map(namespaceRegistryService::buildFullUri).collect(
				Collectors.toList());
		Map<String, ClassInstance> classesCache = getCache().getClassesCache();

		Iterator<String> it = fullUri.iterator();
		ClassInstance lowestClass = getNextValidClass(it, classesCache);
		while (it.hasNext()) {
			ClassInstance current = lowestClass;
			for (String id : fullUri) {
				if (lowestClass != null && lowestClass.hasSubType(id)) {
					lowestClass = getNextValidClass(it, classesCache);
					break;
				}
			}
			// if the references are the same this means that this class does not have a sub class that is any of the
			// other classes and we need to exit
			if (current != null && current == lowestClass) {
				// this here is special case when an instance is resolved with more than one class hierarchy and the
				// second hierarchy is not searchable or does not have any common grounds with the other.
				// as an example for emf:Document is returned ptop:Event (added probably by some inferred statement)
				// that breaks the resolving.
				if (!current.isSearchable() && CollectionUtils.isNotEmpty(current.getSubClasses()) && it.hasNext()) {
					ClassInstance nextValid = getNextValidClass(it, classesCache);
					lowestClass = EqualsHelper.getOrDefault(nextValid, lowestClass);
				} else {
					break;
				}
			}
		}
		if (lowestClass == null) {
			// Fix for CMF-22178. emf:ClassDescription is the default choice if the lowest class can't be retrieved.
			// This can happen due to the messed order of the URIs in the 'collection' method argument
			// (which depends on solr)
			return collection.contains(EMF.CLASS_DESCRIPTION.toString()) ? EMF.CLASS_DESCRIPTION.toString() : null;
		}
		return lowestClass.getId().toString();
	}

	private static ClassInstance getNextValidClass(Iterator<String> classIdProvider,
			Map<String, ClassInstance> classesCache) {
		ClassInstance current = null;
		while (current == null && classIdProvider.hasNext()) {
			current = classesCache.get(classIdProvider.next());
		}
		return current;
	}

	/**
	 * Wrapper object to hold all semantic definition caches to allow group reload and clean up
	 *
	 * @author BBonev
	 */
	private static class SemanticDefinitionCache {

		private static final String PROPERTY_DESCRIPTION = "definition";
		private static final String PROPERTY_DESCRIPTION_LANG = "definitionLanguage";
		private static final String CLASS_DESCRIPTION = "description";
		private static final String CLASS_DESCRIPTION_LANG = "descriptionLanguage";
		private static final String CREATOR = "creator";
		private static final String CREATOR_LANG = "creatorLanguage";
		private final SearchService searchService;
		private final DefinitionService definitionService;

		private Map<String, ClassInstance> classesCache;
		private List<ClassInstance> allClasses;

		private Map<String, ClassInstance> dataTypesCache;
		private List<ClassInstance> allDataTypes;

		private List<PropertyInstance> allProperties;
		private Map<String, PropertyInstance> allPropertiesMap;

		private Map<String, PropertyInstance> relationsCache;
		private List<PropertyInstance> allRelations;
		private List<PropertyInstance> allSearchableRelations;

		private Map<String, List<ClassInstance>> libraries;
		private List<ClassInstance> searchableTypes;

		private Supplier<Collection<String>> topLevelTypes = new CachingSupplier<>(this::calculateTopLevelTypes);

		private final Lock lock = new ReentrantLock();
		private volatile boolean isLoaded = false;
		private volatile boolean isReloading = false;
		private NamespaceRegistryService namespaceRegistryService;

		/**
		 * Instantiates a new semantic definition cache.
		 *
		 * @param searchService
		 *            the search service
		 * @param definitionService
		 *            the definition service
		 * @param namespaceRegistryService
		 *            the namespace registry service
		 */
		public SemanticDefinitionCache(SearchService searchService, DefinitionService definitionService,
				NamespaceRegistryService namespaceRegistryService) {
			this.searchService = searchService;
			this.definitionService = definitionService;
			this.namespaceRegistryService = namespaceRegistryService;
		}

		/**
		 * Reload.
		 */
		public void reload() {

			if (isLoaded && isReloading) {
				// no need to continue we are currently reloading any way
				return;
			}

			boolean loaded = isLoaded;

			lock.lock();
			if (loaded != isLoaded) {
				// the resources has been loading while we were waiting, no need to continue
				lock.unlock();
				return;
			}
			try {
				isReloading = true;
				// semantic classes does not have headers
				Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.enable();
				initializeCacheInternal();
			} finally {
				lock.unlock();
				Options.DISABLE_POST_INSTANCE_LOAD_DECORATION.disable();
				isReloading = false;
			}
			isLoaded = true;
		}

		private void initializeCacheInternal() {
			TimeTracker timeTracker = new TimeTracker().begin();

			Map<String, ClassInstance> tempClassesCache = getClasses();

			List<ClassInstance> tempSearchableTypes = new ArrayList<>(collectSearchable(tempClassesCache));

			Map<String, List<ClassInstance>> tempLibraries = getLibraries(tempClassesCache);

			mapSuperClasses(tempClassesCache);

			sortClasses(tempClassesCache);

			// fetch data types
			Map<String, ClassInstance> tempDataTypesCache = new HashMap<>(16);
			initializeDataTypesCache(tempDataTypesCache);
			dataTypesCache = Collections.unmodifiableMap(tempDataTypesCache);
			allDataTypes = Collections.unmodifiableList(new ArrayList<>(tempDataTypesCache.values()));
			allDataTypes.forEach(ClassInstance::preventModifications);

			// fetch properties
			Map<String, PropertyInstance> tempPropertiesCache = new HashMap<>(250);
			initializeDataPropertiesCache(tempClassesCache, tempPropertiesCache);
			initializeAnnotationPropertiesCache(tempClassesCache, tempPropertiesCache);
			allProperties = Collections.unmodifiableList(new ArrayList<>(tempPropertiesCache.values()));
			allProperties.forEach(PropertyInstance::preventModifications);
			allPropertiesMap = Collections.unmodifiableMap(tempPropertiesCache);

			// fetch relations
			Map<String, PropertyInstance> tempRelationsCache = new HashMap<>(250);
			initializeRelationPropertiesCache(tempClassesCache, tempRelationsCache);
			allSearchableRelations = Collections.unmodifiableList(
					tempRelationsCache.values().stream().filter(PropertyInstance::isSearchable).collect(
							Collectors.toList()));
			allRelations = Collections.unmodifiableList(new ArrayList<>(tempRelationsCache.values()));
			allRelations.forEach(PropertyInstance::preventModifications);
			relationsCache = Collections.unmodifiableMap(tempRelationsCache);

			// seal the instances and forbid modifications so we can return the lists itself instead
			// of creating new one all the time

			allClasses = Collections.unmodifiableList(new ArrayList<>(tempClassesCache.values()));
			allClasses.forEach(ClassInstance::preventModifications);

			classesCache = Collections.unmodifiableMap(tempClassesCache);
			classesCache.values().forEach(ClassInstance::preventModifications);

			searchableTypes = Collections.unmodifiableList(tempSearchableTypes);
			searchableTypes.forEach(ClassInstance::preventModifications);

			libraries = Collections.unmodifiableMap(tempLibraries);
			libraries.values().stream().flatMap(List::stream).forEach(ClassInstance::preventModifications);

			// this will remove the cached value from the supplier
			Resettable.reset(topLevelTypes);

			LOGGER.debug("SemanticDefinitionService load took {} ms!", timeTracker.stop());
		}

		private static Set<ClassInstance> collectSearchable(Map<String, ClassInstance> tempClassesCache) {
			return tempClassesCache.values().stream().filter(clazz -> clazz.type().isSearchable()).collect(
					Collectors.toSet());
		}

		private Collection<String> calculateTopLevelTypes() {
			return getAllClasses()
					.stream()
						.filter(instance -> instance.type().isCreatable() || instance.type().isUploadable()
								|| instance.type().isSearchable() && instance.getSubClasses().isEmpty())
						.map(instance -> instance.getId().toString())
						.collect(Collectors.toSet());
		}

		/*
		 * used for relation properties to merge skos:definition metainfo
		 */
		private static BiConsumer<Instance, Instance> mergePropertyDescriptions() {
			return (propertyInstance, source) -> mergeLangProperty(propertyInstance, source, PROPERTY_DESCRIPTION,
					PROPERTY_DESCRIPTION_LANG, PROPERTY_DESCRIPTION);
		}

		/*
		 * used for classes description field
		 */
		private static BiConsumer<Instance, Instance> mergeClassDescriptions() {
			return (propertyInstance, source) -> mergeLangProperty(propertyInstance, source, CLASS_DESCRIPTION,
					CLASS_DESCRIPTION_LANG, CLASS_DESCRIPTION);
		}

		private static BiConsumer<Instance, Instance> mergeTitles() {
			return (instance, source) -> {
				if (instance instanceof PropertyInstance) {
					((PropertyInstance) instance).setLabel(source.getAsString(TITLE_LANGUAGE), source.getAsString(TITLE));
				} else if (instance instanceof ClassInstance) {
					((ClassInstance) instance).setLabel(source.getAsString(TITLE_LANGUAGE), source.getAsString(TITLE));
				}
				mergeLangProperty(instance, source, TITLE, TITLE_LANGUAGE, TITLE);
			};
		}

		private static BiConsumer<Instance, Instance> mergeCreators() {
			return (propertyInstance, source) -> mergeLangProperty(propertyInstance, source, CREATOR, CREATOR_LANG,
					CREATOR);
		}

		@SuppressWarnings("unchecked")
		private static void mergeLangProperty(Instance propertyInstance, Instance source, String propertyName, String langKey, String valueKey) {
			if (!(source.getProperties().containsKey(langKey) && source.getProperties().containsKey(valueKey))) {
				return;
			}
			propertyInstance.getProperties().computeIfPresent(propertyName, (key, current) -> {
				Map<String, String> value = Collections.emptyMap();
				if (current instanceof Map) {
					value = (Map<String, String>) current;
				} else if (current instanceof String) {
					value = new HashMap<>();
					CollectionUtils.addNonNullValue(value, Objects.toString(propertyInstance.remove(langKey), null), StringUtils.trimToNull((String) current));
				}
				value.put(source.getString(langKey), StringUtils.trimToNull(source.getString(valueKey)));
				return (Serializable) value;
			});
			propertyInstance.getProperties().computeIfAbsent(propertyName, key -> {
				Map<String, String> value = new HashMap<>();
				value.put(source.getString(langKey), source.getString(valueKey));
				return (Serializable) value;
			});
		}
		private static BiConsumer<Instance, Instance> mergePropertyTypes() {
			return (propertyInstance, source) -> {
				String currentValue = propertyInstance.getString(PROPERTY_TYPE);
				String newValue = source.getString(PROPERTY_TYPE);
				propertyInstance.addIfNotNull(PROPERTY_TYPE, StringUtils.trimToNull(currentValue));
				propertyInstance.addIfNotNull(PROPERTY_TYPE, StringUtils.trimToNull(newValue));
			};
		}

		private void initializeRelationPropertiesCache(Map<String, ClassInstance> tempClassesCache,
				Map<String, PropertyInstance> tempRelationsCache) {

			List<CommonInstance> result = performSemanticSearch(SemanticQueries.QUERY_RELATION_PROPERTIES);

			for (Instance instance : result) {
				Map<String, Serializable> properties = instance.getProperties();

				String id = instance.getId().toString();
				if (tempRelationsCache.containsKey(id)) {
					PropertyInstance relationInstance = tempRelationsCache.get(id);
					mergeTitles()
							.andThen(mergePropertyDescriptions())
							.andThen(mergeCreators())
							.andThen(mergePropertyTypes())
							.accept(relationInstance, instance);
				} else {
					PropertyInstance relationInstance = new PropertyInstance();
					relationInstance.setId(id);
					relationInstance.addAllProperties(properties);
					mergeTitles().accept(relationInstance, instance);

					Serializable domainClass = properties.get(DOMAIN_CLASS);
					if (domainClass != null) {
						relationInstance.setDomainClass(domainClass.toString());
					}
					Serializable rangeClass = properties.get(RANGE_CLASS);
					if (rangeClass != null) {
						relationInstance.setRangeClass(rangeClass.toString());
					}

					ClassInstance domainClassInstance = tempClassesCache.get(domainClass);
					if (domainClassInstance != null) {
						domainClassInstance.getRelations().put(id, relationInstance);
					}

					tempRelationsCache.put(id, relationInstance);
				}
			}
		}

		private void initializeDataTypesCache(Map<String, ClassInstance> tempDataTypesCache) {
			populateDataTypesCache(tempDataTypesCache, SemanticQueries.QUERY_DATA_TYPES);
		}

		private void initializeDataPropertiesCache(Map<String, ClassInstance> tempClassesCache,
				Map<String, PropertyInstance> tempPropertiesCache) {
			populatePropertiesCache(tempClassesCache, tempPropertiesCache, SemanticQueries.QUERY_DATA_PROPERTIES);
		}

		private void initializeAnnotationPropertiesCache(Map<String, ClassInstance> tempClassesCache,
				Map<String, PropertyInstance> tempPropertiesCache) {
			populatePropertiesCache(tempClassesCache, tempPropertiesCache, SemanticQueries.QUERY_ANNOTATION_PROPERTIES);
		}

		private void populatePropertiesCache(Map<String, ClassInstance> tempClassesCache,
				Map<String, PropertyInstance> tempPropertiesCache, SemanticQueries query) {
			List<CommonInstance> result = performSemanticSearch(query);

			for (Instance instance : result) {
				String instanceId = instance.getId().toString();
				tempPropertiesCache.computeIfPresent(instanceId,
						(key, existingProperty) -> updateExistingProperty(instance, existingProperty));
				tempPropertiesCache.computeIfAbsent(instanceId, key -> createPropertyFrom(tempClassesCache, instance));
			}
			tempPropertiesCache.values().forEach(this::removeSingleValueCollectionProperties);
		}

		private void populateDataTypesCache(Map<String, ClassInstance> tempDataTypesCache, SemanticQueries query) {
			List<CommonInstance> result = performSemanticSearch(query);

			for (Instance instance : result) {
				String instanceId = instance.getId().toString();
				tempDataTypesCache.computeIfAbsent(instanceId, key -> {
					ClassInstance classInstance = new ClassInstance();
					classInstance.setId(toFullUri(instance.getId()));
					classInstance.addAllProperties(instance.getProperties());
					return classInstance;
				});
				tempDataTypesCache.computeIfPresent(instanceId,
						(key, existingClass) -> updateExistingClass(instance, existingClass));
			}
		}

		@SuppressWarnings("unchecked")
		private void removeSingleValueCollectionProperties(PropertyInstance propertyInstance) {
			propertyInstance.getProperties().replaceAll((k, v) -> {
				if (v instanceof Collection && ((Collection) v).size() == 1) {
					return ((Collection<Serializable>) v).iterator().next();
				}
				return v;
			});

		}

		private List<CommonInstance> performSemanticSearch(SemanticQueries query) {
			SearchArguments<CommonInstance> filter = searchService.getFilter(query.getName(), CommonInstance.class,
					null);
			filter.getQueryConfigurations().put(SPARQLQueryHelper.INCLUDE_INFERRED_CONFIGURATION, Boolean.TRUE);
			filter.setDialect(SearchDialects.SPARQL);
			filter.setPageSize(0);
			filter.setMaxSize(0);
			filter.setPermissionsType(QueryResultPermissionFilter.NONE);
			searchService.search(CommonInstance.class, filter);
			return filter.getResult();
		}

		private static PropertyInstance updateExistingProperty(Instance source, PropertyInstance existingProperty) {
			mergeTitles()
					.andThen(mergePropertyDescriptions())
					.andThen(mergeCreators())
					.andThen(mergePropertyTypes())
					.accept(existingProperty, source);
			return existingProperty;
		}

		private ClassInstance updateExistingClass(Instance source, ClassInstance existingClass) {
			mergeTitles()
					.andThen(mergeClassDescriptions())
					.andThen(mergeCreators())
					.accept(existingClass, source);
			return existingClass;
		}

		private PropertyInstance createPropertyFrom(Map<String, ClassInstance> tempClassesCache, Instance instance) {
			Map<String, Serializable> properties = instance.getProperties();

			PropertyInstance propertyInstance = new PropertyInstance();
			propertyInstance.setId(instance.getId());
			propertyInstance.addAllProperties(properties);

			String domainClass = instance.getAsString(DOMAIN_CLASS);
			propertyInstance.setDomainClass(domainClass);

			Serializable rangeClass = properties.get(RANGE_CLASS);
			if (rangeClass != null) {
				propertyInstance.setRangeClass(rangeClass.toString());
			}

			if (domainClass != null) {
				domainClass = namespaceRegistryService.buildFullUri(domainClass);
			}

			ClassInstance domainClassInstance = tempClassesCache.get(domainClass);
			if (domainClassInstance != null) {
				domainClassInstance.getFields().put(propertyInstance.getId().toString(), propertyInstance);
			}

			mergeTitles().accept(propertyInstance, instance);

			return propertyInstance;
		}

		@SuppressWarnings("unchecked")
		private static void mapSuperClasses(Map<String, ClassInstance> tempClassesCache) {
			for (Entry<String, ClassInstance> key : tempClassesCache.entrySet()) {
				ClassInstance classInstance = key.getValue();
				Set<Serializable> superClasses = classInstance.getAs(SUPER_CLASSES, Set.class::cast);
				if (superClasses.isEmpty()) {
					continue;
				}
				for (Serializable superClassId : superClasses) {
					ClassInstance superClass = tempClassesCache.get(superClassId);
					if (superClass != null) {
						classInstance.getSuperClasses().add(superClass);
						superClass.getSubClasses().put(classInstance.getId().toString(), classInstance);
					}
				}
			}
		}

		private Map<String, ClassInstance> getClasses() {
			SearchArguments<ClassInstance> classFilter = searchService
					.getFilter(SemanticQueries.QUERY_CLASSES_TYPES_FOR_SEARCH.getName(), ClassInstance.class, null);
			classFilter.setDialect(SearchDialects.SPARQL);
			classFilter.getQueryConfigurations().put(SPARQLQueryHelper.INCLUDE_INFERRED_CONFIGURATION, Boolean.FALSE);
			classFilter.setPageSize(0);
			classFilter.setMaxSize(0);
			classFilter.setPermissionsType(QueryResultPermissionFilter.NONE);
			searchService.search(ClassInstance.class, classFilter);

			List<ClassInstance> classResult = classFilter.getResult();

			Map<String, ClassInstance> tempClassesCache = createLinkedHashMap(classResult.size());

			// convert all ids to full uries
			classResult.forEach(instance -> instance.setId(toFullUri(instance.getId())));

			// iterate over classes
			for (ClassInstance instance : classResult) {
				String id = instance.getId().toString();

				// fill super class information for existing entries
				tempClassesCache.computeIfPresent(id, (key, classInstance) -> updateExisting(instance, classInstance));

				// add new class information for non existing entries
				tempClassesCache.computeIfAbsent(id, key -> defineNewClassFrom(instance));
			}

			loadDefinitionsForClasses(tempClassesCache);

			return tempClassesCache;
		}

		private ClassInstance defineNewClassFrom(ClassInstance instance) {
			ClassInstance classInstance = new ClassInstance();
			classInstance.setId(instance.getId());
			classInstance.addAllProperties(instance.getProperties());

			mergeTitles().accept(classInstance, instance);

			Set<Serializable> superClasses = new HashSet<>();
			classInstance.add(SUPER_CLASSES, (Serializable) superClasses);

			// convert the super class id to full uri and if present and if does not matches the current instance add it
			// to the super classes list
			if (instance.transform(SUPER_CLASS, this::toFullUri)
					&& !classInstance.getId().equals(instance.get(SUPER_CLASS))) {
				superClasses.add(instance.get(SUPER_CLASS));
			}
			return classInstance;
		}

		@SuppressWarnings("unchecked")
		private ClassInstance updateExisting(ClassInstance copyFrom, ClassInstance classInstance) {
			updateExistingClass(copyFrom, classInstance);

			Set<Serializable> superClasses = (Set<Serializable>) classInstance.get(SUPER_CLASSES);
			if (copyFrom.transform(SUPER_CLASS, this::toFullUri)) {
				String superClass = copyFrom.getString(SUPER_CLASS);
				if (!superClasses.contains(superClass) && !classInstance.getId().equals(superClass)) {
					superClasses.add(superClass);
				}
			}
			return classInstance;
		}

		@SuppressWarnings("unchecked")
		private Serializable toFullUri(Serializable uri) {
			if (uri instanceof Collection<?>) {
				Collection<Serializable> collection = (Collection<Serializable>) uri;
				Set<String> converted = createHashSet(collection.size());
				for (Serializable object : collection) {
					converted.add(namespaceRegistryService.buildFullUri(object.toString()));
				}
				return (Serializable) converted;
			}
			return namespaceRegistryService.buildFullUri(uri.toString());
		}

		@SuppressWarnings("unchecked")
		private void loadDefinitionsForClasses(Map<String, ClassInstance> tempClassesCache) {
			SearchArguments<ClassInstance> classFilter = searchService
					.getFilter(SemanticQueries.QUERY_DEFINITIONS_FOR_CLASS.getName(), ClassInstance.class, null);
			classFilter.setDialect(SearchDialects.SPARQL);
			classFilter.getQueryConfigurations().put(SPARQLQueryHelper.INCLUDE_INFERRED_CONFIGURATION, Boolean.FALSE);
			classFilter.setPageSize(0);
			classFilter.setMaxSize(0);
			classFilter.setPermissionsType(QueryResultPermissionFilter.NONE);
			searchService.search(ClassInstance.class, classFilter);

			for (ClassInstance instance : classFilter.getResult()) {
				ClassInstance classInstance = tempClassesCache.get(toFullUri(instance.getId()));
				if (classInstance == null) {
					LOGGER.warn("Could not locate class instance with ID: '{}' in the cache!", instance.getId());
					continue;
				}
				if (!classInstance.isPropertyPresent(DEFINITIONS)) {
					classInstance.add(DEFINITIONS, new HashSet<Serializable>());
				}
				CollectionUtils.addNonNullValue(classInstance.get(DEFINITIONS, Set.class), instance.get(DEFINITION_ID));
			}
		}

		private Map<String, List<ClassInstance>> getLibraries(Map<String, ClassInstance> all) {
			List<ClassInstance> tempObjectLibrary = all
					.values()
						.stream()
						.filter(classInstance -> classInstance.type().isPartOflibrary())
						.peek(this::setDefinitionId)
						.peek(instance -> {
							instance.setLibrary(LibraryProvider.OBJECT_LIBRARY);
							// status will be used for permission evaluation
							instance.add(DefaultProperties.STATUS, PrimaryStates.APPROVED_KEY);
						})
						.collect(Collectors.toCollection(LinkedList::new));

			Map<String, List<ClassInstance>> tempLibraries = createLinkedHashMap(1);
			tempLibraries.put(LibraryProvider.OBJECT_LIBRARY, Collections.unmodifiableList(tempObjectLibrary));
			return tempLibraries;
		}

		private void setDefinitionId(ClassInstance classInstance) {
			DefinitionModel definition = definitionService.getInstanceDefinition(classInstance);
			if (definition != null) {
				classInstance.setIdentifier(definition.getIdentifier());
			}
		}

		private static void sortClasses(Map<String, ClassInstance> classes) {
			Set<ClassInstance> sorted = CollectionUtils.createLinkedHashSet(classes.size());
			
			for (ClassInstance classInstance : classes.values()) {
				if (sorted.contains(classInstance)) {
					continue;
				}
				iterateTree(classInstance, sorted);
			}
			classes.clear();
			for (ClassInstance classInstance : sorted) {
				classes.put((String) classInstance.getId(), classInstance);
			}
		}

		private static void iterateTree(ClassInstance classInstance, Set<ClassInstance> sorted) {
			List<ClassInstance> superClasses = classInstance.getSuperClasses();
			if (superClasses.isEmpty()) {
				// if no super classes, just iterate the current class
				iterateInstance(classInstance, sorted);
				return;
			}
			for (ClassInstance superClass : superClasses) {
				iterateTree(superClass, sorted);
				iterateInstance(superClass, sorted);
			}

		}

		private static void iterateInstance(ClassInstance instance, Set<ClassInstance> sorted) {
			sorted.add(instance);
			sorted.addAll(instance.getSubClasses().values());

			for (ClassInstance classInstance : instance.getSubClasses().values()) {
				iterateInstance(classInstance, sorted);
			}
		}

		/**
		 * Gets the all classes.
		 *
		 * @return the all classes
		 */
		List<ClassInstance> getAllClasses() {
			if (!isLoaded) {
				reload();
			}
			return allClasses;
		}

		/**
		 * Gets the classes cache.
		 *
		 * @return the classesCache
		 */
		Map<String, ClassInstance> getClassesCache() {
			if (!isLoaded) {
				reload();
			}
			return classesCache;
		}

		/**
		 * Gets the classes cache.
		 *
		 * @return the classesCache
		 */
		Map<String, ClassInstance> getDataTypesCache() {
			if (!isLoaded) {
				reload();
			}
			return dataTypesCache;
		}

		/**
		 * Gets the all data types.
		 *
		 * @return the all data types
		 */
		List<ClassInstance> getAllDataTypes() {
			if (!isLoaded) {
				reload();
			}
			return allDataTypes;
		}

		/**
		 * Gets the all properties.
		 *
		 * @return the allProperties
		 */
		List<PropertyInstance> getAllProperties() {
			if (!isLoaded) {
				reload();
			}
			return allProperties;
		}

		/**
		 * Gets the all properties map.
		 *
		 * @return the allPropertiesMap
		 */
		Map<String, PropertyInstance> getAllPropertiesMap() {
			if (!isLoaded) {
				reload();
			}
			return allPropertiesMap;
		}

		/**
		 * Gets the relations cache.
		 *
		 * @return the relationsCache
		 */
		Map<String, PropertyInstance> getRelationsCache() {
			if (!isLoaded) {
				reload();
			}
			return relationsCache;
		}

		/**
		 * Gets the all searchable relations. These are relations that user could see for searching
		 *
		 * @return the all searchable relations
		 */
		List<PropertyInstance> getAllSearchableRelations() {
			if (!isLoaded) {
				reload();
			}
			return allSearchableRelations;
		}

		/**
		 * Gets the all relations.
		 *
		 * @return the allRelations
		 */
		List<PropertyInstance> getAllRelations() {
			if (!isLoaded) {
				reload();
			}
			return allRelations;
		}

		/**
		 * Gets the searchable types.
		 *
		 * @return the searchableTypes
		 */
		List<ClassInstance> getSearchableTypes() {
			if (!isLoaded) {
				reload();
			}
			return searchableTypes;
		}

		Supplier<Collection<String>> getTopLevelTypes() {
			if (!isLoaded) {
				reload();
			}
			return topLevelTypes;
		}

		Map<String, List<ClassInstance>> getLibraries() {
			if (!isLoaded) {
				reload();
			}
			return libraries;
		}

	}
}
