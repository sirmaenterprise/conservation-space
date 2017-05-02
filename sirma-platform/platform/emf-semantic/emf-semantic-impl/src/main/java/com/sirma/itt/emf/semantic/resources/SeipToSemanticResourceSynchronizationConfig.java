package com.sirma.itt.emf.semantic.resources;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.archive.ArchivedEntity;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionContext;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationDataProvider;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Synchronization configuration that keeps the users and groups updated in the semantic database from relational
 * database.
 *
 * @author BBonev
 */
@Extension(target = SynchronizationConfiguration.PLUGIN_NAME, order = 10)
public class SeipToSemanticResourceSynchronizationConfig
		implements SynchronizationConfiguration<Serializable, Instance> {

	private static final String INSTANCE_TYPE = EMF.PREFIX + ":" + EMF.INSTANCE_TYPE.getLocalName();
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String NAME = "seipToSemanticResources";

	/**
	 * Query that fetches current users and groups from semantic database.
	 */
	public static final String QUERY_ALL_RESOURCES = "select ?instance ?instanceType where { { ?instance a <" + EMF.USER
			+ ">. } UNION { ?instance a <" + EMF.GROUP + ">. } ?instance " + INSTANCE_TYPE + " ?instanceType. }";

	@Inject
	private ResourceService resourceService;
	@Inject
	@SemanticDb
	private DbDao dbDao;
	@Inject
	private SearchService searchService;
	@Inject
	private NamespaceRegistryService registryService;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private PermissionService permissionService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private InstanceVersionService instanceVersionService;

	@Inject
	private InstanceService instanceService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SynchronizationDataProvider<Serializable, Instance> getSource() {
		return SynchronizationDataProvider.create(loadLocalResources(), Instance::getId);
	}

	@SuppressWarnings("squid:S1452")
	private SynchronizationProvider<Collection<? extends Instance>> loadLocalResources() {
		return () -> resourceService.getAllResources(ResourceType.ALL, null);
	}

	private Instance copy(Instance resource) {
		if (resource instanceof GenericProxy<?>) {
			return (Instance) ((GenericProxy<?>) resource).clone();
		}
		Instance copy = objectMapper.map(resource, resource.getClass());
		copy.addAllProperties(PropertiesUtil.cloneProperties(resource.getProperties()));
		return copy;
	}

	@Override
	public SynchronizationDataProvider<Serializable, Instance> getDestination() {
		return SynchronizationDataProvider.create(loadResourcesFromSemantic(), Instance::getId);
	}

	@SuppressWarnings("squid:S1452")
	private SynchronizationProvider<Collection<? extends Instance>> loadResourcesFromSemantic() {
		return () -> {
			SearchArguments<Instance> arguments = new SearchArguments<>();
			arguments.setStringQuery(QUERY_ALL_RESOURCES);
			arguments.setMaxSize(-1);
			arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
			arguments.setDialect(SearchDialects.SPARQL);
			arguments.setFaceted(false);
			arguments.setFilterOutLatestRevisions(false);

			searchService.searchAndLoad(Instance.class, arguments);
			Collection<? extends Instance> resources = loadProperties(arguments.getResult());
			resources.forEach(resource -> cleanInstance(resource));
			return resources;
		};
	}

	private static void cleanInstance(Instance resource) {
		resource.remove(INSTANCE_TYPE);
	}

	/**
	 * Load the properties of the given instances.
	 *
	 * @param loaded
	 *            the loaded
	 * @return the list< instance>
	 */
	private Collection<? extends Instance> loadProperties(List<? extends Instance> loaded) {
		return FragmentedWork.doWorkWithResult(loaded, 64, data -> {
			List<String> uries = new ArrayList<>(data.size());
			for (Instance i : data) {
				uries.add(registryService.buildFullUri((String) i.getId()));
			}
			List<Pair<String, Object>> params = new ArrayList<>(1);
			params.add(new Pair<String, Object>(NamedQueries.Params.URIS, uries));
			return dbDao.fetchWithNamed(NamedQueries.SELECT_BY_IDS, params);
		});
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public Instance merge(Instance oldValue, Instance newValue) {
		DefinitionModel model = dictionaryService.getInstanceDefinition(newValue);
		Map<String, Serializable> newProperties = null;
		if (model != null) {
			Set<String> dmsEnabledFields = getDmsEnabledFields(model);
			// this will reset any properties that are externally synchronized
			oldValue.getOrCreateProperties().keySet().removeAll(dmsEnabledFields);
			oldValue.setIdentifier(model.getIdentifier());
			// necessary, because newValue is sealed
			newProperties = PropertiesUtil.cloneProperties(newValue.getOrCreateProperties());
			newProperties.keySet().retainAll(dmsEnabledFields);
		}
		oldValue.addAllProperties(newProperties);
		return oldValue;
	}

	@Override
	public BiPredicate<Instance, Instance> getComparator() {
		return (r1, r2) -> {
			DefinitionModel r1Model = dictionaryService.getInstanceDefinition(r1);
			if (r1Model == null) {
				// if we have missing models return that resources as equal so no synchronization to be done as we
				// cannot compare them properly
				return true;
			}
			Instance r1Copy = copy(r1);
			Instance r2Copy = copy(r2);

			Set<String> modelKeys = getDmsEnabledFields(r1Model);

			r1Copy.getProperties().keySet().retainAll(modelKeys);
			r2Copy.getProperties().keySet().retainAll(modelKeys);

			return r1Copy.getProperties().equals(r2Copy.getProperties())
					&& EqualsHelper.nullSafeEquals(r1.getIdentifier(), r2.getIdentifier());
		};
	}

	private static Set<String> getDmsEnabledFields(DefinitionModel r1Model) {
		return r1Model.fieldsStream().filter(PropertyDefinition.hasDmsType()).map(PropertyDefinition::getName).collect(
				Collectors.toSet());
	}

	@Override
	public void save(SynchronizationResult<Serializable, Instance> result,
			SyncRuntimeConfiguration runtimeConfiguration) {
		transactionSupport.invokeBiConsumerInNewTx(this::saveChangesInTx, result, runtimeConfiguration);
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private void saveChangesInTx(SynchronizationResult<Serializable, Instance> result,
			SyncRuntimeConfiguration runtimeConfiguration) {
		boolean hasInstanceWithNoModel = result.getToAdd().values().stream()
				.anyMatch(instance -> dictionaryService.getInstanceDefinition(instance) == null);
		if (hasInstanceWithNoModel) {
			// if any of the instances has no model skip the synchronization, because it saves resources with missing
			// data like modifiedOn, createdOn and etc., those properties are filtered out in the merge method
			LOGGER.warn("No definition model for resources! Skipping synchronization!");
			return;
		}

		Options.DISABLE_AUDIT_LOG.enable();
		try {
			// save changes
			for (Entry<Serializable, Instance> entry : result.getModified().entrySet()) {
				Instance incoming = entry.getValue();
				Instance current = dbDao.find(Instance.class, entry.getKey());

				dbDao.saveOrUpdate(incoming, current);
				// we need to refresh the cache, because we are working with the dao directly
				instanceService.touchInstance(incoming);
				assignDefaultPermissions(incoming, runtimeConfiguration);
			}

			for (Instance newResource : result.getToAdd().values()) {
				Instance converted = toObjectInstance(newResource);
				instanceVersionService.setInitialVersion(converted);
				dbDao.saveOrUpdate(converted);
				instanceVersionService.createVersion(VersionContext.create(converted));
				// cache refresh
				instanceService.touchInstance(converted);
				assignDefaultPermissions(newResource, runtimeConfiguration);
			}

			Map<Serializable, Instance> toRemove = result.getToRemove();
			if (!toRemove.isEmpty()) {
				Set<Serializable> keySet = toRemove.keySet();
				// map key sets are not serializable
				dbDao.delete(Instance.class, new HashSet<>(keySet));
				// cache refresh
				instanceService.touchInstance(keySet);
			}
		} finally {
			Options.DISABLE_AUDIT_LOG.disable();
		}
	}

	private void assignDefaultPermissions(Instance resource, SyncRuntimeConfiguration runtimeConfiguration) {
		PermissionModelType permissionModelType = permissionService.getPermissionModel(resource.toReference());
		if (!runtimeConfiguration.isForceSynchronizationEnabled() && permissionModelType.isDefined()) {
			return;
		}

		InstanceReference reference = resource.toReference();
		PermissionsChangeBuilder builder = PermissionsChange.builder();

		if (resource instanceof User) {
			builder.addRoleAssignmentChange((String) resource.getId(), SecurityModel.BaseRoles.MANAGER.getIdentifier());
		}

		String allOtherUsersId = (String) resourceService.getAllOtherUsers().getId();

		// WORKAROUND; setting the permissions twice in order to force them to be synchronized with the semantic
		// database as they get lost for some reason
		builder.addRoleAssignmentChange(allOtherUsersId, SecurityModel.BaseRoles.NO_PERMISSION.getIdentifier());
		permissionService.setPermissions(reference, builder.build());

		builder = PermissionsChange.builder();
		builder.addRoleAssignmentChange(allOtherUsersId, SecurityModel.BaseRoles.CONSUMER.getIdentifier());
		permissionService.setPermissions(reference, builder.build());
	}

	/**
	 * Converts {@link EmfUser} and {@link EmfGroup} to {@link ObjectInstance}. This is used, when initial version for
	 * the resources is created. We need to create this copy so we could created version with correct instance type.
	 * Also we can't work directly with the resources, because they are sealed.
	 * <p>
	 * TODO probably should be removed, when we remove instance type from the {@link ArchivedEntity}.
	 *
	 * @param instance
	 *            of resource(group or user) from which we create new {@link ObjectInstance}.
	 * @return new {@link ObjectInstance} which could be modified
	 */
	private static Instance toObjectInstance(Instance instance) {
		Instance objectInstance = new ObjectInstance();
		objectInstance.setId(instance.getId());
		objectInstance.setIdentifier(instance.getIdentifier());
		objectInstance.setRevision(instance.getRevision());
		objectInstance.setType(instance.type());
		Map<String, Serializable> clonedProperties = PropertiesUtil.cloneProperties(instance.getProperties());
		objectInstance.addAllProperties(clonedProperties);
		return objectInstance;
	}
}
