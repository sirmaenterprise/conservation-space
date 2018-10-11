package com.sirma.itt.emf.label.retrieve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Converts action id to action label using action codelist.
 */
@ApplicationScoped
@Extension(target = FieldValueRetriever.TARGET_NAME, order = 0)
public class ActionFieldValueRetriever extends PairFieldValueRetriever {
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<String> SUPPORTED_FIELDS;
	private static final Map<Integer, String> SUFFIXES;
	private static final int ALL_ACTIONS = 9;
	private static final int TASK_ACTIONS = 229;

	static {
		SUPPORTED_FIELDS = new ArrayList<>(1);
		SUPPORTED_FIELDS.add(FieldId.ACTION_ID);
		SUFFIXES = new HashMap<>(4);
		SUFFIXES.put(ALL_ACTIONS, "");
		SUFFIXES.put(TASK_ACTIONS, " (Task)");
	}

	@Inject
	private CodelistService codelistService;

	@Override
	public String getLabel(String value, SearchRequest additionalParameters) {
		if (value != null) {
			CodeValue codeValue = codelistService.getCodeValue(TASK_ACTIONS, value);
			String suffix = SUFFIXES.get(TASK_ACTIONS);
			if (codeValue == null) {
				codeValue = codelistService.getCodeValue(ALL_ACTIONS, value);
				suffix = SUFFIXES.get(ALL_ACTIONS);
			}
			if (codeValue != null && codeValue.getProperties() != null) {
				Serializable label = codeValue.getProperties().get(getCurrentUserLanguage()) + suffix;
				if (label != null) {
					return label.toString();
				}
			}
		}
		return value;
	}

	@Override
	public RetrieveResponse getValues(String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		int retrieveOffset = 0;
		if (offset != null) {
			retrieveOffset = offset.intValue();
		}
		long total = 0;

		List<Pair<String, String>> results = new ArrayList<>();
		Set<Integer> codeLists = SUFFIXES.keySet();

		for (Integer codelist : codeLists) {
			Map<String, CodeValue> codeValues = codelistService.getCodeValues(codelist, true);
			for (Entry<String, CodeValue> entry : codeValues.entrySet()) {

				CodeValue codevalue = entry.getValue();

				total = getValuesFromCodeValue(filter, limit, retrieveOffset, total, results, codelist, codevalue);

			}
		}
		return new RetrieveResponse(total, results);
	}

	/**
	 * Based on the provided parameters, retrieves all eligible values from the code value and adds them to the results
	 * map. Finally returns the total amount of retrieved values.
	 *
	 * @param filter
	 *            - the filter used to filter values
	 * @param limit
	 *            - the limit for retrieving
	 * @param retrieveOffset
	 *            - the offset for retrieving
	 * @param total
	 *            - the current amount of retrieved values
	 * @param results
	 *            - the results map where values are stored
	 * @param codelist
	 *            - the code list number
	 * @param codevalue
	 *            - the code value
	 * @return the total amount of retrieved values
	 */
	private long getValuesFromCodeValue(String filter, Integer limit, int retrieveOffset, long total,
			List<Pair<String, String>> results, Integer codelist, CodeValue codevalue) {

		Map<String, Serializable> properties = codevalue.getProperties();
		String currentUserLanguage = getCurrentUserLanguage();
		if (properties != null && properties.containsKey(currentUserLanguage)) {
			Serializable label = properties.get(currentUserLanguage) + SUFFIXES.get(codelist);
			String codeValueValue = label.toString();
			if (StringUtils.isBlank(codeValueValue)) {
				codeValueValue = codevalue.getValue();
			}

			if (StringUtils.isBlank(filter) || codeValueValue.toLowerCase().startsWith(filter.toLowerCase())) {
				validateAndAddPair(results, codevalue.getValue(), codeValueValue, filter, retrieveOffset, limit, total);
				return total + 1;
			}
		}
		return total;
	}

	@Override
	public List<String> getSupportedObjects() {
		return SUPPORTED_FIELDS;
	}
}
