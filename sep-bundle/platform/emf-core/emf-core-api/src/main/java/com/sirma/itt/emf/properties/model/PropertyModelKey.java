package com.sirma.itt.emf.properties.model;

import java.io.Serializable;

import com.sirma.itt.emf.domain.model.PathElement;

/**
 * The Interface PropertyModelKey.
 */
public interface PropertyModelKey extends Serializable {

	/**
	 * Gets the bean id.
	 *
	 * @return the bean id
	 */
	public String getBeanId();

	/**
	 * Gets the bean type.
	 *
	 * @return the bean type
	 */
	public Integer getBeanType();

	/**
	 * Getter method for revision.
	 *
	 * @return the revision
	 */
	public Long getRevision();

	/**
	 * Gets the path element.
	 *
	 * @return the path element
	 */
	public PathElement getPathElement();

	/**
	 * Sets the path element.
	 * 
	 * @param path
	 *            the new path element
	 */
	public void setPathElement(PathElement path);

}