package com.sirma.itt.emf.semantic.resources;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static java.util.Collections.singletonList;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Trackable;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.archive.ArchivedEntity;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.VersionMode;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResourcesUtil;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationDataProvider;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.tx.TransactionSupport;
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

	public static final String NAME = "seipToSemanticResources";

	private static final String INSTANCE_TYPE = EMF.PREFIX + ":" + EMF.INSTANCE_TYPE.getLocalName();

	/**
	 * Query that fetches current users and groups from semantic database.
	 */
	public static final String QUERY_ALL_RESOURCES = "select ?instance ?instanceType where { { ?instance a <" + EMF.USER
			+ ">. } UNION { ?instance a <" + EMF.GROUP + ">. } ?instance " + INSTANCE_TYPE + " ?instanceType. }";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Set<String> ALWAYS_SYNCHRONIZED_PROPERTIES = Stream
			.of(DefaultProperties.STATUS, DefaultProperties.IS_ACTIVE, DefaultProperties.IS_DELETED,
					ResourceProperties.GROUP_ID)
				.collect(Collectors.toSet());

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
	private DefinitionService definitionService;
	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private InstanceService instanceService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SynchronizationDataProvider<Serializable, Instance> getSource() {
		return SynchronizationDataProvider.create(loadLocalResources(), Instance::getId);
	}

	@SuppressWarnings("squid:S1452")
	private SynchronizationProvider<Collection<Instance>> loadLocalResources() {
		return () -> resourceService.getAllResources(ResourceType.ALL, null).stream().map(Instance.class::cast).collect(
				Collectors.toList());
	}

	@Override
	public SynchronizationDataProvider<Serializable, Instance> getDestination() {
		return SynchronizationDataProvider.create(loadResourcesFromSemantic(), Instance::getId);
	}

	@SuppressWarnings("squid:S1452")
	private SynchronizationProvider<Collection<Instance>> loadResourcesFromSemantic() {
		return () -> {
			SearchArguments<Instance> arguments = new SearchArguments<>();
			arguments.setStringQuery(QUERY_ALL_RESOURCES);
			arguments.setMaxSize(-1);
			arguments.setPermissionsType(QueryResultPermissionFilter.NONE);
			arguments.setDialect(SearchDialects.SPARQL);
			arguments.setFaceted(false);

			Set<Serializable> instanceIds = searchService
					.stream(arguments, ResultItemTransformer.asSingleValue("instance"))
						.collect(Collectors.toCollection(LinkedHashSet::new));
			Collection<Instance> resources = loadProperties(instanceIds);
			resources.forEach(SeipToSemanticResourceSynchronizationConfig::cleanInstance);
			return resources;
		};
	}

	private static void cleanInstance(Instance resource) {
		resource.remove(INSTANCE_TYPE);
	}

	/**
	 * Load the properties of the given instances from the semantic database
	 *
	 * @param loaded
	 *            the loaded
	 * @return the list< instance>
	 */
	private Collection<Instance> loadProperties(Collection<Serializable> loaded) {
		return FragmentedWork.doWorkWithResult(loaded, 64, data -> {
			List<String> uries = data
					.stream()
						.map(id -> registryService.buildFullUri(id.toString()))
						.collect(Collectors.toList());
			List<Instance> instances = dbDao.fetchWithNamed(NamedQueries.SELECT_BY_IDS,
					singletonList(new Pair<>(NamedQueries.Params.URIS, uries)));
			instances.forEach(Trackable::enableTracking);
			return instances;
		});
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public Instance merge(Instance oldValue, Instance newValue) {
		oldValue.addAllProperties(EmfResourcesUtil.extractExternalProperties(oldValue, newValue,
				definitionService::getInstanceDefinition));

		// update emf properties like status and isActive
		updateAlwaysSynchronizedProperties(oldValue, newValue);

		return oldValue;
	}

	private static void updateAlwaysSynchronizedProperties(Instance oldValue, Instance newValue) {
		for (String propertyKey : ALWAYS_SYNCHRONIZED_PROPERTIES) {
			oldValue.addIfNotNull(propertyKey, newValue.get(propertyKey));
		}
	}

	@Override
	public BiPredicate<Instance, Instance> getComparator() {
		BiPredicate<Instance, Instance> predicate = EmfResourcesUtil.getComparatorForSynchronization(
				definitionService::getInstanceDefinition, this::copy);
		return predicate
				.and((oldValue, newValue) -> ALWAYS_SYNCHRONIZED_PROPERTIES.stream()
						.allMatch(property -> nullSafeEquals(oldValue.get(property), newValue.get(property))))
				.and((oldValue, newValue) -> nullSafeEquals(oldValue.getIdentifier(), newValue.getIdentifier()));
	}

	private Instance copy(Instance resource) {
		Instance copy = objectMapper.map(resource, resource.getClass());
		copy.addAllProperties(PropertiesUtil.cloneProperties(resource.getProperties()));
		return copy;
	}

	@Override
	public void save(SynchronizationResult<Serializable, Instance> result,
			SyncRuntimeConfiguration runtimeConfiguration) {
		transactionSupport.invokeBiConsumerInNewTx(this::saveChangesInTx, result, runtimeConfiguration);
	}

	@SuppressWarnings("squid:UnusedPrivateMethod")
	private void saveChangesInTx(SynchronizationResult<Serializable, Instance> result,
			SyncRuntimeConfiguration runtimeConfiguration) {
		boolean hasInstanceWithNoModel = result.getToAdd().values().stream().anyMatch(
				instance -> definitionService.getInstanceDefinition(instance) == null);
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
				InstanceSaveContext saveContext = InstanceSaveContext
						.create(incoming, Operation.NO_OPERATION)
							.disableValidation("Not needed during synchronization");
				saveContext.getVersionContext().setVersionMode(VersionMode.NONE);
				domainInstanceService.save(saveContext);
				assignDefaultPermissions(incoming, runtimeConfiguration);
			}

			for (Instance newResource : result.getToAdd().values()) {
				Instance converted = toObjectInstance(newResource);
				InstanceSaveContext saveContext = InstanceSaveContext
						.create(converted, Operation.NO_OPERATION)
							.disableValidation("Not needed during resource synchronization");
				saveContext.getVersionContext().disableObjectPropertiesVersioning();
				domainInstanceService.save(saveContext);
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
		ObjectInstance objectInstance = new ObjectInstance();
		objectInstance.setId(instance.getId());
		objectInstance.setIdentifier(instance.getIdentifier());
		objectInstance.setRevision(instance.getRevision());
		objectInstance.setType(instance.type());
		Map<String, Serializable> clonedProperties = PropertiesUtil.cloneProperties(instance.getProperties());
		objectInstance.enableChangesTracking();
		objectInstance.addAllProperties(clonedProperties);
		return objectInstance;
	}
}
