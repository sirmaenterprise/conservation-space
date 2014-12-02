package com.sirma.itt.cmf.beans.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.VersionableEntity;
import com.sirma.itt.emf.entity.BaseStringIdEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * The Class WorkflowInstanceContextEntity.
 *
 * @author BBonev
 */
@Entity
@Table(name = "cmf_workflowInstance")
@org.hibernate.annotations.Table(appliesTo = "cmf_workflowInstance", indexes = {
		@Index(name = "idx_wic_workFlowInsId", columnNames = "workflowInstanceId"),
		@Index(name = "idx_wic_ownref", columnNames = { "owningreferencetype", "owningreferenceeid" }) })
@AssociationOverrides(value = { @AssociationOverride(name = "owningReference.sourceType", joinColumns = @JoinColumn(name = "owningreferencetype", nullable = true)) })
public class WorkflowInstanceContextEntity extends BaseStringIdEntity implements PathElement,
		VersionableEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7855299628594281862L;

	/** The document management id. */
	@Column(name = "dmId", length = 100, nullable = true)
	private String documentManagementId;

	/** The case definition id. */
	@Column(name = "workflowDefId", length = 100, nullable = false)
	private String workflowDefinitionId;

	/** The case revision. */
	@Column(name = "revision", nullable = false)
	private Long revision;

	/** The workflow instance id. */
	@Column(name = "workflowInstanceId", length = 100, nullable = false)
	private String workflowInstanceId;

	/** The container. */
	@Column(name = "container", length = 100, nullable = true)
	private String container;
	/**
	 * Versionable field for optimistic locking <br/>
	 */
	@Column(name = "version", nullable = true)
	@Version
	private Long version;

	/** The active. */
	@Column(name = "active", nullable = true)
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	private Boolean active;

	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "owningreferenceeid", length = 50, nullable = true)) })
	private LinkSourceId owningReference;

	/** The content management id. The internal generated id or activity stripped id. */
	@Column(name = "cmId", length = 100, nullable = true)
	private String contentManagementId;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getWorkflowDefinitionId();
	}

	/**
	 * Getter method for documentManagementId.
	 *
	 * @return the documentManagementId
	 */
	public String getDocumentManagementId() {
		return documentManagementId;
	}

	/**
	 * Setter method for documentManagementId.
	 *
	 * @param documentManagementId
	 *            the documentManagementId to set
	 */
	public void setDocumentManagementId(String documentManagementId) {
		this.documentManagementId = documentManagementId;
	}

	/**
	 * Getter method for workflowDefinitionId.
	 *
	 * @return the workflowDefinitionId
	 */
	public String getWorkflowDefinitionId() {
		return workflowDefinitionId;
	}

	/**
	 * Setter method for workflowDefinitionId.
	 *
	 * @param workflowDefinitionId
	 *            the workflowDefinitionId to set
	 */
	public void setWorkflowDefinitionId(String workflowDefinitionId) {
		this.workflowDefinitionId = workflowDefinitionId;
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
	 * Getter method for version.
	 *
	 * @return the version
	 */
	@Override
	public Long getVersion() {
		return version;
	}

	/**
	 * Setter method for version.
	 *
	 * @param version
	 *            the version to set
	 */
	@Override
	public void setVersion(Long version) {
		this.version = version;
	}

	/**
	 * Getter method for workflowInstanceId.
	 *
	 * @return the workflowInstanceId
	 */
	public String getWorkflowInstanceId() {
		return workflowInstanceId;
	}

	/**
	 * Setter method for workflowInstanceId.
	 *
	 * @param workflowInstanceId
	 *            the workflowInstanceId to set
	 */
	public void setWorkflowInstanceId(String workflowInstanceId) {
		this.workflowInstanceId = workflowInstanceId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WorkflowInstanceContextEntity [documentManagementId=");
		builder.append(documentManagementId);
		builder.append(", workflowDefinitionId=");
		builder.append(workflowDefinitionId);
		builder.append(", workflowInstanceId=");
		builder.append(workflowInstanceId);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", container=");
		builder.append(container);
		builder.append(", version=");
		builder.append(version);
		builder.append(", owningReference=");
		builder.append(getOwningReference());
		builder.append(", super=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		return result * prime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof WorkflowInstanceContextEntity)) {
			return false;
		}
		return true;
	}

	/**
	 * Getter method for active.
	 *
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * Setter method for active.
	 *
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	@Override
	public String getIdentifier() {
		return getWorkflowDefinitionId();
	}

	@Override
	public void setIdentifier(String identifier) {
		setWorkflowDefinitionId(identifier);
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
	 * @param owningReference the owningReference to set
	 */
	public void setOwningReference(LinkSourceId owningReference) {
		this.owningReference = owningReference;
	}

	/**
	 * Gets the content management id.
	 * 
	 * @return the content management id
	 */
	public String getContentManagementId() {
		return contentManagementId;
	}

	/**
	 * Sets the content management id.
	 * 
	 * @param contentManagementId
	 *            the new content management id
	 */
	public void setContentManagementId(String contentManagementId) {
		this.contentManagementId = contentManagementId;
	}
}
