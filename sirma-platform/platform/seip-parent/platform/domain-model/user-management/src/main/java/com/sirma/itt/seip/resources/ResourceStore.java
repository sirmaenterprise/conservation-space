package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_DELETED;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_ON;
import static com.sirma.itt.seip.resources.ResourceType.GROUP;
import static com.sirma.itt.seip.resources.ResourceType.USER;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.lookup.BaseEntityLookupDao;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.collections.SealedList;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.exceptions.DatabaseException;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.util.PropertiesEvaluationHelper;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.resources.event.ResourceAddedEvent;
import com.sirma.itt.seip.resources.event.ResourcePersistedEvent;
import com.sirma.itt.seip.resources.event.ResourceUpdatedEvent;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * {@link ResourceStore} is a local, cached enabled resource instance provider. There are methods for accessing and
 * updating resource entity and instances and the underlying caches.
 *
 * @author BBonev
 */
class ResourceStore {

	private static final Operation SYNCHRONIZE = new Operation(ActionTypeConstants.SYNCHRONIZE);
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@CacheConfiguration(eviction = @Eviction(maxEntries = 100),
			expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Cache used to store the information for all registered users in the system no matter if they are active or not."
			+ "For every user there are 2 entries in the cache. " + "<br>Minimal value expression: users * 2.2"))
	public static final String RESOURCE_ENTITY_CACHE = "RESOURCE_ENTITY_CACHE";

	@Inject
	private ResourceEntityDao entityDao;
	@Inject
	private ObjectMapper mapper;
	@Inject
	private DbDao dbDao;
	@Inject
	private PropertiesService propertiesService;
	@Inject
	private EventService eventService;
	@Inject
	private DatabaseIdManager idManager;
	@Inject
	private EntityLookupCacheContext cacheContext;
	@Inject
	private ContextualConcurrentMap<ResourceType, List<Resource>> allResources;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private ExpressionsManager expressionsManager;
	@Inject
	private RemoteUserStoreAdapter remoteUserStore;
	@Inject
	private InstanceTypes instanceTypes;
	@Inject
	private SynchronizationRunner synchronizationRunner;
	@Inject
	private SystemConfiguration systemConfigs;
	@Inject
	private StateService stateService;
	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Initialize caches
	 */
	@PostConstruct
	protected void initialize() {
		if (!cacheContext.containsCache(RESOURCE_ENTITY_CACHE)) {
			EntityLookupCache<String, ResourceEntity, Pair<String, ResourceType>> cache = cacheContext
					.createCache(RESOURCE_ENTITY_CACHE, new ResourceLookupDao().enableSecondaryKeyManagement());
			initializeCache(cache);
		}
	}

	private void initializeCache(EntityLookupCache<String, ResourceEntity, Pair<String, ResourceType>> cache) {
		List<ResourceEntity> list = entityDao.getAllResources();
		for (ResourceEntity entity : list) {
			cache.setValue(entity.getId(), entity);
		}
	}

	/**
	 * Gets the all resources as read only instances. The returned collection and elements will be sealed based on the
	 * {@link Sealable} implementation.
	 * <p>
	 * <b>Any modification attempt will fail silently!</b>
	 *
	 * @param resourceType the resource type
	 * @return the all resources
	 */
	protected List<Resource> getAllResourcesReadOnly(ResourceType resourceType) {
		return allResources.computeIfAbsent(resourceType, this::loadAllResources);
	}

	private List<Resource> loadAllResources(ResourceType type) {
		List<ResourceEntity> entities;
		if (type == ResourceType.ALL) {
			entities = entityDao.getAllResources();
		} else {
			entities = entityDao.getAllResourcesByType(type);
		}
		List<Resource> resources = entities.stream().map(this::convertToResource).collect(Collectors.toList());
		propertiesService.loadProperties(resources);
		instanceTypes.resolveTypes(resources);
		// during initial user import the types cannot be resolved as the instances are not present in semanticDb/Solr
		// to be resolved in the first place as we need to initialize them explicitly
		resources.stream()
				.filter(resource -> resource.type() == null)
				.forEach(resource -> resource.setType(resolveType(resource.getType())));
		resources.forEach(Sealable::seal);
		return new SealedList<>(resources);
	}

	private InstanceType resolveType(ResourceType type) {
		InstanceType instanceType = null;
		if (type == GROUP) {
			instanceType = instanceTypes.from(EMF.GROUP.toString())
					.orElseGet(() -> InstanceType.create(EMF.GROUP.toString()));
		} else if (type == USER) {
			instanceType = instanceTypes.from(EMF.USER.toString())
					.orElseGet(() -> InstanceType.create(EMF.USER.toString()));
		}
		return instanceType;
	}

	/**
	 * Update existing resource. Will fail with {@link DatabaseException} if the entity does not exists or does not have
	 * a system id
	 *
	 * @param resource the resource to save
	 * @return the updated resource
	 */
	protected Resource updateResource(Resource resource) {
		EntityLookupCache<Serializable, ResourceEntity, Pair<String, ResourceType>> cache = getCache();

		Pair<Serializable, ResourceEntity> pair = cache.getByKey(resource.getId());
		if (pair == null) {
			throw new DatabaseException("No such resource for id: " + resource.getId());
		}

		resource.addIfNotPresent(IS_DELETED, Boolean.FALSE);
		setDefinitionId(resource);

		ResourceEntity entity = pair.getSecond();
		entity.setDisplayName(resource.getDisplayName());
		entity.setActive(resource.isActive());
		if (resource.getIdentifier() == null) {
			DefinitionModel model = definitionService.getInstanceDefinition(resource);
			if (model != null) {
				resource.setIdentifier(model.getIdentifier());
			}
		}
		entity.setDefinitionId(resource.getIdentifier());

		cache.updateValue(pair.getFirst(), entity);

		addModifyInfo(resource);

		Resource oldVersion = loadProperties(convertToResource(entity));

		saveProperties(resource);

		eventService.fire(new ResourceUpdatedEvent(resource, oldVersion));

		resetCache();
		return resource;
	}

	private void addModifyInfo(Resource resource) {
		resource.add(MODIFIED_ON, new Date());
		resource.add(MODIFIED_BY, securityContextManager.getCurrentContext().getAuthenticated().getSystemId());
	}

	/**
	 * Import new resource.
	 *
	 * @return the resource
	 */
	protected Resource importResource(Resource resource) {
		setTypeIfMissing(resource);

		// initialize
		stateService.changeState(resource, SYNCHRONIZE);
		return persistNewResource(resource, SYNCHRONIZE);
	}

	private static void setTypeIfMissing(Resource resource) {
		if (resource.type() == null) {
			ResourceType resourceType = resource.getType();
			if (resourceType.equals(ResourceType.USER)) {
				resource.setType(InstanceType.create(EMF.USER.toString()));
			} else if (resourceType.equals(ResourceType.GROUP)) {
				resource.setType(InstanceType.create(EMF.GROUP.toString()));
			}
		}
	}

	/**
	 * Persist new resource to database and updates the cache.
	 *
	 * @param resource the resource to persist
	 * @param operation the operation that triggered the new user - create/synchronize
	 * @return the updated resource
	 */
	protected Resource persistNewResource(Resource resource, Operation operation) {

		if (resource.getId() == null) {
			String resourceDbId = ResourceEntityDao.generateResourceDbId(resource.getName(),
					() -> idManager.generateId().toString());
			resource.setId(resourceDbId);
			idManager.register(resource);
		}

		resource.addIfNotPresent(IS_DELETED, Boolean.FALSE);
		setDefinitionId(resource);

		ResourceEntity entity = convertToEntity(resource);
		getCache().updateValue(entity.getId(), entity);

		addDefaultProperties(resource);

		setTitle(resource);

		addCreatorInfo(resource);
		addModifyInfo(resource);

		// if we are importing user that tries to log in and it's valid we set the create operation to log it in audit
		Options.CURRENT_OPERATION.set(operation);
		try {
			// if some custom properties should be added they must come here
			eventService.fire(new ResourceAddedEvent(resource));

			saveProperties(resource);

			eventService.fire(new ResourcePersistedEvent(resource, null, null));
		} finally {
			Options.CURRENT_OPERATION.clear();
		}

		resetCache();
		return resource;
	}

	private void addDefaultProperties(Resource resource) {
		DefinitionModel definition = definitionService.getInstanceDefinition(resource);
		if (definition != null) {
			Map<String, Serializable> model = PropertiesEvaluationHelper.evaluateDefaultPropertiesForModel(definition,
					resource, expressionsManager, idManager);
			// does not override properties just add the missing one
			model.keySet().removeAll(resource.getOrCreateProperties().keySet());
			resource.addAllProperties(model);
		}

		if (resource.getType() == USER) {
			User user = (User) resource;
			if (user.getLanguage() == null) {
				user.setLanguage(systemConfigs.getSystemLanguage());
			}
		}
	}

	private void addCreatorInfo(Resource resource) {
		resource.addIfNotPresent(CREATED_BY,
				securityContextManager.getCurrentContext().getAuthenticated().getSystemId());
		resource.addIfNotPresent(CREATED_ON, new Date());
	}

	/**
	 * Checks if resource exists with the given system id or name
	 *
	 * @param resourceId the resource id to check for
	 * @return true, if exists and <code>false</code> if not.
	 */
	protected boolean resourceExists(Serializable resourceId) {
		return !StringUtils.isBlank(Objects.toString(resourceId, null)) && entityDao.resourceExists(resourceId);

	}

	/**
	 * Gets a resource entity that matches any of the provided information
	 *
	 * @param id the database id of the resource
	 * @param name the name of the resource
	 * @param type the type of the resource
	 * @return the resource entity or <code>null</code> if not found
	 * @see #findById(String)
	 * @see #findByName(String, ResourceType)
	 */
	protected ResourceEntity getResourceEntity(Serializable id, String name, ResourceType type) {
		ResourceEntity resourceEntity = null;
		if (id != null) {
			resourceEntity = findById((String) id);
		} else if (StringUtils.isNotBlank(name) && type != null) {
			resourceEntity = findByName(name, type);
		}
		return resourceEntity;
	}

	/**
	 * Find resource by system id in the cache or in the database
	 *
	 * @param systemId the system id
	 * @return the resource entity
	 */
	protected ResourceEntity findById(String systemId) {
		if (systemId == null) {
			return null;
		}
		Pair<Serializable, ResourceEntity> pair = getCache().getByKey(systemId);
		return pair == null ? null : pair.getSecond();
	}

	/**
	 * Find resource by name and it's type in the cache or in the database
	 *
	 * @param name the name
	 * @param resourceType the resource type
	 * @return the resource entity
	 */
	@SuppressWarnings("boxing")
	protected ResourceEntity findByName(String name, ResourceType resourceType) {
		if (name == null || resourceType == null) {
			return null;
		}
		ResourceEntity entity = new ResourceEntity();
		entity.setIdentifier(name);
		entity.setType(resourceType.getType());
		Pair<Serializable, ResourceEntity> pair = getCache().getByValue(entity);
		return pair == null ? null : pair.getSecond();
	}

	/**
	 * Find resource by it's system id and converts it to instance with properties. The entity will be fetched from the
	 * cache or database
	 *
	 * @param systemId the system id
	 * @return the resource instance
	 */
	protected Resource findResourceById(String systemId) {
		ResourceEntity entity = findById(systemId);
		if (entity == null) {
			return null;
		}
		return loadProperties(convertToResource(entity));
	}

	/**
	 * Find resource by name and resource type
	 *
	 * @param name the name of the resource
	 * @param resourceType the resource type
	 * @return the resource instance with loaded properties if found or <code>null</code> if not found
	 */
	protected Resource findResourceByName(String name, ResourceType resourceType) {
		ResourceEntity entity = findByName(name, resourceType);
		if (entity == null) {
			return null;
		}
		return loadProperties(convertToResource(entity));
	}

	/**
	 * Delete permanently a resource identified by resource
	 *
	 * @param entity the entity that represents a valid resource {@link Resource} or {@link ResourceEntity}
	 */
	protected void deleteResource(Entity<? extends Serializable> entity, boolean permanently) {
		if (entity == null || entity.getId() == null) {
			return;
		}
		Pair<Serializable, ResourceEntity> value = getCache().getByKey(entity.getId());
		if (value == null) {
			// no such resource
			return;
		}
		Resource resource = convertToResource(value.getSecond());
		if (permanently) {
			propertiesService.removeProperties(resource, resource);
			getCache().deleteByKey(entity.getId());
			resetCache();
		} else {
			resource.markAsDeleted();
			updateResource(resource);
		}
		if (resource.getType() == GROUP) {
			// remove all members of the given group
			entityDao.removeAllMembers(resource.getId().toString());
		}
		// remove memberships of the given resource from all groups
		entityDao.removeParticipation(resource);
	}

	private static void setTitle(Resource resource) {
		resource.add(DefaultProperties.TITLE, resource.getDisplayName());
	}

	private void setDefinitionId(Resource resource) {
		if (resource.getIdentifier() == null) {
			String definitionId = definitionService.getDefaultDefinitionId(resource);
			if (definitionId == null) {
				throw new EmfRuntimeException("Missing instance definition for object with id: " + resource.getId());
			}
			resource.setIdentifier(definitionId);
		}
	}

	private void resetCache() {
		allResources.clear();
	}

	private void saveProperties(Resource resource) {
		if (resource != null && resource.getProperties() != null && !resource.getProperties().isEmpty()) {
			// enable custom properties saving
			Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.enable();
			try {
				propertiesService.saveProperties(resource, false);
			} finally {
				Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.disable();
			}
		}
	}

	private <R extends Resource> R loadProperties(R resource) {
		propertiesService.loadProperties(resource);
		instanceTypes.from(resource);
		return resource;
	}

	protected EntityLookupCache<Serializable, ResourceEntity, Pair<String, ResourceType>> getCache() {
		return cacheContext.getCache(RESOURCE_ENTITY_CACHE);
	}

	/**
	 * Converts a single entity instance to resource. No properties are loaded, yet.
	 *
	 * @param entity the entity to convert
	 * @return the resource instance
	 */
	@SuppressWarnings("boxing")
	protected Resource convertToResource(ResourceEntity entity) {
		Resource result = null;
		if (EqualsHelper.nullSafeEquals(entity.getType(), USER.getType())) {
			result = mapper.map(entity, EmfUser.class);
		} else if (EqualsHelper.nullSafeEquals(entity.getType(), GROUP.getType())) {
			result = mapper.map(entity, EmfGroup.class);
		} else if (EqualsHelper.nullSafeEquals(entity.getType(), ResourceType.SYSTEM.getType())) {
			result = mapper.map(entity, EmfGroup.class);
		} else if (EqualsHelper.nullSafeEquals(entity.getType(), ResourceType.UNKNOWN.getType())) {
			// this should not come here but for backward compatibility
			result = mapper.map(entity, EmfUser.class);
		}
		if (result != null) {
			if (result instanceof User) {
				// ensure the current tenant id is set for the loaded users
				((User) result).setTenantId(securityContextManager.getCurrentContext().getCurrentTenantId());
			}
			return result;
		}
		throw new EmfConfigurationException("Unsupported resource entity type: " + entity);
	}

	/**
	 * Converts the given resource to entity resource.
	 *
	 * @param resource the resource
	 * @return the resource entity
	 */
	protected ResourceEntity convertToEntity(Object resource) {
		return mapper.map(resource, ResourceEntity.class);
	}

	/**
	 * Entity lookup cache for project resource entity.
	 *
	 * @author BBonev
	 */
	protected class ResourceLookupDao extends BaseEntityLookupDao<ResourceEntity, Pair<String, ResourceType>, String> {

		@Override
		protected Class<ResourceEntity> getEntityClass() {
			return ResourceEntity.class;
		}

		@Override
		protected Logger getLogger() {
			return LOGGER;
		}

		@Override
		protected DbDao getDbDao() {
			return dbDao;
		}

		@Override
		@SuppressWarnings("boxing")
		protected Pair<String, ResourceType> getValueKeyInternal(ResourceEntity value) {
			if (StringUtils.isBlank(value.getIdentifier())) {
				return null;
			}
			return new Pair<>(value.getIdentifier().toLowerCase(), ResourceType.getById(value.getType()));
		}

		@Override
		public Pair<String, ResourceEntity> createValue(ResourceEntity value) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected List<ResourceEntity> fetchEntityByValue(Pair<String, ResourceType> key) {
			String identifier = key.getFirst();
			ResourceType type = key.getSecond();
			List<ResourceEntity> list = entityDao.findResourcesByNameAndType(Collections.singletonList(identifier), type);
			if (isNotEmpty(list)) {
				return list;
			}
			if (type == USER) {
				return getUserFromRemoteStore(identifier);
				// if there are new resources added run the synchronizations to fully import them
			} else if (type == GROUP && isExistingGroup(identifier)) {
				Group group = new EmfGroup(identifier, identifier);
				Resource imported = importResource(group);
				ResourceEntity importedEntity = convertToEntity(imported);
				return Collections.singletonList(importedEntity);
			}
			return Collections.emptyList();
		}

		private boolean isExistingGroup(String identifier) {
			try {
				return remoteUserStore.isExistingGroup(identifier);
			} catch (RemoteStoreException e) {
				throw new EmfRuntimeException("Could not check of group " + identifier + " exists", e);
			}
		}

		private List<ResourceEntity> getUserFromRemoteStore(String identifier) {
			try {
				Optional<User> user = remoteUserStore.getUserData(identifier);
				if (!user.isPresent()) {
					return Collections.emptyList();
				}

				ResourceEntity entity = transactionSupport.invokeInNewTx(() -> importAndConvert(user));

				synchronizationRunner.runAll();
				return Collections.singletonList(entity);
			} catch (RemoteStoreException e) {
				throw new EmfRuntimeException("Could not fetch data for user " + identifier, e);
			}
		}

		private ResourceEntity importAndConvert(Optional<User> user) {
			return user.map(ResourceStore.this::importResource)
					.map(ResourceStore.this::convertToEntity)
					.orElseThrow(IllegalStateException::new);
		}

	}
}
