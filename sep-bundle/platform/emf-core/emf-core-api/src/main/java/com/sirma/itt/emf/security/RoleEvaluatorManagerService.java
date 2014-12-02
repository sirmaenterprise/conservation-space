package com.sirma.itt.emf.security;

import java.util.Map;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.Role;

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
	public Role getHighestRole(Role role1, Role role2);

	/**
	 * Gets the evaluator type for an instance
	 *
	 * @param instance
	 *            the instance to get info for
	 * @return the evaluator type constant for the particular object
	 */
	String getEvaluatorType(Instance instance);

	/**
	 * Gets the root evaluator. This means for an instance the lower priority registered evaluator,
	 * which may have/ have not chained evaluators
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

	/**
	 * Gets all root evaluators that are registered to all classes in the system
	 *
	 * @return the root evaluators in the system, keyed by the class that are responsible for
	 */
	Map<Class<? extends Instance>, RoleEvaluator<? extends Instance>> getRootEvaluators();
}
