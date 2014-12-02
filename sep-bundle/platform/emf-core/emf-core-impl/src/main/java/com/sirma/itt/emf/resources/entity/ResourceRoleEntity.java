package com.sirma.itt.emf.resources.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.sirma.itt.emf.entity.BaseEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * Entity that represents a join between resource and project entity.
 * 
 * @author BBonev
 */
@Entity
@Table(name = "emf_resourcerole")
@org.hibernate.annotations.Table(appliesTo = "emf_resourcerole", indexes = {
		@Index(name = "idx_prr_trr", columnNames = { "targetrolereferencerid",
				"targetrolereferencetype" }),
		@Index(name = "idx_prr_r_trr", columnNames = { "role", "targetrolereferencerid",
				"targetrolereferencetype" }) })
@AssociationOverrides(value = { @AssociationOverride(name = "targetRoleReference.sourceType", joinColumns = @JoinColumn(name = "targetrolereferencetype", nullable = false)) })
public class ResourceRoleEntity extends BaseEntity {

	private static final long serialVersionUID = -4024290114080051359L;

	/** The role. */
	@Column(name = "role", length = 100, nullable = true)
	private String role;

	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "targetrolereferencerid", length = 100, nullable = false)) })
	private LinkSourceId targetRoleReference;

	/** The resource. */
	@Column(name = "resource_id", length = 100, nullable = false)
	private String resourceId;

	/**
	 * Getter method for role.
	 * 
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Setter method for role.
	 * 
	 * @param role
	 *            the role to set
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Getter method for resourceId.
	 * 
	 * @return the resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * Setter method for resourceId.
	 * 
	 * @param resourceId
	 *            the resourceId to set
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourceRoleEntity [id=");
		builder.append(getId());
		builder.append(", role=");
		builder.append(role);
		builder.append(", resourceId=");
		builder.append(resourceId);
		builder.append(", targetRoleReference=");
		builder.append(getTargetRoleReference());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for targetRoleReference.
	 * 
	 * @return the targetRoleReference
	 */
	public LinkSourceId getTargetRoleReference() {
		return targetRoleReference;
	}

	/**
	 * Setter method for targetRoleReference.
	 * 
	 * @param targetRoleReference
	 *            the targetRoleReference to set
	 */
	public void setTargetRoleReference(LinkSourceId targetRoleReference) {
		this.targetRoleReference = targetRoleReference;
	}
}
