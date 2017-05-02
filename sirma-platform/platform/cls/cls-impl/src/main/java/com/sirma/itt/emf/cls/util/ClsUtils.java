package com.sirma.itt.emf.cls.util;

import java.util.List;
import java.util.Locale;

import com.sirma.itt.emf.cls.entity.Description;

/**
 * Provides common utility methods and constants for working with codelists in CLS.
 *
 * @author Vilizar Tsonev
 */
public final class ClsUtils {

	public static final String LANGUAGE_EN = new Locale("en").getLanguage();

	public static final String LANGUAGE_BG = new Locale("bg").getLanguage();

	/**
	 * Not instantiated utility class.
	 */
	private ClsUtils() {
		// nothing to do
	}

	/**
	 * Gets the description by the given language.
	 *
	 * @param descriptions
	 *            is the {@link List} of descriptions
	 * @param language
	 *            is the language code
	 * @return the {@link Description} in the given language
	 */
	public static Description getDescriptionByLanguage(List<? extends Description> descriptions, String language) {
		return descriptions
				.stream()
					.filter(description -> description.getLanguage().equalsIgnoreCase(language))
					.findFirst()
					.orElse(null);
	}

	/**
	 * Gets the description by language text.
	 *
	 * @param descriptions
	 *            the descriptions
	 * @param language
	 *            the language
	 * @return the description by language text
	 */
	public static String getDescriptionByLanguageText(List<? extends Description> descriptions, String language) {
		Description description = getDescriptionByLanguage(descriptions, language);
		if (description != null) {
			return description.getDescription();
		}
		return null;
	}
}
