package com.sirma.itt.cmf.beans.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.entity.BaseEntity;

/**
 * Entity that represents a single template entity.
 * 
 * @author BBonev
 */
@Entity
@Table(name = "cmf_template")
@org.hibernate.annotations.Table(appliesTo = "cmf_template", indexes = {
		@Index(name = "idx_temp_id", columnNames = "templateid"),
		@Index(name = "idx_temp_grid", columnNames = "groupid") })
public class TemplateEntity extends BaseEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4329521904963321960L;

	/** The template id. */
	@Column(name = "templateid", length = 256, nullable = false)
	private String templateId;

	/** The group id. */
	@Column(name = "groupid", length = 256, nullable = false)
	private String groupId;

	/** The visible to. */
	@Column(name = "visibleto", length = 50, nullable = true)
	private String visibleTo;

	/** The primary. */
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	@Column(name = "primarytemplate", nullable = false)
	private Boolean primary;

	/** The public template. */
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	@Column(name = "publictemplate", nullable = false)
	private Boolean publicTemplate;

	/** The dms id. */
	@Column(name = "dmsid", length = 100, nullable = false)
	private String dmsId;

	/** The container. */
	@Column(name = "container", length = 50, nullable = true)
	private String container;

	/** The template digest. */
	@Column(name = "digest", length = 100, nullable = false)
	private String templateDigest;

	/**
	 * Getter method for groupId.
	 * 
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Setter method for groupId.
	 * 
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * Getter method for visibleTo.
	 * 
	 * @return the visibleTo
	 */
	public String getVisibleTo() {
		return visibleTo;
	}

	/**
	 * Setter method for visibleTo.
	 * 
	 * @param createdBy
	 *            the new visible to
	 */
	public void setVisibleTo(String createdBy) {
		this.visibleTo = createdBy;
	}

	/**
	 * Getter method for primary.
	 * 
	 * @return the primary
	 */
	public Boolean getPrimary() {
		return primary;
	}

	/**
	 * Setter method for primary.
	 * 
	 * @param primary
	 *            the primary to set
	 */
	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}

	/**
	 * Getter method for publicTemplate.
	 * 
	 * @return the publicTemplate
	 */
	public Boolean getPublicTemplate() {
		return publicTemplate;
	}

	/**
	 * Setter method for publicTemplate.
	 * 
	 * @param publicTemplate
	 *            the publicTemplate to set
	 */
	public void setPublicTemplate(Boolean publicTemplate) {
		this.publicTemplate = publicTemplate;
	}

	/**
	 * Getter method for dmsId.
	 * 
	 * @return the dmsId
	 */
	public String getDmsId() {
		return dmsId;
	}

	/**
	 * Setter method for dmsId.
	 * 
	 * @param dmsId
	 *            the dmsId to set
	 */
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateEntity [id=");
		builder.append(getId());
		builder.append(", templateId=");
		builder.append(templateId);
		builder.append(", groupId=");
		builder.append(groupId);
		builder.append(", visibleTo=");
		builder.append(visibleTo);
		builder.append(", primary=");
		builder.append(primary);
		builder.append(", publicTemplate=");
		builder.append(publicTemplate);
		builder.append(", dmsId=");
		builder.append(dmsId);
		builder.append(", templateDigest=");
		builder.append(templateDigest);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for templateId.
	 * 
	 * @return the templateId
	 */
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * Setter method for templateId.
	 * 
	 * @param templateId
	 *            the templateId to set
	 */
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	/**
	 * Getter method for container.
	 * 
	 * @return the container
	 */
	public String getContainer() {
		return container;
	}

	/**
	 * Setter method for container.
	 * 
	 * @param container
	 *            the container to set
	 */
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * Getter method for templateDigest.
	 * 
	 * @return the templateDigest
	 */
	public String getTemplateDigest() {
		return templateDigest;
	}

	/**
	 * Setter method for templateDigest.
	 * 
	 * @param templateDigest
	 *            the templateDigest to set
	 */
	public void setTemplateDigest(String templateDigest) {
		this.templateDigest = templateDigest;
	}

}
