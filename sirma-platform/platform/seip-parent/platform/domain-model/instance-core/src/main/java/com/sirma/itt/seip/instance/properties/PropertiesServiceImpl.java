package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.mapping.ObjectMapper;

/**
 * Implementation of the public service for persisting and retrieving properties.
 *
 * @author BBonev
 */
public class PropertiesServiceImpl implements PropertiesService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesService.class);
	private static final boolean TRACE = LOGGER.isTraceEnabled();

	private Map<Class<?>, PropertyModelCallback<? extends PropertyModel>> callbackMapping;

	private PropertiesDao propertiesDao;
	private PropertyModelCallback<PropertyModel> defaultInstanceCallback;
	private Instance<PropertyModelCallback<? extends PropertyModel>> callbacks;
	private Instance<InstanceDao> instanceDao;
	private PropertiesStorageAccess storageAccess;
	private DatabaseIdManager idManager;
	private InstanceService instanceService;
	private ObjectMapper mapper;

	/**
	 * Instantiates a new properties service impl.
	 *
	 * @param storageAccess
	 *            the storage access
	 * @param propertiesDao
	 *            the properties dao
	 * @param defaultInstanceCallback
	 *            the default instance callback
	 * @param callbacks
	 *            the callbacks
	 * @param instanceDao
	 *            the instance dao
	 * @param idManager
	 *            the id manager
	 * @param instanceService
	 *            used to resolve full instance data
	 * @param mapper
	 *            used to transfer mapped object properties
	 */
	PropertiesServiceImpl(PropertiesStorageAccess storageAccess, PropertiesDao propertiesDao, // NOSONAR
			PropertyModelCallback<PropertyModel> defaultInstanceCallback,
			Instance<PropertyModelCallback<? extends PropertyModel>> callbacks,
			Instance<InstanceDao> instanceDao, DatabaseIdManager idManager,
			InstanceService instanceService, ObjectMapper mapper) {
		this.storageAccess = storageAccess;
		this.propertiesDao = propertiesDao;
		this.defaultInstanceCallback = defaultInstanceCallback;
		this.callbacks = callbacks;
		this.instanceDao = instanceDao;
		this.idManager = idManager;
		this.instanceService = instanceService;
		this.mapper = mapper;
		init();
	}

	/**
	 * Initialize some properties.
	 */
	private void init() {
		callbackMapping = CollectionUtils.createHashMap(30);

		for (PropertyModelCallback<? extends PropertyModel> modelCallback : callbacks) {
			Set<Class<?>> supportedObjects = modelCallback.getSupportedObjects();
			for (Class<?> supportedObject : supportedObjects) {
				if (callbackMapping.containsKey(supportedObject)) {
					throw new EmfConfigurationException("Ambiguous property model callback: " + supportedObject
							+ " already defined by " + callbackMapping.get(supportedObject).getClass());
				}
				if (TRACE) {
					LOGGER.trace("Registering {} to {}", supportedObject, modelCallback.getClass());
				}
				callbackMapping.put(supportedObject, modelCallback);
			}
		}
	}

	@Override
	public <E extends PropertyModel> boolean isModelSupported(E instance) {
		try {
			PropertyModelCallback<E> callback = getCallback(instance, null, true, false);
			return callback != null;
		} catch (RuntimeException e) {
			LOGGER.error("Failed to get property model", e);
		}
		return false;
	}

	@Override
	public Map<String, Serializable> getEntityProperties(Entity entity, PathElement path) {
		if (TRACE) {
			LOGGER.trace("Loading properties for {} : {}", entity.getClass().getSimpleName(), entity.getId());
		}
		return propertiesDao.getEntityProperties(entity, path, getCallback(null, entity, true, true), storageAccess);
	}

	@Override
	public void removeProperties(Entity entity, PathElement path) {
		if (TRACE) {
			LOGGER.trace("Deleting properties for {} : {}", entity.getClass().getSimpleName(), entity.getId());
		}
		propertiesDao.removeProperties(entity, path, getCallback(null, entity, true, true), storageAccess);
	}

	@Override
	public void saveProperties(Entity entity, PathElement path, Map<String, Serializable> properties) {
		saveProperties(entity, path, properties, false);
	}

	@Override
	public void saveProperties(Entity entity, PathElement path, Map<String, ? extends Serializable> properties,
			boolean addOnly) {
		if (TRACE) {
			LOGGER.trace("Saving properties with mode={} for {} : ", addOnly ? "MERGE" : "REPLACE",
					entity.getClass().getSimpleName(), entity.getId());
		}

		propertiesDao.saveProperties(entity, path, properties, addOnly, getCallback(null, entity, true, true),
				storageAccess);
	}

	@Override
	public <E extends PropertyModel> void saveProperties(E instance) {
		saveProperties(instance, false);
	}

	@Override
	public <E extends PropertyModel> void saveProperties(E instance, boolean addOnly) {
		saveProperties(instance, addOnly, false);
	}

	/**
	 * Save properties.
	 *
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 * @param addOnly
	 *            the add only
	 * @param saveFullGraph
	 *            the save full graph
	 */
	@Override
	public <E extends PropertyModel> void saveProperties(E instance, boolean addOnly, boolean saveFullGraph) {
		if (TRACE) {
			LOGGER.trace("Saving properties with mode={}{} for {}", addOnly ? "MERGE" : "REPLACE",
					saveFullGraph ? "_FULL" : "_BASE", instance.getClass().getSimpleName());
		}
		prepareForPropertiesPersist(instance.getProperties());

		propertiesDao.saveProperties(instance, addOnly, getCallback(instance, null, saveFullGraph, true),
				storageAccess);
	}

	@Override
	public <E extends PropertyModel> void loadProperties(E instance) {
		propertiesDao.loadProperties(instance, getCallback(instance, null, true, true), storageAccess);
	}

	@Override
	public <E extends PropertyModel> void loadProperties(E instance, boolean loadAll) {
		propertiesDao.loadProperties(instance, getCallback(instance, null, loadAll, true), storageAccess);
	}

	@Override
	public <E extends PropertyModel> void loadProperties(List<E> instances) {
		loadPropertiesInternal(instances, true);
	}

	@Override
	public <E extends PropertyModel> void loadProperties(List<E> instances, boolean loadAll) {
		loadPropertiesInternal(instances, loadAll);
	}

	/**
	 * Load properties internal.
	 *
	 * @param <E>
	 *            the element type
	 * @param instances
	 *            the instances
	 * @param loadAll
	 *            the load all
	 */
	private <E extends PropertyModel> void loadPropertiesInternal(List<E> instances, boolean loadAll) {
		if (instances.isEmpty()) {
			return;
		}
		if (TRACE) {
			Set<Serializable> list = CollectionUtils.createLinkedHashSet(instances.size());
			for (E instance : instances) {
				list.add(((Entity<?>) instance).getId());
			}
			LOGGER.trace("Loading properties for {} ({}): {}", instances.get(0).getClass().getSimpleName(),
					list.size(), list);
		}
		propertiesDao.loadProperties(instances, getCallback(instances.get(0), null, loadAll, true), storageAccess);
		if (TRACE) {
			LOGGER.trace("Loaded properties for instances: {}", instances);
		}
	}

	@Override
	public <I extends com.sirma.itt.seip.domain.instance.Instance> void loadPropertiesBatch(Collection<I> instances) {
		List<Serializable>  ids = instances.stream().map(Entity::getId).distinct().collect(Collectors.toList());
		List<com.sirma.itt.seip.domain.instance.Instance> loadedInstances = instanceService.loadByDbId(ids);
		Map<Serializable, com.sirma.itt.seip.domain.instance.Instance> mapping = loadedInstances.stream()
				.collect(CollectionUtils.toIdentityMap(Entity::getId));
		for (I instance : instances) {
			com.sirma.itt.seip.domain.instance.Instance loaded = mapping.get(instance.getId());
			if (loaded != null) {
				com.sirma.itt.seip.domain.instance.Instance cleaned = PropertiesUtil.cleanNullProperties(loaded);
				instance.addAllProperties(cleaned.getProperties());
				// set correct information for the instance so that definition and parent resolving could happen
				syncInstances(instance, loaded);
			}
		}
	}

	private <I extends com.sirma.itt.seip.domain.instance.Instance> void syncInstances(I instance,
			com.sirma.itt.seip.domain.instance.Instance loaded) {
		if (StringUtils.isBlank(instance.getIdentifier())) {
			mapper.map(loaded, instance);
		}
	}

	/**
	 * Gets the callback for the given instance.
	 *
	 * @param <E>
	 *            the instance type
	 * @param instance
	 *            the instance to get the callback for (can be <code>null</code> but then the entity should be provided)
	 * @param entity
	 *            the entity to get the callback for it no instance is present (optional if instance is present)
	 * @param loadAll
	 *            the load all properties
	 * @param fail
	 *            if the method should fail if no callback is found
	 * @return the callback
	 */
	@SuppressWarnings("unchecked")
	private  <E extends PropertyModel> PropertyModelCallback<E> getCallback(E instance, Entity<?> entity,
			boolean loadAll, boolean fail) {
		Object target = instance;
		if (target == null) {
			if (entity != null) {
				target = entity;
			} else {
				return failGettingTheCallback(fail, "Cannot get callback for null instance!");
			}
		}
		if (!loadAll && defaultInstanceCallback.canHandle(target)) {
			return (PropertyModelCallback<E>) defaultInstanceCallback;
		}
		Class<?> targetClass = target.getClass();
		PropertyModelCallback<? extends PropertyModel> callback = callbackMapping.get(targetClass);
		if (callback == null) {
			callback = findCompatibleCallback(target, targetClass);
		}
		if (callback == null) {
			return failGettingTheCallback(fail,
					"The entity of type " + targetClass + " is not supported for properties saving!");
		}
		return (PropertyModelCallback<E>) callback;
	}

	/**
	 * Fail getting the callback.
	 *
	 * @param <E>
	 *            the element type
	 * @param fail
	 *            the fail
	 * @param message
	 *            the message
	 * @return the property model callback
	 */
	private static <E extends PropertyModel> PropertyModelCallback<E> failGettingTheCallback(boolean fail,
			String message) {
		if (fail) {
			throw new EmfRuntimeException(message);
		}
		return null;
	}

	/**
	 * Find compatible callback.
	 *
	 * @param target
	 *            the target
	 * @param targetClass
	 *            the target class
	 * @return the property model callback
	 */
	private PropertyModelCallback<? extends PropertyModel> findCompatibleCallback(Object target, Class<?> targetClass) {
		if (TRACE) {
			LOGGER.trace("No callback registered for {}. Will try to lookup one.", targetClass);
		}
		PropertyModelCallback<? extends PropertyModel> localCallback = null;
		for (PropertyModelCallback<? extends PropertyModel> modelCallback : callbacks) {
			if (modelCallback.canHandle(target)) {
				// update the mapping model with the new discovery
				callbackMapping.put(targetClass, modelCallback);
				if (TRACE) {
					LOGGER.trace("Registering {} to {}", targetClass, modelCallback.getClass());
				}
				localCallback = modelCallback;
				break;
			}
		}
		return localCallback;
	}

	/**
	 * Checks for no persisted custom objects of type {@link CommonInstance}. If not already saved we will save them
	 *
	 * @param properties
	 *            the properties
	 */
	private void prepareForPropertiesPersist(Map<String, Serializable> properties) {
		if (properties == null || properties.isEmpty()) {
			return;
		}
		for (Serializable serializable : properties.values()) {
			if (serializable instanceof CommonInstance) {
				saveCommonInstanceIfNeeded((CommonInstance) serializable);
			} else if (serializable instanceof Collection) {
				prepareForPropertiesPersistCollection((Collection<?>) serializable);
			}
		}
	}

	/**
	 * Prepare for properties persist collection.
	 *
	 * @param serializable
	 *            the serializable
	 */
	private void prepareForPropertiesPersistCollection(Collection<?> serializable) {
		for (Object object : serializable) {
			if (object instanceof CommonInstance) {
				saveCommonInstanceIfNeeded((CommonInstance) object);
			} else {
				// no need to iterate more if the objects are not common instance. We does
				// not support non heterogeneous collections
				break;
			}
		}
	}

	/**
	 * Saves the given common instance if needed.
	 *
	 * @param instance
	 *            the instance
	 */
	private void saveCommonInstanceIfNeeded(CommonInstance instance) {
		if (instance == null) {
			return;
		}
		if (!idManager.isPersisted(instance)) {
			instanceDao.get().saveEntity(instance);
		}
		// we check for more properties on the go
		prepareForPropertiesPersist(instance.getProperties());
	}

}
