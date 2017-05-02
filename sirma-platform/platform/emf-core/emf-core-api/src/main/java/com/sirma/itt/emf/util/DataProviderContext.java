package com.sirma.itt.emf.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.seip.context.Context;

/**
 * Contains information needed by {@link DataProvider}s.
 *
 * @author yasko
 */
public final class DataProviderContext extends Context<String, Object> {
	private static final long serialVersionUID = -8484518333768187013L;

	private static final String CONTEXT_KEY_RESULTS = "CONTEXT_KEY_RESULTS";

	/**
	 * Add a result object to the results map.
	 *
	 * @param key
	 *            Result key.
	 * @param value
	 *            Result object.
	 */
	public void addResult(String key, Object value) {
		getResults().put(key, value);
	}

	/**
	 * Getter for the first result in the results map.
	 *
	 * @return First result in the results map.
	 */
	public Object getResult() {
		Map<String, Object> results = getResults();
		if (!results.isEmpty()) {
			return results.values().iterator().next();
		}
		return null;
	}

	/**
	 * Getter for a result in the results map.
	 *
	 * @param key
	 *            Result key.
	 * @return The mapped result.
	 */
	public Object getResult(String key) {
		return getResults().get(key);
	}

	/**
	 * Getter for the results map.
	 *
	 * @return Results map.
	 */
	public Map<String, Object> getResults() {
		Map<String, Object> results = getIfSameType(CONTEXT_KEY_RESULTS, Map.class);
		if (results == null) {
			// TODO: why this here is a map when the only result stored here is a single JsonObject
			results = new LinkedHashMap<>();
			put(CONTEXT_KEY_RESULTS, results);
		}
		return results;
	}

	/**
	 * Clears the results map.
	 */
	public void reset() {
		getResults().clear();
	}
}
