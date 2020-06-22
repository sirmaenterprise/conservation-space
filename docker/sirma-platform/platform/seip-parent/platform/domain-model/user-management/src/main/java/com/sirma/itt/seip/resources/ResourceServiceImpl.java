package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.resources.ResourceProperties.GROUP_PREFIX;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.util.CacheUtils;
import com.sirma.itt.seip.cache.util.CacheUtils.BatchSecondaryKeyEntityLoaderCallback;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.util.InstancePropertyComparator;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Default resource service implementation.
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
	private DefinitionService definitionService;

	@Inject
	private InstanceLoadDecorator loadDecorator;

	@Inject
	private ServiceRegistry serviceRegistry;

	@Inject
	private EventService eventService;

	@Inject
	private SecurityContextManager securityManager;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private InstanceTypes instanceTypes;

	@Inject
	private UserPreferences userPreferences;

	@Inject
	private StateService stateService;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "validation.userName", defaultValue = "^[a-zA-Z0-9]+(?:[\\-\\._][a-zA-Z0-9]+)*(?:@[a-zA-Z0-9]{2,}(?:[\\-\\.]{1}[a-zA-Z0-9]+)*\\.[a-zA-Z0-9]{2,20})?$", system = true, type = Pattern.class, label = "Pattern describing a valid user name")
	private ConfigurationProperty<Pattern> userNameValidationPattern;

	private final BatchSecondaryKeyEntityLoaderCallback<Serializable, ResourceEntity, Pair<String, ResourceType>> secondaryKeyCallback = new ResourceLoaderBySecondaryKey();

	private Resource allOtherUsersGroup = EmfResourcesUtil.createEveryoneGroup();
	private Resource systemAdminGroup = EmfResourcesUtil.createSystemAdminGroup();

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R saveResource(R resource) {
		if (resource == null) {
			return null;
		}

		setTitle(resource);
		setSemanticType(resource);
		ensureActiveAdmin(resource);

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
		if (!current.getActive() && resource.isActive()) {
			instance = activateInternal(resource, ACTIVATE);
		} else if (current.getActive() && !resource.isActive()) {
			instance = deactivateInternal(resource, DEACTIVATE);
		} else {
			stateService.changeState(resource, SYNCHRONIZE);

			instance = resourceStore.updateResource(resource);
		}

		return (R) instance;
	}

	private <R extends Resource> void ensureActiveAdmin(R resource) {
		if (resource.getType() == ResourceType.USER
				&& (nullSafeEquals(resource.getId(), securityManager.getAdminUser().getSystemId())
						|| nullSafeEquals(resource.getName(), securityManager.getAdminUser().getIdentityId()))) {
			// make sure the admin user is always active
			resource.setActive(true);
		}
	}

	private Resource saveResourceInternal(Resource resource, Operation operation) {

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
		if (StringUtils.isBlank(Objects.toString(id, null))) {
			return null;
		}

		return (R) resourceStore.findResourceById(InstanceVersionService.getIdFromVersionId(id).toString());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> R getResource(String name, ResourceType type) {
		if (type == ResourceType.UNKNOWN) {
			Resource resource = getResourceInternal(null, name, ResourceType.USER);
			if (resource == null) {
				resource = getResourceInternal(null, name, ResourceType.GROUP);
			}
			return (R) resource;
		}
		return (R) resourceStore.findResourceByName(name, type);
	}

	private Resource getResourceInternal(Serializable id, String name, ResourceType type) {
		if (id == null && StringUtils.isBlank(name)) {
			return null;
		}

		Resource resource = null;
		Serializable normalizedId = id == null ? null : InstanceVersionService.getIdFromVersionId(id);
		ResourceEntity resourceEntity = resourceStore.getResourceEntity(normalizedId, name, type);
		if (resourceEntity != null) {
			resource = resourceStore.convertToResource(resourceEntity);
		}
		if (resource != null) {
			loadProperties(resource);
		}
		return resource;
	}

	private Resource getCachedResource(Serializable id, String name, ResourceType type) {
		Resource resource = null;

		if (id != null) {
			resource = resourceStore.findResourceById(InstanceVersionService.getIdFromVersionId(id).toString());
		} else if (name != null && type != null) {
			resource = resourceStore.findResourceByName(name, type);
		}

		return resource;
	}

	private static <R extends Resource> void setTitle(R resource) {
		resource.add(DefaultProperties.TITLE, resource.getDisplayName());
	}

	private <R extends Resource> void setSemanticType(R resource) {
		if (resource.isValueNotNull(SEMANTIC_TYPE)) {
			instanceTypes.from(resource);
			return;
		}

		String type = EmfUser.class.getName();
		if (resource.getType() == ResourceType.SYSTEM) {
			type = EmfResource.class.getName();
		} else if (resource.getType() == ResourceType.GROUP) {
			type = EmfGroup.class.getName();
		}
		DataTypeDefinition definition = definitionService.getDataTypeDefinition(type);
		if (definition != null && definition.getFirstUri() != null) {
			resource.add(SEMANTIC_TYPE, typeConverter.convert(Uri.class, definition.getFirstUri()));
			instanceTypes.from(resource);
		}
	}

	private void loadProperties(Instance resource) {
		propertiesService.loadProperties(resource);
		loadDecorator.decorateInstance(resource);
	}

	private void loadProperties(List<Instance> result) {
		loadDecorator.decorateResult(result);
	}

	private <S extends Serializable> List<Instance> batchFetchResourcesInternal(Collection<S> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}

		List<S> normalizedIds = normalizeIdentifiers(ids);
		// create 2 different containers based if we need to keep the order or not
		Map<Serializable, Instance> mapResult = batchFetchResourceMappingInternal(normalizedIds);

		// sort the results
		List<Instance> sortedResult = new ArrayList<>(mapResult.size());
		for (Serializable key : normalizedIds) {
			Instance instance = mapResult.get(key);
			if (instance != null) {
				sortedResult.add(instance);
			}
		}
		return sortedResult;
	}

	@SuppressWarnings("unchecked")
	private static <S extends Serializable> List<S> normalizeIdentifiers(Collection<S> identifiers) {
		return (List<S>) identifiers
				.stream()
					.filter(Objects::nonNull)
					.map(InstanceVersionService::getIdFromVersionId)
					.collect(Collectors.toList());
	}

	private <S extends Serializable> Map<Serializable, Instance> batchFetchResourceMappingInternal(Collection<S> ids) {
		Map<Serializable, Instance> mapResult = CollectionUtils.createLinkedHashMap(ids.size());
		List<Instance> toLoadProperties = new ArrayList<>(ids.size());
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
				toLoadProperties.add(instance);
			} else {
				secondPass.add(dbId);
			}
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<ResourceEntity> list = resourceDao.findResourceEntities(secondPass);

			for (ResourceEntity entity : list) {

				if (cache != null) {
					// update cache
					cache.setValue(entity.getId(), entity);
				}

				Instance instance = resourceStore.convertToResource(entity);
				mapResult.put(instance.getId(), instance);
				toLoadProperties.add(instance);
			}
		}
		propertiesService.loadProperties(toLoadProperties);
		instanceTypes.resolveTypes(toLoadProperties);
		return mapResult;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends Resource> List<R> getAllResources(ResourceType type, String sortByProperty) {
		if (type == null) {
			return Collections.emptyList();
		}

		List<Resource> resources = resourceStore.getAllResourcesReadOnly(type);

		if (sortByProperty != null) {
			InstancePropertyComparator comparator = InstancePropertyComparator.forProperty(sortByProperty)
						.ascending()
						.localeSensitive(Locale.forLanguageTag(userPreferences.getLanguage()))
						.build();
			// the input list of resources is immutable so we cannot sort it directly
			// no need to copy the list to new writable list that internally will create array and sort it
			// this way we will skip applying the results back to the list just create a proxy for the array
			Resource[] array = CollectionUtils.toArray(resources, Resource.class);
			Arrays.sort(array, comparator);
			resources = Arrays.asList(array);
		}

		return (List<R>) resources;
	}

	@Override
	public <R extends Resource> List<R> getAllActiveResources(ResourceType type, String sortColumn) {
		List<R> resources = getAllResources(type, sortColumn);
		return resources.stream().filter(Objects::nonNull).filter(Resource::isActive).collect(Collectors.toList());
	}

	@Override
	public List<Instance> getContainingResources(Serializable resourceId) {
		return getContainingResourcesInternal(resourceId, resourceDao::getContainingGroups);
	}

	private List<Instance> getContainingResourcesInternal(Serializable resourceId,
			Function<String, List<String>> getResources) {
		if (StringUtils.isBlank(Objects.toString(resourceId, null))) {
			return emptyList();
		}

		return loadByDbId(getResources.apply(InstanceVersionService.getIdFromVersionId(resourceId).toString()));
	}

	@Override
	public List<Instance> getContainedResources(Serializable resourceId) {
		return getContainingResourcesInternal(resourceId, resourceDao::getMemberIdsOf);
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
		return resourceDao.getMemberIdsOf(InstanceVersionService.getIdFromVersionId(resource.getId()).toString());
	}

	@Override
	public void modifyMembers(Resource resource, Collection<String> addMembers, Collection<String> removeMembers) {
		InstanceEventProvider<Instance> eventProvider = serviceRegistry.getEventProvider(resource);
		Set<String> toAdd = toSystemIds(addMembers);
		resourceDao.addMembers(resource.getId().toString(), toAdd,
				added -> eventProvider.createAttachEvent(resource, findResource(added)));

		Set<String> toRemove = toSystemIds(removeMembers);
		resourceDao.removeMembers(resource.getId().toString(), toRemove,
				removed -> eventProvider.createDetachEvent(resource, findResource(removed)));

	}

	private Set<String> toSystemIds(Collection<String> ids) {
		return ids
				.stream()
					.map(this::findResource)
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

	private String extractDisplayName(Object resource) {
		String displayName = null;

		Resource asResource = parseAsResource(resource);
		if (asResource != null) {
			displayName = asResource.getDisplayName();
		}
		return displayName;
	}

	/**
	 * Parses the given argument to resource. The method will try to get a resource or instance that represent a
	 * resource based on the argument.
	 *
	 * @param args the argument to parse
	 * @return the instance or <code>null</code>. The instance could be a {@link Resource} instance.
	 */
	private Resource parseAsResource(Object args) {
		if (args == null) {
			// no need to check anymore
			return null;
		}
		return resolveResource(args);
	}

	private Resource resolveResource(Object args) {
		if (args instanceof String && StringUtils.isNotBlank((String) args)) {
			return getResourceFromString(args.toString());
		}

		if (args instanceof Resource) {
			// no check for resource because it will fall in here also
			return getResourceFromResource((Resource) args);
		}

		if (args instanceof Instance) {
			return parseAsResource(((Instance) args).getId());
		}

		if (args instanceof Collection) {
			return getFromCollection(args);
		}

		// otherwise we will try to convert it to resource,
		// probably in some externally supported format
		return tryTypeConvertion(args);
	}

	private Resource getFromCollection(Object args) {
		LOGGER.warn("The passed resource is of type collection. Only the first element will be processed!"
				+ " Resource value - {}", args);
		Iterator<?> iterator = Collection.class.cast(args).iterator();
		return iterator.hasNext() ? resolveResource(iterator.next()) : null;
	}

	private Resource tryTypeConvertion(Object args) {
		try {
			Resource convert = typeConverter.convert(Resource.class, args);
			if (convert != null) {
				return convert;
			}
			// it's not very likely to happen but just in case. The exception is more probable
			LOGGER.warn("Tryied to conver the given object {} to {} but resulted null", args, Resource.class);
		} catch (TypeConversionException e) {
			LOGGER.warn("Cannot convert the resource " + args + " to " + Resource.class, e);
		}
		return null;
	}

	private Resource getResourceFromResource(Resource instance) {
		return getCachedResource(instance.getId(), instance.getName(), instance.getType());
	}

	/**
	 * Gets the resource from string. Tries to determine the source string format and to return a resource that is
	 * represented by the given string
	 *
	 * @param source the source string to parse
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
				LOGGER.warn("No instance found when loading from {}", source);
			}
		} else
		// check if it's URI/primary key
		if (source.contains(":")) {
			ShortUri shortUri = typeConverter.convert(ShortUri.class, source);
			String uri = Objects.toString(shortUri, source);
			Resource result = getCachedResource(uri, null, null);
			if (result != null) {
				instance = result;
			} else {
				LOGGER.warn("Searching for a resource by URI={} but was not found!", source);
			}
		} else if (source.startsWith(GROUP_PREFIX)) {
			instance = getCachedResource(null, source, ResourceType.GROUP);
		} else if (source.contains("@")) {
			instance = getCachedResource(null, source, ResourceType.USER);
		} else {
			// probably a user/group name
			instance = getCachedResource(null, source, ResourceType.USER);
			if (instance == null) {
				String groupname = GROUP_PREFIX + source;
				instance = getCachedResource(null, groupname, ResourceType.GROUP);
			}
		}
		return instance;
	}

	@Override
	public boolean validateUserName(String userId) {
		return userId != null
				&& userNameValidationPattern.getOrFail().matcher(userId).matches()
				&& verifySameTenant(userId);
	}

	private boolean verifySameTenant(String userId) {
		String tenantSuffix = "@" + securityContext.getCurrentTenantId();
		int tenantSeparatorIndex = userId.indexOf('@');
		if (securityContext.isDefaultTenant() && !userId.endsWith(tenantSuffix) && tenantSeparatorIndex != -1) {
			return false;
		}
		return !(tenantSeparatorIndex != -1 && !userId.substring(tenantSeparatorIndex).equals(tenantSuffix));
	}

	@Override
	public User buildUser(Instance instance) {
		String userName = EmfResourcesUtil.buildUserName(instance.getAsString(ResourceProperties.USER_ID),
				securityContext);
		String oldId = Objects.toString(instance.getId(), null);
		String userDbId = ResourceEntityDao.generateResourceDbId(userName, () -> oldId);

		User user = new EmfUser(userName);
		user.setActive(instance.getBoolean(DefaultProperties.IS_ACTIVE));
		user.setId(userDbId);
		user.setIdentifier(instance.getIdentifier());
		user.setType(instance.type());
		user.setTenantId(securityContext.getCurrentTenantId());

		instance.add(ResourceProperties.USER_ID, userName);
		copyDataProperties(instance, user);
		return user;
	}

	@Override
	public User createUser(Instance instance) {
		User user = buildUser(instance);

		if (!isResourceExistsInDb(user)) {
			return (User) resourceStore.persistNewResource(user, new Operation(ActionTypeConstants.CREATE));
		}

		throw new RollbackedRuntimeException("User with id = [" + user.getName() + "] already exists");
	}

	@Override
	public Group buildGroup(Instance instance) {
		String groupId = instance.getString(ResourceProperties.GROUP_ID);
		if (groupId == null) {
			throw new IllegalArgumentException(ResourceProperties.GROUP_ID + " is required property");
		}
		String internalGroupId = groupId;
		if (!internalGroupId.startsWith(GROUP_PREFIX)) {
			internalGroupId = GROUP_PREFIX + internalGroupId;
		}
		String title = instance.getString(TITLE, () -> groupId);
		EmfGroup group = new EmfGroup(internalGroupId, title);
		String oldId = Objects.toString(instance.getId(), null);
		String groupDbId = ResourceEntityDao.generateResourceDbId(internalGroupId, () -> oldId);
		group.setId(groupDbId);

		group.setIdentifier(instance.getIdentifier());
		group.setType(instance.type());

		copyDataProperties(instance, group);
		// its overriden during properties copy so we restore it
		group.add(ResourceProperties.GROUP_ID, internalGroupId);
		return group;
	}

	@Override
	public Group createGroup(Instance instance) {
		Group group = buildGroup(instance);

		if (!isResourceExistsInDb(group)) {
			return (Group) resourceStore.persistNewResource(group, new Operation(ActionTypeConstants.CREATE));
		}

		throw new RollbackedRuntimeException("Group with id = [" + group.getName() + "] already exists");
	}

	@Override
	public Resource updateResource(Instance instance, Operation operation) {
		if (instance == null) {
			return null;
		}

		Options.DISABLE_AUDIT_LOG.enable();

		try {
			Resource resource = findResource(instance.getId());
			if (resource != null) {
				resource.setDisplayName(instance.getAsString(DefaultProperties.TITLE));

				copyDataProperties(instance, resource);
				ensureActiveAdmin(resource);

				Resource updated = saveResourceInternal(resource, operation);
				loadDecorator.decorateInstance(updated);
				return updated;
			}
		} finally {
			Options.DISABLE_AUDIT_LOG.disable();
		}

		return null;
	}

	private void copyDataProperties(Instance instance, Resource resource) {
		definitionService.getInstanceDefinition(instance)
					.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty().negate())
					.map(PropertyDefinition::getName)
					.forEach(property -> PropertiesUtil.copyValue(instance, resource, property));

		PropertiesUtil.copyValue(instance, resource, DefaultProperties.CREATED_BY);
		PropertiesUtil.copyValue(instance, resource, DefaultProperties.MODIFIED_BY);

		setTitle(resource);
		setSemanticType(resource);
	}

	@Override
	public Resource loadByDbId(Serializable id) {
		return getResourceInternal(id, null, null);
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
		return batchFetchResourcesInternal(ids);
	}

	@Override
	public <S extends Serializable> List<Instance> loadByDbId(List<S> ids, boolean allProperties) {
		return batchFetchResourcesInternal(ids);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends Serializable> List<Instance> load(List<S> ids, boolean allProperties) {
		List<ResourceEntity> entities = CacheUtils.batchLoadBySecondaryKey(
				(List<Pair<String, ResourceType>>) normalizeIdentifiers(ids), resourceStore.getCache(),
				secondaryKeyCallback);
		List<Instance> result = new ArrayList<>(entities.size());
		for (ResourceEntity resourceEntity : entities) {
			result.add(resourceStore.convertToResource(resourceEntity));
		}
		loadProperties(result);
		return result;
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

		stateService.changeState(resource, operation);

		resource.setActive(true);
		return (R) updateResource(resource, operation);
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

		stateService.changeState(resource, operation);

		resource.setActive(false);
		if (resource.getType() == ResourceType.GROUP) {
			// remove all members on group deactivation
			resourceDao.removeAllMembers(Objects.toString(resource.getId(), null));
		}
		return (R) updateResource(resource, operation);
	}

	private boolean isInternalResource(String id) {
		return nullSafeEquals(id, securityManager.getAdminUser().getSystemId())
				|| nullSafeEquals(id, securityManager.getSystemUser().getSystemId())
				|| nullSafeEquals(id, securityManager.getSuperAdminUser().getSystemId());
	}

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
			return resourceDao.findResourcesByNameAndType(ids, type);
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
		return (R) parseAsResource(id);
	}

	@Override
	public boolean resourceExists(Serializable id) {
		return resourceStore.resourceExists(InstanceVersionService.getIdFromVersionId(id));
	}

	@Override
	public Resource getAllOtherUsers() {
		return allOtherUsersGroup;
	}

	@Override
	public Resource getSystemAdminGroup() {
		return systemAdminGroup;
	}
}