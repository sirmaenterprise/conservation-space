package com.sirma.itt.cmf.security.provider;

import static com.sirma.itt.emf.security.SecurityModel.ActivitiRoles.ASSIGNEE;
import static com.sirma.itt.emf.security.SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.SecurityUtil;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.ActionRegistry;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.RoleImpl;

/**
 * The ActivitiRoleProviderExtension provides the roles and permission for that roles related to
 * activiti/processes module.
 */
@ApplicationScoped
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 1)
public class ActivitiRoleProviderExtension extends CollectableRoleProviderExtension {

	/** The action registry. */
	@Inject
	private ActionRegistry actionRegistry;

	/**
	 * {@inheritDoc}.<br>
	 * Provides mapping for roles in {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles} enum
	 * and {@link com.sirma.itt.emf.security.SecurityModel.ActivitiRoles}
	 */
	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> enriched) {
		Map<RoleIdentifier, Role> map = new HashMap<RoleIdentifier, Role>();
		map.put(POSSIBLE_ASSIGNEE, possibleAssignee());
		map.put(ASSIGNEE, assignee());
		enriched.putAll(map);
		// create new creator role or update existing one
		enriched.put(CREATOR, creator(enriched));

		// copy from basic permissions
		merge(enriched, CONSUMER, POSSIBLE_ASSIGNEE);
		merge(enriched, CONSUMER, ASSIGNEE);
		merge(enriched, ASSIGNEE, CREATOR);
		return enriched;
	}

	/**
	 * Constructs the
	 * {@link com.sirma.itt.emf.security.SecurityModel.ActivitiRoles#POSSIBLE_ASSIGNEE} role model
	 *
	 * @return the possible assignee role
	 */
	private Role possibleAssignee() {

		// Collaborator
		RoleIdentifier roleId = POSSIBLE_ASSIGNEE;
		// copy permissions for consumer to collaborator
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = new HashMap<Permission, List<Pair<Class<?>, Action>>>();

		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(
				StandaloneTaskInstance.class, actionRegistry, ActionTypeConstants.CLAIM);
		addPermission(permissions, PERMISSION_EDIT, actions);
		actions = SecurityUtil.createActionsList(TaskInstance.class, actionRegistry,
				ActionTypeConstants.CLAIM);
		addPermission(permissions, PERMISSION_EDIT, actions);

		return new RoleImpl(roleId, permissions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.ActivitiRoles#ASSIGNEE} role
	 * model
	 *
	 * @return the assignee role
	 */
	private Role assignee() {
		// ASSIGNEE
		RoleIdentifier roleId = ASSIGNEE;
		// copy permissions for consumer to collaborator
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = new HashMap<Permission, List<Pair<Class<?>, Action>>>();

		List<Pair<Class<?>, Action>> actions = null;
		// ######################### WOREKFLOW TASK ######################
		actions = SecurityUtil.createActionsList(TaskInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_TASK);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(TaskInstance.class, actionRegistry,
				ActionTypeConstants.REASSIGN_TASK, ActionTypeConstants.EDIT_DETAILS,
				ActionTypeConstants.RESTART, ActionTypeConstants.SUSPEND,
				ActionTypeConstants.RELEASE, ActionTypeConstants.CREATE_TASK);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(TaskInstance.class, actionRegistry,
				ActionTypeConstants.COMPLETE);
		addPermission(permissions, PERMISSION_DELETE, actions);
		// ######################### STANDALONE TASK ######################

		actions = SecurityUtil.createActionsList(StandaloneTaskInstance.class, actionRegistry,
				ActionTypeConstants.CREATE_TASK);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(StandaloneTaskInstance.class, actionRegistry,
				ActionTypeConstants.REASSIGN_TASK, ActionTypeConstants.EDIT_DETAILS,
				ActionTypeConstants.RESTART, ActionTypeConstants.SUSPEND,
				ActionTypeConstants.RELEASE, ActionTypeConstants.CREATE_TASK);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(StandaloneTaskInstance.class, actionRegistry,
				ActionTypeConstants.COMPLETE, ActionTypeConstants.STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);

		return new RoleImpl(roleId, permissions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CREATOR} role model
	 *
	 * @param enrichable
	 *            is the current model
	 * @return the creator role
	 */
	private Role creator(Map<RoleIdentifier, Role> enrichable) {
		// the creator
		RoleIdentifier roleId = CREATOR;
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable, roleId);
		if (permissions == null) {
			permissions = new HashMap<Permission, List<Pair<Class<?>, Action>>>();
		}
		List<Pair<Class<?>, Action>> actions = null;
		actions = SecurityUtil.createActionsList(StandaloneTaskInstance.class, actionRegistry,
				ActionTypeConstants.STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);

		actions = SecurityUtil.createActionsList(TaskInstance.class, actionRegistry,
				ActionTypeConstants.STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);

		actions = SecurityUtil.createActionsList(WorkflowInstanceContext.class, actionRegistry,
				ActionTypeConstants.STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);

		return new RoleImpl(roleId, permissions);

	}
}
