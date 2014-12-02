package com.sirma.itt.cmf.beans.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.entity.BaseStringIdEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * Class that represents a document instance in system in relation to the DB.
 *
 * @author BBonev
 */
@Entity
@Table(name = "cmf_documentEntity")
@org.hibernate.annotations.Table(appliesTo = "cmf_documentEntity", fetch = FetchMode.SELECT, indexes = {
		@Index(name = "idx_de_revision_path", columnNames = { "revision", "parentPath" }),
		@Index(name = "idx_de_dmsid", columnNames = "documentDmsId"),
		@Index(name = "idx_de_ownref", columnNames = { "owningreferenceid", "owningreferencetype" }) })
@AssociationOverrides(value = { @AssociationOverride(name = "owningReference.sourceType", joinColumns = @JoinColumn(name = "owningreferencetype", nullable = true)) })
public class DocumentEntity extends BaseStringIdEntity implements BidirectionalMapping,
		PathElement,
		Cloneable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 5388062335686293781L;

	/** The document id. */
	@Column(name = "documentId", length = 100, nullable = true)
	private String documentId;

	@Column(name = "documentRefId", length = 100, nullable = false)
	private String documentRefId;

	/** The document dms id. */
	@Column(name = "documentDmsId", length = 100, nullable = true)
	private String documentDmsId;

	/** The purpose. */
	@Column(name = "purpose", length = 50, nullable = true)
	private String purpose;

	/** The structured. */
	@Column(name = "structured", nullable = false)
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	private Boolean structured = Boolean.FALSE;

	/** The definition revision. */
	@Column(name = "revision", nullable = true)
	private Long revision;

	/** The parent path in case of moved/copied document */
	@Column(name = "parentPath", length = 200, nullable = true)
	private String parentPath;

	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "owningreferenceid", length = 50, nullable = true)) })
	private LinkSourceId owningReference;

	/** The standalone. */
	@Column(name = "standalone", nullable = false)
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	private Boolean standalone = Boolean.FALSE;

	/** The container. */
	@Column(name = "container", length = 100, nullable = true)
	private String container;

	/**
	 * Getter method for documentId.
	 *
	 * @return the documentId
	 */
	public String getDocumentId() {
		return documentId;
	}

	/**
	 * Setter method for documentId.
	 *
	 * @param documentId
	 *            the documentId to set
	 */
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((documentId == null) ? 0 : documentId.hashCode());
		result = (prime * result) + ((documentRefId == null) ? 0 : documentRefId.hashCode());
		result = (prime * result) + ((documentDmsId == null) ? 0 : documentDmsId.hashCode());
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
		if (!(obj instanceof DocumentEntity)) {
			return false;
		}
		DocumentEntity other = (DocumentEntity) obj;
		if (documentId == null) {
			if (other.documentId != null) {
				return false;
			}
		} else if (!documentId.equals(other.documentId)) {
			return false;
		}
		if (documentRefId == null) {
			if (other.documentRefId != null) {
				return false;
			}
		} else if (!documentRefId.equals(other.documentRefId)) {
			return false;
		}
		if (documentDmsId == null) {
			if (other.documentDmsId != null) {
				return false;
			}
		} else if (!documentDmsId.equals(other.documentDmsId)) {
			return false;
		}
		return true;
	}

	@Override
	public void initBidirection() {
		// nothing to do here
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return parentPath;
	}

	/**
	 * Getter method for documentRefId.
	 *
	 * @return the documentRefId
	 */
	public String getDocumentRefId() {
		return documentRefId;
	}

	/**
	 * Setter method for documentRefId.
	 *
	 * @param documentRefId
	 *            the documentRefId to set
	 */
	public void setDocumentRefId(String documentRefId) {
		this.documentRefId = documentRefId;
	}

	/**
	 * Getter method for documentDmsId.
	 *
	 * @return the documentDmsId
	 */
	public String getDocumentDmsId() {
		return documentDmsId;
	}

	/**
	 * Setter method for documentDmsId.
	 *
	 * @param documentDmsId
	 *            the documentDmsId to set
	 */
	public void setDocumentDmsId(String documentDmsId) {
		this.documentDmsId = documentDmsId;
	}

	/**
	 * Getter method for purpose.
	 *
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter method for purpose.
	 *
	 * @param purpose
	 *            the purpose to set
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	/**
	 * Getter method for structured.
	 *
	 * @return the structured
	 */
	public Boolean getStructured() {
		return structured;
	}

	/**
	 * Setter method for structured.
	 *
	 * @param structured
	 *            the structured to set
	 */
	public void setStructured(Boolean structured) {
		if (structured == null) {
			this.structured = Boolean.FALSE;
		} else {
			this.structured = structured;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DocumentEntity [documentId=");
		builder.append(documentId);
		builder.append(", documentRefId=");
		builder.append(documentRefId);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", parentPath=");
		builder.append(parentPath);
		builder.append(", documentDmsId=");
		builder.append(documentDmsId);
		builder.append(", purpose=");
		builder.append(purpose);
		builder.append(", structured=");
		builder.append(structured);
		builder.append(", standalone=");
		builder.append(standalone);
		builder.append(", super=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
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
	 * Getter method for parentPath.
	 *
	 * @return the parentPath
	 */
	public String getParentPath() {
		return parentPath;
	}

	/**
	 * Setter method for parentPath.
	 *
	 * @param parentPath
	 *            the parentPath to set
	 */
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	@Override
	public DocumentEntity clone() {
		DocumentEntity entity = new DocumentEntity();
		entity.documentDmsId = documentDmsId;
		entity.documentId = documentId;
		entity.documentRefId = documentRefId;
		entity.parentPath = parentPath;
		entity.purpose = purpose;
		entity.revision = revision;
		entity.structured = structured;
		entity.standalone = standalone;
		entity.container = container;
		entity.setId(entity.getId());
		return entity;
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
		return getDocumentId();
	}

	@Override
	public void setIdentifier(String identifier) {
		setDocumentId(identifier);
	}

	/**
	 * Gets the owning reference.
	 * 
	 * @return the owning reference
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
	 * Getter method for standalone.
	 * 
	 * @return the standalone
	 */
	public Boolean getStandalone() {
		return standalone;
	}

	/**
	 * Setter method for standalone.
	 * 
	 * @param standalone
	 *            the standalone to set
	 */
	public void setStandalone(Boolean standalone) {
		if (standalone == null) {
			this.standalone = Boolean.FALSE;
		} else {
			this.standalone = standalone;
		}
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
}
