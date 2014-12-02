package com.sirma.itt.cmf.beans.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Table;

import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

import com.sirma.itt.emf.domain.model.Entity;

/**
 * The DraftEntity represent an entry in the draft table for given document and user
 */
@javax.persistence.Entity
@Table(name = "cmf_draftinstances")
@org.hibernate.annotations.Table(appliesTo = "cmf_draftinstances", fetch = FetchMode.SELECT, indexes = {
		@Index(name = "idx_draft_user_id", columnNames = "userId"),
		@Index(name = "idx_draft_instance_id", columnNames = "instanceId") })
public class DraftEntity implements Entity<DraftEntityId>, Serializable, Cloneable {

	/** The id. */
	@EmbeddedId
	private DraftEntityId id;
	/** The created. */
	@Column(name = "created", nullable = false)
	private Date created;
	/** The content. */
	@Column(name = "content", nullable = false)
	private String content;
	/** The content. */
	@Column(name = "status")
	private String status;
	/** The properties. */
	@Column(name = "properties")
	private String properties;

	/** The serialVersionUID. */
	private static final long serialVersionUID = -3417902761945706828L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DraftEntityId getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(DraftEntityId id) {
		this.id = id;
	}

	/**
	 * Gets the created.
	 *
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * Sets the date created.
	 *
	 * @param created
	 *            the new created
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * Gets the content - the text content of document.
	 *
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Sets the content.
	 *
	 * @param content
	 *            the new content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Gets the status of draft - obsolete, etc.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status
	 *            the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Getter method for properties of the draft version.
	 *
	 * @return the properties
	 */
	public String getProperties() {
		return properties;
	}

	/**
	 * Setter method for properties.
	 *
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(String properties) {
		this.properties = properties;
	}

}
