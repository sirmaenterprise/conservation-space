package com.sirma.itt.emf.resources.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.entity.BaseStringIdEntity;

/**
 * Object that represents a single project resource in DB.
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_resource")
@org.hibernate.annotations.Table(appliesTo = "emf_resource", indexes = { @Index(name = "idx_p_res_name", columnNames = "identifier") })
public class ResourceEntity extends BaseStringIdEntity implements Identity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7820910159110494389L;

	/** The identifier. */
	@Column(name = "identifier", length = 100, nullable = false)
	private String identifier;

	/** The display name. */
	@Column(name = "displayName", length = 200, nullable = true)
	private String displayName;

	@Column(name = "type", nullable = true)
	private Integer type;

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name.
	 *
	 * @param displayName
	 *            the new display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the type
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourceEntity [identifier=");
		builder.append(identifier);
		builder.append(", displayName=");
		builder.append(displayName);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}

}
