package com.sirma.itt.seip.permissions.role;

import java.io.Serializable;
import java.util.Set;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.action.RoleActionFilterService;
import com.sirma.itt.seip.resources.Resource;

/**
 * The RoleActionEvaluatorContext is wrapper for settings used in role evaluators to provide fine grained evaluation.
 *
 * @author bbanchev
 */
public class RoleActionEvaluatorContext extends Context<String, Object> {

	private static final long serialVersionUID = 6719976546838335432L;

	private Instance target;
	private Role calculatedRole;
	private Resource authority;
	private RoleActionFilterService roleActionFileterService;

	/**
	 * Instantiates a new role action evaluator context.
	 *
	 * @param contextService
	 *            the context service
	 * @param target
	 *            is the current instance under evaluation
	 * @param authority
	 *            is the current user under evaluation
	 * @param calculatedRole
	 *            is the current role under evaluation
	 */
	public RoleActionEvaluatorContext(RoleActionFilterService contextService, Instance target, Resource authority,
			Role calculatedRole) {
		roleActionFileterService = contextService;
		this.target = target;
		this.authority = authority;
		this.calculatedRole = calculatedRole;
	}

	/**
	 * Getter method for target.
	 *
	 * @return the target
	 */
	public Instance getTarget() {
		return target;
	}

	/**
	 * Getter method for calculatedRole.
	 *
	 * @return the calculatedRole
	 */
	public Role getCalculatedRole() {
		return calculatedRole;
	}

	/**
	 * Getter method for authority.
	 *
	 * @return the authority
	 */
	public Resource getAuthority() {
		return authority;
	}

	/**
	 * Executes the filtering.
	 *
	 * @param action
	 *            the action
	 * @return the sets the
	 */
	public Set<Action> filter(Set<Action> action) {
		return roleActionFileterService.filter(action, this);

	}

	/**
	 * Gets the instance property.
	 *
	 * @param key
	 *            the key to search for
	 * @return the instance property associated with this key
	 */
	public Serializable getInstanceProperty(String key) {
		return getTarget().get(key);
	}
}
