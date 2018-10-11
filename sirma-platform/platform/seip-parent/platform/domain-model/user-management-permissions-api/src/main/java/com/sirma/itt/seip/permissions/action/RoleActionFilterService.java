package com.sirma.itt.seip.permissions.action;

import java.util.Set;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;

/**
 * The RoleActionEvaluatorService is service that is responsible to filter the actions set based on current context.
 *
 * @author bbanchev
 */
public interface RoleActionFilterService {

	/**
	 * Filter the actions using the context
	 *
	 * @param action
	 *            the set
	 * @param context
	 *            is the context
	 * @return the sets the
	 */
	Set<Action> filter(Set<Action> action, RoleActionEvaluatorContext context);

	/**
	 * Get all available filters
	 * 
	 * @return all available filters
	 */
	Set<String> getFilters();
}
