package com.sirma.itt.seip.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Quad;
import com.sirma.itt.seip.Triplet;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheTransactionMode;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.Transaction;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.exceptions.DatabaseException;
import com.sirma.itt.seip.definition.model.PrototypeDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.exceptions.DictionaryException;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Basic implementation for definition services. Defines the caches and installed definition accessors.
 *
 * @author BBonev
 */
public abstract class BaseDefinitionService {

	private static final String MORE_THEN_ONE_DATA_TYPE_DEFINITION_FOUND = "More then one data type definition found";

	@CacheConfiguration(eviction = @Eviction(maxEntries = 256, strategy = "NONE") , doc = @Documentation(""
			+ "Cache used to contain the type definitions in the system. For every type defined are used 2 cache entries."
			+ "<br>Minimal value expression: types * 2.1") )
	private static final String TYPE_DEFINITION_CACHE = "TYPE_DEFINITION_CACHE";

	@CacheConfiguration(eviction = @Eviction(maxEntries = 256, strategy = "NONE") , doc = @Documentation(""
			+ "Cache used to contain the type definitions in the system mapped by db id. "
			+ "<br>Minimal value expression: types * 1.1") )
	private static final String TYPE_DEFINITION_ID_CACHE = "TYPE_DEFINITION_ID_CACHE";

	@CacheConfiguration(eviction = @Eviction(maxEntries = 256, strategy = "NONE") , doc = @Documentation(""
			+ "Cache used to contain the type definitions in the system mapped by URI. For every type defined are used 2 cache entries."
			+ "<br>Minimal value expression: types * 1.2") )
	private static final String TYPE_DEFINITION_URI_CACHE = "TYPE_DEFINITION_URI_CACHE";

	@CacheConfiguration(eviction = @Eviction(maxEntries = 20) , expiration = @Expiration(maxIdle = 900000, interval = 60000) , doc = @Documentation(""
			+ "Cache for the list of all definitions at max revision per type"
			+ "<br>Minimal value expression: types * 1.2") )
	private static final String MAX_REVISIONS_CACHE = "MAX_REVISIONS_CACHE";

	@CacheConfiguration(eviction = @Eviction(maxEntries = 1000) , expiration = @Expiration(maxIdle = 1800000, interval = 60000) , transaction = @Transaction(mode = CacheTransactionMode.NONE) , doc = @Documentation(""
			+ "Fully transactional cache used to store the unique prototy entries. For every unique property are used 2 cache entries."
			+ "<br>Minimal value expression: uniqueProperties * 2.2") )
	private static final String PROTOTYPE_CACHE = "PROTOTYPE_CACHE";

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseDefinitionService.class);

	@Inject
	protected DbDao dbDao;

	@Inject
	private EntityLookupCacheContext cacheContext;

	/** Collect all installed accessors. */
	@Inject
	@Any
	protected javax.enterprise.inject.Instance<DefinitionAccessor> accessors;

	private Map<Class<?>, DefinitionAccessor> accessorMapping;
	private Collection<DefinitionAccessor> accessorsCollection;

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
					throw new EmfConfigurationException("Ambiguous definition accessor: " + supportedObjectType
							+ " already defined by " + accessorMapping.get(supportedObjectType).getClass());
				}
				LOGGER.trace("Registering {} to {}", supportedObjectType, accessor.getClass());
				accessorMapping.put(supportedObjectType, accessor);
			}
		}
		accessorsCollection = Collections.unmodifiableCollection(accessorMapping.values());

		if (!cacheContext.containsCache(TYPE_DEFINITION_ID_CACHE)) {
			cacheContext.createCache(TYPE_DEFINITION_ID_CACHE, new TypeDefinitionIdLookup());
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
	 * Returns a {@link DataTypeDefinition} cache mapped by database id and type name as secondary.
	 *
	 * @return the typeDefinitionCache
	 */
	protected EntityLookupCache<Long, DataTypeDefinition, String> getTypeDefinitionIdCache() {
		return cacheContext.getCache(TYPE_DEFINITION_ID_CACHE);
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
	protected EntityLookupCache<Class, List<DefinitionModel>, Serializable> getMaxRevisionsCache() {
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
		if (errorOnMissing && accessor == null) {
			throw new EmfConfigurationException("The requested definition type " + ref + " is not supproted!");
		}
		return accessor;
	}

	/**
	 * Gets the collect all installed accessors.
	 *
	 * @return the collect all installed accessors
	 */
	protected Collection<DefinitionAccessor> getAccessors() {
		return accessorsCollection;
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
	protected static <E> E getCacheValue(Pair<?, E> pair) {
		if (pair == null) {
			return null;
		}
		return pair.getSecond();
	}

	/**
	 * Type definition lookup cache that have a database id as primary and name as secondary.
	 *
	 * @author BBonev
	 */
	class TypeDefinitionIdLookup extends EntityLookupCallbackDAOAdaptor<Long, DataTypeDefinition, String> {

		@Override
		public String getValueKey(DataTypeDefinition value) {
			if (value == null) {
				return null;
			}
			return value.getName();
		}

		@Override
		public Pair<Long, DataTypeDefinition> findByValue(DataTypeDefinition value) {
			String valueKey = value.getJavaClassName();
			if (valueKey == null) {
				return null;
			}
			List<Pair<String, Object>> args = new ArrayList<>(2);
			args.add(new Pair<String, Object>("javaClassName", valueKey));

			String query = DataType.QUERY_TYPE_DEFINITION_BY_CLASS_KEY;
			String name = value.getName();

			// if the name is provided we will also filter by it too
			if (StringUtils.isNotBlank(name)) {
				query = DataType.QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS_KEY;
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
			return new Pair<>(type.getId(), type);
		}

		@Override
		public Pair<Long, DataTypeDefinition> findByKey(Long key) {
			if (key == null) {
				return null;
			}
			DataType type = dbDao.find(DataType.class, key);
			return new Pair<>(key, type);
		}

		@Override
		public Pair<Long, DataTypeDefinition> createValue(DataTypeDefinition value) {
			if (value == null) {
				return null;
			}

			DataTypeDefinition typeDefinition = (DataTypeDefinition) dbDao.saveOrUpdate((Entity<?>) value);

			return new Pair<>(typeDefinition.getId(), typeDefinition);
		}

	}

	/**
	 * The Class TypeDefinitionLookup.
	 *
	 * @author BBonev
	 */
	class TypeDefinitionLookup extends EntityLookupCallbackDAOAdaptor<String, DataTypeDefinition, String> {

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
			String query = DataType.QUERY_TYPE_DEFINITION_BY_CLASS_KEY;
			List<Pair<String, Object>> args = new ArrayList<>(2);
			args.add(new Pair<String, Object>("javaClassName", valueKey));
			// if the name is provided also we will filter by it too
			if (StringUtils.isNotBlank(name)) {
				query = DataType.QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS_KEY;
				args.add(new Pair<String, Object>("name", name));
			}
			List<DataType> list = dbDao.fetchWithNamed(query, args);
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				// this should not happen or someone broke the DB on purpose
				throw new DictionaryException(MORE_THEN_ONE_DATA_TYPE_DEFINITION_FOUND);
			}
			DataType type = list.get(0);
			return new Pair<>(type.getName(), type);
		}

		@Override
		public Pair<String, DataTypeDefinition> findByKey(String key) {
			if (key == null) {
				return null;
			}
			List<DataType> list = dbDao.fetchWithNamed(DataType.QUERY_TYPE_DEFINITION_KEY,
					Collections.singletonList(new Pair<>("name", key)));
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				// this should not happen or someone broke the DB on purpose
				throw new DictionaryException(MORE_THEN_ONE_DATA_TYPE_DEFINITION_FOUND);
			}
			DataType type = list.get(0);
			return new Pair<>(key, type);
		}

		@Override
		public Pair<String, DataTypeDefinition> createValue(DataTypeDefinition value) {
			if (value == null) {
				return null;
			}

			DataTypeDefinition typeDefinition = (DataTypeDefinition) dbDao.saveOrUpdate((Entity<?>) value);

			return new Pair<>(typeDefinition.getName(), typeDefinition);
		}

	}

	/**
	 * Type lookup cache by type URI
	 *
	 * @author BBonev
	 */
	class TypeDefinitionUriLookup extends EntityLookupCallbackDAOAdaptor<String, String, Serializable> {

		@Override
		public Pair<String, String> findByKey(String key) {
			if (key == null) {
				return null;
			}
			List<DataType> list = dbDao.fetchWithNamed(DataType.QUERY_TYPE_DEFINITION_BY_URI_KEY,
					Collections.singletonList(new Pair<>("uri", "%" + key + "%")));
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
			extends EntityLookupCallbackDAOAdaptor<Long, PrototypeDefinition, Triplet<String, Boolean, Long>> {

		@Override
		public Triplet<String, Boolean, Long> getValueKey(PrototypeDefinition value) {
			if (value == null) {
				return null;
			}
			return new Triplet<>(value.getIdentifier(), value.isMultiValued(), value.getDataType().getId());
		}

		@Override
		public Pair<Long, PrototypeDefinition> findByValue(PrototypeDefinition value) {
			Triplet<String, Boolean, Long> pair = getValueKey(value);
			if (pair == null) {
				return null;
			}

			List<Pair<String, Object>> args = new ArrayList<>(3);
			args.add(new Pair<>("name", pair.getFirst()));
			args.add(new Pair<>("multiValued", pair.getSecond()));
			args.add(new Pair<>("type", pair.getThird()));
			List<PrototypeDefinition> list = dbDao
					.fetchWithNamed(PrototypeDefinitionImpl.QUERY_PROTO_TYPE_DEFINITION_KEY, args);
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				LOGGER.warn("Found mode then one proto type definition for key {}", pair);
			}
			PrototypeDefinition definition = list.get(0);
			return new Pair<>(definition.getId(), definition);
		}

		@Override
		public Pair<Long, PrototypeDefinition> findByKey(Long key) {
			try {
				PrototypeDefinitionImpl definitionImpl = dbDao.find(PrototypeDefinitionImpl.class, key);
				return new Pair<>(key, definitionImpl);
			} catch (DatabaseException e) {
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
	class MaxRevisionsLookup extends EntityLookupCallbackDAOAdaptor<Class, List<DefinitionModel>, Serializable> {

		@Override
		public Pair<Class, List<DefinitionModel>> findByKey(Class key) {
			List<DefinitionModel> result;
			DefinitionAccessor accessor = getDefinitionAccessor(key, true);
			result = accessor.getAllDefinitions();

			if (result == null) {
				return null;
			}
			return new Pair<>(key, result);
		}

		@Override
		public Pair<Class, List<DefinitionModel>> createValue(List<DefinitionModel> value) {
			throw new UnsupportedOperationException("Max revison cannot be created");
		}
	}

}
