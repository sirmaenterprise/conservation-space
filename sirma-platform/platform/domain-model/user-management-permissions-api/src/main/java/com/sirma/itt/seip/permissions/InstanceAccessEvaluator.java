package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;

/**
 * Service to provide means to verify if user could access and/or modify instance. This class should be as fast as
 * possible because will called really often to check the access to instances.
 *
 * @author BBonev
 */
public interface InstanceAccessEvaluator {

	/**
	 * Checks if the current user has at least the given role on the given instance.
	 *
	 * @param instanceId
	 *            the instance id to check for. The argument could be instance identifier, {@link InstanceReference} or
	 *            {@link Instance}
	 * @param minimumRole
	 *            the minimum role to check. If <code>null</code> the {@link SecurityModel.BaseRoles#VIEWER} will be
	 *            used.
	 * @return true, if the current user has role equal to the given or higher.
	 */
	default boolean isAtLeastRole(Serializable instanceId, RoleIdentifier minimumRole) {
		return isAtLeastRole(instanceId, null, minimumRole);
	}

	/**
	 * Checks if the given user has at least the given role on the given instance.
	 *
	 * @param instanceId
	 *            the instance id to check for. The argument could be instance identifier, {@link InstanceReference} or
	 *            {@link Instance}
	 * @param resourceId
	 *            the resource id to check if can open the given instance
	 * @param minimumRole
	 *            the minimum role to check. If <code>null</code> the {@link SecurityModel.BaseRoles#VIEWER} will be
	 *            used.
	 * @return true, if the current user has role equal to the given or higher.
	 */
	boolean isAtLeastRole(Serializable instanceId, Serializable resourceId, RoleIdentifier minimumRole);

	/**
	 * Checks if the current user has specific role with which he/she could be able to read or write specific instances.
	 * The roles for read and write are explicitly passed as parameters of the method and they are required. For every
	 * instance are calculated read and write permissions. The method supports calculation of permissions for version
	 * instances.<br>
	 * The result is map, where the for keys are instances ids and the value are the permissions represented by custom
	 * object.
	 * <p>
	 * <b>Note that the results for the version instances are mapped to the original instance id (without version
	 * suffix).</b>
	 *
	 * @param identifiers
	 *            of the instances, which permissions should be calculated
	 * @param minimumReadRole
	 *            the minimum role that is required for the current user to be able to read(open) specific instance
	 * @param minimumWriteRole
	 *            the minimum role that is required for the current user to be able to write(edit) specific instance
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	default Map<Serializable, InstanceAccessPermissions> isAtLeastRole(Collection<? extends Serializable> identifiers,
			RoleIdentifier minimumReadRole, RoleIdentifier minimumWriteRole) {
		return isAtLeastRole(identifiers, null, minimumReadRole, minimumWriteRole);
	}

	/**
	 * Checks if specific user has specific role with which he/she could be able to read or write specific instances.
	 * The roles for read and write are explicitly passed as parameters of the method and they are required. For every
	 * instance are calculated read and write permissions. The method supports calculation of permissions for version
	 * instances.<br>
	 * The result is map, where the for keys are instances ids and the value are the permissions represented by custom
	 * object.
	 * <p>
	 * <b>Note that the results for the version instances are mapped to the original instance id (without version
	 * suffix).</b>
	 *
	 * @param identifiers
	 *            of the instances, which permissions should be calculated
	 * @param resourceId
	 *            the id of the user for which will be calculated permissions, if missing the current user will be used
	 * @param minimumReadRole
	 *            the minimum role that is required for the user to be able to read(open) specific instance
	 * @param minimumWriteRole
	 *            the minimum role that is required for the user to be able to write(edit) specific instance
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	Map<Serializable, InstanceAccessPermissions> isAtLeastRole(Collection<? extends Serializable> identifiers,
			Serializable resourceId, RoleIdentifier minimumReadRole, RoleIdentifier minimumWriteRole);

	/**
	 * Check read access to the given instance by the current user. The method checks if the user could access and open
	 * the given instance.
	 *
	 * @param instanceId
	 *            the instance id to check for. The argument could be instance identifier, {@link InstanceReference} or
	 *            {@link Instance}
	 * @return true, if the current user is allowed to access the provided instance id.
	 */
	default boolean canRead(Serializable instanceId) {
		return canRead(instanceId, null);
	}

	/**
	 * Check read access to the given instance by the given user. The method checks if the user could access and open
	 * the given instance.
	 *
	 * @param instanceId
	 *            the instance id to check for. The argument could be instance identifier, {@link InstanceReference} or
	 *            {@link Instance}
	 * @param resourceId
	 *            the resource id to check if can open the given instance
	 * @return true, if the current user is allowed to access the provided instance id.
	 */
	boolean canRead(Serializable instanceId, Serializable resourceId);

	/**
	 * Check write access to the given instance by the current user. The method checks if the user could access and
	 * change the content of the given instance.
	 *
	 * @param instanceId
	 *            the instance id to check for. The argument could be instance identifier, {@link InstanceReference} or
	 *            {@link Instance}
	 * @return true, if the current user is allowed to modify the provided instance id.
	 */
	default boolean canWrite(Serializable instanceId) {
		return canWrite(instanceId, null);
	}

	/**
	 * Check write access to the given instance by the given user. The method checks if the user could access and change
	 * the content of the given instance.
	 *
	 * @param instanceId
	 *            the instance id to check for. The argument could be instance identifier, {@link InstanceReference} or
	 *            {@link Instance}
	 * @param resourceId
	 *            the resource id to check if can open the given instance
	 * @return true, if the current user is allowed to modify the provided instance id.
	 */
	boolean canWrite(Serializable instanceId, Serializable resourceId);

	/**
	 * Checks if the current user has specific role with which he/she could be able to read or write specific instance.
	 * For read and write roles are used default system roles that have the minimum permissions that are required to
	 * read or write specific instance. The method supports calculation of permissions for version instances.<br>
	 *
	 * @param identifier
	 *            of the instance, which permissions should be calculated
	 * @return the instance access for the given instance and the current user.
	 *         {@link InstanceAccessPermissions#NO_ACCESS} will be return in case the passed id is null or could not
	 *         have it's permissions fetched
	 */
	default InstanceAccessPermissions getAccessPermission(Serializable identifier) {
		if (identifier == null) {
			return InstanceAccessPermissions.NO_ACCESS;
		}
		Map<Serializable, InstanceAccessPermissions> permissions = getAccessPermissions(
				Collections.singletonList(identifier), null);
		if (isEmpty(permissions)) {
			return InstanceAccessPermissions.NO_ACCESS;
		}
		return permissions.values().iterator().next();
	}

	/**
	 * Checks if the current user has specific role with which he/she could be able to read or write specific instances.
	 * For read and write roles are used default system roles that have the minimum permissions that are required to
	 * read or write specific instance. For every instance are calculated read and write permissions. The method
	 * supports calculation of permissions for version instances.<br>
	 * The result is map, where the for keys are instances ids and the value are the permissions represented by custom
	 * object.
	 * <p>
	 * <b>Note that the results for the version instances are mapped to the original instance id (without version
	 * suffix).</b>
	 *
	 * @param identifiers
	 *            of the instances, which permissions should be calculated
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	default Map<Serializable, InstanceAccessPermissions> getAccessPermissions(
			Collection<? extends Serializable> identifiers) {
		return getAccessPermissions(identifiers, null);
	}

	/**
	 * Checks if specific user has specific role with which he/she could be able to read or write specific instances.
	 * For read and write roles are used default system roles that have the minimum permissions that are required to
	 * read or write specific instance. For every instance are calculated read and write permissions. The method
	 * supports calculation of permissions for version instances.<br>
	 * The result is map, where the for keys are instances ids and the value are the permissions represented by custom
	 * object.
	 * <p>
	 * <b>Note that the results for the version instances are mapped to the original instance id (without version
	 * suffix).</b>
	 *
	 * @param identifiers
	 *            of the instances, which permissions should be calculated
	 * @param resourceId
	 *            the id of the user for which will be calculated permissions, if missing the current user will be used
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	Map<Serializable, InstanceAccessPermissions> getAccessPermissions(Collection<? extends Serializable> identifiers,
			Serializable resourceId);

}
