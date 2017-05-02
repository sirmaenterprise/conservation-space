package com.sirma.itt.seip.permissions;

import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Provides means to resolve permission hierarchy dependencies.
 *
 * @author BBonev
 */
public interface InstancePermissionsHierarchyResolver {

	/**
	 * Gets the permission inheritance from.
	 *
	 * @param instance
	 *            the instance
	 * @return the permission inheritance from
	 */
	InstanceReference getPermissionInheritanceFrom(InstanceReference instance);

	/**
	 * Check if given instance id represents root of permission hierarchy (it is class)
	 *
	 * @param instanceId
	 *            the instance id to check
	 * @return true if so, false otherwise
	 */
	boolean isInstanceRoot(String instanceId);

	/**
	 * Gets the library for a given instance.
	 *
	 * @param reference
	 *            reference of the instance.
	 * @return the library or null of a reference to an instance that is already a library is provided.
	 */
	InstanceReference getLibrary(InstanceReference reference);

	/**
	 * Checks if a parent object is eligible for permission inheritance - e.g. its permissions are allowed to be
	 * inherited. This is true when the object is not a library, group or a user.
	 *
	 * @param parent
	 *            object to check.
	 * @return true if eligible, false otherwise.
	 */
	boolean isAllowedForPermissionSource(InstanceReference parent);
}
