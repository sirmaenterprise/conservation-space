package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.READ_ALLOWED;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.WRITE_ALLOWED;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.permissions.InstanceAccessPermissions;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.itt.seip.security.exception.NoPermissionsException;

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
	private DictionaryService dictionaryService;

	@Inject
	private InstanceSaveManager instanceSaveManager;

	@Inject
	private TaskExecutor taskExecutor;

	@Inject
	private InstanceContextInitializer contextInitializer;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Override
	public Instance loadInstance(String identifier) {
		InstanceReference reference = resolver
				.resolveReference(identifier)
					.orElseThrow(() -> new InstanceNotFoundException(identifier));
		InstanceAccessPermissions accessPermission = accessEvaluator.getAccessPermission(reference);
		if (!InstanceAccessPermissions.canRead(accessPermission)) {
			throw new NoPermissionsException(identifier, "No read permissions");
		}

		Instance instance = reference.toInstance();
		if (instance == null) {
			throw new InstanceNotFoundException(identifier);
		}

		return addPermissionsModelToInstance(instance, accessPermission);
	}

	@Override
	public Instance loadInstance(String identifier, boolean allowDeleted) {
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
	public Collection<Instance> loadInstances(Collection<String> identifiers) {
		if (isEmpty(identifiers)) {
			return Collections.emptyList();
		}

		try {
			Future<Map<Serializable, InstanceAccessPermissions>> permissionsFuture = taskExecutor
					.submit(() -> accessEvaluator.getAccessPermissions(identifiers));
			Future<Collection<Instance>> instancesFuture = taskExecutor
					.submit(() -> resolver.resolveInstances(identifiers));
			Map<Serializable, InstanceAccessPermissions> permissions = permissionsFuture.get();
			return instancesFuture
					.get()
						.stream()
						// filter all with no permissions
						.filter(canRead(permissions))
						// set read and add supplier for write permissions
						.map(instance -> addPermissionsModelToInstance(instance, getPermissions(instance, permissions)))
						.collect(Collectors.toList());
		} catch (Exception e) {
			throw new ResourceException(Status.INTERNAL_SERVER_ERROR,
					new ErrorData("Error occurred while retrieving instance data!"), e);
		}
	}

	private static Predicate<Instance> canRead(Map<Serializable, InstanceAccessPermissions> permissions) {
		return instance -> InstanceAccessPermissions.canRead(getPermissions(instance, permissions));
	}

	private static InstanceAccessPermissions getPermissions(Instance instance,
			Map<Serializable, InstanceAccessPermissions> permissions) {
		// the permissions are mapped to original instance id and here we could have version id
		Serializable id = InstanceVersionService.getIdFromVersionId(instance.getId());
		return permissions.get(id);
	}

	@Override
	public void delete(String id) {
		if (StringUtils.isNullOrEmpty(id)) {
			throw new IllegalArgumentException();
		}

		Instance instance = loadInstance(id);
		instanceService.delete(instance, OP_DELETE, false);
	}

	@Override
	public Instance save(InstanceSaveContext saveContext) {
		return instanceSaveManager.saveInstance(saveContext);
	}

	@Override
	public Instance save(Instance instance, Operation operation, Date versionCreatedOn) {
		return save(InstanceSaveContext.create(instance, operation, versionCreatedOn));
	}

	@Override
	public Instance clone(String identifier, Operation operation) {
		return instanceService.clone(loadInstance(identifier), operation);
	}

	/**
	 * Creates the instance from the given definition and parent
	 *
	 * @param definition
	 *            the definition
	 * @param parent
	 *            the parent
	 * @return the instance
	 */
	@Override
	public Instance createInstance(DefinitionModel definition, Instance parent) {
		Instance instance = instanceService.createInstance(definition, parent);
		instanceVersionService.setInitialVersion(instance);
		return instance;
	}

	/**
	 * Creates the instance.
	 *
	 * @param definitionId
	 *            the definition id
	 * @param parentId
	 *            the parent id
	 * @return the instance
	 */
	@Override
	public Instance createInstance(String definitionId, String parentId) {
		DefinitionModel definition = dictionaryService.find(definitionId);
		Instance parent = null;
		if (StringUtils.isNotNullOrEmpty(parentId)) {
			parent = loadInstance(parentId);
		}
		return createInstance(definition, parent);
	}

	@Override
	public List<Instance> getInstanceContext(String instanceId) {
		// load the instance and check for at least read permissions
		Instance instance = loadInstance(instanceId);

		contextInitializer.restoreHierarchy(instance);

		LinkedList<Instance> path = new LinkedList<>();
		Instance parent = instance.getOwningInstance();
		while (parent != null) {
			path.add(parent);
			parent = parent.getOwningInstance();
		}
		Map<Serializable, InstanceAccessPermissions> accessPermissions = accessEvaluator.getAccessPermissions(path);

		path.clear();

		// we already checked this instance permissions
		path.add(instance);

		parent = instance.getOwningInstance();
		Predicate<Instance> canRead = canRead(accessPermissions);
		while (parent != null && canRead.test(parent)) {
			path.addFirst(addPermissionsModelToInstance(parent, getPermissions(parent, accessPermissions)));
			parent = parent.getOwningInstance();
		}

		instanceLoadDecorator.decorateResult(path);
		return path;
	}

}
