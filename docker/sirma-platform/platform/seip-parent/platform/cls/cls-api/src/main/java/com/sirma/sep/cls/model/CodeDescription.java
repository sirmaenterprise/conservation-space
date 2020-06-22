package com.sirma.sep.cls.model;

import java.util.Objects;

/**
 * Represents a {@link Code} description for specific language.
 *
 * @author Mihail Radkov
 */
public class CodeDescription {

	private String language;

	private String name;

	private String comment;

	public String getLanguage() {
		return language;
	}

	public CodeDescription setLanguage(String language) {
		this.language = language;
		return this;
	}

	public String getName() {
		return name;
	}

	public CodeDescription setName(String name) {
		this.name = name;
		return this;
	}

	public String getComment() {
		return comment;
	}

	public CodeDescription setComment(String comment) {
		this.comment = comment;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(language, name, comment);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CodeDescription)) {
			return false;
		}
		CodeDescription description = (CodeDescription) obj;
		return Objects.equals(language, description.language) && Objects.equals(name, description.name) && Objects.equals(comment,
																														  description.comment);
	}
}
