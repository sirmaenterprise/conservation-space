package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Used as result object for existing check for instances. It provides convenient methods for retrieving specific
 * information from the result map from the check.
 *
 * @author A. Kunchev
 */
public class InstanceExistResult<S extends Serializable> {

	private final Map<S, Boolean> RESULT_MAP;

	/**
	 * Instantiates new result object.
	 *
	 * @param results map with results from existence check
	 */
	public InstanceExistResult(final Map<S, Boolean> results) {
		RESULT_MAP = Objects.requireNonNull(results, "The result map should not be [null].");
	}

	/**
	 * Returns modifiable map with the results from the check. As map keys are used the ids of the instances and the
	 * value shows if that instance exists in the system or not.
	 *
	 * @return modifiable map with the results from the existence check
	 */
	public Map<S, Boolean> getAll() {
		return new HashMap<>(RESULT_MAP);
	}

	/**
	 * Returns only ids of instances which exist in the system.
	 *
	 * @return collection of ids of existing instances
	 */
	public Collection<S> get() {
		return filterResults(onlyExistingFilter());
	}

	/**
	 * Returns only ids of instances which does not exist in the system.
	 *
	 * @return collection of ids of not existing instance
	 */
	public Collection<S> getNotExisting() {
		return filterResults(onlyExistingFilter().negate());
	}

	private Collection<S> filterResults(Predicate<S> filter) {
		return RESULT_MAP.keySet().stream().filter(filter).collect(Collectors.toSet());
	}

	private Predicate<S> onlyExistingFilter() {
		return RESULT_MAP::get;
	}
}
