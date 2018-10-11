package com.sirma.itt.seip.permissions.role;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * The RoleEvaluatorManagerService provides capsulated logic that calculates/manipulates user roles.
 */
public interface RoleEvaluatorManagerService {

	/**
	 * Gets the highest role among two roles. If one is null, the other is returned immediately
	 *
	 * @param role1
	 *            the first role
	 * @param role2
	 *            is the second role
	 * @return the highest role from the both roles
	 */
	Role getHighestRole(Role role1, Role role2);

	/**
	 * Gets the root evaluator. This means for an instance the lower priority registered evaluator, which may have/ have
	 * not chained evaluators
	 *
	 * @param <T>
	 *            the generic type for the instance that evaluator works on
	 * @param <S>
	 *            the generic type for the instance
	 * @param instance
	 *            the instance to get evaluator for
	 * @return the root evaluator
	 */
	<T extends Instance, S extends Instance> RoleEvaluator<T> getRootEvaluator(S instance);
}
