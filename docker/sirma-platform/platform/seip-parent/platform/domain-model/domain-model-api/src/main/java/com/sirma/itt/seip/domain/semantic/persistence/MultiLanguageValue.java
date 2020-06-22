package com.sirma.itt.seip.domain.semantic.persistence;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.exception.EmfRuntimeException;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a multi-language literal.
 *
 * @author nvelkov
 */
public class MultiLanguageValue implements Serializable, Copyable<MultiLanguageValue> {
	private static final long serialVersionUID = 1965358289351900387L;
	/**
	 * Default size of the language to value mapping.
	 */
	private static final int MAPPING_DEFAULT_SIZE = 5;
	private Map<String, Serializable> languageToValueMapping;

	/**
	 * Streams all values for the given language. If no value is defined for the language empty stream will be returned.
	 *
	 * @param language
	 * 		the language to fetch it's values
	 * @return the stream of all values for the given language or empty stream
	 */
	public Stream<String> getValues(String language) {
		if (CollectionUtils.isEmpty(languageToValueMapping)) {
			return Stream.empty();
		}
		Serializable values = languageToValueMapping.get(language);
		return streamValue(values);
	}

	/**
	 * Checks if the current instance has a value for the given language.
	 *
	 * @param language the language to check for
	 * @return true if there a value entry for the given language and the method {@link #getValues(String)}
	 * will return non empty {@link Stream}
	 */
	public boolean hasValueForLanguage(String language) {
		return CollectionUtils.isNotEmpty(languageToValueMapping) && languageToValueMapping.containsKey(language);
	}

	@SuppressWarnings("unchecked")
	private Stream<String> streamValue(Serializable value) {
		if (value instanceof Collection) {
			return ((Collection<String>) value).stream();
		} else if (value instanceof String) {
			return Stream.of((String) value);
		}
		return Stream.empty();
	}

	/**
	 * Gets all possible values in all languages.
	 *
	 * @return list of Serializable that contains different languages.
	 */
	public Stream<Serializable> getAllValues() {
		if (languageToValueMapping == null) {
			return Stream.empty();
		}
		return languageToValueMapping.values().stream().flatMap(this::streamValue);
	}

	/**
	 * Executes the provided consumer on the available language mapping. If for some language there multiple values, then the consumer will
	 * be invoked for all of them.
	 *
	 * @param consumer consumer to be invoked for each available language <-> value mapping
	 */
	public void forEach(BiConsumer<String, String> consumer) {
		if (languageToValueMapping == null) {
			return;
		}
		languageToValueMapping.forEach((lang, label) -> {
			if (label instanceof String) {
				consumer.accept(lang, (String) label);
			} else if (label instanceof Collection) {
				// Single language could have multiple values in MultiLanguageValue
				Collection labels = (Collection) label;
				labels.forEach(l -> consumer.accept(lang, l.toString()));
			}
		});
	}

	/**
	 * Add a value to the multi language mapping. If no value is found for the given language, it will be inserted in
	 * the mapping. If a value is already found for the given language, it will be converted to a {@link Set} and the new value
	 * will be added to that set.
	 *
	 * @param language
	 * 		the language
	 * @param value
	 * 		the value
	 */
	public void addValue(String language, String value) {
		if (language == null || StringUtils.isBlank(value)) {
			return;
		}
		if (CollectionUtils.isEmpty(languageToValueMapping)) {
			languageToValueMapping = new HashMap<>(MAPPING_DEFAULT_SIZE);
		}
		languageToValueMapping.computeIfPresent(language, (lang, current) -> mergeSameLangMultiValues(value, current));
		languageToValueMapping.putIfAbsent(language, value);
	}

	@SuppressWarnings("unchecked")
	private Serializable mergeSameLangMultiValues(String value, Serializable current) {
		if (current instanceof Set) {
			((Set<String>) current).add(value);
			return current;
		}
		HashSet<Serializable> valuesSet = new HashSet<>(MAPPING_DEFAULT_SIZE);
		valuesSet.add(current);
		valuesSet.add(value);
		return valuesSet;
	}

	/**
	 * Remove any value or values associated with the supplied language.
	 *
	 * @param language the language for which to remove value or values
	 */
	public void removeValue(String language) {
		if (language == null) {
			return;
		}
		if (!CollectionUtils.isEmpty(languageToValueMapping)) {
			languageToValueMapping.remove(language);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MultiLanguageValue that = (MultiLanguageValue) o;
		return Objects.equals(languageToValueMapping, that.languageToValueMapping);
	}

	@Override
	public int hashCode() {
		return Objects.hash(languageToValueMapping);
	}

	@Override
	public MultiLanguageValue createCopy() {
		MultiLanguageValue copy = new MultiLanguageValue();
		if (languageToValueMapping != null) {
			this.languageToValueMapping.keySet()
					.forEach(lang -> getValues(lang).forEach(value -> copy.addValue(lang, value)));
		}
		return copy;
	}
}
