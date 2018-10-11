package com.sirma.itt.seip.domain.semantic.persistence;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.exception.EmfRuntimeException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Represents a multi-language literal.
 *
 * @author nvelkov
 */
public class MultiLanguageValue implements Serializable {
	private static final long serialVersionUID = 1965358289351900387L;
	/**
	 * Default size of the language to value mapping.
	 */
	private static final int MAPPING_DEFAULT_SIZE = 5;
	private Map<String, Serializable> languageToValueMapping;

	/**
	 * Retrieve the values based on the language. If no values are found for the provided language, the values for the
	 * first found language will be returned. <b> The returned values might be a single serializable value or a set of
	 * serializable values depending on the value in the semantic db. </b>
	 *
	 * @param language
	 * 		the language
	 * @return the values
	 */
	public Serializable getValues(String language) {
		if (CollectionUtils.isEmpty(languageToValueMapping)) {
			throw new EmfRuntimeException("There are no values at all in the multi-language map. "
												  + "Check the library configuration if there are correctly set titles");
		}
		Serializable values = languageToValueMapping.get(language);
		if (values == null) {
			return languageToValueMapping.entrySet().iterator().next().getValue();
		}
		return values;
	}

	/**
	 * Gets all possible values in all languages.
	 *
	 * @return list of Serializable that contains different languages.
	 */
	public Stream<Serializable> getAllValues() {
		return languageToValueMapping.values().stream();
	}

	/**
	 * Add a value to the multi language mapping. If no value is found for the given language, it will be inserted in
	 * the mapping. If a value is already found for the given language, it will be converted to a set and the new value
	 * will be added to that set.
	 *
	 * @param language
	 * 		the language
	 * @param value
	 * 		the value
	 */
	@SuppressWarnings("unchecked")
	public void addValue(String language, Serializable value) {
		if (CollectionUtils.isEmpty(languageToValueMapping)) {
			languageToValueMapping = new HashMap<>(MAPPING_DEFAULT_SIZE);
		}
		Serializable values = languageToValueMapping.get(language);
		if (values != null) {
			if (!(values instanceof Set)) {
				Set<Serializable> valuesSet = new HashSet<>(MAPPING_DEFAULT_SIZE);
				valuesSet.add(values);
				values = (Serializable) valuesSet;
			}
			((Set<Serializable>) values).add(value);
		} else {
			values = value;
		}
		languageToValueMapping.put(language, values);

	}
}
