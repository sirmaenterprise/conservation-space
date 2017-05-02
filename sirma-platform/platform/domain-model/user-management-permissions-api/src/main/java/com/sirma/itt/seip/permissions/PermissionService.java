package com.sirma.itt.seip.permissions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.ResourceRole;

/**
 * The PermissionService is responsible to find/set/delete the roles for given instance and authorities.
 *
 * @author bbanchev
 */
public interface PermissionService {

	/**
	 * Gets all authorities with their role for the given instance mapped by authority db id.
	 *
	 * @param reference
	 *            the instance reference which permissions to get
	 * @return the permission assignments
	 */
	default Map<String, ResourceRole> getPermissionAssignments(InstanceReference reference) {
		return getPermissionAssignments(reference, null, null);
	}

	/**
	 * Gets all authorities with their role for the given instance mapped by authority db id providing filtering
	 * options.
	 *
	 * @param reference
	 *            the instance reference which permissions to get
	 * @param includeParentPermissions
	 *            when false, the parent permissions will be skipped. When null the filter won't be taken into account
	 *            and all parent permissions will be fetched (just like if it is true).
	 * @param includeLibraryPermissions
	 *            when false, the library permissions will be skipped. When null the filter won't be taken into account
	 *            and all library permissions will be fetched (just like if it is true).
	 * @return the permission assignments
	 */
	Map<String, ResourceRole> getPermissionAssignments(InstanceReference reference, Boolean includeParentPermissions,
			Boolean includeLibraryPermissions);

	/**
	 * Gets all authorities with their role for the given instances mapped by authority db id providing filtering
	 * options.
	 *
	 * @param references
	 *            of the instances which permissions are searched
	 * @param includeParentPermissions
	 *            when false, the parent permissions will be skipped. When null the filter won't be taken into account
	 *            and all parent permissions will be fetched (just like if it is true)
	 * @param includeLibraryPermissions
	 *            when false, the library permissions will be skipped. When null the filter won't be taken into account
	 *            and all library permissions will be fetched (just like if it is true)
	 * @return {@link Map} containing as keys references of the instances which permissions are searched and as values
	 *         another {@link Map} representing the assignments for that instance
	 */
	Map<InstanceReference, Map<String, ResourceRole>> getPermissionAssignmentsForInstances(
			Collection<InstanceReference> references, Boolean includeParentPermissions,
			Boolean includeLibraryPermissions);

	/**
	 * Gets the authorities role for a instance by given authority. All inherited permissions are searched. The most
	 * specific role is used searching starting from the instance and going to all inherited from instances.
	 *
	 * @param reference
	 *            the target instance reference to find role for
	 * @param authorityId
	 *            id of the authority which permissions to provide
	 * @return the authority role or null if nothing could not be found
	 */
	ResourceRole getPermissionAssignment(InstanceReference reference, Serializable authorityId);

	/**
	 * Gets the authorities role for instances by given authority. All inherited permissions are searched. The result
	 * map contains the reference and bound role to it.
	 *
	 * @param references
	 *            of the instances which permission assignments are searched
	 * @param authorityId
	 *            id of the authority which permissions to provide
	 * @return {@link Map} with references for the instances and their resource roles or empty map if noting is found
	 */
	Map<InstanceReference, ResourceRole> getPermissionAssignment(Collection<InstanceReference> references,
			Serializable authorityId);

	/**
	 * Sets permissions for an instance based on set of changes on the permission model.
	 *
	 * @param instance
	 *            instance for which to set the permissions.
	 * @param changes
	 *            set of changes to apply.
	 */
	void setPermissions(InstanceReference instance, Collection<PermissionsChange> changes);

	/**
	 * Determines the permission model type based on current instance.
	 *
	 * @param reference
	 *            the instance reference to check
	 * @return the permission model type
	 */
	PermissionModelType getPermissionModel(InstanceReference reference);

	/**
	 * Remove the assignments of all descendants of the provided instance and set their inherit from parent flag
	 * according to the flag defined in the model.
	 *
	 * @param reference
	 *            instance reference which children to restore.
	 */
	void restoreParentPermissions(InstanceReference reference);

	/**
	 * Check if this is root instance and manager permissions should be processed in diff manner
	 *
	 * @param refrence
	 *            is the instance reference to check
	 * @return true if this is root (class, project, savedfilter)
	 */
	boolean checkIsRoot(InstanceReference refrence);

}
