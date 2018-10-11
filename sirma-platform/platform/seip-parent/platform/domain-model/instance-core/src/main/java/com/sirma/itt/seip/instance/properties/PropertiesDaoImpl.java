package com.sirma.itt.seip.instance.properties;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.properties.entity.NodePropertyHelper;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.seip.util.EqualsHelper.MapValueComparison;
import com.sirma.itt.seip.util.LoggingUtil;

/**
 * Default implementation of properties DAO.
 *
 * @author BBonev
 */
@Singleton
public class PropertiesDaoImpl implements PropertiesDao {

	private static final String NO_PROPERTIES_SUPPORT_FOR = "No properties support for {}";
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesDaoImpl.class);

	/** The allowed length of property value to print */
	private static final int PROPERTY_PRINT_LENGHT = 150;

	@Inject
	private NodePropertyHelper nodePropertyHelper;

	@Override
	public <E extends PropertyModel> void saveProperties(E model, PropertyModelCallback<E> callback,
			PropertiesStorageAccess access) {
		savePropertiesInternal(model, false, callback, access);
	}

	@Override
	public <E extends PropertyModel> void saveProperties(E model, boolean addOnly, PropertyModelCallback<E> callback,
			PropertiesStorageAccess access) {
		savePropertiesInternal(model, addOnly, callback, access);
	}

	private <E extends PropertyModel> void savePropertiesInternal(E model, boolean addOnly,
			PropertyModelCallback<E> callback, PropertiesStorageAccess access) {
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
			setPropertiesImpl(entry.getKey(), target.getProperties(), addOnly, target, access);
		}
	}

	@Override
	public <E extends PropertyModel> void loadProperties(E model, PropertyModelCallback<E> callback,
			PropertiesStorageAccess access) {
		loadPropertiesInternal(Collections.singletonList(model), callback, access);
	}

	@Override
	public <E extends PropertyModel> void loadProperties(List<E> models, PropertyModelCallback<E> callback,
			PropertiesStorageAccess access) {
		loadPropertiesInternal(models, callback, access);
	}

	private <E extends PropertyModel> void loadPropertiesInternal(List<E> models, PropertyModelCallback<E> callback,
			PropertiesStorageAccess access) {
		if (models.isEmpty()) {
			return;
		}

		Map<PropertyModelKey, Object> loadedModels = new LinkedHashMap<>();
		for (PropertyModel propertyModel : models) {
			Map<PropertyModelKey, Object> model = callback.getModel((E) propertyModel);
			for (Entry<PropertyModelKey, Object> entry : model.entrySet()) {
				setValueToModel(loadedModels, entry.getKey(), entry.getValue());
			}
			loadedModels.putAll(model);
		}

		callback.updateModel(loadedModels, loadPropertiesInternal(loadedModels.keySet(), access));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void setValueToModel(Map<PropertyModelKey, Object> loadedModels, PropertyModelKey key,
			Object newValue) {
		Object oldValue = loadedModels.get(key);
		Object toSet = null;
		if (oldValue == null) {
			toSet = newValue;
		} else if (oldValue instanceof List) {
			List<?> list = (List<?>) oldValue;
			toSet = addValueToList(newValue, list);
		} else {
			List list = new LinkedList<>();
			list.add(oldValue);
			toSet = addValueToList(newValue, list);
		}
		if (toSet != null) {
			loadedModels.put(key, toSet);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object addValueToList(Object valueToAdd, List list) {
		Object toSet;
		if (valueToAdd instanceof Collection) {
			list.addAll((Collection<?>) valueToAdd);
		} else {
			list.add(valueToAdd);
		}
		toSet = list;
		return toSet;
	}

	@Override
	public Map<String, Serializable> getEntityProperties(Entity entity, PathElement path, PropertyModelCallback<PropertyModel> callback,
			PropertiesStorageAccess access) {
		PropertyModelKey entityId = callback.createModelKey(entity);
		if (entityId == null) {
			LOGGER.warn(NO_PROPERTIES_SUPPORT_FOR, entity.getClass());
			return Collections.emptyMap();
		}
		entityId.setPathElement(path);
		return getNodePropertiesCached(entityId, access);
	}

	@Override
	public void removeProperties(Entity entity, PathElement path, PropertyModelCallback<PropertyModel> callback,
			PropertiesStorageAccess access) {
		PropertyModelKey entityId = callback.createModelKey(entity);
		if (entityId == null) {
			LOGGER.warn(NO_PROPERTIES_SUPPORT_FOR, entity.getClass());
			return;
		}
		entityId.setPathElement(path);

		access.getCache().deleteByKey(entityId);
	}

	@Override
	public void saveProperties(Entity entity, PathElement path, Map<String, Serializable> properties, PropertyModelCallback<PropertyModel> callback,
			PropertiesStorageAccess access) {
		savePropertiesInternal(entity, path, properties, false, callback, access);
	}

	@Override
	public void saveProperties(Entity entity, PathElement path, Map<String, ? extends Serializable> properties, boolean addOnly,
			PropertyModelCallback<PropertyModel> callback, PropertiesStorageAccess access) {
		savePropertiesInternal(entity, path, properties, addOnly, callback, access);
	}

	@SuppressWarnings("rawtypes")
	private void savePropertiesInternal(Entity entity, PathElement path, Map<String, ? extends Serializable> properties,
			boolean addOnly, PropertyModelCallback<PropertyModel> callback, PropertiesStorageAccess access) {
		PropertyModelKey key = callback.createModelKey(entity);
		if (key == null) {
			LOGGER.warn(NO_PROPERTIES_SUPPORT_FOR, entity.getClass());
			return;
		}
		key.setPathElement(path);

		PropertyModel model = null;
		if (entity instanceof PropertyModel) {
			model = (PropertyModel) entity;
		}
		setPropertiesImpl(key, properties, addOnly, model, access);
	}

	/**
	 * Does differencing to add and/or remove properties. Internally, the existing properties will be retrieved and a
	 * difference performed to work out which properties need to be created, updated or deleted. It is only necessary to
	 * pass in old and new values for <i>changes</i> i.e. when setting a single property, it is only necessary to pass
	 * that property's value in the <b>old</b> and </b>new</b> maps; this improves execution speed significantly -
	 * although it has no effect on the number of resulting DB operations.
	 * <p/>
	 * Note: The cached properties are not updated
	 *
	 * @param entityId
	 *            the node ID
	 * @param newProps
	 *            the properties to add or update
	 * @param isAddOnly
	 *            <tt>true</tt> if the new properties are just an update or <tt>false</tt> if the properties are a
	 *            complete set
	 * @param model
	 *            the target model that holds the provided properties
	 * @param access
	 *            accessor used to access the properties store
	 * @return Returns <tt>true</tt> if any properties were changed
	 */
	private boolean setPropertiesImpl(PropertyModelKey entityId, Map<String, ? extends Serializable> newProps,
			boolean isAddOnly, PropertyModel model, PropertiesStorageAccess access) {
		if (isAddOnly && newProps.isEmpty()) {
			// No point adding nothing
			return false;
		}

		// Copy inbound values
		Map<String, Serializable> newPropsLocal = new LinkedHashMap<>(newProps);

		// Remove properties that should not be updated from the user
		access.filterOutForbiddenProperties(newPropsLocal.keySet());

		// Load the current properties.
		// This means that we have to go to the DB during cold-write operations,
		// but usually a write occurs after a node has been fetched of viewed in
		// some way by the client code. Loading the existing properties has the
		// advantage that the differencing code can eliminate unnecessary writes
		// completely.
		Map<String, Serializable> oldPropsCached = getNodePropertiesCached(entityId, access);
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
		Map<PropertyEntryKey, PropertyModelValue> newPropsRaw = nodePropertyHelper
				.convertToPersistentProperties(newPropsLocal, entityId.getPathElement(), access);
		newPropsLocal = nodePropertyHelper.convertToPublicProperties(newPropsRaw);
		// Now find out what's changed
		Map<String, MapValueComparison> diff = EqualsHelper.getMapComparison(oldProps, newPropsLocal);
		// Keep track of properties to delete and add
		Map<String, Serializable> propsToDelete = new LinkedHashMap<>(oldProps.size() * 2);
		Map<String, Serializable> propsToAdd = new LinkedHashMap<>(newPropsLocal.size() * 2);

		for (Map.Entry<String, MapValueComparison> entry : diff.entrySet()) {
			distributePropertiesBasedOnDiff(newPropsLocal, oldProps, propsToDelete, propsToAdd, entry.getKey(),
					entry.getValue());
		}

		boolean updated = !propsToDelete.isEmpty() || !propsToAdd.isEmpty();

		Map<String, Serializable> actualProperties = calculateActualProperties(entityId, isAddOnly, access,
				newPropsLocal, oldPropsCached, oldProps, diff, propsToDelete, propsToAdd, updated);
		// Touch to bring into current transaction
		if (updated) {
			access.notifyForChanges(model, propsToDelete, propsToAdd, actualProperties);
		}
		// Done
		if (updated && LOGGER.isDebugEnabled()) {
			LOGGER.debug("Modified node properties: {}\n   Removed: {}\n   Added:   {}", entityId,
					printUserFriendly(propsToDelete), printUserFriendly(propsToAdd));
		}
		return updated;
	}

	// disable rule for arguments number
	@SuppressWarnings("squid:S00107")
	private Map<String, Serializable> calculateActualProperties(PropertyModelKey entityId, boolean isAddOnly,
			PropertiesStorageAccess access, Map<String, Serializable> newPropsLocal,
			Map<String, Serializable> oldPropsCached, Map<String, Serializable> oldProps,
			Map<String, MapValueComparison> diff, Map<String, Serializable> propsToDelete,
			Map<String, Serializable> propsToAdd, boolean updated) {

		Map<PropertyEntryKey, PropertyModelValue> newPropsRaw;
		Map<String, Serializable> actualProperties = null;

		if (updated) {
			try {
				// Apply deletes
				deleteNodeProperties(entityId, propsToDelete, access);
				// Now create the raw properties for adding
				newPropsRaw = nodePropertyHelper.convertToPersistentProperties(propsToAdd, entityId.getPathElement(),
						access);
				insertNodeProperties(entityId, newPropsRaw, access);
			} catch (Exception e) {
				// Focused error
				throw new EmfRuntimeException("Failed to write property deltas: \n" + "  Node:          " + entityId
						+ "\n" + "  Old:           " + oldProps + "\n" + "  New:           " + newPropsLocal + "\n"
						+ "  Diff:          " + diff + "\n" + "  Delete Tried:  " + propsToDelete + "\n"
						+ "  Add Tried:     " + propsToAdd, e);
			}

			Map<String, Serializable> propsToCache = prepareForCaching(isAddOnly, newPropsLocal, oldPropsCached,
					propsToAdd);
			// Update cache
			actualProperties = setNodePropertiesCached(entityId, propsToCache, access);
			// XXX: this should be defined somewhere - EntityIdType.INSTANCE.getType()
		} else if (entityId.getBeanType() == 7) {
			actualProperties = setNodePropertiesCached(entityId, newPropsLocal, access);
		}
		return actualProperties;
	}

	private void deleteNodeProperties(PropertyModelKey entityId, Map<String, Serializable> propsToDelete,
			PropertiesStorageAccess access) {
		Set<Long> propertiesToDelete = nodePropertyHelper
				.convertToPersistentProperties(propsToDelete, entityId.getPathElement(), access)
					.keySet()
					.stream()
					.map(PropertyEntryKey::getPropertyId)
					.collect(toSet());

		if (!propertiesToDelete.isEmpty()) {
			access.deleteProperties(entityId, propertiesToDelete);
		}
	}

	private static Map<String, Serializable> prepareForCaching(boolean isAddOnly,
			Map<String, Serializable> newPropsLocal, Map<String, Serializable> oldPropsCached,
			Map<String, Serializable> propsToAdd) {
		// Build the properties to cache based on whether this is an append
		// or replace
		Map<String, Serializable> propsToCache;
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
		return propsToCache;
	}

	private static void distributePropertiesBasedOnDiff(Map<String, Serializable> newPropsLocal,
			Map<String, Serializable> oldProps, Map<String, Serializable> propsToDelete,
			Map<String, Serializable> propsToAdd, String qname, MapValueComparison value) {
		switch (value) {
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
				propsToAdd.put(qname, newPropsLocal.get(qname));
				break;
			case RIGHT_ONLY:
				// We're adding this
				propsToAdd.put(qname, newPropsLocal.get(qname));
				break;
			default:
				throw new IllegalStateException("Unknown MapValueComparison: " + value);
		}
	}

	private static String printUserFriendly(Map<String, Serializable> map) {
		StringBuilder builder = new StringBuilder(1024);
		builder.append('{');
		for (Iterator<Entry<String, Serializable>> it = map.entrySet().iterator(); it.hasNext();) {
			Entry<String, Serializable> entry = it.next();
			builder.append(entry.getKey()).append("=");
			if (entry.getValue() == null) {
				builder.append("null");
			} else {
				String value = entry.getValue().toString();
				builder.append(LoggingUtil.shorten(value, PROPERTY_PRINT_LENGHT));
			}
			if (it.hasNext()) {
				builder.append(", ");
			}
		}
		builder.append('}');
		return builder.toString();
	}

	private static Map<String, Serializable> setNodePropertiesCached(PropertyModelKey entityId,
			Map<String, Serializable> properties, PropertiesStorageAccess access) {
		Map<String, Serializable> actualProperties = Collections
				.unmodifiableMap(PropertiesUtil.cloneProperties(properties));
		access.getCache().setValue(entityId, actualProperties);
		return actualProperties;
	}

	private static void insertNodeProperties(PropertyModelKey entityId,
			Map<PropertyEntryKey, PropertyModelValue> newPropsRaw, PropertiesStorageAccess access) {
		if (newPropsRaw.isEmpty()) {
			return;
		}
		access.insertProperties(entityId, newPropsRaw);
	}

	private static Map<String, Serializable> getNodePropertiesCached(PropertyModelKey entityId,
			PropertiesStorageAccess access) {
		Pair<PropertyModelKey, Map<String, Serializable>> cacheEntry = access.getCache().getByKey(entityId);
		if (cacheEntry == null) {
			// when the node is newly created and not persisted, yet. then we have no properties
			return new LinkedHashMap<>();
		}
		Map<String, Serializable> cachedProperties = cacheEntry.getSecond();

		return PropertiesUtil.cloneProperties(cachedProperties);
	}

	/**
	 * Load properties for all given entity IDs in one query.
	 *
	 * @param entityIds
	 *            the entity ids
	 * @param access
	 *            accessor used to access the properties store
	 * @return the map
	 */
	private Map<PropertyModelKey, Map<String, Serializable>> loadPropertiesInternal(Set<PropertyModelKey> entityIds,
			PropertiesStorageAccess access) {

		// group entity IDs by entity type in here
		Map<Integer, Set<String>> argsMapping = new LinkedHashMap<>(6);

		// mapping for easy convert between DB EntityId and fully filled EntityId
		Map<PropertyModelKey, PropertyModelKey> mapping = CollectionUtils.createLinkedHashMap(entityIds.size());

		// the final result
		Map<PropertyModelKey, Map<String, Serializable>> result = CollectionUtils.createLinkedHashMap(entityIds.size());

		groupEntitiesByType(entityIds, argsMapping, mapping, result, access);

		// all data is fetched from the cache so no need to continue
		if (argsMapping.isEmpty()) {
			return result;
		}

		// accumulate the DB result here
		List<PropertyModelEntity> results = new LinkedList<>();

		// REVIEW:BB NOTE: probably we can add parallel loading of properties
		// will fetch multiple results for particular type
		for (Entry<Integer, Set<String>> entry : argsMapping.entrySet()) {
			List<PropertyModelEntity> list = access.batchLoadProperties(entry.getKey(), entry.getValue());
			results.addAll(list);
		}

		// nothing is fetched from the DB return the current result
		if (results.isEmpty()) {
			return result;
		}
		// organize results
		Map<PropertyModelKey, Map<PropertyEntryKey, PropertyModelValue>> map = organizeLoadedEntries(entityIds, mapping,
				results);

		// convert the results and update the cache
		convertAndUpdateCache(access, result, map);

		// fill the properties for not found entities
		processNotFoundInstanceProperties(entityIds, result);
		return result;
	}

	private static void processNotFoundInstanceProperties(Set<PropertyModelKey> entityIds,
			Map<PropertyModelKey, Map<String, Serializable>> result) {
		Set<PropertyModelKey> foundEntities = new LinkedHashSet<>(entityIds);
		// we check by removing the found entries till now
		if (foundEntities.removeAll(result.keySet())) {
			if (!foundEntities.isEmpty() && LOGGER.isDebugEnabled()) {
				LOGGER.debug("No properties information for ({}) entity ids: {}", foundEntities.size(), foundEntities);
			}
			for (PropertyModelKey entityId : foundEntities) {
				result.put(entityId, new LinkedHashMap<>());
			}
		}
	}

	private void convertAndUpdateCache(PropertiesStorageAccess access,
			Map<PropertyModelKey, Map<String, Serializable>> result,
			Map<PropertyModelKey, Map<PropertyEntryKey, PropertyModelValue>> map) {
		for (Entry<PropertyModelKey, Map<PropertyEntryKey, PropertyModelValue>> entry : map.entrySet()) {
			Map<String, Serializable> publicProperties = nodePropertyHelper.convertToPublicProperties(entry.getValue());
			// update the cache
			setNodePropertiesCached(entry.getKey(), publicProperties, access);

			result.put(entry.getKey(), publicProperties);
		}
	}

	private static Map<PropertyModelKey, Map<PropertyEntryKey, PropertyModelValue>> organizeLoadedEntries(
			Set<PropertyModelKey> entityIds, Map<PropertyModelKey, PropertyModelKey> mapping,
			List<PropertyModelEntity> results) {
		Map<PropertyModelKey, Map<PropertyEntryKey, PropertyModelValue>> map = CollectionUtils
				.createLinkedHashMap(entityIds.size());
		for (PropertyModelEntity propertyEntity : results) {
			// convert from DB entityId to fully populated entity ID
			PropertyModelKey dbEntity = propertyEntity.getEntityId();
			PropertyModelKey localEntity = mapping.get(dbEntity);

			// fetched not needed result
			if (localEntity == null) {
				// after the optimization for fetching results this should not happen
				LOGGER.warn("DB {} not needed!!", dbEntity);
				continue;
			}

			map.computeIfAbsent(localEntity, key -> new LinkedHashMap<>()).put(propertyEntity.getKey(),
					propertyEntity.getValue());
		}
		return map;
	}

	private static void groupEntitiesByType(Set<PropertyModelKey> entityIds, Map<Integer, Set<String>> argsMapping,
			Map<PropertyModelKey, PropertyModelKey> mapping, Map<PropertyModelKey, Map<String, Serializable>> result,
			PropertiesStorageAccess access) {
		EntityLookupCache<PropertyModelKey, Map<String, Serializable>, Serializable> propertiesCache = access
				.getCache();
		for (PropertyModelKey entityId : entityIds) {
			mapping.put(entityId, entityId);
			// check if the given entity if is found in the cache if so we does
			// not fetch it from the DB again
			Map<String, Serializable> cachedValue = propertiesCache.getValue(entityId);
			if (cachedValue == null) {
				// if not found in cache we add it to the query arguments
				// for optimization we group all entities by entity type to
				// simplify queries
				Set<String> set = argsMapping.computeIfAbsent(entityId.getBeanType(), k -> new LinkedHashSet<>());
				set.add(entityId.getBeanId());
			} else {
				// add to the result the cached value
				result.put(entityId, PropertiesUtil.cloneProperties(cachedValue));
			}
		}
	}
}
