package com.sirma.itt.emf.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Index;

import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.VersionableEntity;

/**
 * Entity class for common instance objects
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_commonEntity")
@org.hibernate.annotations.Table(appliesTo = "emf_commonEntity", indexes = {
		@Index(name = "idx_comE_definitionId", columnNames = "definitionId"),
		@Index(name = "idx_comE_path", columnNames = "path"),
		@Index(name = "idx_comE_path_rev", columnNames = { "path", "revision" }) })
public class CommonEntity extends BaseStringIdEntity implements PathElement, VersionableEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2104398620348704165L;

	/** The definition id. */
	@Column(name = "definitionId", length = 100, nullable = false)
	private String definitionId;

	/** The revision. */
	@Column(name = "revision", nullable = false)
	private Long revision;

	/** The path. */
	@Column(name = "path", length = 1000)
	private String path;

	/** The version. */
	@Version
	private Long version;

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
	 * Getter method for path.
	 *
	 * @return the path
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * Setter method for path.
	 *
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

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
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommonEntity [definitionId=");
		builder.append(definitionId);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", path=");
		builder.append(path);
		builder.append(", id=");
		builder.append(getId());
		builder.append("]");
		return builder.toString();
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
	 * @param version the version to set
	 */
	@Override
	public void setVersion(Long version) {
		this.version = version;
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
		return getDefinitionId();
	}

	@Override
	public void setIdentifier(String identifier) {
		setDefinitionId(identifier);
	}

}
