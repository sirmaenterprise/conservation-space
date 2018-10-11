package com.sirma.itt.seip.testutil.mocks;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import javax.ejb.EJBException;
import javax.transaction.Synchronization;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;

/**
 * {@link TransactionalPermissionChanges} implementation that imitates its behaviour. The mock for
 * {@link PermissionService} should be set via setPermissionService method.
 *
 * @author smustafov
 */
public class TransactionalPermissionChangesFake implements TransactionalPermissionChanges, Synchronization {

	private Map<InstanceReference, PermissionsChangeBuilder> changes = new ConcurrentHashMap<>();

	private boolean allowAutomaticFlush = true;

	private PermissionService permissionService;

	/**
	 * Creates new mock with given permission service.
	 *
	 * @param permissionService the service to use for permission flush
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@Override
	public void beforeCompletion() {
		if (allowAutomaticFlush) {
			drainChanges(permissionService::setPermissions);
		}
	}

	@Override
	public void afterCompletion(int committed) {
		changes.clear();
	}

	@Override
	public PermissionsChangeBuilder builder(InstanceReference reference) {
		return changes.computeIfAbsent(reference, k -> PermissionsChange.builder());
	}

	@Override
	public void disableAutomaticFlush() {
		allowAutomaticFlush = false;
	}

	@Override
	public void drainChanges(BiConsumer<InstanceReference, List<PermissionsChange>> onChange) {
		changes.forEach((ref, builder) -> onChange.accept(ref, builder.buildAndReset()));
	}

	/**
	 * Adds the given builder to the map, mapped with the given instance reference as key
	 *
	 * @param instanceReference that will be the key
	 * @param builder will be the value in the map
	 */
	public void addBuilder(InstanceReference instanceReference, PermissionsChangeBuilder builder) {
		changes.put(instanceReference, builder);
	}

}
