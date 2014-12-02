package com.sirma.itt.cmf.beans.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.entity.BaseStringIdEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * Entity class that represents a Section in the database.
 * 
 * @author BBonev
 */
@Entity
@Table(name = "cmf_sectionEntity")
@org.hibernate.annotations.Table(appliesTo = "cmf_sectionEntity", indexes = {
		@Index(name = "idx_sece_dmid", columnNames = "dmid"),
		@Index(name = "idx_sece_ownref", columnNames = { "owninginstanceid", "owninginstancetype" }),
		@Index(name = "idx_sece_sId_rev", columnNames = { "sectionId", "revision" }) })
@AssociationOverrides(value = { @AssociationOverride(name = "owningInstance.sourceType", joinColumns = @JoinColumn(name = "owninginstancetype", nullable = true)) })
public class SectionEntity extends BaseStringIdEntity implements BidirectionalMapping, PathElement,
		Purposable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3803806218817351819L;

	/** The section id. */
	@Column(name = "sectionId", length = 100, nullable = true)
	private String sectionId;

	/** The document management id. */
	@Column(name = "dmid", length = 100, nullable = true)
	private String documentManagementId;

	/** The case revision. */
	@Column(name = "revision", nullable = false)
	private Long revision;

	/** The container. */
	@Column(name = "container", length = 100, nullable = true)
	private String container;

	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "owninginstanceid", length = 50, nullable = true)) })
	private LinkSourceId owningInstance;

	@Column(name = "defpath", length = 256, nullable = false)
	private String definitionPath;

	/** The purpose. */
	@Column(name = "purpose", length = 50, nullable = true)
	private String purpose;

	/** The index. */
	@Column(name = "listindex", insertable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long index;

	/** The standalone. */
	@Column(name = "standalone", nullable = false)
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	private Boolean standalone = Boolean.FALSE;

	/**
	 * Getter method for sectionId.
	 *
	 * @return the sectionId
	 */
	public String getSectionId() {
		return sectionId;
	}

	/**
	 * Setter method for sectionId.
	 *
	 * @param sectionId
	 *            the sectionId to set
	 */
	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	@Override
	public void initBidirection() {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((sectionId == null) ? 0 : sectionId.hashCode());
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
		if (!(obj instanceof SectionEntity)) {
			return false;
		}
		SectionEntity other = (SectionEntity) obj;
		if (sectionId == null) {
			if (other.sectionId != null) {
				return false;
			}
		} else if (!sectionId.equals(other.sectionId)) {
			return false;
		}
		return true;
	}


	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getDefinitionPath();
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
		return getSectionId();
	}

	@Override
	public void setIdentifier(String identifier) {
		setSectionId(identifier);
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
	 * @param owningInstance
	 *            the owningInstance to set
	 */
	public void setOwningInstance(LinkSourceId owningInstance) {
		this.owningInstance = owningInstance;
	}

	/**
	 * Getter method for definitionPath.
	 * 
	 * @return the definitionPath
	 */
	public String getDefinitionPath() {
		return definitionPath;
	}

	/**
	 * Setter method for definitionPath.
	 * 
	 * @param definitionPath
	 *            the definitionPath to set
	 */
	public void setDefinitionPath(String definitionPath) {
		this.definitionPath = definitionPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SectionEntity [sectionId=");
		builder.append(sectionId);
		builder.append(", documentManagementId=");
		builder.append(documentManagementId);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", container=");
		builder.append(container);
		builder.append(", definitionPath=");
		builder.append(definitionPath);
		builder.append(", owningInstance=");
		builder.append(owningInstance);
		builder.append(", purpose=");
		builder.append(purpose);
		builder.append(", standalone=");
		builder.append(standalone);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for purpose.
	 * 
	 * @return the purpose
	 */
	@Override
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter method for purpose.
	 * 
	 * @param purpose
	 *            the purpose to set
	 */
	@Override
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	/**
	 * Getter method for index.
	 *
	 * @return the index
	 */
	public Long getIndex() {
		return index;
	}

	/**
	 * Setter method for index.
	 *
	 * @param index the index to set
	 */
	public void setIndex(Long index) {
		this.index = index;
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

}
