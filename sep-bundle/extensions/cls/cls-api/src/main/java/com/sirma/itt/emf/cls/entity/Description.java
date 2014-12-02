package com.sirma.itt.emf.cls.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.sirma.itt.emf.entity.BaseEntity;

/**
 * POJO representing a code description. Contains all common fields contained
 * both in code lists and code values. By extending {@link BaseEntity} the ID is
 * of type Long. <br>
 * <b>NOTE</b>: The DB ID is not serialized to JSON.<br>
 * <b>NOTE</b>: Attributes with null values are not serialized to JSON.
 * 
 * @author Mihail Radkov
 * @author Nikolay Velkov
 */
@MappedSuperclass
public abstract class Description extends BaseEntity {

	/** Auto generated serial version UID. */
	private static final long serialVersionUID = -2819358498225303181L;

	/** The description language's abbreviation. */
	@Column(length = 3, name = "LANGUAGE")
	private String language;

	/** The description. */
	@Column(length = 1024, name = "DESCRIPTION")
	private String description;

	/** The comment. */
	@Column(length = 5120, name = "COMMENT")
	private String comment;

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the description language's abbreviation.
	 * 
	 * @return the description language's abbreviation
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets the language's abbreviation.
	 * 
	 * @param language
	 *            the new language's abbreviation
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Gets the comment.
	 * 
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Sets the comment.
	 * 
	 * @param comment
	 *            the new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

}
