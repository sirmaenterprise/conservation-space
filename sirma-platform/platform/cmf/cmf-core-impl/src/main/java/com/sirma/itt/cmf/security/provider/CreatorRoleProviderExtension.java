package com.sirma.itt.cmf.security.provider;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.DELETE;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CREATOR;

import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.permissions.AbstractRoleProviderExtension;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The Role provider related to creator permissions. See {@link BaseRoles#CREATOR}
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 0.1)
public class CreatorRoleProviderExtension extends AbstractRoleProviderExtension {

	/**
	 * {@inheritDoc}.<br>
	 * Provides mapping for roles in {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles} enum. This is the root
	 * provider that registers most of the base roles
	 */
	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> enriched) {
		creator(enriched);
		return enriched;
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CREATOR} role model.
	 *
	 * @param enriched
	 *
	 * @return the creator role
	 */
	private Role creator(Map<RoleIdentifier, Role> enriched) {
		RoleIdentifier roleId = CREATOR;
		Role role = RoleProviderExtension.getOrCreateRole(enriched, roleId);
		Set<Action> actions;

		actions = Role.createActionsSet(actionRegistry, DELETE);
		addActions(role, actions);

		// ############################## TASKS & WORKFLOW #########################/
		actions = Role.createActionsSet(actionRegistry, ActionTypeConstants.STOP);
		addActions(role, actions);

		return role;
	}

}