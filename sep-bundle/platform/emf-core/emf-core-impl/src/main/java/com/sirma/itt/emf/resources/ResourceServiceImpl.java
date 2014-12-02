/*
 *
 */
package com.sirma.itt.emf.resources;

import java.io.Serializable;
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
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.BaseEntityLookupDao;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Triplet;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.ShortUri;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.dozer.DozerMapper;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.resource.ResourcesChangedEvent;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader.BatchPrimaryKeyEntityLoaderCallback;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader.BatchSecondaryKeyEntityLoaderCallback;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.resources.entity.ResourceEntity;
import com.sirma.itt.emf.resources.entity.ResourceRoleEntity;
import com.sirma.itt.emf.resources.event.AttachedChildToResourceEvent;
import com.sirma.itt.emf.resources.event.ResourceAddedEvent;
import com.sirma.itt.emf.resources.event.ResourcePersistedEvent;
import com.sirma.itt.emf.resources.event.ResourceUpdatedEvent;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.security.RoleService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.serialization.SerializationUtil;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Default project service implementation.
 * 
 * @author BBonev
 */
@Stateless
public class ResourceServiceImpl implements ResourceService {

	private static final String ROLE = "role";
	private static final String SOURCE_TYPE = "sourceType";
	private static final String SOURCE_ID = "sourceId";

	/** The Constant RESOURCE_ENTITY_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Cache used to store the information for all registered users in the system no matter if they are active or not."
			+ "For every user there are 2 entries in the cache. "
			+ "<br>Minimal value expression: users * 2.2"))
	public static final String RESOURCE_ENTITY_CACHE = "RESOURCE_ENTITY_CACHE";

	/** The Constant RESOURCE_ROLE_ENTITY_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Cache used to store the information for all user assigments/roles per."
			+ "For every loaded role there are 2 entries in the cache. "
			+ "<br>Minimal value expression: users * 50"))
	public static final String RESOURCE_ROLE_ENTITY_CACHE = "RESOURCE_ROLE_ENTITY_CACHE";

	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;

	/** The mapper. */
	@Inject
	protected DozerMapper mapper;

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The resource providers. */
	@Inject
	@ExtensionPoint(value = ResourceProviderExtension.TARGET_NAME)
	private Iterable<ResourceProviderExtension> resourceProviders;

	/** The properties service. */
	@Inject
	private PropertiesService propertiesService;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The role service. */
	@Inject
	private RoleService roleService;

	/** The hash calculator. */
	@Inject
	private HashCalculator hashCalculator;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The runtime roles. */
	private Map<Triplet<Serializable, Class<?>, Serializable>, ResourceRole> runtimeRoles = CollectionUtils
			.createHashMap(5000);

	/** The secondary key callback. */
	private BatchSecondaryKeyEntityLoaderCallback<Serializable, ResourceEntity, Pair<String, ResourceType>> secondaryKeyCallback = new ResourceLoaderBySecondaryKey();

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		if (!cacheContext.containsCache(RESOURCE_ENTITY_CACHE)) {
			EntityLookupCache<String, ResourceEntity, Pair<String, ResourceType>> cache = cacheContext
					.createCache(RESOURCE_ENTITY_CACHE, new ResourceLookupDao());
			initializeCache(cache);
		}
		if (!cacheContext.containsCache(RESOURCE_ROLE_ENTITY_CACHE)) {
			cacheContext.createCache(RESOURCE_ROLE_ENTITY_CACHE, new ResourceRoleLookupDao());
		}
	}

	/**
	 * Initialize resources cache.
	 * 
	 * @param cache
	 *            the cache
	 */
	private void initializeCache(
			EntityLookupCache<String, ResourceEntity, Pair<String, ResourceType>> cache) {
		List<ResourceEntity> list = dbDao.fetchWithNamed(EmfQueries.QUERY_ALL_RESOURCES_KEY,
				Collections.<Pair<String, Object>> emptyList());
		for (ResourceEntity entity : list) {
			cache.setValue(entity.getId(), entity);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <R extends Resource> ResourceRole assignResource(R resource, RoleIdentifier role,
			Instance instance) {
		InstanceReference reference = convertToReference(instance);
		List<Resource> resources = getResources(instance);
		ResourceRole resourceRole = addOrUpdateResourceInternal(resource, role, reference);

		List<Resource> currentResourcesAssigned = new ArrayList<Resource>(resources);
		currentResourcesAssigned.add(resource);
		ResourcesChangedEvent event = new ResourcesChangedEvent(instance, currentResourcesAssigned,
				new ArrayList<>(resources));
		eventService.fire(event);
		return resourceRole;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ResourceRole assignResource(String id, ResourceType type, RoleIdentifier role,
			Instance instance) {
		Resource resource = getResource(id, type);
		return assignResource(resource, role, instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResources(Map<String, RoleIdentifier> roles, Instance instance) {
		List<Resource> batchFetchResourcesInternal = batchFetchResourcesInternal(roles.keySet(),
				false);
		Map<Resource, RoleIdentifier> authorityRoles = new HashMap<Resource, RoleIdentifier>(
				roles.size());
		for (Resource resource : batchFetchResourcesInternal) {
			authorityRoles.put(resource, roles.get(resource.getId()));
		}
		Pair<List<ResourceRole>, ResourcesChangedEvent> updateResourcesInternal = updateResourcesInternal(
				authorityRoles, instance, true);

		eventService.fire(updateResourcesInternal.getSecond());

	}

	/**
	 * Internal add/set resources to instance.
	 * 
	 * @param authorityRoles
	 *            the roles to assign
	 * @param instance
	 *            the instance to assign to
	 * @param setMode
	 *            whether this is setMode or only add
	 * @return the list of current assigned roles assigned.
	 */
	private Pair<List<ResourceRole>, ResourcesChangedEvent> updateResourcesInternal(
			Map<Resource, RoleIdentifier> authorityRoles, Instance instance, boolean setMode) {
		List<ResourceRole> assigned = new LinkedList<ResourceRole>();
		InstanceReference reference = convertToReference(instance);

		List<Resource> resources = getResources(instance);
		Set<Entry<Resource, RoleIdentifier>> assignableResources = authorityRoles.entrySet();
		for (Entry<Resource, RoleIdentifier> resource : assignableResources) {
			assigned.add(addOrUpdateResourceInternal(resource.getKey(), resource.getValue(),
					reference));
		}
		List<Resource> newResources = null;
		if (!setMode) {
			newResources = new ArrayList<Resource>(resources.size() + authorityRoles.size());
			newResources.addAll(resources);
		} else {
			newResources = new ArrayList<Resource>(authorityRoles.size());
		}
		newResources.addAll(authorityRoles.keySet());

		ResourcesChangedEvent event = new ResourcesChangedEvent(instance, newResources,
				new ArrayList<>(resources));
		if (setMode) {
			boolean removeAll = resources.removeAll(authorityRoles.keySet());
			if (removeAll) {
				for (Resource resource : resources) {
					removeResourceInternal(resource, reference);
				}
			}
		}
		return new Pair<List<ResourceRole>, ResourcesChangedEvent>(assigned, event);
	}

	@Override
	public final <R extends Resource> List<ResourceRole> assignResources(Instance instance,
			Map<Resource, RoleIdentifier> authorityRoles) {
		Pair<List<ResourceRole>, ResourcesChangedEvent> updateResourcesInternal = updateResourcesInternal(
				authorityRoles, instance, false);
		eventService.fire(updateResourcesInternal.getSecond());
		return updateResourcesInternal.getFirst();
	}

	/**
	 * Adds the or update resource and role.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param resource
	 *            the resource
	 * @param role
	 *            the new role to set
	 * @param reference
	 *            the reference
	 * @return the r
	 */
	protected <R extends Resource> ResourceRole addOrUpdateResourceInternal(R resource,
			RoleIdentifier role, InstanceReference reference) {
		// find or create the role entity
		ResourceEntity entity = convertResource(resource);
		Pair<Serializable, ResourceEntity> pair;
		if (!SequenceEntityGenerator.isPersisted(entity)) {
			pair = getResourceCache().getOrCreateByValue(entity);
		} else {
			pair = getResourceCache().getByKey(entity.getId());
		}
		// if the cached value is different against the given value we should update it
		if ((pair != null)
				&& (EqualsHelper.nullSafeEquals(pair.getSecond().getIdentifier(),
						resource.getIdentifier(), false) || EqualsHelper.nullSafeEquals(pair
						.getSecond().getDisplayName(), resource.getDisplayName(), false))) {

			entity = pair.getSecond();
			entity.setIdentifier(resource.getIdentifier());
			entity.setDisplayName(resource.getDisplayName());

			// save entity and update cache
			entity = getDbDao().saveOrUpdate(entity);
			getResourceCache().setValue(entity.getId(), entity);
		}
		// if not found
		if (pair == null) {
			return null;
		}
		return assignEntityInternal(role, reference, pair);
	}

	/**
	 * Removes a role internally from instance.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param resource
	 *            the resource to remove
	 * @param reference
	 *            the instance to remove role for
	 * @return the removed role on success
	 */
	protected <R extends Resource> boolean removeResourceInternal(R resource,
			InstanceReference reference) {
		Pair<Serializable, ResourceEntity> pair = getResourceCache().getByKey(resource.getId());
		if (pair == null) {
			return false;
		}
		// REVIEW: should fire an event for role change
		// resource, old role, new role
		// find the current role or create the new
		ResourceRoleEntity roleEntity = new ResourceRoleEntity();
		roleEntity.setTargetRoleReference((LinkSourceId) reference);
		roleEntity.setResourceId((String) pair.getFirst());
		// TODO: for now the role is not part of the selection
		// save role to DB if not saved yet
		EntityLookupCache<Long, ResourceRoleEntity, Triplet<Long, String, Long>> roleCache = getResourceRoleCache();
		Pair<Long, ResourceRoleEntity> createByValue = roleCache.getByValue(roleEntity);
		if (createByValue == null) {
			return false;
		}
		return roleCache.deleteByKey(createByValue.getFirst()) > 0;
	}

	/**
	 * Assign entity internal.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param role
	 *            the role to assign
	 * @param reference
	 *            the reference to instance
	 * @param pair
	 *            the pair of id and entity
	 * @return the resource role assigned
	 */
	private <R extends Resource> ResourceRole assignEntityInternal(RoleIdentifier role,
			InstanceReference reference, Pair<Serializable, ResourceEntity> pair) {

		// REVIEW: should fire an event for role change
		// resource, old role, new role
		// find the current role or create the new
		ResourceRoleEntity roleEntity = new ResourceRoleEntity();
		roleEntity.setTargetRoleReference((LinkSourceId) reference);
		roleEntity.setResourceId((String) pair.getFirst());
		// TODO: for now the role is not part of the selection
		// save role to DB if not saved yet
		EntityLookupCache<Long, ResourceRoleEntity, Triplet<Long, String, Long>> roleCache = getResourceRoleCache();
		Pair<Long, ResourceRoleEntity> createByValue = roleCache.getOrCreateByValue(roleEntity);
		roleEntity = createByValue.getSecond();
		roleEntity.setId(createByValue.getFirst());
		// if values was fetched from the cache then we should check the previous role if is
		// different we need to update it.
		// also if role is not the same this means the resource had a different role in the project
		// and we request role change
		if (!EqualsHelper.nullSafeEquals(roleEntity.getRole(), role.getIdentifier(), false)) {
			// updated the existing role
			roleEntity.setRole(role.getIdentifier());
			roleEntity = getDbDao().saveOrUpdate(roleEntity);
			roleCache.setValue(roleEntity.getId(), roleEntity);
		}

		// convert and return the final result
		R converted = convertToInstance(pair.getSecond());
		ResourceRole resourceRole = createResourceRole(roleEntity, converted, reference);
		return resourceRole;
	}

	/**
	 * Internal map between resource and its type.
	 * 
	 * @param resource
	 *            is the resource to get type for
	 * @return the {@link ResourceType} or unknown if not found
	 */
	protected ResourceType getTypeByResource(Resource resource) {
		if (resource instanceof User) {
			return ResourceType.USER;
		}
		if (resource instanceof EmfGroup) {
			return ResourceType.GROUP;
		}
		return ResourceType.UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R extends Resource> R saveResource(R resource) {
		if (resource == null) {
			return resource;
		}
		ResourceEntity entity = convertResource(resource);
		setSemanticType(resource);
		Integer newHash = hashCalculator.computeHash(resource);

		if (entity.getType() == null) {
			entity.setType(getTypeByResource(resource).getType());
		}
		Pair<Serializable, ResourceEntity> pair = getResourceCache().getOrCreateByValue(entity);
		if (pair == null) {
			return null;
		}
		R instance = convertToInstance(pair.getSecond());
		loadProperties(instance);
		Integer oldHash = hashCalculator.computeHash(instance);
		// create copy that will be used for semantic diff save
		R copy = SerializationUtil.copy(instance);
		instance.getProperties().putAll(PropertiesUtil.cloneProperties(resource.getProperties()));

		if (!EqualsHelper.nullSafeEquals(oldHash, newHash)) {
			saveProperties(instance);
			eventService.fire(new ResourceUpdatedEvent(instance, copy));
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R extends Resource> R getResource(Serializable id) {
		return getResourceInternal(id, null, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R extends Resource> R getResource(String name, ResourceType type) {
		if (type == ResourceType.UNKNOWN) {
			// TODO user the permission adapter
			R resource = getResourceInternal(null, name, ResourceType.USER, true);
			if (resource == null) {
				resource = getResourceInternal(null, name, ResourceType.GROUP, true);
			}
			return resource;
		}
		return getResourceInternal(null, name, type, true);
	}

	/**
	 * Gets the resource internal.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param loadProperties
	 *            the load properties
	 * @return the resource internal
	 */
	protected <R extends Resource> R getResourceInternal(Serializable id, String name,
			ResourceType type, boolean loadProperties) {
		R resource = null;
		if (id != null) {
			Pair<Serializable, ResourceEntity> pair = getResourceCache().getByKey(id);
			if (pair != null) {
				ResourceEntity resourceEntity = pair.getSecond();
				resource = convertToInstance(resourceEntity);
			}
		} else if (StringUtils.isNotNullOrEmpty(name) && (type != null)) {
			ResourceEntity entity = new ResourceEntity();
			entity.setIdentifier(name);
			entity.setType(type.getType());
			Pair<Serializable, ResourceEntity> pair = getResourceCache().getByValue(entity);
			if (pair != null) {
				resource = convertToInstance(pair.getSecond());
			}
		}
		if ((resource != null) && loadProperties) {
			loadProperties(resource);
		}
		return resource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <R extends Resource> R getOrCreateResource(R resource) {
		if ((resource == null) || (resource.getType() == null)) {
			LOGGER.warn("Resource " + resource + " is not supported for persist!");
			return resource;
		}
		ResourceType type = resource.getType();
		if (type == ResourceType.GROUP) {
			// clear the calculated runtime roles
			synchronized (runtimeRoles) {
				runtimeRoles.clear();
			}
		}
		Serializable id = resource.getId();
		boolean toRegister = false;
		if (id == null) {
			id = "emf:" + resource.getIdentifier();
			resource.setId(id);
			toRegister = true;
		}
		// check if the resource already exists also this will serve for changes check
		Resource resourceInternal = getResourceInternal(id, null, type, true);
		if (resourceInternal == null) {
			if (toRegister) {
				SequenceEntityGenerator.register(resource);
			}
			ResourceEntity resourceEntity = new ResourceEntity();
			resourceEntity.setId(id.toString());
			resourceEntity.setIdentifier(resource.getIdentifier());
			resourceEntity.setDisplayName(resource.getDisplayName());
			resourceEntity.setType(type.getType());
			resourceEntity = dbDao.saveOrUpdate(resourceEntity);
			getResourceCache().setValue(resourceEntity.getId(), resourceEntity);

			eventService.fire(new ResourceAddedEvent(resource));
			eventService.fire(new ResourcePersistedEvent(resource, null, null));
		}

		setSemanticType(resource);
		// actual resource id
		saveProperties(resource);
		// notify for found changes
		if (resourceInternal != null) {
			Integer oldHash = hashCalculator.computeHash(resourceInternal);
			Integer newHash = hashCalculator.computeHash(resource);
			// check if we have changes
			if (!EqualsHelper.nullSafeEquals(oldHash, newHash)) {
				eventService.fire(new ResourceUpdatedEvent(resource, resourceInternal));
			}
		}
		return resource;
	}

	/**
	 * Sets the semantic type.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param resource
	 *            the new semantic type
	 */
	private <R extends Resource> void setSemanticType(R resource) {
		String type = EmfUser.class.getName();
		if (resource.getType() == ResourceType.GROUP) {
			type = EmfGroup.class.getName();
		}
		DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(type);
		if ((definition != null) && (definition.getFirstUri() != null)) {
			resource.getProperties().put("rdf:type",
					typeConverter.convert(Uri.class, definition.getFirstUri()));
		}
	}

	/**
	 * Save properties.
	 * 
	 * @param resource
	 *            the resource
	 */
	void saveProperties(Resource resource) {
		if ((resource != null) && (resource.getProperties() != null)
				&& !resource.getProperties().isEmpty()) {
			// enable custom properties saving
			RuntimeConfiguration
					.setConfiguration(
							RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION,
							Boolean.TRUE);
			try {
				propertiesService.saveProperties(resource, false);
			} finally {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);
			}
		}
	}

	/**
	 * Load properties. the generic type
	 * 
	 * @param resource
	 *            the resource
	 */
	private void loadProperties(Resource resource) {
		propertiesService.loadProperties(resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R extends Resource> List<R> getResources(List<Serializable> ids) {
		return batchFetchResourcesInternal(ids, true);
	}

	/**
	 * Batch fetch resources internal.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @param keepOrder
	 *            the return sorted
	 * @return the loaded resources if found.
	 */
	protected <R extends Resource, S extends Serializable> List<R> batchFetchResourcesInternal(
			Collection<S> ids, boolean keepOrder) {
		if ((ids == null) || ids.isEmpty()) {
			return Collections.emptyList();
		}
		// create 2 different containers based if we need to keep the order or not
		Map<Serializable, R> mapResult = batchFetchResourceMappingInternal(ids, true);

		if (keepOrder) {
			// sort the results
			List<R> sortedResult = new ArrayList<R>(mapResult.size());
			for (Serializable key : ids) {
				R instance = mapResult.get(key);
				if (instance != null) {
					sortedResult.add(instance);
				}
			}
			return sortedResult;
		}
		return new ArrayList<R>(mapResult.values());
	}

	/**
	 * Batch fetch resource mapping internal.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @param loadProperties
	 *            the load properties
	 * @return the map
	 */
	protected <R extends Resource, S extends Serializable> Map<Serializable, R> batchFetchResourceMappingInternal(
			Collection<S> ids, boolean loadProperties) {
		Map<Serializable, R> mapResult = CollectionUtils.createLinkedHashMap(ids.size());
		List<R> toLoadProperties = null;
		if (loadProperties) {
			toLoadProperties = new ArrayList<R>(ids.size());
		}
		Set<Serializable> secondPass = new LinkedHashSet<Serializable>();
		// first we check for hits in the cache and filter the cache misses
		EntityLookupCache<Serializable, ResourceEntity, Pair<String, ResourceType>> cache = getResourceCache();
		for (Serializable dbId : ids) {
			if (cache != null) {
				// convert the cache entry to instance and schedule
				// properties loading instead of one by one loading
				ResourceEntity entity = cache.getValue(dbId);
				if (entity != null) {
					R instance = convertToInstance(entity);
					mapResult.put(dbId, instance);
					if (loadProperties) {
						toLoadProperties.add(instance);
					}
					continue;
				}
			}
			// no cache or not found in cache search later in DB
			secondPass.add(dbId);
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<ResourceEntity> list = findResourceEntities(secondPass);

			if (!list.isEmpty()) {
				for (ResourceEntity entity : list) {

					if (cache != null) {
						// update cache
						cache.setValue(entity.getId(), entity);
					}

					R instance = convertToInstance(entity);
					mapResult.put(instance.getId(), instance);
					if (loadProperties) {
						toLoadProperties.add(instance);
					}
				}
			}
		}
		if (loadProperties) {
			propertiesService.loadProperties(toLoadProperties);
		}
		return mapResult;
	}

	/**
	 * Batch fetch resource entities mapping internal. Method is optimized to work only with
	 * entities, without convert
	 * 
	 * @param ids
	 *            the ids of the entities
	 * @return the map of loaded entities
	 */
	protected Map<Serializable, ResourceEntity> batchFetchResourceEntitiesMappingInternal(
			Collection<Serializable> ids) {
		Map<Serializable, ResourceEntity> mapResult = CollectionUtils.createLinkedHashMap(ids
				.size());
		Set<Serializable> secondPass = new LinkedHashSet<Serializable>();
		// first we check for hits in the cache and filter the cache misses
		EntityLookupCache<Serializable, ResourceEntity, Pair<String, ResourceType>> cache = getResourceCache();
		for (Serializable dbId : ids) {
			if (cache != null) {
				// convert the cache entry to instance and schedule
				// properties loading instead of one by one loading
				ResourceEntity entity = cache.getValue(dbId);
				if (entity != null) {
					mapResult.put(dbId, entity);
					continue;
				}
			}
			// no cache or not found in cache search later in DB
			secondPass.add(dbId);
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<ResourceEntity> list = findResourceEntities(secondPass);

			if (!list.isEmpty()) {
				for (ResourceEntity entity : list) {

					if (cache != null) {
						// update cache
						cache.setValue(entity.getId(), entity);
					}
					mapResult.put(entity.getId(), entity);
				}
			}
		}
		return mapResult;
	}

	/**
	 * Batch fetch resources and if project id is provided will also fetch the project role for each
	 * resource.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param resourceRoleIds
	 *            the ids
	 * @param reference
	 *            the reference
	 * @return the loaded resources if found.
	 */
	protected <R extends Resource> List<ResourceRole> batchFetchResourcesInternal(
			List<Long> resourceRoleIds, InstanceReference reference) {

		// load resource entities
		List<ResourceRoleEntity> resourceRoles = BatchEntityLoader.batchLoadByPrimaryKey(
				resourceRoleIds, getResourceRoleCache(), new PrimaryBatchResourceRoleLoader());

		// now fetch the resources
		List<String> resources = new ArrayList<String>(resourceRoles.size());
		for (ResourceRoleEntity resourceRoleEntity : resourceRoles) {
			resources.add(resourceRoleEntity.getResourceId());
		}
		Map<Serializable, Resource> resourceMapping = batchFetchResourceMappingInternal(resources,
				true);

		// merge objects to create the final result
		List<ResourceRole> sortedResult = new ArrayList<ResourceRole>(resources.size());
		for (ResourceRoleEntity resourceRoleEntity : resourceRoles) {
			Resource resource = resourceMapping.get(resourceRoleEntity.getResourceId());
			sortedResult.add(createResourceRole(resourceRoleEntity, resource, reference));
		}
		return sortedResult;
	}

	/**
	 * Find entities.
	 * 
	 * @param ids
	 *            the ids
	 * @return the list
	 */
	protected List<ResourceEntity> findResourceEntities(Set<Serializable> ids) {
		return getDbDao().fetchWithNamed(EmfQueries.QUERY_PROJECT_RESOURCE_BY_IDS_KEY,
				Arrays.asList(new Pair<String, Object>("ids", ids)));
	}

	/**
	 * Find resource entities by names.
	 * 
	 * @param ids
	 *            the ids
	 * @return the list
	 */
	protected List<ResourceEntity> findResourceEntitiesByNames(Set<Pair<String, ResourceType>> ids) {
		return getDbDao().fetchWithNamed(EmfQueries.QUERY_PROJECT_RESOURCE_BY_IDS_KEY,
				Arrays.asList(new Pair<String, Object>("ids", ids)));
	}

	/**
	 * Find entities.
	 * 
	 * @param ids
	 *            the ids
	 * @return the list
	 */
	protected List<ResourceRoleEntity> findRoleEntities(Set<Long> ids) {
		return getDbDao().fetchWithNamed(EmfQueries.QUERY_RESOURCE_ROLES_BY_IDS_KEY,
				Arrays.asList(new Pair<String, Object>("ids", ids)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R extends Resource> List<R> getResources(Instance instance) {
		return getResourcesInternal(instance, null);
	}

	/**
	 * Gets the resources internal.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @param role
	 *            the role
	 * @return the resources internal
	 */
	protected <R extends Resource> List<R> getResourcesInternal(Instance instance,
			RoleIdentifier role) {
		if (instance == null) {
			return Collections.emptyList();
		}

		InstanceReference reference = convertToReference(instance);
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
		args.add(new Pair<String, Object>(SOURCE_ID, reference.getIdentifier()));
		args.add(new Pair<String, Object>(SOURCE_TYPE, reference.getReferenceType().getId()));

		String query = EmfQueries.QUERY_RESOURCE_ROLE_BY_TARGET_ID_KEY;
		if (role != null) {
			args.add(new Pair<String, Object>(ROLE, role.getIdentifier()));
			query = EmfQueries.QUERY_RESOURCE_ROLE_ID_BY_TARGET_ID_KEY;
		}

		List<Serializable> list = getDbDao().fetchWithNamed(query, args);
		return batchFetchResourcesInternal(list, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceRole getResourceRole(Instance instance, String name, ResourceType type) {
		Resource resource = getResource(name, type);
		return getResourceRole(instance, resource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R extends Resource> List<R> getResourcesByRole(Instance instance, RoleIdentifier role) {
		return getResourcesInternal(instance, role);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ResourceRole> getResourceRoles(Instance instance) {
		if (instance == null) {
			return Collections.emptyList();
		}
		InstanceReference reference = convertToReference(instance);
		if ((reference == null) || (reference.getReferenceType() == null)) {
			return Collections.emptyList();
		}
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
		args.add(new Pair<String, Object>(SOURCE_ID, reference.getIdentifier()));
		args.add(new Pair<String, Object>(SOURCE_TYPE, reference.getReferenceType().getId()));
		List<Long> list = getDbDao().fetchWithNamed(
				EmfQueries.QUERY_RESOURCE_ROLES_BY_TARGET_ID_KEY, args);
		return batchFetchResourcesInternal(list, reference);
	}

	/**
	 * Creates the resource role.
	 * 
	 * @param entity
	 *            the entity
	 * @param resource
	 *            the resource
	 * @param reference
	 *            the reference
	 * @return the resource role
	 */
	protected ResourceRole createResourceRole(ResourceRoleEntity entity, Resource resource,
			InstanceReference reference) {
		ResourceRole resourceRole = new ResourceRole();
		resourceRole.setId(entity.getId());
		resourceRole.setResource(resource);
		resourceRole.setRole(getRoleIdentifier(entity.getRole()));
		if (reference == null) {
			resourceRole.setTargetRoleReference(entity.getTargetRoleReference());
		} else {
			resourceRole.setTargetRoleReference(reference);
		}
		return resourceRole;
	}

	/**
	 * Gets the role based on id identificator.
	 * 
	 * @param role
	 *            is the role id
	 * @return the {@link RoleIdentifier} instance or null if not found
	 */
	private RoleIdentifier getRoleIdentifier(String role) {
		return roleService.getRoleIdentifier(role);
	}

	/**
	 * Gets the cache.
	 * 
	 * @return the cache
	 */
	protected EntityLookupCache<Serializable, ResourceEntity, Pair<String, ResourceType>> getResourceCache() {
		return cacheContext.getCache(RESOURCE_ENTITY_CACHE);
	}

	/**
	 * Gets the resource role cache.
	 * 
	 * @return the cache
	 */
	protected EntityLookupCache<Long, ResourceRoleEntity, Triplet<Long, String, Long>> getResourceRoleCache() {
		return cacheContext.getCache(RESOURCE_ROLE_ENTITY_CACHE);
	}

	/**
	 * Gets the db dao.
	 * 
	 * @return the db dao
	 */
	protected DbDao getDbDao() {
		return dbDao;
	}

	/**
	 * Converts the given rsource to entity resource.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param resource
	 *            the resource
	 * @return the resource entity
	 */
	protected <R extends Resource> ResourceEntity convertResource(R resource) {
		return mapper.getMapper().map(resource, ResourceEntity.class);
	}

	/**
	 * Convert to reference.
	 * 
	 * @param instance
	 *            the instance
	 * @return the instance reference
	 */
	protected InstanceReference convertToReference(Instance instance) {
		return typeConverter.convert(InstanceReference.class, instance);
	}

	/**
	 * Converts a single entity instance to project resource impl.
	 * 
	 * @param <R>
	 *            the generic type
	 * @param entity
	 *            the entity
	 * @return the r
	 */
	@SuppressWarnings("unchecked")
	protected <R extends Resource> R convertToInstance(ResourceEntity entity) {
		if (EqualsHelper.nullSafeEquals(entity.getType(), ResourceType.USER.getType())) {
			return (R) mapper.getMapper().map(entity, EmfUser.class);
		} else if (EqualsHelper.nullSafeEquals(entity.getType(), ResourceType.GROUP.getType())) {
			return (R) mapper.getMapper().map(entity, EmfGroup.class);
		} else if (EqualsHelper.nullSafeEquals(entity.getType(), ResourceType.UNKNOWN.getType())) {
			// this should not come here but for backward compatibility
			return (R) mapper.getMapper().map(entity, EmfUser.class);
		}
		throw new EmfConfigurationException("Unsupported resource entity type: " + entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResourceRole getResourceRole(Instance instance, Resource resource) {
		if ((resource == null) || (instance == null)) {
			// no such resource
			return null;
		}
		InstanceReference reference = convertToReference(instance);
		ResourceRoleEntity value = new ResourceRoleEntity();
		value.setTargetRoleReference((LinkSourceId) reference);
		Resource localResource = resource;
		if (localResource.getId() == null) {
			localResource = getResource(localResource.getIdentifier(), localResource.getType());
		}
		value.setResourceId((String) localResource.getId());
		Pair<Long, ResourceRoleEntity> pair = getResourceRoleCache().getByValue(value);
		if (pair == null) {
			// no resource to project
			synchronized (runtimeRoles) {
				ResourceRole runtimeResourceRole = null;
				Triplet<Serializable, Class<?>, Serializable> key = new Triplet<Serializable, Class<?>, Serializable>(
						instance.getId(), instance.getClass(), resource.getId());
				if (runtimeRoles.containsKey(key)) {
					return runtimeRoles.get(key);
				}
				runtimeResourceRole = getRuntimeResourceRole(instance, resource);
				// put null as well for unknown roles
				runtimeRoles.put(key, runtimeResourceRole);
				return runtimeResourceRole;
			}
		}
		ResourceRoleEntity roleEntity = pair.getSecond();
		ResourceRole resourceRole = createResourceRole(roleEntity, resource,
				roleEntity.getTargetRoleReference());
		return resourceRole;
	}

	/**
	 * Calculates the runtime role for user, depending on groups as well the user is contained in.
	 * 
	 * @param instance
	 *            the instance to check
	 * @param resource
	 *            the user to check roles for
	 * @return the highest role available from the first met
	 */
	private ResourceRole getRuntimeResourceRole(Instance instance, Resource resource) {
		// TODO groups in group?
		if (resource.getType() == ResourceType.GROUP) {
			return null;
		}
		List<RoleIdentifier> activeRoles = roleService.getActiveRoles();
		InternalRole role = new InternalRole();
		for (ResourceProviderExtension resourceExtension : resourceProviders) {
			if (resourceExtension.isApplicable(ResourceType.USER)) {
				List<Resource> containedResources = resourceExtension
						.getContainingResources(resource);
				int maxLevel = -1;
				ResourceRole maxResourceRole = null;
				for (Resource containedResource : containedResources) {
					ResourceRole resourceRole = getResourceRole(instance, containedResource);
					if (resourceRole == null) {
						// nothing more to do
						continue;
					}
					role.setRoleId(resourceRole.getRole());
					int level = activeRoles.indexOf(resourceRole.getRole());
					if (maxLevel == activeRoles.size()) {
						// if manager from some group just return it
						return resourceRole;
					} else if (level > maxLevel) {
						maxResourceRole = resourceRole;
						maxLevel = level;
					}
				}
				return maxResourceRole;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R extends Resource> List<R> getAllResources(ResourceType type, String sortColumn) {

		if (type == null) {
			return Collections.emptyList();
		}

		for (ResourceProviderExtension resourceExtension : resourceProviders) {
			if (resourceExtension.isApplicable(type)) {
				return resourceExtension.getAllResources(sortColumn);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Find resource in db.
	 * 
	 * @param ids
	 *            the ids
	 * @param type
	 *            the type
	 * @return the list
	 */
	List<ResourceEntity> findResourceInDbBySecondaryKey(Collection<String> ids, ResourceType type) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("identifier", ids));
		args.add(new Pair<String, Object>("type", type.getType()));
		List<ResourceEntity> list = getDbDao().fetchWithNamed(
				EmfQueries.QUERY_PROJECT_RESOURCE_BY_NAME_KEY, args);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R extends Resource> List<R> getContainingResources(Resource resource) {
		ResourceType type = resource.getType();
		for (ResourceProviderExtension resourceExtension : resourceProviders) {
			if (resourceExtension.isApplicable(type)) {
				return resourceExtension.getContainingResources(resource);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R extends Resource> List<R> getContainedResources(Resource resource) {
		ResourceType type = resource.getType();
		for (ResourceProviderExtension resourceExtension : resourceProviders) {
			if (resourceExtension.isApplicable(type)) {
				return resourceExtension.getContainedResources(resource);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<String> getContainedResourceIdentifiers(Resource resource) {
		ResourceType type = resource.getType();
		for (ResourceProviderExtension resourceExtension : resourceProviders) {
			if (resourceExtension.isApplicable(type)) {
				return resourceExtension.getContainedResourceIdentifiers(resource);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getDisplayName(Serializable resource) {
		String displayName = null;

		if (resource instanceof Collection) {
			StringBuilder builder = new StringBuilder();
			for (Object object : (Collection<?>) resource) {
				String value = extractDisplayName(object);
				if (value != null) {
					if (builder.length() != 0) {
						builder.append(", ");
					}
					builder.append(value);
				}
			}
			if (builder.length() > 0) {
				displayName = builder.toString();
			}
		} else {
			displayName = extractDisplayName(resource);
		}
		return displayName;
	}

	/**
	 * Extract display name.
	 * 
	 * @param resource
	 *            the resource
	 * @return the string
	 */
	private String extractDisplayName(Object resource) {
		String displayName = null;

		Instance asResource = parseAsResource(resource);
		if (asResource instanceof Resource) {
			displayName = ((Resource) asResource).getDisplayName();
		} else if (asResource != null) {
			displayName = EmfResourcesUtil.buildDisplayName(asResource.getProperties());
		}
		return displayName;
	}

	/**
	 * Parses the given argument to resource. The method will try to get a resource or instance that
	 * represent a resource based on the argument.
	 * 
	 * @param args
	 *            the argument to parse
	 * @return the instance or <code>null</code>. The instance could be a {@link Resource} instance.
	 */
	private Instance parseAsResource(Object args) {
		if (args == null) {
			// no need to check anymore
			return null;
		}
		Instance instance = null;
		if ((args instanceof String) && StringUtils.isNotNullOrEmpty((String) args)) {
			String asString = args.toString();

			// probably instance reference as string
			if (asString.startsWith("{")) {
				InstanceReference reference = typeConverter.convert(InstanceReference.class, args);
				if (reference != null) {
					instance = reference.toInstance();
				} else {
					LOGGER.warn("No instance found when loading from " + args);
				}
			} else
			// check if it's URI/primary key
			if (asString.contains(":")) {
				ShortUri shortUri = typeConverter.convert(ShortUri.class, asString);
				Resource result = getResource(shortUri.toString());
				if (result != null) {
					instance = result;
				} else {
					LOGGER.warn("Searching for a resource by URI=" + args + " but was not found!");
				}
			} else {
				// probably a user/group identifier
				Resource foundUser = getResource(asString, ResourceType.USER);
				if (foundUser == null) {
					foundUser = getResource(asString, ResourceType.GROUP);
				}
				instance = foundUser;
			}
		} else if (args instanceof Instance) {
			// no check for resource because it will fall in here also
			instance = (Instance) args;
		} else {
			// otherwise we will try to convert it to resource, probably in some externally
			// supported format
			try {
				Resource convert = typeConverter.convert(Resource.class, args);
				if (convert != null) {
					instance = convert;
				} else {
					// it's not very likely to happen but just in case. The exception is more
					// probable
					LOGGER.warn("Tryied to conver the given object " + args + " to "
							+ Resource.class + " but resulted null");
				}
			} catch (TypeConversionException e) {
				LOGGER.warn("Cannot convert the resource " + args + " to " + Resource.class, e);
			}
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<GenericDefinition> getInstanceDefinitionClass() {
		return GenericDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Resource createInstance(GenericDefinition definition, Instance parent) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Resource createInstance(GenericDefinition definition, Instance parent,
			Operation operation) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Resource save(Resource instance, Operation operation) {
		return saveResource(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Resource cancel(Resource instance) {
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void refresh(Resource instance) {
		// does not support refresh
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<Resource> loadInstances(Instance owner) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Resource loadByDbId(Serializable id) {
		return getResource(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Resource load(Serializable instanceId) {
		if (instanceId instanceof Pair) {
			Pair<String, ResourceType> pair = (Pair<String, ResourceType>) instanceId;
			return getResource(pair.getFirst(), pair.getSecond());
		} else if (instanceId instanceof String) {
			return getResource((String) instanceId, ResourceType.UNKNOWN);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<Resource> load(List<S> ids) {
		return load(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<Resource> loadByDbId(List<S> ids) {
		return loadByDbId(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<Resource> load(List<S> ids, boolean allProperties) {
		List<ResourceEntity> entities = BatchEntityLoader.batchLoadBySecondaryKey(
				convertToPairCollection(ids), getResourceCache(), secondaryKeyCallback);
		List<Resource> result = new ArrayList<>(entities.size());
		for (ResourceEntity resourceEntity : entities) {
			Resource instance = convertToInstance(resourceEntity);
			result.add(instance);
		}
		propertiesService.loadProperties(result);
		return result;
	}

	/**
	 * Convert to pair collection.
	 * 
	 * @param list
	 *            the list
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	private List<Pair<String, ResourceType>> convertToPairCollection(List<?> list) {
		return (List<Pair<String, ResourceType>>) list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<Resource> loadByDbId(List<S> ids, boolean allProperties) {
		return getResources((List<Serializable>) ids);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<DefinitionModel>> getAllowedChildren(Resource owner) {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(Resource owner, String type) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(Resource owner, String type) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Resource clone(Resource instance, Operation operation) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Resource instance, Operation operation, boolean permanent) {
		// TODO Auto-generated method stub

	}

	/**
	 * The Class ResourceLoaderBySecondaryKey.
	 * 
	 * @author BBonev
	 */
	private final class ResourceLoaderBySecondaryKey
			implements
			BatchSecondaryKeyEntityLoaderCallback<Serializable, ResourceEntity, Pair<String, ResourceType>> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Serializable getPrimaryKey(ResourceEntity entity) {
			return entity.getId();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<String, ResourceType> getSecondaryKey(ResourceEntity entity) {
			return new Pair<String, ResourceType>(entity.getIdentifier(),
					ResourceType.getById(entity.getType()));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<ResourceEntity> findEntitiesBySecondaryKey(
				Set<Pair<String, ResourceType>> secondPass) {
			Collection<String> ids = new ArrayList<>(secondPass.size());
			ResourceType type = secondPass.iterator().next().getSecond();
			for (Pair<String, ResourceType> pair : secondPass) {
				ids.add(pair.getFirst());
			}
			return findResourceInDbBySecondaryKey(ids, type);
		}
	}

	/**
	 * Provides mutable implementation of user Role for internal use only.
	 * 
	 * @author BBonev
	 */
	private static class InternalRole implements Role {

		/** The role id. */
		private RoleIdentifier roleId;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isSealed() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void seal() {
			// does not support sealing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public RoleIdentifier getRoleId() {
			return roleId;
		}

		/**
		 * Setter method for roleId.
		 * 
		 * @param roleId
		 *            the roleId to set
		 */
		public void setRoleId(RoleIdentifier roleId) {
			this.roleId = roleId;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <A extends Action> Set<A> getAllAllowedActions() {
			return Collections.emptySet();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <A extends Action> Set<A> getAllowedActions(Class<?> class1) {
			return Collections.emptySet();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <A extends Action, P extends Permission> Map<P, List<Pair<Class<?>, Action>>> getPermissions() {
			return Collections.emptyMap();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <A extends Action, P extends Permission> void addPermission(P permission,
				List<Pair<Class<?>, Action>> actions) {
			// nothing to do here
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((getRoleId() == null) ? 0 : getRoleId().hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
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
			InternalRole other = (InternalRole) obj;
			if (getRoleId() == null) {
				if (other.getRoleId() != null) {
					return false;
				}
			} else if (!getRoleId().equals(other.getRoleId())) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Batch loader callback for loading {@link ResourceRoleEntity}s.
	 * 
	 * @author BBonev
	 */
	protected class PrimaryBatchResourceRoleLoader implements
			BatchPrimaryKeyEntityLoaderCallback<Long, ResourceRoleEntity> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long getPrimaryKey(ResourceRoleEntity entity) {
			return entity.getId();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<ResourceRoleEntity> findEntitiesByPrimaryKey(Set<Long> secondPass) {
			return findRoleEntities(secondPass);
		}
	}

	/**
	 * Entity lookup cache for project resource entity.
	 * 
	 * @author BBonev
	 */
	protected class ResourceLookupDao extends
			BaseEntityLookupDao<ResourceEntity, Pair<String, ResourceType>, String> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Class<ResourceEntity> getEntityClass() {
			return ResourceEntity.class;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Logger getLogger() {
			return LOGGER;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected DbDao getDbDao() {
			return ResourceServiceImpl.this.getDbDao();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Pair<String, ResourceType> getValueKeyInternal(ResourceEntity value) {
			if (StringUtils.isNullOrEmpty(value.getIdentifier())) {
				return null;
			}
			return new Pair<String, ResourceType>(value.getIdentifier(), ResourceType.getById(value
					.getType()));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<String, ResourceEntity> createValue(ResourceEntity value) {
			if (value.getId() == null) {
				value.setId("emf:" + value.getIdentifier());
				SequenceEntityGenerator.register(value);
			}
			Pair<String, ResourceEntity> pair = super.createValue(value);
			if (pair != null) {
				eventService.fire(new ResourcePersistedEvent(convertToInstance(pair.getSecond()),
						null, null));
			}
			return pair;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<ResourceEntity> fetchEntityByValue(Pair<String, ResourceType> key) {
			List<ResourceEntity> list = findResourceInDbBySecondaryKey(
					Arrays.asList(key.getFirst()), key.getSecond());
			if (list.isEmpty()) {
				for (ResourceProviderExtension extension : resourceProviders) {
					if (extension.isApplicable(key.getSecond())) {
						Resource resource = extension.getResource(key.getFirst());
						if (resource != null) {
							ResourceEntity resourceEntity = convertResource(resource);
							// if fetched from external source we should persist it for later use
							Pair<String, ResourceEntity> pair = createValue(resourceEntity);
							resource.setId(pair.getFirst());
							if (!resource.getProperties().isEmpty()) {
								saveProperties(resource);
							}
							return Arrays.asList(resourceEntity);
						}
					}
				}
			}
			return list;
		}
	}

	/**
	 * Lookup DAO that fetches the {@link ResourceRoleEntity}.
	 * 
	 * @author BBonev
	 */
	public class ResourceRoleLookupDao extends
			BaseEntityLookupDao<ResourceRoleEntity, Triplet<Serializable, String, Long>, Long> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Class<ResourceRoleEntity> getEntityClass() {
			return ResourceRoleEntity.class;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected DbDao getDbDao() {
			return ResourceServiceImpl.this.getDbDao();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Triplet<Serializable, String, Long> getValueKeyInternal(ResourceRoleEntity value) {
			return new Triplet<Serializable, String, Long>(value.getResourceId(), value
					.getTargetRoleReference().getIdentifier(), value.getTargetRoleReference()
					.getReferenceType().getId());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<ResourceRoleEntity> fetchEntityByValue(
				Triplet<Serializable, String, Long> key) {
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(3);
			args.add(new Pair<String, Object>("resourceId", key.getFirst()));
			args.add(new Pair<String, Object>(SOURCE_ID, key.getSecond()));
			args.add(new Pair<String, Object>(SOURCE_TYPE, key.getThird()));
			return getDbDao().fetchWithNamed(EmfQueries.QUERY_PROJECT_RESOURCE_ROLE_BY_PROJECT_KEY,
					args);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void attach(Resource targetInstance, Operation operation, Instance... children) {
		if ((targetInstance == null) || (children == null) || (children.length == 0)) {
			return;
		}
		// TODO: add user/s to group
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void detach(Resource sourceInstance, Operation operation, Instance... instances) {
		if ((sourceInstance == null) || (instances == null) || (instances.length == 0)) {
			return;
		}
		// TODO: remove user/s from group
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean areEqual(Object resource1, Object resource2) {
		Instance asResource1 = parseAsResource(resource1);
		if (asResource1 == null) {
			// no need to continue e with the checks. This is not just a equals method for objects
			// is for persons so we can't say that 2 non existent persons are equal
			return false;
		}
		Instance asResource2 = parseAsResource(resource2);
		if (asResource2 == null) {
			return false;
		}
		// we check the ids because we could have a resource and external resource(object that is
		// not registered as resource) and if that have the same URI/db id that are the same.
		return EqualsHelper.nullSafeEquals(asResource1.getId(), asResource2.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R extends Resource> R findResource(Serializable id) {
		Instance resource = parseAsResource(id);
		if (resource instanceof Resource) {
			return (R) resource;
		}
		return null;
	}

	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void synchContainedResources() {
		List<Resource> groups = getAllResources(ResourceType.GROUP, null);
		for (Resource group : groups) {
			// we are skipping specific DMS groups - we does not care for these
			if (!group.getIdentifier().startsWith("GROUP_site")) {
				List<Resource> resources = getContainedResources(group);
				for (Resource added : resources) {
					eventService.fire(new AttachedChildToResourceEvent(group, added));
				}
			}
		}
	}
}
