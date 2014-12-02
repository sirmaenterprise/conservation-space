package com.sirma.itt.pm.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Index;

import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.VersionableEntity;
import com.sirma.itt.emf.entity.BaseStringIdEntity;

/**
 * Entity class for project instance
 * 
 * @author BBonev
 */
@Entity
@Table(name = "pmf_projectentity")
@org.hibernate.annotations.Table(appliesTo = "pmf_projectentity", indexes = {
		@Index(name = "idx_pe_dmid", columnNames = "dmid"),
		@Index(name = "idx_pe_cmid", columnNames = "cmid"),
		@Index(name = "idx_pe_defid", columnNames = "definitionid"),
		@Index(name = "idx_pe_defid_rev", columnNames = { "definitionid", "revision" }) })
public class ProjectEntity extends BaseStringIdEntity implements VersionableEntity, PathElement,
		BidirectionalMapping {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7018550957528230128L;

	@Column(name = "definitionId", length = 100, nullable = false)
	private String definitionId;
	/** The revision. */
	@Column(name = "revision", nullable = false)
	private Long revision;
	/** The container. */
	@Column(name = "container", length = 100, nullable = true)
	private String container;
	@Column(name = "dmId", length = 100, nullable = true)
	private String documentManagementId;
	@Column(name = "cmId", length = 100, nullable = true)
	private String contentManagementId;
	/** The version. */
	@Column(name = "version", nullable = true)
	@Version
	private Long version;

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
		return getDefinitionId();
	}

	@Override
	public void setIdentifier(String identifier) {
		setDefinitionId(identifier);
	}

	@Override
	public void initBidirection() {

	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	public Long getVersion() {
		return version;
	}

	@Override
	public void setVersion(Long version) {
		this.version = version;
	}

	/**
	 * Getter method for definitionId.
	 * 
	 * @return the definitionId
	 */
	public String getDefinitionId() {
		return definitionId;
	}

	/**
	 * Setter method for definitionId.
	 * 
	 * @param definitionId
	 *            the definitionId to set
	 */
	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
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
}
