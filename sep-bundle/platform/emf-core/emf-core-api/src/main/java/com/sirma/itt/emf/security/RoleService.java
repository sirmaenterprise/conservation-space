package com.sirma.itt.emf.security;

import java.util.List;

import com.sirma.itt.emf.security.model.RoleIdentifier;

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

}
