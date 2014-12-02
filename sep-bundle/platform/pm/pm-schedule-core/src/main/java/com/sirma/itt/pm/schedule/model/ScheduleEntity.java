package com.sirma.itt.pm.schedule.model;

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
 * Entity class for representing the schedule instance in DB
 *
 * @author BBonev
 */
@Entity
@Table(name = "pmfs_scheduleentity")
@org.hibernate.annotations.Table(appliesTo = "pmfs_scheduleentity", indexes = {
		@Index(name = "idx_sche_or", columnNames = { "owningReferenceId", "owningReferenceType" }),
		@Index(name = "idx_sche_iden", columnNames = "identifier") })
@AssociationOverrides(value = { @AssociationOverride(name = "owningReference.sourceType", joinColumns = @JoinColumn(name = "owningReferenceType", nullable = false)) })
public class ScheduleEntity extends BaseEntity {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8827789621833628689L;
	/** The definition identifier of the target instance that need to be created. */
	@Column(name = "identifier", length = 100, nullable = true)
	private String identifier;
	/** The revision of the project definition. */
	@Column(name = "revision", nullable = false)
	private Long revision;
	/** The container. */
	@Column(name = "container", length = 100, nullable = false)
	private String container;
	/** The owning reference. */
	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "owningReferenceId", length = 50, nullable = false)) })
	private LinkSourceId owningReference;

	/**
	 * Getter method for identifier.
	 *
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Setter method for identifier.
	 *
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Getter method for revision.
	 *
	 * @return the revision
	 */
	public Long getRevision() {
		return revision;
	}

	/**
	 * Setter method for revision.
	 *
	 * @param revision
	 *            the revision to set
	 */
	public void setRevision(Long revision) {
		this.revision = revision;
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
	 * Getter method for owningReference.
	 *
	 * @return the owningReference
	 */
	public LinkSourceId getOwningReference() {
		return owningReference;
	}

	/**
	 * Setter method for owningReference.
	 *
	 * @param owningReference
	 *            the owningReference to set
	 */
	public void setOwningReference(LinkSourceId owningReference) {
		this.owningReference = owningReference;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScheduleEntity [id=");
		builder.append(getId());
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", container=");
		builder.append(container);
		builder.append(", owningReference=");
		builder.append(owningReference);
		builder.append("]");
		return builder.toString();
	}

}
