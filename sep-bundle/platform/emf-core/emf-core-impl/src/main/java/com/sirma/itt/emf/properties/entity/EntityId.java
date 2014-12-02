package com.sirma.itt.emf.properties.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.properties.model.PropertyModelKey;
import com.sirma.itt.emf.util.PathHelper;

/**
 * The Class EntityId.
 * 
 * @author BBonev
 */
@Embeddable
public class EntityId implements PropertyModelKey {

	private static final long serialVersionUID = 1873640466015341931L;

	/** The bean id. */
	@Column(name = "bean_id", length = 100, nullable = true)
	private String beanId;

	/**
	 * The bean type. <br>
	 * 
	 * @see com.sirma.itt.emf.entity.BaseEntity#getEntityType()
	 */
	@Column(name = "bean_type", nullable = false)
	private Integer beanType;

	/** The revision. */
	@Transient
	private Long revision;

	/** The path. */
	@Transient
	private String path;

	/** The path element. */
	@Transient
	private PathElement pathElement;

	/**
	 * Getter method for pathElement.
	 * 
	 * @return the pathElement
	 */
	@Override
	public PathElement getPathElement() {
		return pathElement;
	}

	/**
	 * Setter method for pathElement.
	 * 
	 * @param pathElement
	 *            the pathElement to set
	 */
	@Override
	public void setPathElement(PathElement pathElement) {
		this.pathElement = pathElement;
		path = PathHelper.getPath(pathElement);
	}

	/**
	 * Instantiates a new entity id.
	 */
	public EntityId() {
		// default constructor
	}

	/**
	 * Instantiates a new entity id.
	 * 
	 * @param beanId
	 *            the bean id
	 * @param beanType
	 *            the bean type
	 */
	public EntityId(String beanId, Integer beanType) {
		this.beanId = beanId;
		this.beanType = beanType;
	}

	/**
	 * Instantiates a new entity id.
	 * 
	 * @param beanId
	 *            the bean id
	 * @param beanType
	 *            the bean type
	 * @param revision
	 *            the revision
	 */
	public EntityId(String beanId, Integer beanType, Long revision) {
		this.beanId = beanId;
		this.beanType = beanType;
		this.revision = revision;
	}

	/**
	 * Instantiates a new entity id.
	 * 
	 * @param beanId
	 *            the bean id
	 * @param beanType
	 *            the bean type
	 * @param revision
	 *            the revision
	 * @param path
	 *            the path
	 */
	public EntityId(String beanId, Integer beanType, Long revision, PathElement path) {
		this.beanId = beanId;
		this.beanType = beanType;
		this.revision = revision;
		pathElement = path;
		this.path = PathHelper.getPath(path);
	}

	/**
	 * Getter method for beanId.
	 * 
	 * @return the beanId
	 */
	@Override
	public String getBeanId() {
		return beanId;
	}

	/**
	 * Setter method for beanId.
	 * 
	 * @param beanId
	 *            the beanId to set
	 */
	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}

	/**
	 * Getter method for beanType.
	 * 
	 * @return the beanType
	 */
	@Override
	public Integer getBeanType() {
		return beanType;
	}

	/**
	 * Setter method for beanType.
	 * 
	 * @param beanType
	 *            the beanType to set
	 */
	public void setBeanType(Integer beanType) {
		this.beanType = beanType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (((beanId == null) ? 0 : beanId.hashCode()) * prime);
		result = (prime * result) + ((beanType == null) ? 0 : beanType.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EntityId other = (EntityId) obj;
		if (beanId == null) {
			if (other.beanId != null) {
				return false;
			}
		} else if (!beanId.equals(other.beanId)) {
			return false;
		}
		if (beanType == null) {
			if (other.beanType != null) {
				return false;
			}
		} else if (!beanType.equals(other.beanType)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "EntityId [beanId=" + beanId + ", beanType=" + beanType + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

}
