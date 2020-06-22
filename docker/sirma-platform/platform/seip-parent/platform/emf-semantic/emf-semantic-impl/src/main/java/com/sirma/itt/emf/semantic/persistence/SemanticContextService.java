package com.sirma.itt.emf.semantic.persistence;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheTransactionMode;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.Transaction;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Metric.Builder;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.ReadOnly;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * {@link InstanceContextService} implementation that queries the semantic database to fetch the parent context for a
 * given instance. The loading is done via single database query call.
 *
 * @author BBonev
 * @author bbanchev
 */
@ApplicationScoped
public class SemanticContextService implements InstanceContextService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String ROOT_REFERENCE_ID = "rootReference";
	private static final Collection<String> INSTANCE_TO_CONTEXT_PROPERTIES = Arrays.asList(HAS_PARENT, PART_OF_URI);

	@CacheConfiguration(eviction = @Eviction(strategy = "LRU"), expiration = @Expiration(maxIdle = 600000, interval = 60000, lifespan = 600000), transaction = @Transaction(mode = CacheTransactionMode.NON_XA), doc = @Documentation(""
			+ "Contains retrieved contexts keyed by instance id. Size of cache should be about the count of instances in the tenant."))
	private static final String INSTANCE_CONTEXT_STORE_CACHE = "INSTANCE_CONTEXT_STORE_CACHE";

	private static final Metric SEMANTIC_RESOLVE_HIERARCHY_DURATION_SEC = Builder
			.timer("semantic_resolve_hierarchy_duration_seconds",
					"Semantic hierarchy resolution duration in seconds.")
			.build();

	@Inject
	@ReadOnly
	private RepositoryConnection repositoryConnection;
	@Inject
	private NamespaceRegistryService namespaceRegistryService;
	@Inject
	private InstanceTypeResolver typeResolver;
	@Inject
	private Statistics statistics;
	@Inject
	private EntityLookupCacheContext cacheContext;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private InstancePropertyNameResolver fieldConverter;

	/**
	 * Completes initialization by creating the local cache.
	 */
	@PostConstruct
	public void initialize() {
		cacheContext.createCacheIfAbsent(INSTANCE_CONTEXT_STORE_CACHE, true, new ContextCacheLookUp());
	}

	/**
	 * Retrieves the context using semantic query for domain object or semantic class model for semantic class
	 * objects.<br>
	 *
	 * @param key
	 *            is the current cache key
	 * @return the retrieved context or null if context could be resolved
	 */
	Pair<ContextCacheKey, String> restoreHierarchy(ContextCacheKey key) {
		if (key == null || !key.isValid()) {
			return Pair.nullPair();
		}
		String instanceId = key.getId();
		LOGGER.trace("Lookup context for {} from db!", instanceId);
		// we don't need to restore hierarchy for version instances at the moment(requirements). Also we'll need custom
		// logic for that, because version instances are not stored in the semantic
		if (InstanceVersionService.isVersion(instanceId)) {
			return new Pair<>(key, ROOT_REFERENCE_ID);
		}
		ClassInstance semanticType = checkAndGetSemanticType(key);
		if (semanticType != null) {
			return processSemanticContext(key, semanticType);
		}
		Collection<String> parentIds = resolveParentIds(instanceId);
		return new Pair<>(key, linkParents(instanceId, parentIds));
	}

	private Pair<ContextCacheKey, String> processSemanticContext(ContextCacheKey contextCacheKey,
			ClassInstance semanticType) {
		String superClass = ROOT_REFERENCE_ID;
		List<String> superClasses = semanticType
				.getSuperClasses()
					.stream()
					.map(Instance::getId)
					.map(Object::toString)
					.collect(Collectors.toList());
		if (superClasses.size() == 1) {
			superClass = superClasses.get(0);
		} else if (!superClasses.isEmpty()) {
			superClass = semanticDefinitionService.getMostConcreteClass(superClasses);
		}
		return new Pair<>(contextCacheKey, superClass);
	}

	private String linkParents(String id, Collection<String> parents) {
		String currentRefId = id;
		String directContext = null;
		for (String parentId : parents) {
			if (directContext == null) {
				directContext = parentId;
			} else {
				// put in cache all already retrieved ids to optimize future calls
				setContextInternal(createContextCacheKey(currentRefId), parentId);
			}
			currentRefId = parentId;
		}
		// mark the last context as root
		setContextInternal(createContextCacheKey(currentRefId), ROOT_REFERENCE_ID);
		return directContext;
	}

	private List<String> resolveParentIds(String startInstanceId) {
		Map<Value, Set<Value>> hierarchyMapping = getHierarchy(startInstanceId);
		List<Value> hierarchyIds = restoreHierarchy(hierarchyMapping);
		return loadParents(hierarchyIds);
	}

	@SuppressWarnings("boxing")
	private Map<Value, Set<Value>> getHierarchy(String startInstanceId) {
		try {
			statistics.track(SEMANTIC_RESOLVE_HIERARCHY_DURATION_SEC);

			IRI initial = namespaceRegistryService.buildUri(startInstanceId);
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection,
					SemanticQueries.QUERY_INSTANCE_HIERARCHY.getQuery(), Collections.singletonMap("initial", initial),
					true);
			return executeHierarchyQuery(tupleQuery);
		} finally {
			statistics.end(SEMANTIC_RESOLVE_HIERARCHY_DURATION_SEC);
		}
	}

	private static Map<Value, Set<Value>> executeHierarchyQuery(TupleQuery tupleQuery) {
		final Map<Value, Set<Value>> hierarchyMapping = new HashMap<>();
		try (TupleQueryResultIterator iterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			for (BindingSet bindingSet : iterator) {
				Value parentValue = bindingSet.getBinding("parent").getValue();
				Binding binding = bindingSet.getBinding("parentOfParent");
				Value parentOfParentValue = null;
				if (binding != null) {
					parentOfParentValue = binding.getValue();
				}
				Set<Value> parents = hierarchyMapping.computeIfAbsent(parentValue, k -> new LinkedHashSet<>(8));
				addNonNullValue(parents, parentOfParentValue);
			}
		}
		return hierarchyMapping;
	}

	private List<String> loadParents(List<Value> hierarchyIds) {
		return hierarchyIds
				.stream()
					.map(ValueConverter::convertValue)
					.filter(Objects::nonNull)
					.map(uri -> namespaceRegistryService.getShortUri(uri.toString()))
					.collect(Collectors.toList());
	}

	private static List<Value> restoreHierarchy(Map<Value, Set<Value>> hierarchyMapping) {
		if (hierarchyMapping.isEmpty()) {
			// we have top level instance without any parents
			return Collections.emptyList();
		}
		if (hierarchyMapping.size() == 1) {
			// we have only one element and the key is the single parent
			// this is instance with a parent that does not have any parents
			return Collections.singletonList(hierarchyMapping.keySet().iterator().next());
		}

		// For instance hierarchy instance1 -> instance2 -> instance3 -> instance4
		// The input data when searched for instance4 we have:
		// instance1 -> empty
		// instance2 -> instance1
		// instance3 -> instance1
		// instance3 -> instance2
		// that is transformed into:
		// instance1 -> []
		// instance2 -> [instance1]
		// instance3 -> [instance1, instance2]
		// we remove from hierarchyMapping entry that has empty value - this is the current root
		// and remove from all other sets the current root, the next root is the one with empty value
		// repeat
		// the result should be instance3 -> instance2 -> instance1

		List<Value> parentPath = new ArrayList<>(hierarchyMapping.size());
		for (Value root = getTopParent(hierarchyMapping); root != null; root = getNextParent(hierarchyMapping, root)) {
			addNonNullValue(parentPath, root);
		}
		Collections.reverse(parentPath);
		return parentPath;
	}

	private static Value getTopParent(Map<Value, Set<Value>> hierarchyMapping) {
		Value currentRoot = null;
		for (Entry<Value, Set<Value>> entry : hierarchyMapping.entrySet()) {
			if (isEmpty(entry.getValue())) {
				currentRoot = entry.getKey();
				break;
			}
		}
		if (currentRoot != null) {
			hierarchyMapping.remove(currentRoot);
		}
		return currentRoot;
	}

	private static Value getNextParent(Map<Value, Set<Value>> hierarchyMapping, Value toFind) {
		Value currentRoot = null;
		for (Entry<Value, Set<Value>> entry : hierarchyMapping.entrySet()) {
			Set<Value> set = entry.getValue();
			set.remove(toFind);
			if (isEmpty(set)) {
				currentRoot = entry.getKey();
				// does not stop here we need to iterate all elements
			}
		}
		if (currentRoot != null) {
			// we have an empty element that is the current root so remove it from the mapping
			hierarchyMapping.remove(currentRoot);
		}
		return currentRoot;
	}

	@Override
	public Optional<InstanceReference> getContext(Serializable instance) {
		String foundContext = getContextInternal(createContextCacheKey(instance));
		return loadReference(foundContext);
	}

	private String getContextInternal(ContextCacheKey cacheKey) {
		if (!cacheKey.isValid() || InstanceVersionService.isVersion(cacheKey.getId())) {
			return null;
		}

		Pair<Serializable, String> retrieved = retrieveAndValidateContext(cacheKey);
		String contextId = retrieved.getSecond();
		LOGGER.trace("Context of {} = {}", cacheKey.getId(), contextId);
		if (ROOT_REFERENCE_ID.equals(contextId)) {
			return null;
		}
		return contextId;
	}

	private Pair<Serializable, String> retrieveAndValidateContext(ContextCacheKey cacheKey) {
		Pair<Serializable, String> contextFromCache = getContextCache().getByKey(cacheKey);
		Pair<Serializable, String> contextFromInstance = getContextFromInstance(cacheKey, contextFromCache.getSecond());
		// consider check only if instance has data
		if (contextFromInstance.getSecond() != null
				&& !contextFromInstance.getSecond().equals(contextFromCache.getSecond())) {
			LOGGER.warn(
					"Detected inconsistent state for instance {}. Cached context value {}, value from provided data {}",
					cacheKey.getId(), contextFromCache.getSecond(), contextFromInstance.getSecond());
		}
		// try with provided from instance
		if (contextFromCache.getSecond() == null) {
			return contextFromInstance;
		}
		return contextFromCache;
	}

	private Pair<Serializable, String> getContextFromInstance(ContextCacheKey cacheKey, String cachedValue) {
		Serializable value = cacheKey.getRawValue();
		Instance provided = null;
		if (value instanceof Instance) {
			provided = (Instance) value;
		} else if (cachedValue == null) {
			// force loading or converting instance
			InstanceReference reference = cacheKey.getAsReference(this::loadReference);
			if (reference != null) {
				provided = reference.toInstance();
			}
		}
		if (provided == null) {
			return Pair.nullPair();
		}
		for (String nextProperty : INSTANCE_TO_CONTEXT_PROPERTIES) {
			Serializable contextRaw = provided.get(nextProperty);
			// try to extract cached values from a list of relations.
			// this is workaround for multivalue hasParent relation
			if (contextRaw instanceof Collection && ((Collection<?>) contextRaw).contains(cachedValue)) {
				// return null to reduce object creation
				return Pair.nullPair();
			}
			if (contextRaw != null) {
				return new Pair<>(cacheKey.getId(), extractId(contextRaw));
			}
		}
		return Pair.nullPair();
	}

	private ClassInstance checkAndGetSemanticType(ContextCacheKey key) {
		// try with the already loaded raw value or retrieve it from db
		Serializable rawValue = key.getRawValue() != null ? key.getRawValue() : key.getId();
		if (rawValue instanceof Instance && !(rawValue instanceof ClassInstance)) {
			return null;
		}
		if (rawValue instanceof ClassInstance) {
			return (ClassInstance) rawValue;
		}
		InstanceReference instanceRef = key.getAsReference(this::loadReference);
		if (instanceRef != null && instanceRef.getType() != null && instanceRef.getType().is("classinstance")) {
			Instance convertedClassInfo = instanceRef.toInstance();
			if (!(convertedClassInfo instanceof ClassInstance)) {
				LOGGER.warn("Detected classinstance type converted to wrong type {}", convertedClassInfo);
				return null;
			}
			return (ClassInstance) convertedClassInfo;
		}
		return null;
	}

	@Override
	public Optional<InstanceReference> getRootContext(Serializable instance) {
		String root = getContextInternal(createContextCacheKey(instance));
		while (root != null) {
			String contextInternal = getContextInternal(createContextCacheKey(root));
			if (contextInternal == null) {// last valid state reached
				return loadReference(root);
			}
			root = contextInternal;
		}
		return Optional.empty();
	}

	private Optional<InstanceReference> loadReference(String dbId) {
		if (dbId == null) {
			return Optional.empty();
		}
		return typeResolver.resolveReference(dbId);
	}

	@Override
	public void bindContext(Instance instance, Serializable context) {
		if (instance == null) {
			return;
		}
		ContextCacheKey cacheKey = createContextCacheKey(instance);
		Instance provided = cacheKey.getAsInstance(this::loadReference);
		if (provided == null) {
			throw new EmfRuntimeException("Unsupported instance type provided for context update: " + extractId(instance));
		}
		String contextId = extractId(context);
		setContextInternal(cacheKey, contextId);

		String currentContextId = getContextInternal(cacheKey);
		updateDomainModel(provided, currentContextId, contextId);
	}

	@Override
	public boolean isContextChanged(Instance instance) {
		Serializable newContextId = toShortUri(instance.get(InstanceContextService.HAS_PARENT, fieldConverter));
		String oldContextId = toShortUri(getContext(instance.getId()).map(InstanceReference::getId).orElse(null));
		return oldContextId == null ? newContextId != null : !oldContextId.equals(newContextId);
	}

	private String toShortUri(Serializable uri) {
		return uri == null ? null : namespaceRegistryService.getShortUri((String) uri);
	}

	private void updateDomainModel(Instance instance, String currentContextId, String newContextId) {
		LOGGER.info("Changing instance {} context from {} to {}", instance.getId(), currentContextId, newContextId);
		if (newContextId != null) {
			INSTANCE_TO_CONTEXT_PROPERTIES.forEach(property -> instance.add(property, newContextId, fieldConverter));
		} else {
			instance.removeProperties(INSTANCE_TO_CONTEXT_PROPERTIES, fieldConverter);
		}
	}

	@SuppressWarnings("unchecked")
	private String extractId(Serializable value) {
		if (value == null) {
			return null;
		}
		String id;
		if (value instanceof String) {
			id = (String) value;
		} else if (value instanceof InstanceReference) {
			id = ((InstanceReference) value).getId();
		} else if (value instanceof Instance) {
			// chain call since id might be IRI, Uri
			id = extractId(((Instance) value).getId());
		} else if (value instanceof IRI) {
			id = new URIProxy((IRI) value).stringValue();
		} else if (value instanceof Uri) {
			id = typeConverter.stringValue(value);
		} else if (value instanceof Collection && ((Collection<?>) value).size() == 1) {
			return extractId(((Collection<Serializable>) value).iterator().next());
		} else {
			throw new EmfRuntimeException("Unsupported context type provided - " + value);
		}
		if (id == null) {
			throw new EmfRuntimeException("Unsupported (not persisted) context provided - " + value);
		}
		return id;
	}

	private void setContextInternal(ContextCacheKey contextCacheKey, String contextId) {
		// validate new value and update cache
		validateNewContext(contextCacheKey, contextId).setValue(contextCacheKey, contextId);
	}

	private EntityLookupCache<Serializable, String, Serializable> validateNewContext(ContextCacheKey contextCacheKey,
			String newContextId) {
		if (EqualsHelper.nullSafeEquals(contextCacheKey.getId(), newContextId)) {
			throw new EmfRuntimeException("Could not set same instance as context: " + newContextId);
		}
		validateCycleDependency(contextCacheKey, newContextId);
		return getContextCache();
	}

	private void validateCycleDependency(ContextCacheKey instance, String newContextId) {
		boolean cycleDetected = false;
		if (newContextId == null || ROOT_REFERENCE_ID.equals(newContextId)) {
			return;
		}
		String currentContextId = newContextId;
		while (currentContextId != null) {
			if (EqualsHelper.nullSafeEquals(currentContextId, instance.getId())) {
				cycleDetected = true;
				break;
			}
			currentContextId = getContextInternal(createContextCacheKey(currentContextId));
		}

		if (cycleDetected) {
			throw new EmfRuntimeException("Detected cycle dependency during context assigment on: " + instance.getId());
		}
	}

	@Override
	public List<InstanceReference> getFullPath(Serializable instance) {
		InstanceReference source = createContextCacheKey(instance).getAsReference(this::loadReference);
		LinkedList<InstanceReference> fullContext = (LinkedList<InstanceReference>) getContextPath(source);
		if (source != null) {
			fullContext.addLast(source);
		}
		return fullContext;
	}

	private ContextCacheKey createContextCacheKey(Serializable rawValue) {
		return ContextCacheKey.create(rawValue, extractId(rawValue));
	}

	private EntityLookupCache<Serializable, String, Serializable> getContextCache() {
		return cacheContext.getCache(INSTANCE_CONTEXT_STORE_CACHE);
	}

	private static final class ContextCacheKey implements Serializable {
		private static final long serialVersionUID = 2189104453102242846L;
		private String id;
		private transient Serializable rawValue;

		private ContextCacheKey(final Serializable rawValue, final String id) {
			this.rawValue = rawValue;
			this.id = id;
		}

		boolean isValid() {
			return id != null;
		}

		String getId() {
			return id;
		}

		Serializable getRawValue() {
			return rawValue;
		}

		static ContextCacheKey create(Serializable rawValue, String id) {
			return new ContextCacheKey(rawValue, id);
		}

		InstanceReference getAsReference(Function<String, Optional<InstanceReference>> loader) {
			if ((rawValue == null || rawValue instanceof String) && isValid()) {
				rawValue = loader.apply(id).orElse(null);
			}
			if (rawValue instanceof InstanceReference) {
				return (InstanceReference) rawValue;
			} else if (rawValue instanceof Instance) {
				return ((Instance) rawValue).toReference();
			}
			return null;
		}

		Instance getAsInstance(Function<String, Optional<InstanceReference>> loader) {
			if ((rawValue == null || rawValue instanceof String) && isValid()) {
				rawValue = loader.apply(id).map(InstanceReference::toInstance).orElse(null);
			}
			if (rawValue instanceof Instance) {
				return (Instance) rawValue;
			} else if (rawValue instanceof InstanceReference) {
				return ((InstanceReference) rawValue).toInstance();
			}
			return null;
		}

		@Override
		public int hashCode() {
			return 31 * ((id == null) ? 0 : id.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ContextCacheKey other = (ContextCacheKey) obj;
			return EqualsHelper.nullSafeEquals(id, other.id);
		}

		@Override
		public String toString() {
			return "Context [id=" + id + "]";
		}

	}

	/**
	 * {@link ContextCacheLookUp} is simple adapter that delegates context resolving to to semantic service through
	 * semantic query
	 *
	 * @author bbanchev
	 */
	class ContextCacheLookUp extends EntityLookupCallbackDAOAdaptor<ContextCacheKey, String, Serializable> {
		@Override
		public Pair<ContextCacheKey, String> findByKey(ContextCacheKey key) {
			return restoreHierarchy(key);
		}

		@Override
		public Pair<ContextCacheKey, String> createValue(String value) {
			throw new UnsupportedOperationException("Contexts are managed externally!");

		}
	}
}
