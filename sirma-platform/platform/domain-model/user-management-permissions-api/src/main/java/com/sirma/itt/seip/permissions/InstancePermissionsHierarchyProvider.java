package com.sirma.itt.seip.permissions;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Provides correct permission hierarchy during path/inheritance set.
 *
 * @author bbanchev
 */
@Documentation("Provides the hierarchy path/parent to inhertit from for given instance")
public interface InstancePermissionsHierarchyProvider {

	/**
	 * Gets the permission inheritance from.
	 *
	 * @param reference
	 *            the instance
	 * @return the permission inheritance from
	 */
	InstanceReference getPermissionInheritanceFrom(InstanceReference reference);

	/**
	 * Gets the library for a given instance.
	 *
	 * @param reference
	 *            instance reference.
	 * @return the library or null of a reference to an instance that is already a library is provided.
	 */
	InstanceReference getLibrary(InstanceReference reference);

	/**
	 * Check if given instance id points to a root of permission hierarchy (it is class)
	 *
	 * @param instanceId
	 *            is the instance to check
	 * @return true if so, false otherwise
	 */
	boolean isInstanceRoot(String instanceId);

	/**
	 * Checks if a parent object is eligible for permission inheritance - e.g. its permissions are allowed to be
	 * inherited. This is true when the object is not a library, user or a group.
	 *
	 * @param parent
	 *            object to check.
	 * @return true if eligible, false otherwise.
	 */
	boolean isAllowedForPermissionSource(InstanceReference parent);
}
