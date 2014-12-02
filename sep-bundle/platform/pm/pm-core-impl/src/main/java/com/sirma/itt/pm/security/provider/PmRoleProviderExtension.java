package com.sirma.itt.pm.security.provider;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONTRIBUTOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.MANAGER;
import static com.sirma.itt.pm.security.PmSecurityModel.PmRoles.PROJECT_MANAGER;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.security.provider.AbstractRoleProviderExtension;
import com.sirma.itt.cmf.security.provider.RoleProviderExtension;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.SecurityUtil;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.ActionRegistry;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.RoleImpl;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;
import com.sirma.itt.pm.security.PmSecurityModel;

/**
 * The PmRoleProviderExtension provides the roles and permission for that roles related to pm
 * module.
 */
@ApplicationScoped
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 10)
public class PmRoleProviderExtension extends AbstractRoleProviderExtension implements
		PmSecurityModel, PmActionTypeConstants {

	/** The action registry. */
	@Inject
	private ActionRegistry actionRegistry;

	/**
	 * {@inheritDoc}.<br>
	 * Provides mapping for roles in {@link com.sirma.itt.pm.security.PmSecurityModel.PmRoles} enum
	 */
	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> enrichable) {
		consumer(enrichable);
		contributor(enrichable);
		manager(enrichable);
		enrichable.put(PROJECT_MANAGER, pmManager());

		return enrichable;
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CONSUMER} role model
	 * 
	 * @param enrichable
	 *            the current permission model to be updated
	 */
	private void consumer(Map<RoleIdentifier, Role> enrichable) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable,
				CONSUMER);
		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(
				ProjectInstance.class, actionRegistry, ActionTypeConstants.CREATE_TOPIC);
		addPermission(permissions, PERMISSION_COMMENT, actions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#CONTRIBUTOR} role
	 * model
	 * 
	 * @param enrichable
	 *            the current permission model to be updated
	 */
	private void contributor(Map<RoleIdentifier, Role> enrichable) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable,
				CONTRIBUTOR);
		// add the start task and create case on project
		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(
				ProjectInstance.class, actionRegistry, CREATE_CASE, CREATE_TASK, CREATE_LINK,
				ATTACH_OBJECT, CREATE_OBJECT);
		addPermission(permissions, PERMISSION_CREATE, actions);

	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles#MANAGER} role model
	 * 
	 * @param enrichable
	 *            the current permission model to be updated
	 */
	private void manager(Map<RoleIdentifier, Role> enrichable) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable,
				MANAGER);
		// add the delete operation
		List<Pair<Class<?>, Action>> actions = SecurityUtil.createActionsList(
				ProjectInstance.class, actionRegistry, DELETE);
		addPermission(permissions, PERMISSION_DELETE, actions);

		actions = SecurityUtil.createActionsList(WorkflowInstanceContext.class, actionRegistry,
				ActionTypeConstants.STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);
	}

	/**
	 * Constructs the {@link com.sirma.itt.pm.security.PmSecurityModel.PmRoles#PROJECT_MANAGER} role
	 * model
	 * 
	 * @return the pm manager role
	 */
	private Role pmManager() {
		RoleIdentifier roleId;

		List<Pair<Class<?>, Action>> actions;
		// copy permissions for PROJECT_MEMBER to PROJECT_MANAGER
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = new LinkedHashMap<>();

		// Collaborator
		roleId = PROJECT_MANAGER;

		// put the default transitions to the proper permission groups
		// all non standard will go to all list
		actions = SecurityUtil.createActionsList(ProjectInstance.class, actionRegistry,
				CREATE_PROJECT, START, APPROVE, RESTART, CREATE_CASE, CREATE_TASK, ATTACH_OBJECT,
				CREATE_OBJECT);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(ProjectInstance.class, actionRegistry,
				MANAGE_RELATIONS, MANAGE_RESOURCES, EDIT_DETAILS, COMPLETE,
				ActionTypeConstants.EDIT_DETAILS);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(ProjectInstance.class, actionRegistry, DELETE,
				STOP, SUSPEND);
		addPermission(permissions, PERMISSION_DELETE, actions);

		return new RoleImpl(roleId, permissions);
	}
}
