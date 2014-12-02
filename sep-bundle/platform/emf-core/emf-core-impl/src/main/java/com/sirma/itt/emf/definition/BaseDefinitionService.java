package com.sirma.itt.emf.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PrototypeDefinition;
import com.sirma.itt.emf.definition.model.PrototypeDefinitionImpl;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Quad;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.exceptions.CmfDatabaseException;
import com.sirma.itt.emf.exceptions.DictionaryException;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Basic implementation for definition services. Defines the caches and installed definition
 * accessors.
 * 
 * @author BBonev
 */
public abstract class BaseDefinitionService {

	/** The Constant TYPE_DEFINITION_CACHE. */
	@CacheConfiguration(container = "cmf", doc = @Documentation(""
			+ "Cache used to contain the type definitions in the system. For every type defined are used 2 cache entries."
			+ "<br>Minimal value expression: types * 2.1"))
	private static final String TYPE_DEFINITION_CACHE = "TYPE_DEFINITION_CACHE";
	@CacheConfiguration(container = "cmf", doc = @Documentation(""
			+ "Cache used to contain the type definitions in the system mapped by URI. For every type defined are used 2 cache entries."
			+ "<br>Minimal value expression: types * 1.2"))
	private static final String TYPE_DEFINITION_URI_CACHE = "TYPE_DEFINITION_URI_CACHE";
	/** The Constant MAX_REVISIONS_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 20), expiration = @Expiration(maxIdle = 900000, interval = 60000), doc = @Documentation(""
			+ "Cache for the list of all definitions at max revision per type"
			+ "<br>Minimal value expression: types * 1.2"))
	private static final String MAX_REVISIONS_CACHE = "MAX_REVISIONS_CACHE";
	/** The Constant PROTOTYPE_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), transaction = CacheTransactionMode.FULL_XA, doc = @Documentation(""
			+ "Fully transactional cache used to store the unique prototy entries. For every unique property are used 2 cache entries."
			+ "<br>Minimal value expression: uniqueProperties * 2.2"))
	private static final String PROTOTYPE_CACHE = "PROTOTYPE_CACHE";
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseDefinitionService.class);
	/** The db dao. */
	@Inject
	protected DbDao dbDao;
	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;
	/** Collect all installed accessors. */
	@Inject
	@Any
	protected javax.enterprise.inject.Instance<DefinitionAccessor> accessors;

	private Map<Class<?>, DefinitionAccessor> accessorMapping;

	/**
	 * Initialize the instance by collecting information about the accessors and cache instances
	 */
	@PostConstruct
	public void init() {
		accessorMapping = CollectionUtils.createHashMap(50);
		for (DefinitionAccessor accessor : accessors) {
			// map all supported classes to the same accessor for faster lookup
			Set<Class<?>> supportedObjects = accessor.getSupportedObjects();
			for (Class<?> supportedObjectType : supportedObjects) {
				if (accessorMapping.containsKey(supportedObjectType)) {
					throw new EmfConfigurationException("Ambiguous definition accessor: "
							+ supportedObjectType + " already defined by "
							+ accessorMapping.get(supportedObjectType).getClass());
				}
				LOGGER.trace("Registering {} to {}", supportedObjectType, accessor.getClass());
				accessorMapping.put(supportedObjectType, accessor);
			}
		}

		if (!cacheContext.containsCache(TYPE_DEFINITION_CACHE)) {
			cacheContext.createCache(TYPE_DEFINITION_CACHE, new TypeDefinitionLookup());
		}
		if (!cacheContext.containsCache(TYPE_DEFINITION_URI_CACHE)) {
			cacheContext.createCache(TYPE_DEFINITION_URI_CACHE, new TypeDefinitionUriLookup());
		}
		if (!cacheContext.containsCache(MAX_REVISIONS_CACHE)) {
			cacheContext.createCache(MAX_REVISIONS_CACHE, new MaxRevisionsLookup());
		}
		if (!cacheContext.containsCache(PROTOTYPE_CACHE)) {
			cacheContext.createCache(PROTOTYPE_CACHE, new PrototypeDefinitionLookup());
		}
	}

	/**
	 * Getter method for typeDefinitionCache.
	 *
	 * @return the typeDefinitionCache
	 */
	protected EntityLookupCache<String, DataTypeDefinition, String> getTypeDefinitionCache() {
		return cacheContext.getCache(TYPE_DEFINITION_CACHE);
	}

	/**
	 * Getter method for typeDefinitionCache.
	 *
	 * @return the typeDefinitionCache
	 */
	protected EntityLookupCache<String, String, Serializable> getTypeDefinitionUriCache() {
		return cacheContext.getCache(TYPE_DEFINITION_URI_CACHE);
	}

	/**
	 * Gets the prototype cache.
	 *
	 * @return the prototype cache
	 */
	protected EntityLookupCache<Long, PrototypeDefinition, Quad<String, String, Boolean, Long>> getPrototypeCache() {
		return cacheContext.getCache(PROTOTYPE_CACHE);
	}

	/**
	 * Gets the max revisions cache.
	 *
	 * @return the max revisions cache
	 */
	@SuppressWarnings("rawtypes")
	protected EntityLookupCache<Pair<Class, String>, List<DefinitionModel>, Serializable> getMaxRevisionsCache() {
		return cacheContext.getCache(MAX_REVISIONS_CACHE);
	}

	/**
	 * Gets the definition accessor.
	 * 
	 * @param ref
	 *            the ref
	 * @param errorOnMissing
	 *            if true the method will throw an error if the accessor is not found
	 * @return the definition accessor
	 */
	protected DefinitionAccessor getDefinitionAccessor(Class<?> ref, boolean errorOnMissing) {
		DefinitionAccessor accessor = accessorMapping.get(ref);
		if (errorOnMissing && (accessor == null)) {
			throw new EmfConfigurationException("The requested definition type " + ref
					+ " is not supproted!");
		}
		return accessor;
	}

	/**
	 * Gets the collect all installed accessors.
	 * 
	 * @return the collect all installed accessors
	 */
	protected Collection<DefinitionAccessor> getAccessors() {
		return Collections.unmodifiableCollection(accessorMapping.values());
	}

	/**
	 * Gets the cache value.
	 * 
	 * @param <E>
	 *            the element type
	 * @param pair
	 *            the pair
	 * @return the cache value
	 */
	protected <E> E getCacheValue(Pair<?, E> pair) {
		if (pair == null) {
			return null;
		}
		return pair.getSecond();
	}

	/**
	 * The Class TypeDefinitionLookup.
	 *
	 * @author BBonev
	 */
	class TypeDefinitionLookup extends
			EntityLookupCallbackDAOAdaptor<String, DataTypeDefinition, String> {

		@Override
		public String getValueKey(DataTypeDefinition value) {
			if (value == null) {
				return null;
			}
			if (EqualsHelper.nullSafeEquals(DataTypeDefinition.DATE, value.getName(), true)) {
				// we assume the date type does not have a secondary key as it is equal to the
				// datetime type so it will always be fetched from database
				return null;
			}
			return value.getJavaClassName();
		}

		@Override
		public Pair<String, DataTypeDefinition> findByValue(DataTypeDefinition value) {
			String valueKey = value.getJavaClassName();
			if (valueKey == null) {
				return null;
			}
			String name = value.getName();
			String query = EmfQueries.QUERY_TYPE_DEFINITION_BY_CLASS_KEY;
			List<Pair<String, Object>> args = new ArrayList<>(2);
			args.add(new Pair<String, Object>("javaClassName", valueKey));
			// if the name is provided also we will filter by it too
			if (StringUtils.isNotNullOrEmpty(name)) {
				query = EmfQueries.QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS_KEY;
				args.add(new Pair<String, Object>("name", name));
			}
			List<DataType> list = dbDao.fetchWithNamed(query, args);
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				// this should not happen or someone broke the DB on purpose
				throw new DictionaryException("More then one data type definition found");
			}
			DataType type = list.get(0);
			return new Pair<String, DataTypeDefinition>(type.getName(), type);
		}

		@Override
		public Pair<String, DataTypeDefinition> findByKey(String key) {
			if (key == null) {
				return null;
			}
			List<DataType> list = dbDao.fetchWithNamed(EmfQueries.QUERY_TYPE_DEFINITION_KEY,
					Arrays.asList(new Pair<String, Object>("name", key)));
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				// this should not happen or someone broke the DB on purpose
				throw new DictionaryException("More then one data type definition found");
			}
			DataType type = list.get(0);
			return new Pair<String, DataTypeDefinition>(key, type);
		}

		@Override
		public Pair<String, DataTypeDefinition> createValue(DataTypeDefinition value) {
			if (value == null) {
				return null;
			}

			DataTypeDefinition typeDefinition = (DataTypeDefinition) dbDao
					.saveOrUpdate((Entity<?>) value);

			return new Pair<>(typeDefinition.getName(), typeDefinition);
		}

	}

	/**
	 * Type lookup cache by type URI
	 *
	 * @author BBonev
	 */
	class TypeDefinitionUriLookup extends
			EntityLookupCallbackDAOAdaptor<String, String, Serializable> {

		@Override
		public Pair<String, String> findByKey(String key) {
			if (key == null) {
				return null;
			}
			List<DataType> list = dbDao.fetchWithNamed(EmfQueries.QUERY_TYPE_DEFINITION_BY_URI_KEY,
					Arrays.asList(new Pair<String, Object>("uri", "%" + key + "%")));
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				for (DataType dataType : list) {
					for (String string : dataType.getUries()) {
						if (string.equals(key)) {
							return new Pair<>(key, dataType.getName());
						}
					}
				}
				// this should not happen or someone broke the DB on purpose
				LOGGER.warn("More then one data type definition found for URI={}\n{}", key, list);
			}
			String type = list.get(0).getName();
			return new Pair<>(key, type);
		}

		@Override
		public Pair<String, String> createValue(String value) {
			throw new UnsupportedOperationException("This cache cannot create values.");
		}

	}

	/**
	 * Prototype definition lookup class.
	 *
	 * @author BBonev
	 */
	class PrototypeDefinitionLookup
			extends
			EntityLookupCallbackDAOAdaptor<Long, PrototypeDefinition, Quad<String, String, Boolean, Long>> {

		@Override
		public Quad<String, String, Boolean, Long> getValueKey(PrototypeDefinition value) {
			if (value == null) {
				return null;
			}
			return new Quad<>(value.getIdentifier(), value.getContainer(), value.isMultiValued(),
					value.getDataType().getId());
		}

		@Override
		public Pair<Long, PrototypeDefinition> findByValue(PrototypeDefinition value) {
			Quad<String, String, Boolean, Long> pair = getValueKey(value);
			if (pair == null) {
				return null;
			}

			List<Pair<String, Object>> args = new ArrayList<>(2);
			args.add(new Pair<String, Object>("name", pair.getFirst()));
			args.add(new Pair<String, Object>("container", pair.getSecond()));
			args.add(new Pair<String, Object>("multiValued", pair.getThird()));
			args.add(new Pair<String, Object>("type", pair.getForth()));
			List<Object> list = dbDao.fetchWithNamed(EmfQueries.QUERY_PROTO_TYPE_DEFINITION_KEY,
					args);
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				LOGGER.warn("Found mode then one proto type definition for key " + pair);
			}
			PrototypeDefinition definition = (PrototypeDefinition) list.get(0);
			return new Pair<>(definition.getId(), definition);
		}

		@Override
		public Pair<Long, PrototypeDefinition> findByKey(Long key) {
			try {
				PrototypeDefinitionImpl definitionImpl = dbDao.find(PrototypeDefinitionImpl.class,
						key);
				return new Pair<Long, PrototypeDefinition>(key, definitionImpl);
			} catch (CmfDatabaseException e) {
				String string = "Prototype property with {} not found!";
				LOGGER.debug(string, key);
				LOGGER.trace(string, key, e);
				return null;
			}
		}

		@Override
		public Pair<Long, PrototypeDefinition> createValue(PrototypeDefinition value) {
			PrototypeDefinition update = dbDao.saveOrUpdateInNewTx(value);
			return new Pair<>(update.getId(), update);
		}

	}

	/**
	 * Fetches all max revisions for the given type.
	 *
	 * @author BBonev
	 */
	@SuppressWarnings("rawtypes")
	class MaxRevisionsLookup
			extends
			EntityLookupCallbackDAOAdaptor<Pair<Class, String>, List<DefinitionModel>, Serializable> {

		@Override
		public Pair<Pair<Class, String>, List<DefinitionModel>> findByKey(Pair<Class, String> key) {
			List<DefinitionModel> result = null;
			DefinitionAccessor accessor = getDefinitionAccessor(key.getFirst(), true);
			result = accessor.getAllDefinitions(key.getSecond());

			if (result == null) {
				return null;
			}
			return new Pair<>(key, result);
		}

		@Override
		public Pair<Pair<Class, String>, List<DefinitionModel>> createValue(
				List<DefinitionModel> value) {
			throw new UnsupportedOperationException("Max revison cannot be created");
		}
	}

}