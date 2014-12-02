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

import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.VersionableEntity;
import com.sirma.itt.emf.entity.BaseStringIdEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * Entity class that represents a Case in the database
 * 
 * @author BBonev
 */
@Entity
@Table(name = "cmf_caseEntity")
@org.hibernate.annotations.Table(appliesTo = "cmf_caseEntity", indexes = {
		@Index(name = "idx_ce_dmId", columnNames = "dmId"),
		@Index(name = "idx_ce_cmId", columnNames = "cmId"),
		@Index(name = "idx_ce_cdId", columnNames = "cdId"),
		@Index(name = "idx_ce_cdId_rev", columnNames = { "cdId", "caseRevision" }),
		@Index(name = "idx_ce_ownref", columnNames = { "owninginstanceid", "owninginstancetype" }) })
@AssociationOverrides(value = { @AssociationOverride(name = "owningInstance.sourceType", joinColumns = @JoinColumn(name = "owninginstancetype", nullable = true)) })
public class CaseEntity extends BaseStringIdEntity implements PathElement, VersionableEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7559770452641527064L;

	/** The document management id. */
	@Column(name = "dmId", length = 100, nullable = true)
	private String documentManagementId;

	/** The content management id. */
	@Column(name = "cmId", length = 100, nullable = true)
	private String contentManagementId;

	/** The case definition id. */
	@Column(name = "cdId", length = 100, nullable = false)
	private String caseDefinitionId;

	/** The case revision. */
	@Column(name = "caseRevision", nullable = false)
	private Long caseRevision;

	/** The container. */
	@Column(name = "container", length = 100, nullable = true)
	private String container;

	/**
	 * Versionable field for optimistic locking <br/>
	 */
	@Column(name = "version", nullable = true)
	@Version
	private Long version;

	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "owninginstanceid", length = 50, nullable = true)) })
	private LinkSourceId owningInstance;

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
	 * Getter method for contentManagementId.
	 *
	 * @return the contentManagementId
	 */
	public String getContentManagementId() {
		return contentManagementId;
	}

	/**
	 * Setter method for contentManagementId.
	 *
	 * @param contentManagementId
	 *            the contentManagementId to set
	 */
	public void setContentManagementId(String contentManagementId) {
		this.contentManagementId = contentManagementId;
	}

	/**
	 * Getter method for caseDefinitionId.
	 *
	 * @return the caseDefinitionId
	 */
	public String getCaseDefinitionId() {
		return caseDefinitionId;
	}

	/**
	 * Setter method for caseDefinitionId.
	 *
	 * @param caseDefinitionId
	 *            the caseDefinitionId to set
	 */
	public void setCaseDefinitionId(String caseDefinitionId) {
		this.caseDefinitionId = caseDefinitionId;
	}

	/**
	 * Getter method for caseRevision.
	 *
	 * @return the caseRevision
	 */
	public Long getCaseRevision() {
		return caseRevision;
	}

	/**
	 * Setter method for caseRevision.
	 *
	 * @param caseRevision
	 *            the caseRevision to set
	 */
	public void setCaseRevision(Long caseRevision) {
		this.caseRevision = caseRevision;
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
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((caseDefinitionId == null) ? 0 : caseDefinitionId.hashCode());
		result = (prime * result) + ((container == null) ? 0 : container.hashCode());
		return result;
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
		if (!(obj instanceof CaseEntity)) {
			return false;
		}
		CaseEntity other = (CaseEntity) obj;
		if (caseDefinitionId == null) {
			if (other.caseDefinitionId != null) {
				return false;
			}
		} else if (!caseDefinitionId.equals(other.caseDefinitionId)) {
			return false;
		}
		if (container == null) {
			if (other.container != null) {
				return false;
			}
		} else if (!container.equals(other.container)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CaseEntity [caseDefinitionId=");
		builder.append(caseDefinitionId);
		builder.append(", container=");
		builder.append(container);
		builder.append(", caseRevision=");
		builder.append(caseRevision);
		builder.append(", documentManagementId=");
		builder.append(documentManagementId);
		builder.append(", contentManagementId=");
		builder.append(contentManagementId);
		builder.append(", version=");
		builder.append(version);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getCaseDefinitionId();
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
		return getCaseDefinitionId();
	}

	@Override
	public void setIdentifier(String identifier) {
		setCaseDefinitionId(identifier);
	}

	/**
	 * Getter method for owningInstance.
	 *
	 * @return the owningInstance
	 */
	public LinkSourceId getOwningInstance() {
		return owningInstance;
	}

	/**
	 * Setter method for owningInstance.
	 *
	 * @param owningInstance the owningInstance to set
	 */
	public void setOwningInstance(LinkSourceId owningInstance) {
		this.owningInstance = owningInstance;
	}

}
