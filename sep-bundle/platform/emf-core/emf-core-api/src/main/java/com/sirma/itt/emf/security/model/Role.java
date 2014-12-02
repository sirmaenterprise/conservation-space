package com.sirma.itt.emf.security.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.emf.domain.Pair;


/**
 * Defines the methods for a single user role. The implementation must provide
 * the allowed actions and concrete permissions for each single role.
 *
 * @author BBonev
 */
public interface Role extends Sealable {

	/**
	 * Gets the role id.
	 *
	 * @return the role id
	 */
	RoleIdentifier getRoleId();

	/**
	 * Gets the allowed actions for the current role.
	 *
	 * @param <A>
	 *            the generic type
	 * @return the allowed actions
	 */
	<A extends Action> Set<A> getAllAllowedActions();

	/**
	 * Gets the allowed actions for the current role.
	 *
	 * @param <A>
	 *            the generic type
	 * @param targetClass
	 *            the target class
	 * @return the allowed actions
	 */
	<A extends Action> Set<A> getAllowedActions(Class<?> targetClass);

	/**
	 * Gets the permissions associated with the given role.
	 *
	 * @param <A>
	 *            the generic type
	 * @param <P>
	 *            the generic type
	 * @return the permissions
	 */
	<A extends Action, P extends Permission> Map<P, List<Pair<Class<?>, Action>>> getPermissions();

	/**
	 * Adds the given permission with it's actions to the permissions map if the
	 * object is not sealed, yet. If sealed the method should ignore the
	 * request.
	 *
	 * @param <A>
	 *            the generic action type
	 * @param <P>
	 *            the generic permission type
	 * @param permission
	 *            the permission
	 * @param actions
	 *            the list of actions for that permission
	 */
	<A extends Action, P extends Permission> void addPermission(P permission,
			List<Pair<Class<?>, Action>> actions);
}
