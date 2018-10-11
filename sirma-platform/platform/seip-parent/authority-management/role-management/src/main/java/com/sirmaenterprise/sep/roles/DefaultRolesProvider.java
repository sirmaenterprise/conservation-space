package com.sirmaenterprise.sep.roles;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.DELETE;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CONTRIBUTOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.MANAGER;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.NO_PERMISSION;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.permissions.action.ActionRegistry;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;

/**
 * Provides the initial map of roles to start with when initiating an import. Contains the default system roles.
 * 
 * @author Vilizar Tsonev
 * @author bbanchev
 * @author smustafov
 */
@Singleton
public class DefaultRolesProvider {

	@Inject
	private ActionRegistry actionRegistry;

	/**
	 * Gets the initial map of roles to start with when initiating an import. Contains the default system roles.
	 * 
	 * @return the map of roles
	 */
	public Map<RoleIdentifier, Role> getDefaultRoles() {
		Map<RoleIdentifier, Role> roles = new HashMap<>();
		RoleProviderExtension.getOrCreateRole(roles, CONSUMER);
		RoleProviderExtension.getOrCreateRole(roles, CONTRIBUTOR);
		RoleProviderExtension.getOrCreateRole(roles, COLLABORATOR);
		RoleProviderExtension.getOrCreateRole(roles, MANAGER);
		RoleProviderExtension.getOrCreateRole(roles, NO_PERMISSION);

		addCreator(roles);

		return roles;
	}

	private Role addCreator(Map<RoleIdentifier, Role> enriched) {
		RoleIdentifier roleId = CREATOR;
		Role role = RoleProviderExtension.getOrCreateRole(enriched, roleId);
		Set<Action> actions = Role.createActionsSet(actionRegistry, DELETE);
		role.addActions(actions);

		// TASKS & WORKFLOW
		actions = Role.createActionsSet(actionRegistry, ActionTypeConstants.STOP);
		role.addActions(actions);
		return role;
	}

}
