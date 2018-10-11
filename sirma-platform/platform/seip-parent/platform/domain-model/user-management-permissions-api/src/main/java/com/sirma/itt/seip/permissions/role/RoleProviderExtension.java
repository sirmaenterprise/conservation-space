package com.sirma.itt.seip.permissions.role;

import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The RoleProviderExtension should provide role and permission mapping in sub modules. Extensions' response is to or
 * not to override current chained state. If extension wants only to enrich {@link RoleIdentifier} should work with
 * current state otherwise should reset what is set on the key<br>
 * <strong> Each module should include permission and actions for the lowest level to appear. Roles are propagated as
 * final step to the higher roles automatically</strong>
 */
public interface RoleProviderExtension extends Plugin {

	/** The target name. */
	String TARGET_NAME = "RoleProviderExtension";

	/**
	 * Gets the role to permission and actions mapping. This is module specific logic. Extensions' response is to or not
	 * to override current chained state. If extension wants only to enrich {@link RoleIdentifier} should work with
	 * current holden in chainedRoles
	 *
	 * @param chainedRoles
	 *            provides the current state roles that are updated from lower priority modules
	 * @return the updated state
	 */
	Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> chainedRoles);

	/**
	 * Merger roles. The second role is enriched with roles from the first argument
	 *
	 * @param source
	 *            the source role
	 * @param destination
	 *            the destination to merge to
	 */
	default void mergerRoles(Role source, Role destination) {
		Role.mergerRoles(source, destination);
	}

	/**
	 * Gets the model for some {@link RoleIdentifier} it exists in the current permission model<br>
	 * <strong>This is not a snapshot but actual model!</strong>
	 *
	 * @param permissionModel
	 *            is the model with all roles
	 * @param roleId
	 *            is the role to get
	 * @return the found role model or new map if the permission model has no such key or associated value
	 */
	static Role getOrCreateRole(Map<RoleIdentifier, Role> permissionModel, RoleIdentifier roleId) {
		if (!permissionModel.containsKey(roleId)) {
			permissionModel.put(roleId, new Role(roleId));
		}
		return permissionModel.get(roleId);
	}

	/**
	 * Adds a permission with its action the a model holding the permission for some role.
	 *
	 * @param role
	 *            the role to update
	 * @param actions
	 *            the actions to add
	 */
	default void addActions(Role role, Set<Action> actions) {
		role.addActions(actions);
	}

}
