package com.sirma.itt.seip.permissions.action;

import java.util.Map;
import java.util.function.Predicate;

import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provide predicates for filter actions. Data is in format key-&gt;filter function
 *
 * @author bbanchev
 */
public interface RoleActionFilterProvider extends Plugin {
	/** Extension name. */
	String TARGET_NAME = "RoleActionFilterProvider";

	/**
	 * Provide a map of filter by name to its predicate function
	 *
	 * @return a set of unique/overriding filters
	 */
	Map<String, Predicate<RoleActionEvaluatorContext>> provideFilters();

}
