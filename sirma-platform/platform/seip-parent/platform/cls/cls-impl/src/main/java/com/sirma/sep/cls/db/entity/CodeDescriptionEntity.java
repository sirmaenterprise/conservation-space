package com.sirma.sep.cls.db.entity;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

/**
 * Abstract database entity for mapping the properties of {@link com.sirma.sep.cls.model.CodeDescription}.
 *
 * @author Mihail Radkov
 */
@MappedSuperclass
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
public abstract class CodeDescriptionEntity extends BaseEntity {

	@Column(length = 3, name = "LANGUAGE")
	private String language;

	@Column(length = 1024, name = "DESCRIPTION")
	private String description;

	@Column(length = 5120, name = "COMMENT")
	private String comment;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), language, description, comment);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CodeDescriptionEntity)) {
			return false;
		}
		CodeDescriptionEntity descriptionEntity = (CodeDescriptionEntity) obj;
		return super.equals(obj) && Objects.equals(language, descriptionEntity.language) && Objects
				.equals(description, descriptionEntity.description) && Objects.equals(comment, descriptionEntity.comment);
	}
}
