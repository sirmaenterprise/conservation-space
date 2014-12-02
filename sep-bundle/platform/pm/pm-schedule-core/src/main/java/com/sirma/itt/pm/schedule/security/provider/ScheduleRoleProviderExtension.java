package com.sirma.itt.pm.schedule.security.provider;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONTRIBUTOR;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.security.ScheduleActionConstants;
import com.sirma.itt.pm.security.PmSecurityModel;

/**
 * Role provider for schedule actions. Enriches the existing ones
 *
 * @author BBonev
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 11)
public class ScheduleRoleProviderExtension extends AbstractRoleProviderExtension implements
		ScheduleActionConstants, PmSecurityModel {

	/** The action registry. */
	@Inject
	private ActionRegistry actionRegistry;

	/**
	 * {@inheritDoc}.<br>
	 * Provides mapping for roles in {@link com.sirma.itt.emf.security.SecurityModel.BaseRoles} enum
	 */
	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> enrichable) {
		consumer(enrichable);

		contributor(enrichable);

		assignee(enrichable);

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
		List<Pair<Class<?>, Action>> actions = null;
		actions = SecurityUtil.createActionsList(ScheduleEntry.class, actionRegistry, OPEN);
		addPermission(permissions, PERMISSION_READ, actions);
		actions = SecurityUtil.createActionsList(ScheduleEntry.class, actionRegistry,
				CHANGE_TASK_COLOR, EDIT_LEFT_LABEL, EDIT_RIGHT_LABEL);
		addPermission(permissions, PERMISSION_CUSTOMIZE, actions);
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
		List<Pair<Class<?>, Action>> actions = null;
		// put the default transitions to the proper permission groups
		// all non standard will go to all list
		actions = SecurityUtil.createActionsList(ScheduleEntry.class, actionRegistry,
				ADD_TASK_MENU, ADD_TASK_ABOVE, ADD_TASK_BELOW, ADD_MILESTONE, ADD_CHILD,
				ADD_SUCCESSOR, ADD_PREDECESSOR, APPROVE, INDENT, OUTDENT);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(ScheduleEntry.class, actionRegistry, EDIT_DETAILS);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(ScheduleEntry.class, actionRegistry, DELETE,
				DELETE_DEPENDENCY_MENU, STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);

	}

	/**
	 * Constructs the {@link com.sirma.itt.emf.security.SecurityModel.ActivitiRoles#ASSIGNEE} role
	 * model
	 *
	 * @param enrichable
	 *            the current permission model to be updated
	 */
	private void assignee(Map<RoleIdentifier, Role> enrichable) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = getPermissions(enrichable,
				ActivitiRoles.ASSIGNEE);
		List<Pair<Class<?>, Action>> actions = null;
		// put the default transitions to the proper permission groups
		// all non standard will go to all list
		actions = SecurityUtil.createActionsList(ScheduleEntry.class, actionRegistry,
				ADD_TASK_MENU, ADD_TASK_ABOVE, ADD_TASK_BELOW, ADD_MILESTONE, ADD_CHILD,
				ADD_SUCCESSOR, ADD_PREDECESSOR, APPROVE, INDENT, OUTDENT);
		addPermission(permissions, PERMISSION_CREATE, actions);

		actions = SecurityUtil.createActionsList(ScheduleEntry.class, actionRegistry, EDIT_DETAILS);
		addPermission(permissions, PERMISSION_EDIT, actions);

		actions = SecurityUtil.createActionsList(ScheduleEntry.class, actionRegistry, DELETE,
				DELETE_DEPENDENCY_MENU, STOP);
		addPermission(permissions, PERMISSION_DELETE, actions);

	}

}
