package com.sirma.itt.seip.domain.semantic.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Converter implementation for the {@link MultiLanguageValue}.
 *
 * @author nvelkov
 */
@ApplicationScoped
public class MultiLanguageValueConverter implements TypeConverterProvider {

	@Inject
	private UserPreferences userPreferences;

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(MultiLanguageValue.class, String.class, source -> getLabel(source));
	}

	@SuppressWarnings("unchecked")
	private String getLabel(MultiLanguageValue multiLanguageValue) {
		String language = userPreferences.getLanguage();
		Serializable values = multiLanguageValue.getValues(language);
		if (values instanceof Set) {
			return ((Set<Serializable>) values).stream().map(Serializable::toString).collect(Collectors.joining(","));
		}
		return Objects.toString(values, null);
	}

}
