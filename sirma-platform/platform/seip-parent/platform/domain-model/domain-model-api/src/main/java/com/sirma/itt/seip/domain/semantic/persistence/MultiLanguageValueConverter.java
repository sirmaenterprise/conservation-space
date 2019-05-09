package com.sirma.itt.seip.domain.semantic.persistence;

import java.util.Locale;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

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

	private static final String ENGLISH = Locale.ENGLISH.getLanguage();
	@Inject
	private UserPreferences userPreferences;

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(MultiLanguageValue.class, String.class, this::getLabel);
	}

	@SuppressWarnings("unchecked")
	private String getLabel(MultiLanguageValue multiLanguageValue) {
		String language = userPreferences.getLanguage();
		if (!multiLanguageValue.hasValueForLanguage(language) && multiLanguageValue.hasValueForLanguage(ENGLISH)) {
			language = ENGLISH;
		}
		String values = multiLanguageValue.getValues(language).collect(Collectors.joining(","));
		return StringUtils.trimToNull(values);
	}

}
