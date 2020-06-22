package com.sirma.itt.emf.label.retrieve;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Abstract class containing common logic for field value retrievers that need to be internationalized and return pairs
 * of data.
 *
 * @author nvelkov
 */
public abstract class PairFieldValueRetriever implements FieldValueRetriever {

	@Inject
	private UserPreferences userPreferences;

	/**
	 * Gets the current user language.
	 *
	 * @return the current user language
	 */
	protected String getCurrentUserLanguage() {
		return userPreferences.getLanguage();
	}

	@Override
	public String getLabel(String value) {
		return getLabel(value, null);
	}

	@Override
	public Map<String, String> getLabels(String[] values) {
		return getLabels(values, null);
	}

	@Override
	public Map<String, String> getLabels(String[] values, SearchRequest additionalParameters) {
		Map<String, String> result = CollectionUtils.createHashMap(values.length);
		for (int i = 0; i < values.length; i++) {
			String label = getLabel(values[i], additionalParameters);
			if (StringUtils.isBlank(label)) {
				label = values[i];
			}
			result.put(values[i], label);
		}
		return result;
	}

	@Override
	public RetrieveResponse getValues(String filter, Integer offset, Integer limit) {
		return getValues(filter, null, offset, limit);
	}

	/**
	 * Validate and create a pair from the first and second elements. The second element should start with the filter,
	 * the offset should be lower than the total, which should be lower than the limit (done for paging purposes).
	 *
	 * @param results
	 *            the results to which the pair will be added
	 * @param first
	 *            the first element of the pair
	 * @param second
	 *            the second element of the pair
	 * @param filter
	 *            the filter, the second element should start with
	 * @param offset
	 *            the offset
	 * @param limit
	 *            the limit
	 * @param total
	 *            the total
	 */
	@SuppressWarnings("static-method")
	protected void validateAndAddPair(List<Pair<String, String>> results, String first, String second, String filter,
			int offset, Integer limit, long total) {
		if (offset <= total && (limit == null || results.size() < limit.intValue())) {
			results.add(new Pair<>(first, second));
		}
	}

	/**
	 * Adds multiple values the to multi value map. If the map does not contain a list related to the provided key, it
	 * creates the list.
	 *
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @param values
	 *            the values
	 */
	protected static void addToMultiValueMap(Map<String, List<String>> map, String key, List<String> values) {
		List<String> list = map.get(key);
		if (list == null) {
			list = new ArrayList<>(values.size());
		}
		list.addAll(values);
		map.put(key, list);
	}

	/**
	 * Adds single value the to multi value map. If the map does not contain a list related to the provided key, it
	 * creates the list.
	 *
	 * @param map
	 *            the map
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	protected static void addToMultiValueMap(Map<String, List<String>> map, String key, String value) {
		List<String> list = map.get(key);
		if (list == null) {
			list = new ArrayList<>(1);
		}
		list.add(value);
		map.put(key, list);
	}

}
