package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;

import com.sirma.itt.seip.domain.PathElement;

/**
 * The Interface PropertyModelKey.
 */
public interface PropertyModelKey extends Serializable {

	/**
	 * Gets the bean id.
	 *
	 * @return the bean id
	 */
	String getBeanId();

	/**
	 * Gets the bean type.
	 *
	 * @return the bean type
	 */
	Integer getBeanType();

	/**
	 * Gets the path element.
	 *
	 * @return the path element
	 */
	PathElement getPathElement();

	/**
	 * Sets the path element.
	 *
	 * @param path
	 *            the new path element
	 */
	void setPathElement(PathElement path);

}