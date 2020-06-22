package com.sirma.itt.seip.resources.synchronization;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.LANGUAGE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.resources.EmfResourcesUtil;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationDataProvider;
import com.sirma.itt.seip.synchronization.SynchronizationProvider;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Base class for implementing resource synchronization between external user/group provider and internal user store.
 *
 * @author BBonev
 */
public abstract class BaseExternalProviderToSepResourceSynchronizationConfig
		implements SynchronizationConfiguration<String, Resource> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final Operation DELETE_OP = new Operation(ActionTypeConstants.DELETE);
	private static final Operation DEACTIVATE_OP = new Operation(ActionTypeConstants.DEACTIVATE);

	@Inject
	protected ResourceService resourceService;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private ObjectMapper objectMapper;

	@Override
	public SynchronizationDataProvider<String, Resource> getDestination() {
		return SynchronizationDataProvider.create(loadLocalResources(), getIdentityResolver());
	}

	/**
	 * Load local resources used as base for comparison of the incoming resource.
	 *
	 * @return the synchronization provider that provides current local resource
	 */
	@SuppressWarnings("squid:S1452")
	protected abstract SynchronizationProvider<Collection<Resource>> loadLocalResources();

	/**
	 * Copy the resource so not to try to modify the original instance.
	 *
	 * @param resource the resource to copy
	 * @return the resource clone
	 */
	@SuppressWarnings("static-method")
	protected Resource copyResource(Resource resource) {
		if (resource instanceof GenericProxy<?>) {
			return (Resource) ((GenericProxy<?>) resource).createCopy();
		}
		return resource;
	}

	/**
	 * Clean any internal properties that should not be taken into account when comparing resources.
	 *
	 * @param resource the resource to update
	 */
	protected void cleanSystemProperties(Resource resource) {
		DefinitionModel model = definitionService.getInstanceDefinition(resource);
		resource.remove("dcterms:description");
		resource.remove(DefaultProperties.TITLE);
		if (model == null) {
			resource.remove(DefaultProperties.SEMANTIC_TYPE);
			resource.remove(MODIFIED_BY);
			resource.remove(MODIFIED_ON);
			resource.remove(CREATED_BY);
			resource.remove(CREATED_ON);
			resource.remove(LANGUAGE);
			resource.remove(VERSION);
		} else {
			// remove the system and editable fields
			// the system fields are internally set and the editable are modified by users
			Set<String> externalProperties = model
					.fieldsStream()
					.filter(PropertyDefinition.hasDmsType())
					.map(PropertyDefinition::getName)
					.collect(Collectors.toSet());
			resource.getOrCreateProperties().keySet().retainAll(externalProperties);
		}
	}

	@Override
	public SynchronizationDataProvider<String, Resource> getSource() {
		return SynchronizationDataProvider.create(loadRemoteResources(), getIdentityResolver());
	}

	/**
	 * Load remote resources (incoming) resource changes.
	 *
	 * @return the synchronization provider that fetches the incoming changes
	 */
	@SuppressWarnings("squid:S1452")
	protected abstract SynchronizationProvider<Collection<Resource>> loadRemoteResources();

	private static Function<Resource, String> getIdentityResolver() {
		// makes diff calculation case insensitive, since both source and destination mappings will be with lower case keys
		return resource -> resource.getName().toLowerCase();
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public Resource merge(Resource oldValue, Resource newValue) {
		// update fresh copy of the modified resource
		Resource resource = (Resource) resourceService.loadByDbId(oldValue.getId());
		resource.addAllProperties(EmfResourcesUtil.extractExternalProperties(oldValue, newValue, definitionService::getInstanceDefinition));
		resource.setActive(newValue.isActive());
		return resource;
	}

	@Override
	public BiPredicate<Resource, Resource> getComparator() {
		BiPredicate<Instance, Instance> comparator = EmfResourcesUtil.getComparatorForSynchronization(
				definitionService::getInstanceDefinition, this::copy);
		return comparator::test;
	}

	private Instance copy(Instance resource) {
		Instance copy = objectMapper.map(resource, resource.getClass());
		copy.addAllProperties(PropertiesUtil.cloneProperties(resource.getProperties()));
		return copy;
	}

	@Override
	public void save(SynchronizationResult<String, Resource> result, SyncRuntimeConfiguration runtimeConfiguration) {
		transactionSupport.invokeBiConsumerInNewTx(this::saveResultInTx, result, runtimeConfiguration);
	}

	/**
	 * Save the synchronization. This method will be in new transaction when called
	 *
	 * @param result the result to save
	 */
	protected void saveResultInTx(SynchronizationResult<String, Resource> result,
			SyncRuntimeConfiguration runtimeConfiguration) {
		Options.DISABLE_AUDIT_LOG.enable();
		try {
			if (isNotEmpty(result.getToAdd())) {
				addNew(result.getToAdd());
			}
			if (isNotEmpty(result.getToRemove())) {
				removeDeleted(result.getToRemove(), runtimeConfiguration);
			}
			if (isNotEmpty(result.getModified())) {
				saveModified(result.getModified());
			}
		} finally {
			Options.DISABLE_AUDIT_LOG.disable();
		}
	}

	/**
	 * Save modified.
	 *
	 * @param modified the modified
	 */
	protected void saveModified(Map<String, Resource> modified) {
		for (Resource resource : modified.values()) {
			LOGGER.debug("Found changes in -> {}", resource);
			resourceService.saveResource(resource);
		}
	}

	/**
	 * Removes the deleted.
	 *
	 * @param removed the removed
	 * @param runtimeConfiguration the runtime configuration
	 */
	protected void removeDeleted(Map<String, Resource> removed, SyncRuntimeConfiguration runtimeConfiguration) {
		for (Resource resource : removed.values()) {
			deleteResource(resource, runtimeConfiguration);
		}
	}

	/**
	 * Call resource deletion. If deletion is not allowed then deactivation will occur
	 *
	 * @param resource the resource
	 * @param runtimeConfiguration the runtime configuration
	 */
	protected void deleteResource(Resource resource, SyncRuntimeConfiguration runtimeConfiguration) {
		if (runtimeConfiguration.isDeleteAllowed()) {
			// this will allow user deletion from the database
			resourceService.delete(resource, DELETE_OP, true);
		} else {
			// if called for a group it will be actually deleted
			resourceService.deactivate(resource, DEACTIVATE_OP);
		}
	}

	/**
	 * Adds the new resources.
	 *
	 * @param newResources map of new resources which should be saved
	 */
	protected void addNew(Map<String, Resource> newResources) {
		for (Resource resource : newResources.values()) {
			LOGGER.debug("Adding new -> {}", resource);
			resourceService.saveResource(resource);
		}
	}
}
