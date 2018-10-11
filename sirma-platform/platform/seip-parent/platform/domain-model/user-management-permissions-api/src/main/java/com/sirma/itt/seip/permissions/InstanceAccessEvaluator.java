package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
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
	 * @param instanceId the instance id to check for. The argument could be instance identifier,
	 *        {@link InstanceReference} or {@link Instance}
	 * @param minimumRole the minimum role to check. If <code>null</code> the {@link SecurityModel.BaseRoles#VIEWER}
	 *        will be used.
	 * @return true, if the current user has role equal to the given or higher.
	 */
	default boolean isAtLeastRole(Serializable instanceId, RoleIdentifier minimumRole) {
		return isAtLeastRole(instanceId, null, minimumRole);
	}

	/**
	 * Checks if the given user has at least the given role on the given instance.
	 *
	 * @param instanceId the instance id to check for. The argument could be instance identifier,
	 *        {@link InstanceReference} or {@link Instance}
	 * @param resourceId the resource id to check if can open the given instance
	 * @param minimumRole the minimum role to check. If <code>null</code> the {@link SecurityModel.BaseRoles#VIEWER}
	 *        will be used.
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
	 * @param identifiers of the instances, which permissions should be calculated
	 * @param minimumReadRole the minimum role that is required for the current user to be able to read(open) specific
	 *        instance
	 * @param minimumWriteRole the minimum role that is required for the current user to be able to write(edit) specific
	 *        instance
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
	 * @param identifiers of the instances, which permissions should be calculated
	 * @param resourceId the id of the user for which will be calculated permissions, if missing the current user will
	 *        be used
	 * @param minimumReadRole the minimum role that is required for the user to be able to read(open) specific instance
	 * @param minimumWriteRole the minimum role that is required for the user to be able to write(edit) specific
	 *        instance
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	Map<Serializable, InstanceAccessPermissions> isAtLeastRole(Collection<? extends Serializable> identifiers,
			Serializable resourceId, RoleIdentifier minimumReadRole, RoleIdentifier minimumWriteRole);

	/**
	 * Check read access to the given instance by the current user. The method checks if the user could access and open
	 * the given instance.
	 *
	 * @param instanceId the instance id to check for. The argument could be instance identifier,
	 *        {@link InstanceReference} or {@link Instance}
	 * @return true, if the current user is allowed to access the provided instance id.
	 */
	default boolean canRead(Serializable instanceId) {
		return canRead(instanceId, null);
	}

	/**
	 * Check read access to the given instance by the given user. The method checks if the user could access and open
	 * the given instance.
	 *
	 * @param instanceId the instance id to check for. The argument could be instance identifier,
	 *        {@link InstanceReference} or {@link Instance}
	 * @param resourceId the resource id to check if can open the given instance
	 * @return true, if the current user is allowed to access the provided instance id.
	 */
	boolean canRead(Serializable instanceId, Serializable resourceId);

	/**
	 * Check write access to the given instance by the current user. The method checks if the user could access and
	 * change the content of the given instance.
	 *
	 * @param instanceId the instance id to check for. The argument could be instance identifier,
	 *        {@link InstanceReference} or {@link Instance}
	 * @return true, if the current user is allowed to modify the provided instance id.
	 */
	default boolean canWrite(Serializable instanceId) {
		return canWrite(instanceId, null);
	}

	/**
	 * Check write access to the given instance by the given user. The method checks if the user could access and change
	 * the content of the given instance.
	 *
	 * @param instanceId the instance id to check for. The argument could be instance identifier,
	 *        {@link InstanceReference} or {@link Instance}
	 * @param resourceId the resource id to check if can open the given instance
	 * @return true, if the current user is allowed to modify the provided instance id.
	 */
	boolean canWrite(Serializable instanceId, Serializable resourceId);

	/**
	 * Calculates permissions for the given instance and the current user. The calculated permissions are read, write or
	 * no access at all. The permissions calculation is based on instance actions and user's actions for a role. For
	 * read permissions the {@link ActionTypeConstants#READ} action is used, for write the
	 * {@link ActionTypeConstants#EDIT_DETAILS} action is used. If neither of these two actions are not allowed then the
	 * user has no access.<br>
	 * The method supports calculation of permissions for version instances.
	 *
	 * @param instance which permissions should be calculated
	 * @return the instance access for the given instance and the current user.
	 *         {@link InstanceAccessPermissions#NO_ACCESS} will be return in case the passed id is null or could not
	 *         have it's permissions fetched
	 */
	default InstanceAccessPermissions getAccessPermission(Instance instance) {
		if (instance == null) {
			return InstanceAccessPermissions.NO_ACCESS;
		}
		Map<Serializable, InstanceAccessPermissions> permissions = getAccessPermissions(
				Collections.singletonList(instance), null);
		if (isEmpty(permissions)) {
			return InstanceAccessPermissions.NO_ACCESS;
		}
		return permissions.values().iterator().next();
	}

	/**
	 * Calculates permissions for every instance, in the given collection, and the current user. The calculated
	 * permissions are read, write or no access at all. The permissions calculation is based on instance actions and
	 * user's actions for a role. For read permissions the {@link ActionTypeConstants#READ} action is used, for write
	 * the {@link ActionTypeConstants#EDIT_DETAILS} action is used. If neither of these two actions are not allowed then
	 * the user has no access.<br>
	 * The method supports calculation of permissions for version instances. The result is map, where the keys are
	 * instances ids and the values are the permissions represented by custom object. <br>
	 * <b>Note that the results for the version instances are mapped to the original instance id (without version
	 * suffix).</b>
	 *
	 * @param instances which permissions should be calculated
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	default Map<Serializable, InstanceAccessPermissions> getAccessPermissions(Collection<Instance> instances) {
		return getAccessPermissions(instances, null);
	}

	/**
	 * Calculates permissions for every instance, in the given collection, and the given user. The calculated
	 * permissions are read, write or no access at all. The permissions calculation is based on instance actions and
	 * user's actions for a role. For read permissions the {@link ActionTypeConstants#READ} action is used, for write
	 * the {@link ActionTypeConstants#EDIT_DETAILS} action is used. If neither of these two actions are not allowed then
	 * the user has no access.<br>
	 * The method supports calculation of permissions for version instances. The result is map, where the keys are
	 * instances ids and the values are the permissions represented by custom object. <br>
	 * <b>Note that the results for the version instances are mapped to the original instance id (without version
	 * suffix).</b>
	 *
	 * @param instances which permissions should be calculated
	 * @param resourceId the id of the user for which will be calculated permissions, if missing the current user will
	 *        be used
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	Map<Serializable, InstanceAccessPermissions> getAccessPermissions(Collection<Instance> instances,
			Serializable resourceId);

	/**
	 * Checks if the current user is allowed to access given action for given instance.
	 *
	 * @param instanceId the instance id for which the action will be checked
	 * @param actionId the action id for which the current user will be checked
	 * @return true if the current user can access the given action for the given instance
	 */
	default boolean actionAllowed(Serializable instanceId, String actionId) {
		return actionAllowed(instanceId, null, actionId);
	}

	/**
	 * Checks if the given user is allowed to access given action for given instance.
	 *
	 * @param instanceId instance id or {@link InstanceReference} for which the action will be checked
	 * @param resourceId id of a user for which the action for the given instance will be checked, if null the current
	 *        user will be used, if user with the given id cannot be found also the current user will be used
	 * @param actionId the action id for which the given user will be checked
	 * @return true if the given user can access the given action for the given instance
	 */
	boolean actionAllowed(Serializable instanceId, Serializable resourceId, String actionId);

}
