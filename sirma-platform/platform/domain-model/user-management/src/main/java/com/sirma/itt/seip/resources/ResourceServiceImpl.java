package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_DEFAULT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_ACTIVE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_DELETED;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.dao.BatchEntityLoader;
import com.sirma.itt.seip.instance.dao.BatchEntityLoader.BatchSecondaryKeyEntityLoaderCallback;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.OperationExecutedEvent;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;

/**
 * Default project service implementation.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ResourceServiceImpl implements ResourceService {
	private static final Operation SYNCHRONIZE = new Operation(ActionTypeConstants.SYNCHRONIZE);
	private static final Operation ACTIVATE = new Operation(ActionTypeConstants.ACTIVATE);
	private static final Operation DEACTIVATE = new Operation(ActionTypeConstants.DEACTIVATE);
	@Inject
	protected ObjectMapper mapper;

	@Inject
	private ResourceEntityDao resourceDao;
	@Inject
	private ResourceStore resourceStore;

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private PropertiesService propertiesService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private InstanceLoadDecorator loadDecorator;
	@Inject
	private ServiceRegistry serviceRegistry;
	@Inject
	private EventService eventService;

	@Inject
	private SecurityContextManager securityManager;

	@Inject
	private InstanceTypes instanceTypes;

	private final BatchSecondaryKeyEntityLoaderCallback<Serializable, ResourceEntity, Pair<String, ResourceType>> secondaryKeyCallback = new ResourceLoaderBySecondaryKey();

	/** The id all other users. */
	private static String ID_ALL_OTHER_USERS = Security.SYSTEM_ALL_OTHER_USERS.getLocalName();

	static String ALL_OTHER_HEADER = "<a class=\"instance-link\" href=\"\"><span data-property=\"title\">"
			+ ID_ALL_OTHER_USERS + "</span></a>";

	/** The all other authority. */
	private Resource allOtherAuthority = initAllOtherAuthority();

	private static Resource initAllOtherAuthority() {
		EmfGroup authority = new EmfGroup(ID_ALL_OTHER_USERS, ID_ALL_OTHER_USERS);
		authority.setType(ResourceType.SYSTEM);
		authority.setId(Security.PREFIX + ":" + ID_ALL_OTHER_USERS);
		authority.add(IS_DELETED, Boolean.FALSE);
		authority.add(IS_ACTIVE, Boolean.TRUE);
		authority.add(HEADER_DEFAULT, ALL_OTHER_HEADER);
		authority.add(HEADER_COMPACT, ALL_OTHER_HEADER);
		authority.add(HEADER_BREADCRUMB, ALL_OTHER_HEADER);
		authority.setType(InstanceType.create(EMF.GROUP.toString()));
		authority.seal();
		return authority;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(TxType.REQUIRED)
	public <R extends Resource> R saveResource(R resource) {
		if (resource == null) {
			return resource;
		}
		OperationExecutedEvent event;

		setTitle(resource);
		setSemanticType(resource);

		// if new resource just import it
		if (!isResourceExistsInDb(resource)) {
			return (R) resourceStore.importResource(resource);
		}

		// if the resource does not exists this will trigger synchronization with the external systems and we do not
		// want that when saving resource during synchronization
		ResourceEntity current = resourceStore.getResourceEntity(resource.getId(), resource.getName(),
				resource.getType());
		if (resource.getId() == null) {
			resource.setId(current.getId());
		}
		Resource instance;
		if (!current.getActive().booleanValue() && resource.isActive()) {
			instance = activateInternal(resource, ACTIVATE);
		} else if (current.getActive().booleanValue() && !resource.isActive()) {
			instance = deactivateInternal(resource, DEACTIVATE);
		} else {
			event = new OperationExecutedEvent(SYNCHRONIZE, resource);
			eventService.fire(event);

			instance = resourceStore.updateResource(resource);
		}

		return (R) instance;
	}

	private Resource saveResourceInternal(Resource resource, Operation operation) {
		setTitle(resource);
		setSemanticType(resource);

		if (!isResourceExistsInDb(resource)) {
			return resourceStore.persistNewResource(resource,
					Operation.getNotNull(operation, ActionTypeConstants.CREATE));
		}
		// if the resource does not exists this will trigger synchronization with the external systems and we do not
		// want that when saving resource during synchronization
		ResourceEntity current = resourceStore.getResourceEntity(resource.getId(), resource.getName(),
				resource.getType());
		if (resource.getId() == null) {
			resource.setId(current.getId());
		}
		return resourceStore.updateResource(resource);
	}

	private boolean isResourceExistsInDb(Resource resource) {
		return resourceStore.resourceExists(resource.getId()) || resourceStore.resourceExists(resource.getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R getResource(Serializable id) {
		return (R) resourceStore.findResourceById((String) id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R getResource(String name, ResourceType type) {
		if (type == ResourceType.UNKNOWN) {
			Resource resource = getResourceInternal(null, name, ResourceType.USER, true);
			if (resource == null) {
				resource = getResourceInternal(null, name, ResourceType.GROUP, true);
			}
			return (R) resource;
		}
		return (R) resourceStore.findResourceByName(name, type);
	}

	protected Resource getResourceInternal(Serializable id, String name, ResourceType type, boolean loadProperties) {
		Resource resource = null;
		ResourceEntity resourceEntity = resourceStore.getResourceEntity(id, name, type);
		if (resourceEntity != null) {
			resource = resourceStore.convertToResource(resourceEntity);
		}
		if (resource == null) {
			Resource allOtherUsers = getAllOtherUsers();
			if (nullSafeEquals(id, allOtherUsers.getId()) || nullSafeEquals(name, allOtherUsers.getName())) {
				return allOtherUsers;
			}
		}
		if (resource != null && loadProperties) {
			loadProperties(resource);
		}
		return resource;
	}

	protected Resource getCachedResource(Serializable id, String name, ResourceType type) {
		Resource resource = null;

		if (id != null) {
			resource = resourceStore.findResourceById(id.toString());
		} else if (name != null && type != null) {
			resource = resourceStore.findResourceByName(name, type);
		}

		if (resource == null) {
			Resource allOtherUsers = getAllOtherUsers();
			if (nullSafeEquals(id, allOtherUsers.getId()) || nullSafeEquals(name, allOtherUsers.getName())) {
				return allOtherUsers;
			}
		}
		return resource;
	}

	/**
	 * Sets the title.
	 *
	 * @param <R>
	 *            the generic type
	 * @param resource
	 *            the new title
	 */
	private static <R extends Resource> void setTitle(R resource) {
		resource.add(DefaultProperties.TITLE, resource.getDisplayName());
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
		if (resource.getType() == ResourceType.SYSTEM) {
			type = EmfResource.class.getName();
		} else if (resource.getType() == ResourceType.GROUP) {
			type = EmfGroup.class.getName();
		}
		DataTypeDefinition definition = dictionaryService.getDataTypeDefinition(type);
		if (definition != null && definition.getFirstUri() != null) {
			resource.add(DefaultProperties.SEMANTIC_TYPE, typeConverter.convert(Uri.class, definition.getFirstUri()));
		}
	}

	/**
	 * Save properties.
	 *
	 * @param resource
	 *            the resource
	 */
	void saveProperties(Resource resource) {
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

	/**
	 * Load properties. the generic type
	 *
	 * @param resource
	 *            the resource
	 */
	private void loadProperties(Instance resource) {
		propertiesService.loadProperties(resource);
		loadDecorator.decorateInstance(resource);
	}

	private void loadProperties(List<Instance> result) {
		loadDecorator.decorateResult(result);
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
	protected <S extends Serializable> List<Instance> batchFetchResourcesInternal(Collection<S> ids,
			boolean keepOrder) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		// create 2 different containers based if we need to keep the order or not
		Map<Serializable, Instance> mapResult = batchFetchResourceMappingInternal(ids, true);

		if (keepOrder) {
			// sort the results
			List<Instance> sortedResult = new ArrayList<>(mapResult.size());
			for (Serializable key : ids) {
				Instance instance = mapResult.get(key);
				if (instance != null) {
					sortedResult.add(instance);
				}
			}
			return sortedResult;
		}
		return new ArrayList<>(mapResult.values());
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
	protected <S extends Serializable> Map<Serializable, Instance> batchFetchResourceMappingInternal(Collection<S> ids,
			boolean loadProperties) {
		Map<Serializable, Instance> mapResult = CollectionUtils.createLinkedHashMap(ids.size());
		List<Instance> toLoadProperties = Collections.emptyList();
		if (loadProperties) {
			toLoadProperties = new ArrayList<>(ids.size());
		}
		Set<Serializable> secondPass = new LinkedHashSet<>();
		// first we check for hits in the cache and filter the cache misses
		EntityLookupCache<Serializable, ResourceEntity, Pair<String, ResourceType>> cache = resourceStore.getCache();
		for (Serializable dbId : ids) {
			if (cache == null) {
				// no cache or not found in cache search later in DB
				secondPass.add(dbId);
				continue;
			}
			// convert the cache entry to instance and schedule
			// properties loading instead of one by one loading
			ResourceEntity entity = cache.getValue(dbId);
			if (entity != null) {
				Instance instance = resourceStore.convertToResource(entity);
				mapResult.put(dbId, instance);
				if (loadProperties) {
					toLoadProperties.add(instance);
				}
			} else {
				secondPass.add(dbId);
			}
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<ResourceEntity> list = findResourceEntities(secondPass);

			for (ResourceEntity entity : list) {

				if (cache != null) {
					// update cache
					cache.setValue(entity.getId(), entity);
				}

				Instance instance = resourceStore.convertToResource(entity);
				mapResult.put(instance.getId(), instance);
				if (loadProperties) {
					toLoadProperties.add(instance);
				}
			}
		}
		if (loadProperties) {
			propertiesService.loadProperties(toLoadProperties);
			instanceTypes.resolveTypes(toLoadProperties);
		}
		return mapResult;
	}

	/**
	 * Batch fetch resource entities mapping internal. Method is optimized to work only with entities, without convert
	 *
	 * @param ids
	 *            the ids of the entities
	 * @return the map of loaded entities
	 */
	protected Map<Serializable, ResourceEntity> batchFetchResourceEntitiesMappingInternal(
			Collection<Serializable> ids) {
		Map<Serializable, ResourceEntity> mapResult = CollectionUtils.createLinkedHashMap(ids.size());
		Set<Serializable> secondPass = new LinkedHashSet<>();
		// first we check for hits in the cache and filter the cache misses
		EntityLookupCache<Serializable, ResourceEntity, Pair<String, ResourceType>> cache = resourceStore.getCache();
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

			for (ResourceEntity entity : list) {
				if (cache != null) {
					// update cache
					cache.setValue(entity.getId(), entity);
				}
				mapResult.put(entity.getId(), entity);
			}
		}
		return mapResult;
	}

	/**
	 * Find entities.
	 *
	 * @param ids
	 *            the ids
	 * @return the list
	 */
	protected List<ResourceEntity> findResourceEntities(Set<Serializable> ids) {
		return resourceDao.findResourceEntities(ids);
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
		return resourceStore.convertToEntity(resource);
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

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> List<R> getAllResources(ResourceType type, String sortColumn) {

		if (type == null) {
			return Collections.emptyList();
		}

		return (List<R>) resourceStore.getAllResourcesReadOnly(type);
	}

	@Override
	public <R extends Resource> List<R> getAllActiveResources(ResourceType type, String sortColumn) {
		List<R> resources = getAllResources(type, sortColumn);
		return resources.stream().filter(Objects::nonNull).filter(Resource::isActive).collect(Collectors.toList());
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
		return resourceDao.findResourcesByNameAndType(ids, type);
	}

	@Override
	public List<Instance> getContainingResources(Serializable resourceId) {
		return loadByDbId(resourceDao.getContainingGroups((String) resourceId));
	}

	@Override
	public List<Instance> getContainedResources(Serializable resourceId) {
		return loadByDbId(resourceDao.getMemberIdsOf((String) resourceId));
	}

	@Override
	public List<Instance> getContainedResources(Collection<?> resourceNames, ResourceType filterBy) {
		if (resourceNames == null) {
			return Collections.emptyList();
		}
		// assure we don't have duplications in final result
		Set<Instance> resources = new HashSet<>();
		for (Object resourceName : resourceNames) {
			Resource found = parseAsResource(resourceName);
			if (found != null) {
				// don't need to expand
				if (found.getType() == ResourceType.USER) {
					resources.add(found);
					continue;
				}
				Collection<Instance> containedResources = getContainedResourcesInternal(found, filterBy);
				resources.addAll(containedResources);
			}
		}
		return new LinkedList<>(resources);
	}

	@Override
	public List<Instance> getContainedResources(Instance resource, ResourceType filterBy) {
		if (resource == null) {
			return Collections.emptyList();
		}
		return getContainedResourcesInternal(resource, filterBy);
	}

	private List<Instance> getContainedResourcesInternal(Instance resource, ResourceType filterBy) {
		List<Instance> resources = getContainedResources(resource.getId());
		List<Instance> result = new LinkedList<>();
		ResourceType filter = filterBy;
		if (filter == null) {
			filter = ResourceType.ALL;
		}
		for (Instance contained : resources) {
			if (filter == ResourceType.ALL || isType(contained, filter)) {
				result.add(contained);
			}
			if (isGroup(contained)) {
				result.addAll(getContainedResourcesInternal(contained, filter));
			}
		}
		return result;
	}

	@Override
	public List<String> getContainedResourceIdentifiers(Resource resource, ResourceType filterBy) {
		return getContainedResourceNamesInternal(resource, filterBy);
	}

	private List<String> getContainedResourceNamesInternal(Instance resource, ResourceType filterBy) {
		List<Instance> resources = getContainedResources(resource.getId());
		List<String> result = new LinkedList<>();
		ResourceType filter = filterBy;
		if (filter == null) {
			filter = ResourceType.ALL;
		}
		for (Instance contained : resources) {
			if (filter == ResourceType.ALL || isType(contained, filter)) {
				result.add(((Resource) contained).getName());
			}
			if (isGroup(contained)) {
				result.addAll(getContainedResourceNamesInternal(contained, filter));
			}
		}
		return result;
	}

	@Override
	public List<String> getContainedResourceIdentifiers(Resource resource) {
		return resourceDao.getMemberIdsOf((String) resource.getId());
	}

	@Override
	public void modifyMembers(Resource resource, Collection<String> addMembers, Collection<String> removeMembers) {
		Set<String> toAdd = toSystemIds(addMembers);
		resourceDao.addMembers(resource.getId().toString(), toAdd);

		Set<String> toRemove = toSystemIds(removeMembers);
		resourceDao.removeMembers(resource.getId().toString(), toRemove);

	}

	private Set<String> toSystemIds(Collection<String> ids) {
		return ids
				.stream()
					.map(id -> findResource(id))
					.filter(Objects::nonNull)
					.map(r -> ((Resource) r).getId().toString())
					.collect(Collectors.toSet());
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getDisplayName(Serializable resource) {
		String displayName;

		if (resource instanceof Collection) {
			displayName = ((Collection<Object>) resource)
					.stream()
						.map(this::extractDisplayName)
						.collect(Collectors.joining(", "));
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
	 * Parses the given argument to resource. The method will try to get a resource or instance that represent a
	 * resource based on the argument.
	 *
	 * @param args
	 *            the argument to parse
	 * @return the instance or <code>null</code>. The instance could be a {@link Resource} instance.
	 */
	private Resource parseAsResource(Object args) {
		if (args == null) {
			// no need to check anymore
			return null;
		}
		Resource instance = null;
		if (args instanceof String && StringUtils.isNotNullOrEmpty((String) args)) {
			instance = getResourceFromString(args.toString());
		} else if (args instanceof Resource) {
			// no check for resource because it will fall in here also
			instance = getResourceFromResource((Resource) args);
		} else if (args instanceof Instance) {
			instance = parseAsResource(((Instance) args).getId());
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
					LOGGER.warn("Tryied to conver the given object " + args + " to " + Resource.class
							+ " but resulted null");
				}
			} catch (TypeConversionException e) {
				LOGGER.warn("Cannot convert the resource " + args + " to " + Resource.class, e);
			}
		}
		return instance;
	}

	/**
	 * Gets the resource from instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the resource from instance
	 */
	private Resource getResourceFromResource(Resource instance) {
		return getCachedResource(instance.getId(), instance.getName(), instance.getType());
	}

	/**
	 * Gets the resource from string. Tries to determine the source string format and to return a resource that is
	 * represented by the given string
	 *
	 * @param source
	 *            the source string to parse
	 * @return the loaded resource or <code>null</code> if not a valid resource or not found.
	 */
	private Resource getResourceFromString(String source) {
		Resource instance = null;
		// probably instance reference as string
		if (source.startsWith("{")) {
			InstanceReference reference = typeConverter.convert(InstanceReference.class, source);
			if (reference != null && reference.toInstance() instanceof Resource) {
				instance = (Resource) reference.toInstance();
			} else {
				LOGGER.warn("No instance found when loading from " + source);
			}
		} else
		// check if it's URI/primary key
		if (source.contains(":")) {
			ShortUri shortUri = typeConverter.convert(ShortUri.class, source);
			String uri = source;
			if (shortUri != null) {
				uri = shortUri.toString();
			}
			Resource result = getCachedResource(uri, null, null);
			if (result != null) {
				instance = result;
			} else {
				LOGGER.warn("Searching for a resource by URI=" + source + " but was not found!");
			}
		} else if (source.startsWith("GROUP_")) {
			instance = getCachedResource(null, source, ResourceType.GROUP);
		} else {
			// probably a user/group name
			Resource foundResource = getCachedResource(null, source, ResourceType.USER);
			if (foundResource == null) {
				String groupname = "GROUP_" + source;
				foundResource = getCachedResource(null, groupname, ResourceType.GROUP);
			}
			instance = foundResource;
		}
		return instance;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public Instance save(Instance resource, Operation operation) {
		if (resource == null) {
			return resource;
		}
		if (operation != null) {
			Options.CURRENT_OPERATION.set(operation);
		}
		try {
			Instance oldVersion = findResource(resource);

			eventService.fire(new OperationExecutedEvent(operation, resource));

			Resource instance = saveResourceInternal((Resource) resource, operation);

			loadDecorator.decorateInstance(instance);
			eventService.fire(serviceRegistry.getEventProvider(resource).createPersistedEvent(instance, oldVersion,
					Operation.getOperationId(operation)));
			return instance;
		} finally {
			if (operation != null) {
				Options.CURRENT_OPERATION.clear();
			}
		}
	}

	@Override
	public Resource loadByDbId(Serializable id) {
		return getResourceInternal(id, null, null, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Resource load(Serializable instanceId) {
		if (instanceId instanceof Pair) {
			Pair<String, ResourceType> pair = (Pair<String, ResourceType>) instanceId;
			return getResource(pair.getFirst(), pair.getSecond());
		} else if (instanceId instanceof String) {
			return getResource((String) instanceId, ResourceType.UNKNOWN);
		}
		return null;
	}

	@Override
	public <S extends Serializable> List<Instance> load(List<S> ids) {
		return load(ids, true);
	}

	@Override
	public <S extends Serializable> List<Instance> loadByDbId(List<S> ids) {
		return batchFetchResourcesInternal(ids, true);
	}

	@Override
	public <S extends Serializable> List<Instance> loadByDbId(List<S> ids, boolean allProperties) {
		return batchFetchResourcesInternal(ids, true);
	}

	@Override
	public <S extends Serializable> List<Instance> load(List<S> ids, boolean allProperties) {
		List<ResourceEntity> entities = BatchEntityLoader.batchLoadBySecondaryKey(convertToPairCollection(ids),
				resourceStore.getCache(), secondaryKeyCallback);
		List<Instance> result = new ArrayList<>(entities.size());
		for (ResourceEntity resourceEntity : entities) {
			result.add(resourceStore.convertToResource(resourceEntity));
		}
		loadProperties(result);
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
	private static List<Pair<String, ResourceType>> convertToPairCollection(List<?> list) {
		return (List<Pair<String, ResourceType>>) list;
	}

	@Override
	public void delete(Instance instance, Operation operation, boolean permanent) {
		if (!(instance instanceof Resource)) {
			return;
		}
		Resource resource = (Resource) instance;
		ResourceEntity entity = resourceStore.getResourceEntity(resource.getId(), resource.getName(),
				resource.getType());
		if (entity == null) {
			return;
		}
		if (isInternalResource(entity.getId())) {
			return;
		}
		TwoPhaseEvent deleteEvent = serviceRegistry
				.getEventProvider(resource)
					.createBeforeInstanceDeleteEvent(resource);
		eventService.fire(deleteEvent);
		LOGGER.info("Deleting resource {}", entity.getId());
		resourceStore.deleteResource(entity, permanent);
		eventService.fireNextPhase(deleteEvent);
	}

	@Override
	public <R extends Resource> R activate(Serializable resourceId, Operation operation) {
		R resource = findResource(resourceId);
		if (resource == null || resource.isActive() || !ResourceType.isUserOrGroup(resource.getType())) {
			return resource;
		}
		return activateInternal(resource, operation);
	}

	@SuppressWarnings("unchecked")
	private <R extends Resource> R activateInternal(R resource, Operation operation) {
		LOGGER.info("Activating resource {}", resource.getId());

		resource.setActive(true);
		return (R) save(resource, operation);
	}

	@Override
	public <R extends Resource> R deactivate(Serializable resourceId, Operation operation) {
		R resource = findResource(resourceId);
		if (resource == null || !resource.isActive() || !ResourceType.isUserOrGroup(resource.getType())) {
			return resource;
		}
		return deactivateInternal(resource, operation);
	}

	@SuppressWarnings("unchecked")
	private <R extends Resource> R deactivateInternal(R resource, Operation operation) {
		LOGGER.info("Deactivating resource {}", resource.getId());

		resource.setActive(false);
		if (resource.getType() == ResourceType.GROUP) {
			// remove all members on group deactivation
			resourceDao.removeAllMembers(Objects.toString(resource.getId(), null));
		}
		return (R) save(resource, operation);
	}

	private boolean isInternalResource(String id) {
		return nullSafeEquals(id, securityManager.getAdminUser().getSystemId())
				|| nullSafeEquals(id, securityManager.getSystemUser().getSystemId())
				|| nullSafeEquals(id, securityManager.getSuperAdminUser().getSystemId());
	}

	/**
	 * The Class ResourceLoaderBySecondaryKey.
	 *
	 * @author BBonev
	 */
	private final class ResourceLoaderBySecondaryKey
			implements BatchSecondaryKeyEntityLoaderCallback<Serializable, ResourceEntity, Pair<String, ResourceType>> {

		@Override
		public Serializable getPrimaryKey(ResourceEntity entity) {
			return entity.getId();
		}

		@Override
		@SuppressWarnings("boxing")
		public Pair<String, ResourceType> getSecondaryKey(ResourceEntity entity) {
			return new Pair<>(entity.getIdentifier(), ResourceType.getById(entity.getType()));
		}

		@Override
		public List<ResourceEntity> findEntitiesBySecondaryKey(Set<Pair<String, ResourceType>> secondPass) {
			Collection<String> ids = new ArrayList<>(secondPass.size());
			ResourceType type = secondPass.iterator().next().getSecond();
			for (Pair<String, ResourceType> pair : secondPass) {
				ids.add(pair.getFirst());
			}
			return findResourceInDbBySecondaryKey(ids, type);
		}
	}

	@Override
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

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R findResource(Serializable id) {
		Instance resource = parseAsResource(id);
		if (resource instanceof Resource) {
			return (R) resource;
		}
		return null;
	}

	@Override
	public boolean resourceExists(Serializable id) {
		return resourceStore.resourceExists(id);
	}

	@Override
	public Resource getAllOtherUsers() {
		return allOtherAuthority;
	}
}
