package com.sirma.itt.cmf.security.provider;

import static com.sirma.itt.seip.permissions.SecurityModel.ActivitiRoles.ASSIGNEE;
import static com.sirma.itt.seip.permissions.SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CONSUMER;
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
 * The ActivitiRoleProviderExtension provides the roles and permission for that roles related to activiti/processes
 * module.
 */
@ApplicationScoped
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 2)
public class ActivitiRoleProviderExtension extends AbstractRoleProviderExtension {

	/**
	 * {@inheritDoc}.<br>
	 * Provides mapping for roles in {@link com.sirma.itt.seip.permissions.SecurityModel.BaseRoles} enum and
	 * {@link com.sirma.itt.seip.permissions.SecurityModel.ActivitiRoles}
	 */
	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> enriched) {
		possibleAssignee(enriched);
		assignee(enriched);

		// copy from basic permissions
		merge(enriched, CONSUMER, POSSIBLE_ASSIGNEE);
		merge(enriched, CONSUMER, ASSIGNEE);
		merge(enriched, ASSIGNEE, CREATOR);
		return enriched;
	}

	/**
	 * Constructs the {@link com.sirma.itt.seip.permissions.SecurityModel.ActivitiRoles#POSSIBLE_ASSIGNEE} role model
	 *
	 * @param enriched
	 *
	 * @return the possible assignee role
	 */
	private Role possibleAssignee(Map<RoleIdentifier, Role> enriched) {

		RoleIdentifier roleId = POSSIBLE_ASSIGNEE;
		Role role = RoleProviderExtension.getOrCreateRole(enriched, roleId);

		Set<Action> actions = Role.createActionsSet(actionRegistry, ActionTypeConstants.CLAIM);
		addActions(role, actions);

		return role;
	}

	/**
	 * Constructs the {@link com.sirma.itt.seip.permissions.SecurityModel.ActivitiRoles#ASSIGNEE} role model
	 *
	 * @param enriched
	 *
	 * @return the assignee role
	 */
	private Role assignee(Map<RoleIdentifier, Role> enriched) {
		// ASSIGNEE
		RoleIdentifier roleId = ASSIGNEE;
		Role role = RoleProviderExtension.getOrCreateRole(enriched, roleId);

		Set<Action> actions;
		actions = Role.createActionsSet(actionRegistry,
				ActionTypeConstants.REASSIGN_TASK,
				ActionTypeConstants.EDIT_DETAILS,
				ActionTypeConstants.RESTART,
				ActionTypeConstants.SUSPEND,
				ActionTypeConstants.RELEASE,
				ActionTypeConstants.CREATE);
		addActions(role, actions);

		actions = Role.createActionsSet(actionRegistry, ActionTypeConstants.COMPLETE);
		addActions(role, actions);

		return role;
	}

}
