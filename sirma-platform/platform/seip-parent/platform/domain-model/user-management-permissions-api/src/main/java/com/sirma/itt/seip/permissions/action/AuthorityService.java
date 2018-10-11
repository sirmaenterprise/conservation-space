package com.sirma.itt.seip.permissions.action;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.PermissionIdentifier;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.resources.Resource;

/**
 * AuthorityService for managing user operations and roles.
 *
 * @author BBonev
 */
public interface AuthorityService {

	/**
	 * Gets the current user allowed actions on the given instance.
	 *
	 * @param instance
	 *            the instance to calculate actions for
	 * @param placeholder
	 *            the placeholder html element from where the actions are requested
	 * @return the allowed actions
	 */
	Set<Action> getAllowedActions(Instance instance, String placeholder);

	/**
	 * Gets the current user allowed action names on the given instance.
	 *
	 * @param instance
	 *            the instance to calculate actions for
	 * @param placeholder
	 *            the placeholder html element from where the actions are requested
	 * @return the allowed actions
	 */
	default Set<String> getAllowedActionNames(Instance instance, String placeholder) {
		return getAllowedActions(instance, placeholder).stream().map(Action::getActionId).collect(Collectors.toSet());
	}

	/**
	 * Gets the allowed action by actionId for provided instance.
	 *
	 * @param instance
	 *            the instance
	 * @param actionId
	 *            the action id
	 * @param placeholder
	 *            the placeholder html element from where the actions are requested
	 * @return the allowed action
	 */
	Action getAllowedAction(Instance instance, String actionId, String placeholder);

	/**
	 * Gets the user role.
	 *
	 * @param instance
	 *            the instance
	 * @param resource
	 *            the resource
	 * @return the user role
	 */
	Role getUserRole(Instance instance, Resource resource);

	/**
	 * Checks if is operation allowed to be executed by the current user on the given instance context.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param placeholder
	 *            for where in the web pages this action is intended to be visualized
	 * @return true, if is operation allowed
	 */
	boolean isActionAllowed(Instance instance, String operation, String placeholder);

	/**
	 * Checks if is operation allowed to be executed by the given user on the given instance context.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param resource
	 *            the resource
	 * @param placeholder
	 *            for where in the web pages this action is intended to be visualized
	 * @return true, if is operation allowed
	 */
	boolean isActionAllowed(Instance instance, String operation, Resource resource, String placeholder);

	/**
	 * Filter allowed actions for the current user in the given instance context.
	 *
	 * @param instance
	 *            the instance
	 * @param placeholder
	 *            the placeholder html element from where the actions are requested
	 * @param actions
	 *            the actions
	 * @return the sets of allowed actions. The result may be empty list if nothing is allowed but never
	 *         <code>null</code>.
	 */
	Set<Action> filterAllowedActions(Instance instance, String placeholder, String... actions);

	/**
	 * Check if user has a permission for given instance
	 *
	 * @param permission
	 *            to check for
	 * @param target
	 *            the instance to check
	 * @param resource
	 *            the user to check
	 * @return {@link Boolean#TRUE} on has permission, {@link Boolean#FALSE}, null on unknown
	 */
	boolean hasPermission(PermissionIdentifier permission, Instance target, Resource resource);

	/**
	 * Check if user has a permission for given collection of instances
	 *
	 * @param permission
	 *            to check for
	 * @param target
	 *            the instances to check permission of
	 * @param resource
	 *            the user to check
	 * @return for each instance {@link Boolean#TRUE} on has permission, {@link Boolean#FALSE}, null on unknown
	 */
	Map<Instance, Boolean> hasPermission(PermissionIdentifier permission, Collection<Instance> target, Resource resource);

	/**
	 * Checks if currently logged user is admin or system user.
	 *
	 * @return true, if user is admin or system user or belongs to admin group
	 */
	boolean isAdminOrSystemUser();

	/**
	 * Checks if is the user is admin or system user.
	 *
	 * @param user
	 *            to be checked
	 * @return true, if user is admin or system user or belongs to admin group
	 */
	boolean isAdminOrSystemUser(Resource user);
}
