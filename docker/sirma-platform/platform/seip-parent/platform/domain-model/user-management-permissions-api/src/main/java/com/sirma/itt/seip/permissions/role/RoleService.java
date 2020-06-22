package com.sirma.itt.seip.permissions.role;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.List;

/**
 * The RoleService provides actions regarding roles.
 */
public interface RoleService {

	/**
	 * Gets the active roles in the system.
	 *
	 * @return the active roles
	 */
	List<RoleIdentifier> getActiveRoles();

	/**
	 * Gets the role identifier based on role id
	 *
	 * @param rolename
	 *            is the role id
	 * @return the role identifier or null if not found
	 */
	RoleIdentifier getRoleIdentifier(String rolename);

	/**
	 * Gets the role identifier for the system manager role
	 *
	 * @return role identifier of the manager role
	 */
	RoleIdentifier getManagerRole();

	/**
	 * Checks if the given role name matches the manager role.
	 *
	 * @param role
	 *            the role name to check
	 * @return <code>true</code>, if is manager role
	 */
	default boolean isManagerRole(String role) {
		return nullSafeEquals(getManagerRole().getIdentifier(), role, true);
	}

	/**
	 * Checks if the given role name matches the manager role.
	 *
	 * @param role
	 *            the role name to check
	 * @return <code>true</code>, if is manager role
	 */
	default boolean isManagerRole(RoleIdentifier role) {
		return role != null && nullSafeEquals(getManagerRole().getIdentifier(), role.getIdentifier(), true);
	}
}
