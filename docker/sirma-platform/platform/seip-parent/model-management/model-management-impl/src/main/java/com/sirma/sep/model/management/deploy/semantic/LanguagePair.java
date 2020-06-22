package com.sirma.sep.model.management.deploy.semantic;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing a pair between label and its language.
 *
 * @author Mihail Radkov
 */
class LanguagePair implements Serializable {

	private static final long serialVersionUID = 2964258289311506383L;

	private final String language;
	private final String label;

	/**
	 * Constructs a pair with the provided label and its language.
	 *
	 * @param language the language of the label
	 * @param label the label
	 */
	public LanguagePair(String language, String label) {
		this.language = language;
		this.label = label;
	}

	public String getLanguage() {
		return language;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LanguagePair that = (LanguagePair) o;
		return Objects.equals(language, that.language) &&
				Objects.equals(label, that.label);
	}

	@Override
	public int hashCode() {
		return Objects.hash(language, label);
	}
}
