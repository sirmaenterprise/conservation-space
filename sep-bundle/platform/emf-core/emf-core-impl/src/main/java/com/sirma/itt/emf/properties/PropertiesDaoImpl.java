package com.sirma.itt.emf.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.properties.dao.PropertiesDao;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.dao.RelationalNonPersistentPropertiesExtension;
import com.sirma.itt.emf.properties.entity.EntityId;
import com.sirma.itt.emf.properties.entity.NodePropertyHelper;
import com.sirma.itt.emf.properties.entity.PropertyEntity;
import com.sirma.itt.emf.properties.entity.PropertyKey;
import com.sirma.itt.emf.properties.entity.PropertyValue;
import com.sirma.itt.emf.properties.event.PropertiesChangeEvent;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.properties.model.PropertyModelKey;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.EqualsHelper.MapValueComparison;

/**
 * Default implementation of properties DAO.
 *
 * @author BBonev
 */
@Stateless
public class PropertiesDaoImpl implements PropertiesDao, Serializable {

	private static final String BEAN_TYPE = "beanType";
	private static final String BEAN_ID = "beanId";
	private static final String NO_PROPERTIES_SUPPORT_FOR = "No properties support for {}";

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesDaoImpl.class);

	private static final int PROPERTY_PRINT_LENGHT = 150;

	/** The Constant PROPERTY_ENTITY_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 50000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Cache used to properties for the loaded active instances. The cache does NOT handle instance that are stored only in a semantic database. "
			+ "The cache SHOULD not be transactional due to invalid state when cascading properties save/load."
			+ "<br>Minimal value expression: (caseCache + documentCache + sectionCache + projectCache + averageNonStartedScheduleEntries + workflowTaskCache + standaloneTaskCache + workflowCache) * 1.2"))
	private static final String PROPERTY_ENTITY_CACHE = "PROPERTY_ENTITY_CACHE";

	private static final long serialVersionUID = -7760421274652309552L;

	/** The Constant FORBIDDEN_PROPERTIES. */
	private Set<String> forbiddenProperties;

	/** The node property helper. */
	@Inject
	private NodePropertyHelper nodePropertyHelper;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The is debug enabled. */
	private boolean isDebugEnabled;
	/** The is trace enabled. */
	private boolean isTraceEnabled;

	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;

	/** The non persistent properties. */
	@Inject
	@ExtensionPoint(value = RelationalNonPersistentPropertiesExtension.TARGET_NAME)
	private Iterable<RelationalNonPersistentPropertiesExtension> nonPersistentProperties;

	/** The event service. */
	@Inject
	private EventService eventService;

	/**
	 * Inits the cache context.
	 */
	@PostConstruct
	public void init() {
		if (!cacheContext.containsCache(PROPERTY_ENTITY_CACHE)) {
			cacheContext.createCache(PROPERTY_ENTITY_CACHE, new PropertiesLookupCallback());
		}

		forbiddenProperties = new LinkedHashSet<>(50);
		for (RelationalNonPersistentPropertiesExtension extension : nonPersistentProperties) {
			forbiddenProperties.addAll(extension.getNonPersistentProperties());
		}

		isDebugEnabled = LOGGER.isDebugEnabled();
		isTraceEnabled = LOGGER.isTraceEnabled();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E extends PropertyModel> void saveProperties(E model, PropertyModelCallback<E> callback) {
		savePropertiesInternal(model, false, callback);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E extends PropertyModel> void saveProperties(E model, boolean addOnly,
			PropertyModelCallback<E> callback) {
		savePropertiesInternal(model, addOnly, callback);
	}

	/**
	 * Save properties internal.
	 *
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the model
	 * @param addOnly
	 *            the add only
	 * @param callback
	 *            the callback
	 */
	@SuppressWarnings("rawtypes")
	private <E extends PropertyModel> void savePropertiesInternal(E model, boolean addOnly,
			PropertyModelCallback<E> callback) {
		Map<PropertyModelKey, Object> map = callback.getModel(model);
		for (Entry<PropertyModelKey, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			PropertyModel target = null;
			if (value instanceof List) {
				List list = (List) value;
				if (list.isEmpty()) {
					continue;
				}
				target = (PropertyModel) list.get(0);
			} else {
				target = (PropertyModel) value;
			}
			setPropertiesImpl(entry.getKey(), target.getProperties(), addOnly, target);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends PropertyModel> void loadProperties(E model, PropertyModelCallback<E> callback) {
		loadPropertiesInternal(Arrays.asList(model), callback);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends PropertyModel> void loadProperties(List<E> models,
			PropertyModelCallback<E> callback) {
		loadPropertiesInternal(models, callback);
	}

	/**
	 * Load properties internal.
	 *
	 * @param <E>
	 *            the element type
	 * @param models
	 *            the models
	 * @param callback
	 *            the callback
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <E extends PropertyModel> void loadPropertiesInternal(List<E> models,
			PropertyModelCallback<E> callback) {
		if (models.isEmpty()) {
			return;
		}
		Map<PropertyModelKey, Object> loadedModels = new LinkedHashMap<>();
		for (PropertyModel propertyModel : models) {
			Map<PropertyModelKey, Object> model = callback.getModel((E) propertyModel);
			for (Entry<PropertyModelKey, Object> entry : model.entrySet()) {
				Object object = loadedModels.get(entry.getKey());
				Object toSet = null;
				if (object == null) {
					toSet = entry.getValue();
				} else if (object instanceof List) {
					List list = (List) object;
					if (entry.getValue() instanceof List) {
						list.addAll((Collection) entry.getValue());
					} else {
						list.add(entry.getValue());
					}
					toSet = list;
				} else {
					List list = new LinkedList();
					list.add(object);
					if (entry.getValue() instanceof List) {
						list.addAll((Collection) entry.getValue());
					} else {
						list.add(entry.getValue());
					}
					toSet = list;
				}
				if (toSet != null) {
					loadedModels.put(entry.getKey(), toSet);
				}
			}
			loadedModels.putAll(model);
		}

		callback.updateModel(loadedModels, loadPropertiesInternal(loadedModels.keySet()));
	}

	@SuppressWarnings("rawtypes")
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, Serializable> getEntityProperties(Entity entity, Long revision,
			PathElement path, PropertyModelCallback<PropertyModel> callback) {
		PropertyModelKey entityId = callback.createModelKey(entity, revision);
		if (entityId == null) {
			LOGGER.warn(NO_PROPERTIES_SUPPORT_FOR, entity.getClass());
			return Collections.emptyMap();
		}
		entityId.setPathElement(path);
		return getNodePropertiesCached(entityId);
	}

	@SuppressWarnings("rawtypes")
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeProperties(Entity entity, Long revision, PathElement path,
			PropertyModelCallback<PropertyModel> callback) {
		PropertyModelKey entityId = callback.createModelKey(entity, revision);
		if (entityId == null) {
			LOGGER.warn(NO_PROPERTIES_SUPPORT_FOR, entity.getClass());
			return;
		}
		entityId.setPathElement(path);

		getPropertiesCache().deleteByKey(entityId);
	}

	@SuppressWarnings("rawtypes")
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void saveProperties(Entity entity, Long revision, PathElement path,
			Map<String, Serializable> properties, PropertyModelCallback<PropertyModel> callback) {
		savePropertiesInternal(entity, revision, path, properties, false, callback);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void saveProperties(Entity entity, Long revision, PathElement path,
			Map<String, Serializable> properties, boolean addOnly,
			PropertyModelCallback<PropertyModel> callback) {
		savePropertiesInternal(entity, revision, path, properties, addOnly, callback);
	}

	/**
	 * Save properties internal.
	 *
	 * @param entity
	 *            the entity
	 * @param revision
	 *            the revision
	 * @param path
	 *            the path
	 * @param properties
	 *            the properties
	 * @param addOnly
	 *            the add only
	 * @param callback
	 *            the callback
	 */
	@SuppressWarnings("rawtypes")
	private void savePropertiesInternal(Entity entity, Long revision, PathElement path,
			Map<String, Serializable> properties, boolean addOnly,
			PropertyModelCallback<PropertyModel> callback) {
		PropertyModelKey key = callback.createModelKey(entity, revision);
		if (key == null) {
			LOGGER.warn(NO_PROPERTIES_SUPPORT_FOR, entity.getClass());
			return;
		}
		key.setPathElement(path);

		PropertyModel model = null;
		if (entity instanceof PropertyModel) {
			model = (PropertyModel) entity;
		}
		setPropertiesImpl(key, properties, addOnly, model);
	}

	/**
	 * Does differencing to add and/or remove properties. Internally, the existing properties will
	 * be retrieved and a difference performed to work out which properties need to be created,
	 * updated or deleted. It is only necessary to pass in old and new values for <i>changes</i>
	 * i.e. when setting a single property, it is only necessary to pass that property's value in
	 * the <b>old</b> and </b>new</b> maps; this improves execution speed significantly - although
	 * it has no effect on the number of resulting DB operations.
	 * <p/>
	 * Note: The cached properties are not updated
	 *
	 * @param entityId
	 *            the node ID
	 * @param newProps
	 *            the properties to add or update
	 * @param isAddOnly
	 *            <tt>true</tt> if the new properties are just an update or <tt>false</tt> if the
	 *            properties are a complete set
	 * @param model
	 *            the target model that holds the provided properties
	 * @return Returns <tt>true</tt> if any properties were changed
	 */
	private boolean setPropertiesImpl(PropertyModelKey entityId,
			Map<String, Serializable> newProps, boolean isAddOnly, PropertyModel model) {
		if (isAddOnly && newProps.isEmpty()) {
			// No point adding nothing
			return false;
		}

		// Copy inbound values
		Map<String, Serializable> newPropsLocal = new LinkedHashMap<>(newProps);

		// Remove properties that should not be updated from the user
		newPropsLocal.keySet().removeAll(forbiddenProperties);

		// Load the current properties.
		// This means that we have to go to the DB during cold-write operations,
		// but usually a write occurs after a node has been fetched of viewed in
		// some way by the client code. Loading the existing properties has the
		// advantage that the differencing code can eliminate unnecessary writes
		// completely.
		Map<String, Serializable> oldPropsCached = getNodePropertiesCached(entityId);
		// Keep pristine for caching
		Map<String, Serializable> oldProps = new LinkedHashMap<>(oldPropsCached);
		// If we're adding, remove current properties that are not of interest
		if (isAddOnly) {
			oldProps.keySet().retainAll(newPropsLocal.keySet());
		}

		// We need to convert the new properties to our internally-used format,
		// which is compatible with model i.e. people may have passed in data
		// which needs to be converted to a model-compliant format. We do this
		// before comparisons to avoid false negatives.
		Map<PropertyKey, PropertyValue> newPropsRaw = nodePropertyHelper
				.convertToPersistentProperties(newPropsLocal, entityId.getRevision(),
						entityId.getPathElement());
		newPropsLocal = nodePropertyHelper.convertToPublicProperties(newPropsRaw,
				entityId.getRevision(), entityId.getPathElement());
		// Now find out what's changed
		Map<String, MapValueComparison> diff = EqualsHelper.getMapComparison(oldProps, newPropsLocal);
		// Keep track of properties to delete and add
		Map<String, Serializable> propsToDelete = new LinkedHashMap<>(oldProps.size() * 2);
		Map<String, Serializable> propsToAdd = new LinkedHashMap<>(newPropsLocal.size() * 2);
		for (Map.Entry<String, MapValueComparison> entry : diff.entrySet()) {
			String qname = entry.getKey();

			switch (entry.getValue()) {
				case EQUAL:
					// Ignore
					break;
				case LEFT_ONLY:
					// Not in the new properties
					propsToDelete.put(qname, oldProps.get(qname));
					break;
				case NOT_EQUAL:
					// Must remove from the LHS
					propsToDelete.put(qname, oldProps.get(qname));
					Serializable value = newPropsLocal.get(qname);
					propsToAdd.put(qname, value);
					break;
				case RIGHT_ONLY:
					// We're adding this
					value = newPropsLocal.get(qname);
					propsToAdd.put(qname, value);
					break;
				default:
					throw new IllegalStateException("Unknown MapValueComparison: "
							+ entry.getValue());
			}
		}

		boolean updated = !propsToDelete.isEmpty() || !propsToAdd.isEmpty();

		// Touch to bring into current txn
		if (updated) {
			try {
				// Apply deletes
				Set<Long> propStringIdsToDelete = convertStringsToIds(propsToDelete,
						entityId.getRevision(), entityId.getPathElement());
				deleteNodeProperties(entityId, propStringIdsToDelete);
				// Now create the raw properties for adding
				newPropsRaw = nodePropertyHelper.convertToPersistentProperties(propsToAdd,
						entityId.getRevision(), entityId.getPathElement());
				insertNodeProperties(entityId, newPropsRaw);
			} catch (Exception e) {
				// Focused error
				throw new EmfRuntimeException("Failed to write property deltas: \n"
						+ "  Node:          " + entityId + "\n" + "  Old:           " + oldProps
						+ "\n" + "  New:           " + newPropsLocal + "\n" + "  Diff:          " + diff
						+ "\n" + "  Delete Tried:  " + propsToDelete + "\n" + "  Add Tried:     "
						+ propsToAdd, e);
			}

			// Build the properties to cache based on whether this is an append
			// or replace
			Map<String, Serializable> propsToCache = null;
			if (isAddOnly) {
				// Combine the old and new properties
				propsToCache = oldPropsCached;
				propsToCache.putAll(propsToAdd);
			} else {
				// Replace old properties
				propsToCache = newPropsLocal;
				// Ensure correct types
				propsToCache.putAll(propsToAdd);
			}
			// Update cache
			setNodePropertiesCached(entityId, propsToCache);
			// XXX: this should be defined somewhere - EntityIdType.INSTANCE.getType()
		} else if (entityId.getBeanType() == 7) {
			setNodePropertiesCached(entityId, newPropsLocal);
		}
		// Touch to bring into current transaction
		if (updated) {
			notifyForChanges(model, propsToDelete, propsToAdd);
		}
		// Done
		if (isDebugEnabled && updated) {
			LOGGER.debug("Modified node properties: " + entityId + "\n   Removed: "
					+ printUserFriendly(propsToDelete) + "\n   Added:   "
					+ printUserFriendly(propsToAdd));
		}
		return updated;
	}

	/**
	 * Notify for changes in properties for instance.
	 *
	 * @param model
	 *            the model
	 * @param propsToDelete
	 *            the props to delete
	 * @param propsToAdd
	 *            the props to add
	 */
	@SuppressWarnings("rawtypes")
	private void notifyForChanges(PropertyModel model, Map<String, Serializable> propsToDelete,
			Map<String, Serializable> propsToAdd) {
		Entity entity = null;
		if (model instanceof Entity) {
			entity = (Entity) model;
		}
		String operation = (String) RuntimeConfiguration
				.getConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
		// change event here
		eventService.fire(new PropertiesChangeEvent(entity, propsToAdd, propsToDelete,
				operation));
	}

	/**
	 * Prints the user friendly.
	 *
	 * @param map
	 *            the map
	 * @return the string
	 */
	private String printUserFriendly(Map<String, Serializable> map) {
		StringBuilder builder = new StringBuilder(1024);
		builder.append('{');
		for (Iterator<Entry<String, Serializable>> it = map.entrySet().iterator(); it.hasNext();) {
			Entry<String, Serializable> entry = it.next();
			builder.append(entry.getKey()).append("=");
			if (entry.getValue() == null) {
				builder.append("null");
			} else {
				String value = entry.getValue().toString();
				if (value.length() > PROPERTY_PRINT_LENGHT) {
					builder.append(value.substring(0, PROPERTY_PRINT_LENGHT)).append(" ... ")
							.append(value.length() - PROPERTY_PRINT_LENGHT).append(" more");
				} else {
					builder.append(value);
				}
			}
			if (it.hasNext()) {
				builder.append(", ");
			}
		}
		builder.append('}');
		return builder.toString();
	}

	/**
	 * Sets the node properties cached.
	 *
	 * @param entityId
	 *            the node id
	 * @param properties
	 *            the props to cache
	 */
	private void setNodePropertiesCached(PropertyModelKey entityId,
			Map<String, Serializable> properties) {
		getPropertiesCache().setValue(entityId,
				Collections.unmodifiableMap(PropertiesUtil.cloneProperties(properties)));
	}

	/**
	 * Insert node properties.
	 *
	 * @param entityId
	 *            the node id
	 * @param newPropsRaw
	 *            the new props raw
	 */
	private void insertNodeProperties(PropertyModelKey entityId,
			Map<PropertyKey, PropertyValue> newPropsRaw) {
		if (newPropsRaw.isEmpty()) {
			return;
		}

		for (Entry<PropertyKey, PropertyValue> entry : newPropsRaw.entrySet()) {
			PropertyEntity propertyEntity = new PropertyEntity();
			propertyEntity.setKey(entry.getKey());
			propertyEntity.setValue(entry.getValue());
			propertyEntity.setEntityId((EntityId) entityId);
			dbDao.saveOrUpdate(propertyEntity);
		}
	}

	/**
	 * Convert strings to ids.
	 *
	 * @param propsToDelete
	 *            the props to delete
	 * @param revision
	 *            the property revision
	 * @param pathElement
	 *            the path element
	 * @return the sets the
	 */
	private Set<Long> convertStringsToIds(Map<String, Serializable> propsToDelete, Long revision,
			PathElement pathElement) {
		Set<Long> result = new LinkedHashSet<>((int) (propsToDelete.size() * 1.2), 1f);
		for (Entry<String, Serializable> entry : propsToDelete.entrySet()) {
			Long propertyId = dictionaryService.getPropertyId(entry.getKey(), revision,
					pathElement, entry.getValue());
			if (propertyId != null) {
				result.add(propertyId);
			}
		}
		return result;
	}

	/**
	 * Delete node properties.
	 *
	 * @param entityId
	 *            the node id
	 * @param propStringIdsToDelete
	 *            the prop string ids to delete
	 */
	private void deleteNodeProperties(PropertyModelKey entityId, Set<Long> propStringIdsToDelete) {
		if (!propStringIdsToDelete.isEmpty()) {
			List<Pair<String, Object>> args = new ArrayList<>(3);
			args.add(new Pair<String, Object>("id", propStringIdsToDelete));
			args.add(new Pair<String, Object>(BEAN_ID, entityId.getBeanId()));
			args.add(new Pair<String, Object>(BEAN_TYPE, entityId.getBeanType()));
			int removed = dbDao.executeUpdate(EmfQueries.DELETE_PROPERTIES_KEY, args);
			LOGGER.debug("Removed {} properties {}", removed, entityId);
		}
	}

	/**
	 * Gets the node properties cached.
	 *
	 * @param entityId
	 *            the node id
	 * @return the node properties cached
	 */
	private Map<String, Serializable> getNodePropertiesCached(PropertyModelKey entityId) {
		Pair<PropertyModelKey, Map<String, Serializable>> cacheEntry = getPropertiesCache()
				.getByKey(entityId);
		if (cacheEntry == null) {
			// when the node is newly created and not persisted, yet. then we have no properties
			return new LinkedHashMap<>();
		}
		Map<String, Serializable> cachedProperties = cacheEntry.getSecond();
		Map<String, Serializable> properties = PropertiesUtil.cloneProperties(cachedProperties);

		return properties;
	}

	/**
	 * Load properties for all given entity IDs in one query.
	 *
	 * @param entityIds
	 *            the entity ids
	 * @return the map
	 */
	private Map<PropertyModelKey, Map<String, Serializable>> loadPropertiesInternal(
			Set<PropertyModelKey> entityIds) {

		TimeTracker tracker = null;
		StringBuilder debugMessage = null;
		if (isTraceEnabled) {
			tracker = new TimeTracker().begin().begin();
			debugMessage = new StringBuilder(100);
		}

		// group entity IDs by entity type in here
		Map<Integer, Set<String>> argsMapping = new LinkedHashMap<>(6);

		// mapping for easy convert between DB EntityId and fully filled EntityId
		Map<PropertyModelKey, PropertyModelKey> mapping = new LinkedHashMap<>(
				(int) (entityIds.size() * 1.1), 0.95f);

		// the final result
		Map<PropertyModelKey, Map<String, Serializable>> result = new LinkedHashMap<>(
				(int) (entityIds.size() * 1.2), 1f);

		EntityLookupCache<PropertyModelKey, Map<String, Serializable>, Serializable> propertiesCache = getPropertiesCache();

		for (PropertyModelKey entityId : entityIds) {
			mapping.put(entityId, entityId);
			// check if the given entity if is found in the cache if so we does
			// not fetch it from the DB again
			Map<String, Serializable> cachedValue = propertiesCache.getValue(entityId);
			if (cachedValue == null) {
				// if not found in cache we add it to the query arguments
				// for optimization we group all entities by entity type to
				// simplify queries
				Set<String> set = argsMapping.get(entityId.getBeanType());
				if (set == null) {
					set = new LinkedHashSet<>();
					argsMapping.put(entityId.getBeanType(), set);
				}
				set.add(entityId.getBeanId());
			} else {
				// add to the result the cached value
				result.put(entityId, PropertiesUtil.cloneProperties(cachedValue));
			}
		}

		// all data is fetched from the cache so no need to continue
		if (argsMapping.isEmpty()) {
			if (isTraceEnabled) {
				LOGGER.trace("Properties for " + entityIds.size()
						+ " entities fetched from cache for " + tracker.stop() + " ms");
			}
			return result;
		} else if (isTraceEnabled) {
			debugMessage.append("Cache lookup took ").append(tracker.stop()).append(" ms.");
			tracker.begin();
		}

		// accumulate the DB result here
		List<PropertyEntity> results = new LinkedList<>();

		// REVIEW:BB NOTE: probably we can add parallel loading of properties
		// will fetch multiple results for particular type
		for (Entry<Integer, Set<String>> entry : argsMapping.entrySet()) {
			List<Pair<String, Object>> params = new ArrayList<>(2);
			params.add(new Pair<String, Object>(BEAN_ID, entry.getValue()));
			params.add(new Pair<String, Object>(BEAN_TYPE, entry.getKey()));
			List<PropertyEntity> list = dbDao.fetchWithNamed(EmfQueries.QUERY_PROPERTIES_KEY,
					params);
			if (isTraceEnabled) {
				LOGGER.trace("For beanType={} AND beanId in ({}) fetched {} results",
						entry.getKey(), entry.getValue(), list.size());
			}
			results.addAll(list);
		}

		// nothing is fetched from the DB return the current result
		if (results.isEmpty()) {
			if (isTraceEnabled) {
				debugMessage.append(" No properties found in DB for ")
						.append(tracker.stopInSeconds()).append(" s. Total time ")
						.append(tracker.stopInSeconds()).append(" s");
				LOGGER.trace(debugMessage.toString());
			}
			return result;
		} else if (isTraceEnabled) {
			debugMessage.append(" Db lookup took ").append(tracker.stopInSeconds()).append(" s.");
			tracker.begin();
		}

		if (isTraceEnabled) {
			LOGGER.trace("Total: for {} keys fetched {} results", entityIds.size(), results.size());
		}
		// organize results
		Map<PropertyModelKey, Map<PropertyKey, PropertyValue>> map = new LinkedHashMap<>(
				(int) (entityIds.size() * 1.2), 1f);
		for (PropertyEntity propertyEntity : results) {
			// convert from DB entityId to fully populated entity ID
			PropertyModelKey dbEntity = propertyEntity.getEntityId();
			PropertyModelKey localEntity = mapping.get(dbEntity);

			// fetched not needed result
			if (localEntity == null) {
				// after the optimization for fetching results this should not happen
				LOGGER.warn("DB {} not needed!!", dbEntity);
				continue;
			}

			Map<PropertyKey, PropertyValue> local = map.get(localEntity);
			if (local == null) {
				local = new LinkedHashMap<>();
				map.put(localEntity, local);
			}

			local.put(propertyEntity.getKey(), propertyEntity.getValue());
		}

		// convert the results and update the cache
		for (Entry<PropertyModelKey, Map<PropertyKey, PropertyValue>> entry : map.entrySet()) {
			Map<String, Serializable> publicProperties = nodePropertyHelper
					.convertToPublicProperties(entry.getValue(), entry.getKey().getRevision(),
							entry.getKey().getPathElement());
			// update the cache
			setNodePropertiesCached(entry.getKey(), publicProperties);

			result.put(entry.getKey(), publicProperties);
		}

		// fill the properties for not found entities
		Set<PropertyModelKey> foundEntities = new LinkedHashSet<>(entityIds);
		// we check by removing the found entries till now
		if (foundEntities.removeAll(result.keySet())) {
			if (isDebugEnabled && !foundEntities.isEmpty()) {
				LOGGER.debug("No properties information for ({}) entity ids: {}",
						foundEntities.size(), foundEntities);
			}
			for (PropertyModelKey entityId : foundEntities) {
				result.put(entityId, new LinkedHashMap<String, Serializable>());
			}
		}

		if (isTraceEnabled) {
			debugMessage.append(" Property conversion took ").append(tracker.stopInSeconds())
					.append(" s. Total properties fetch time ").append(tracker.stopInSeconds());
			LOGGER.trace(debugMessage.toString());
		}

		return result;
	}

	/**
	 * Getter method for propertiesCache.
	 *
	 * @return the propertiesCache
	 */
	private EntityLookupCache<PropertyModelKey, Map<String, Serializable>, Serializable> getPropertiesCache() {
		return cacheContext.getCache(PROPERTY_ENTITY_CACHE);
	}

	/**
	 * The Class PropertiesLookupCallback. Fetches entity properties by EntityId
	 *
	 * @author BBonev
	 */
	class PropertiesLookupCallback
			extends
			EntityLookupCallbackDAOAdaptor<PropertyModelKey, Map<String, Serializable>, Serializable> {

		@Override
		public Pair<PropertyModelKey, Map<String, Serializable>> findByKey(PropertyModelKey key) {
			List<Pair<String, Object>> args = new ArrayList<>(2);
			args.add(new Pair<String, Object>(BEAN_ID, key.getBeanId()));
			args.add(new Pair<String, Object>(BEAN_TYPE, key.getBeanType()));
			List<PropertyEntity> resultList = dbDao.fetchWithNamed(
					EmfQueries.QUERY_PROPERTIES_BY_ENTITY_ID_KEY, args);
			if (resultList.isEmpty()) {
				return null;
			}
			if (isTraceEnabled) {
				LOGGER.trace("Fetched for key " + key + " " + resultList.size() + " results");
			}
			Map<PropertyKey, PropertyValue> propertyValues = new LinkedHashMap<>(
					(int) (resultList.size() * 1.2), 1f);
			for (PropertyEntity propertyEntity : resultList) {
				propertyValues.put(propertyEntity.getKey(), propertyEntity.getValue());
			}
			if (isTraceEnabled) {
				LOGGER.trace("Returning unique results " + propertyValues.size());
			}
			Map<String, Serializable> publicProperties = nodePropertyHelper
					.convertToPublicProperties(propertyValues, key.getRevision(),
							key.getPathElement());
			return new Pair<>(key, Collections.unmodifiableMap(publicProperties));
		}

		@Override
		public Pair<PropertyModelKey, Map<String, Serializable>> createValue(
				Map<String, Serializable> value) {
			throw new UnsupportedOperationException("A node always has a 'map' of properties.");
		}

		@Override
		public int deleteByKey(PropertyModelKey key) {
			List<Pair<String, Object>> args = new ArrayList<>(2);
			args.add(new Pair<String, Object>(BEAN_ID, key.getBeanId()));
			args.add(new Pair<String, Object>(BEAN_TYPE, key.getBeanType()));
			int update = dbDao.executeUpdate(EmfQueries.DELETE_ALL_PROPERTIES_FOR_BEAN_KEY, args);
			if (isDebugEnabled) {
				LOGGER.debug("Removed {} properties for {}", update, key);
			}
			return update;
		}

	}
}
