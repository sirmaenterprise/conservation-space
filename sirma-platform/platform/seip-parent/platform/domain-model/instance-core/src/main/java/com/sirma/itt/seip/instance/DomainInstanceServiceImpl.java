package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.collections.CollectionUtils.toIdentityMap;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.READ_ALLOWED;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.WRITE_ALLOWED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.permissions.InstanceAccessPermissions;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Concrete implementation of {@link DomainInstanceService}. This service will be used as one level above
 * {@link InstanceService} and it will contain business logic for the instance related functionalities.
 *
 * @author yasko
 * @author A. Kunchev
 */
@ApplicationScoped
public class DomainInstanceServiceImpl implements DomainInstanceService {

	private static final Operation OP_DELETE = new Operation(ActionTypeConstants.DELETE, true);

	@Inject
	private InstanceTypeResolver resolver;

	@Inject
	private InstanceService instanceService;

	@Inject
	private InstanceVersionService instanceVersionService;

	@Inject
	private InstanceAccessEvaluator accessEvaluator;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private InstanceSaveManager instanceSaveManager;

	@Inject
	private InstanceContextService contextService;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Override
	public Instance loadInstance(String instanceId) {
		// make sure that the passed argument is in the correct form. If passed full URI the instance is found but not
		// returned due to a limitation in the DefaultInstanceLoader.buildResultFromMapping method
		String identifier = toShortUri(instanceId);
		Collection<Instance> instances = resolver.resolveInstances(Collections.singletonList(identifier));
		if (instances.isEmpty()) {
			throw new InstanceNotFoundException(identifier);
		}

		Instance instance = instances.iterator().next();
		instanceLoadDecorator.decorateInstance(instance);
		InstanceAccessPermissions accessPermission = accessEvaluator.getAccessPermission(instance);
		if (!InstanceAccessPermissions.canRead(accessPermission)) {
			throw new NoPermissionsException(identifier, "No read permissions");
		}

		return addPermissionsModelToInstance(instance, accessPermission);
	}

	private String toShortUri(String identifier) {
		return namespaceRegistryService.getShortUri(identifier);
	}

	@Override
	@Monitored({@MetricDefinition(name = "instance_load_duration_seconds", type = Type.TIMER, descr = "Instance load duration in seconds via service method.")})
	public Instance loadInstance(String instanceId, boolean allowDeleted) {
		String identifier = toShortUri(instanceId);
		Instance instance;
		if (InstanceVersionService.isVersion(identifier)) {
			instance = instanceVersionService.loadVersion(identifier);
		} else if (allowDeleted) {
			instance = instanceService.loadDeleted(identifier).orElse(null);
		} else {
			instance = instanceService.loadByDbId(identifier);
		}

		if (instance == null) {
			throw new InstanceNotFoundException(identifier);
		}

		instanceLoadDecorator.decorateInstance(instance);
		InstanceAccessPermissions accessPermissions = accessEvaluator.getAccessPermission(instance);
		if (!instance.isDeleted() && !InstanceAccessPermissions.canRead(accessPermissions)) {
			throw new NoPermissionsException(identifier, "No read permissions");
		}

		return addPermissionsModelToInstance(instance, accessPermissions);
	}

	@SuppressWarnings("boxing")
	private static Instance addPermissionsModelToInstance(Instance instance,
			InstanceAccessPermissions accessPermission) {
		instance.add(READ_ALLOWED, InstanceAccessPermissions.canRead(accessPermission));
		instance.add(WRITE_ALLOWED, InstanceAccessPermissions.canWrite(accessPermission));
		return instance;
	}

	@Override
	@Monitored({
		@MetricDefinition(name = "instance_batch_load_duration_seconds", type = Type.TIMER, descr = "Instance batch load duration in seconds for the service method."),
		@MetricDefinition(name = "instance_batch_load_hit_count", type = Type.COUNTER, descr = "Hit counter on the instance batch load service method.")
	})
	public Collection<Instance> loadInstances(Collection<String> instanceIds, boolean allowDeleted) {
		if (isEmpty(instanceIds)) {
			return Collections.emptyList();
		}

		try {
			Collection<String> identifiers = instanceIds.stream()
					.filter(Objects::nonNull)
					.map(this::toShortUri)
					.collect(Collectors.toList());
			Collection<Instance> instances;
			Collection<Instance> nonDeleted = resolver.resolveInstances(identifiers);
			if (allowDeleted && nonDeleted.size() != identifiers.size()) {
				// the main resolver does not fetch deleted instances so
				// we should check only if the result size is different than the requested
				Map<Serializable, Instance> deletedInstances = loadDeleted(identifiers, nonDeleted);

				// restore the requested order
				nonDeleted.forEach(instance -> deletedInstances.putIfAbsent(instance.getId(), instance));
				instances = identifiers
						.stream()
							.map(deletedInstances::get)
							.filter(Objects::nonNull)
							.collect(Collectors.toList());
			} else {
				instances = nonDeleted;
			}

			instanceLoadDecorator.decorateResult(instances);
			Map<Serializable, InstanceAccessPermissions> permissions = accessEvaluator.getAccessPermissions(instances);

			return instances
					.stream()
						// filter all with no permissions
						.filter(instance ->InstanceAccessPermissions.canRead(
										permissions.get(InstanceVersionService.getIdFromVersionId(instance.getId()))))
						// set read and add supplier for write permissions
						.map(instance -> addPermissionsModelToInstance(instance,
								permissions.get(InstanceVersionService.getIdFromVersionId(instance.getId()))))
						.collect(Collectors.toList());
		} catch (Exception e) {
			throw new ResourceException(INTERNAL_SERVER_ERROR, "Error occurred while retrieving instance data!", e);
		}
	}

	private Map<Serializable, Instance> loadDeleted(Collection<String> identifiers, Collection<Instance> nonDeleted) {
		Set<String> loadedIds = nonDeleted
				.stream()
					.map(Instance::getId)
					.map(Object::toString)
					.collect(Collectors.toSet());

		return identifiers
				.stream()
					.filter(id -> !loadedIds.contains(id))
					.map(instanceService::loadDeleted)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(toIdentityMap(Instance::getId));
	}

	@Override
	public Collection<String> delete(String id) {
		if (StringUtils.isBlank(id)) {
			throw new IllegalArgumentException();
		}

		Instance instance = loadInstance(toShortUri(id));
		return instanceService.delete(instance, OP_DELETE, false);
	}

	@Override
	@Transactional
	// TODO check method call stack
	@Monitored(@MetricDefinition(name = "instance_save_duration_seconds", type = Type.TIMER, descr = "Instance save duration in seconds via service method."))
	public Instance save(InstanceSaveContext saveContext) {
		return instanceSaveManager.saveInstance(saveContext);
	}

	@Override
	public Instance clone(String identifier, Operation operation) {
		return instanceService.clone(loadInstance(identifier), operation);
	}

	@Override
	public Instance createInstance(DefinitionModel definition, Instance parent) {
		Instance instance = instanceService.createInstance(definition, parent);
		instanceVersionService.populateVersion(instance);
		return instance;
	}

	@Override
	public Instance createInstance(String definitionId, String parentId) {
		DefinitionModel definition = definitionService.find(definitionId);
		Instance parent = null;
		if (StringUtils.isNotBlank(parentId)) {
			parent = loadInstance(parentId);
		}
		return createInstance(definition, parent);
	}

	@Override
	public List<Instance> getInstanceContext(String instanceId) {
		// load the instance and check for at least read permissions
		Instance instance = loadInstance(instanceId);

		LinkedList<Instance> path = contextService
				.getContextPath(instance)
					.stream()
					.map(InstanceReference::toInstance)
					.collect(Collectors.toCollection(LinkedList::new));
		Map<Serializable, InstanceAccessPermissions> accessPermissions = accessEvaluator.getAccessPermissions(path);

		path.clear();

		List<InstanceReference> fullContext = contextService.getContextPath(instance);
		for (InstanceReference context : fullContext) {
			path.addLast(addPermissionsModelToInstance(context.toInstance(), accessPermissions.get(context.getId())));
		}

		// we already checked this instance permissions
		path.addLast(instance);
		instanceLoadDecorator.decorateResult(path);
		return path;
	}

	@Override
	public void touchInstance(Object object) {
		instanceService.touchInstance(object);
	}
}
