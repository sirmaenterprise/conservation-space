package com.sirma.itt.emf.semantic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.ClassInstance;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.PropertyInstance;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Implementation of SemanticDefinitionsService interface
 *
 * @author kirq4e
 */
@ApplicationScoped
public class SemanticDefinitionServiceImpl implements SemanticDefinitionService {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticDefinitionService.class);

	@Inject
	private SearchService searchService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	private Map<String, ClassInstance> classesCache;
	private List<ClassInstance> allClasses;

	private Map<String, PropertyInstance> propertiesCache;
	private List<PropertyInstance> allProperties;

	private Map<String, PropertyInstance> relationsCache;
	private List<PropertyInstance> allRelations;

	private List<ClassInstance> objectLibrary;
	private List<ClassInstance> searchableTypes;


	/**
	 * Initializes
	 */
	@PostConstruct
	public void init() {
		TimeTracker timeTracker = new TimeTracker().begin();
		classesCache = new LinkedHashMap<>(250);
		propertiesCache = new HashMap<>(250);
		relationsCache = new HashMap<>(250);
		objectLibrary = new ArrayList<>(25);
		searchableTypes = new ArrayList<>(25);

		// fetch classes
		SearchArguments<CommonInstance> filter = searchService.getFilter(
				SemanticQueries.QUERY_CLASSES_TYPES_FOR_SEARCH.getName(), CommonInstance.class,
				null);
		filter.getArguments().put("includeInferred", Boolean.FALSE);
		filter.setPageSize(0);
		searchService.search(CommonInstance.class, filter);

		List<CommonInstance> result = filter.getResult();
		// iterate over classes
		for (CommonInstance instance : result) {
			Map<String, Serializable> properties = instance.getProperties();
			if (classesCache.containsKey(instance.getId().toString())) {
				ClassInstance classInstance = classesCache.get(instance.getId().toString());
				classInstance.setLabel(properties.get("titleLanguage").toString(),
						properties.get("title").toString());

				HashSet<Serializable> superClasses = (HashSet<Serializable>) classInstance
						.getProperties().get("superClasses");
				Serializable superClass = properties.get("superClass");
				if ((superClass != null) && !superClasses.contains(superClass)) {
					superClasses.add(superClass);
				}

			} else {
				ClassInstance classInstance = new ClassInstance();
				classInstance.setId(instance.getId());
				classInstance.getProperties().putAll(Collections.unmodifiableMap(properties));

				classInstance.setLabel(properties.get("titleLanguage").toString(),
						properties.get("title").toString());

				Serializable partOfObjecLibrary = properties.get("partOfObjectLibrary");

				if (Boolean.TRUE.equals(partOfObjecLibrary)) {
					objectLibrary.add(classInstance);
				}

				if (Boolean.TRUE.equals(classInstance.getProperties().get("searchable"))) {
					searchableTypes.add(classInstance);
				}

				HashSet<Serializable> superClasses = new HashSet<>();
				classInstance.getProperties().put("superClasses", superClasses);

				Serializable superClass = properties.get("superClass");
				if (superClass != null) {
					superClasses.add(superClass);
				}

				classesCache.put(classInstance.getId().toString(), classInstance);
			}
		}

		for (String key : classesCache.keySet()) {
			ClassInstance classInstance = classesCache.get(key);
			@SuppressWarnings("unchecked")
			HashSet<Serializable> superClasses = (HashSet<Serializable>) classInstance
					.getProperties().get("superClasses");
			if (!superClasses.isEmpty()) {
				for (Serializable superClassId : superClasses) {
					ClassInstance superClass = classesCache.get(superClassId);
					if (superClass != null) {
						classInstance.setOwningInstance(superClass);
						superClass.getSubClasses().put(classInstance.getId().toString(),
								classInstance);
					}
				}
			}
		}

		sortClasses(classesCache);

		// fetch properties
		filter = searchService.getFilter(SemanticQueries.QUERY_DATA_PROPERTIES.getName(),
				CommonInstance.class, null);
		filter.getArguments().put("includeInferred", Boolean.FALSE);
		filter.setPageSize(0);
		searchService.search(CommonInstance.class, filter);
		result = filter.getResult();

		for (Instance instance : result) {
			Map<String, Serializable> properties = instance.getProperties();

			PropertyInstance propertyInstance = new PropertyInstance();
			propertyInstance.setId(instance.getId());
			propertyInstance.setProperties(Collections.unmodifiableMap(properties));
			String domainClass = properties.get("domainClass").toString();
			propertyInstance.setDomainClass(domainClass);
			Serializable rangeClass = properties.get("rangeClass");
			if (rangeClass != null) {
				propertyInstance.setRangeClass(rangeClass.toString());
			}

			ClassInstance domainClassInstance = classesCache.get(domainClass);
			if (domainClassInstance != null) {
				domainClassInstance.getFields().put(propertyInstance.getId().toString(),
						propertyInstance);
			}

			propertiesCache.put(propertyInstance.getId().toString(), propertyInstance);
		}

		// fetch relations
		filter = searchService.getFilter(SemanticQueries.QUERY_RELATION_PROPERTIES.getName(),
				CommonInstance.class, null);
		filter.getArguments().put("includeInferred", Boolean.TRUE);
		filter.setPageSize(0);
		searchService.search(CommonInstance.class, filter);
		result = filter.getResult();

		for (Instance instance : result) {
			Map<String, Serializable> properties = instance.getProperties();

			PropertyInstance relationInstance = new PropertyInstance();
			relationInstance.setId(instance.getId());
			relationInstance.setProperties(Collections.unmodifiableMap(properties));

			Serializable domainClass = properties.get("domainClass");
			if (domainClass != null) {
				relationInstance.setDomainClass(domainClass.toString());
			}
			Serializable rangeClass = properties.get("rangeClass");
			if (rangeClass != null) {
				relationInstance.setRangeClass(rangeClass.toString());
			}

			ClassInstance domainClassInstance = classesCache.get(domainClass);
			if (domainClassInstance != null) {
				domainClassInstance.getRelations().put(relationInstance.getId().toString(),
						relationInstance);
			}

			relationsCache.put(relationInstance.getId().toString(), relationInstance);
		}

		// seal the instances and forbid modifications so we can return the lists itself instead of
		// creating new one all the time
		objectLibrary = Collections.unmodifiableList(objectLibrary);
		searchableTypes = Collections.unmodifiableList(searchableTypes);

		allClasses = Collections.unmodifiableList(new ArrayList<ClassInstance>(classesCache
				.values()));
		classesCache = Collections.unmodifiableMap(classesCache);

		allProperties = Collections.unmodifiableList(new ArrayList<PropertyInstance>(
				propertiesCache.values()));
		propertiesCache = Collections.unmodifiableMap(propertiesCache);

		allRelations = Collections.unmodifiableList(new ArrayList<PropertyInstance>(relationsCache
				.values()));
		relationsCache = Collections.unmodifiableMap(relationsCache);

		LOGGER.debug("SemanticDefinitionService load took {} s!", timeTracker.stopInSeconds());
	}

	/**
	 * Sort classes.
	 *
	 * @param classes
	 *            the classes
	 */
	private void sortClasses(Map<String, ClassInstance> classes) {
		Set<ClassInstance> sorted = CollectionUtils.createLinkedHashSet(classes.size());

		for (ClassInstance classInstance : classes.values()) {
			if (sorted.contains(classInstance)) {
				continue;
			}

			ClassInstance instance = (ClassInstance) InstanceUtil.getRootInstance(classInstance,
					true);

			if (instance == null) {
				instance = classInstance;
			}
			iterateInstance(instance, sorted);
		}

		classes.clear();
		for (ClassInstance classInstance : sorted) {
			classes.put((String) classInstance.getId(), classInstance);
		}
	}

	/**
	 * Iterate instance.
	 *
	 * @param instance
	 *            the instance
	 * @param sorted
	 *            the sorted
	 */
	private void iterateInstance(ClassInstance instance, Set<ClassInstance> sorted) {
		sorted.add(instance);
		for (ClassInstance classInstance : instance.getSubClasses().values()) {
			sorted.add(classInstance);
		}
		for (ClassInstance classInstance : instance.getSubClasses().values()) {
			iterateInstance(classInstance, sorted);
		}
	}

	@Override
	public List<ClassInstance> getClasses() {
		return allClasses;
	}

	@Override
	public List<String> getHierarchy(String classType) {
		List<String> result = new LinkedList<>();

		ClassInstance classInstance = classesCache.get(namespaceRegistryService.getShortUri(classType));
		if (classInstance == null) {
			LOGGER.warn("No class information found for type {}", classType);
			return result;
		}
		result.add(classInstance.getId().toString());
		classInstance = (ClassInstance) classInstance.getOwningInstance();

		while (classInstance != null) {
			result.add(0, classInstance.getId().toString());
			classInstance = (ClassInstance) classInstance.getOwningInstance();
		}

		return result;
	}

	@Override
	public List<PropertyInstance> getProperties() {
		return allProperties;
	}

	@Override
	public List<PropertyInstance> getProperties(String classType) {
		ClassInstance classInstance = classesCache.get(classType);
		List<PropertyInstance> result = new ArrayList<>();
		while (classInstance != null) {
			result.addAll(classInstance.getFields().values());
			classInstance = (ClassInstance) classInstance.getOwningInstance();
		}
		return result;
	}

	@Override
	public List<PropertyInstance> getRelations() {
		return allRelations;
	}

	@Override
	public PropertyInstance getRelation(String relationUri) {
		// TODO : Remove conversion to short URI and define short URI in
		// types.xml
		// and avoid using namespaceRegistryService.getShortUri(fromClass) on
		// the following line
		return relationsCache.get(namespaceRegistryService.getShortUri(relationUri));
	}

	@Override
	public List<PropertyInstance> getRelations(String fromClass, String toClass) {
		// linked list because we have some removals later and unknown size
		List<PropertyInstance> result = new LinkedList<>();
		if (StringUtils.isNotNullOrEmpty(fromClass)) {
			// TODO : Remove conversion to short URI and define short URI in
			// types.xml
			// and avoid using namespaceRegistryService.getShortUri(fromClass)
			// on the following line
			ClassInstance classInstance = classesCache.get(namespaceRegistryService
					.getShortUri(fromClass));
			while (classInstance != null) {
				result.addAll(classInstance.getRelations().values());
				classInstance = (ClassInstance) classInstance.getOwningInstance();
			}
		} else {
			result.addAll(allRelations);
		}

		if (StringUtils.isNotNullOrEmpty(toClass)) {
			ClassInstance classInstance = classesCache.get(namespaceRegistryService
					.getShortUri(toClass));

			Set<String> classHierarchy = new HashSet<>();
			while (classInstance != null) {
				classHierarchy.add(classInstance.getId().toString());
				classInstance = (ClassInstance) classInstance.getOwningInstance();
			}

			Iterator<PropertyInstance> iterator = result.iterator();
			while (iterator.hasNext()) {
				PropertyInstance propertyInstance = iterator.next();
				if (!classHierarchy.contains(propertyInstance.getRangeClass())) {
					iterator.remove();
				}
			}
		}
		return result;
	}

	@Override
	public Map<String, PropertyInstance> getRelationsMap() {
		return relationsCache;
	}

	@Override
	public List<ClassInstance> getSearchableClasses() {
		return searchableTypes;
	}

	@Override
	public ClassInstance getClassInstance(String identifier) {
		return classesCache.get(identifier);
	}

	@Override
	public List<PropertyInstance> getOwnProperties(String classType) {
		ClassInstance classInstance = classesCache.get(classType);
		return new ArrayList<>(classInstance.getFields().values());
	}

	@Override
	public List<ClassInstance> getObjectLibrary() {
		return objectLibrary;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void observeReloadDefinitionEvent(@Observes LoadSemanticDefinitions event) {

		SemanticOperationLogger.saveLog();
		init();
	}

}
