package com.sirma.itt.emf.properties.model;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.domain.model.PathElement;

/**
 * The Interface PropertyModel. Specifies common access to object properties
 *
 * @author BBonev
 */
public interface PropertyModel extends PathElement {

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	Map<String, Serializable> getProperties();

	/**
	 * Sets the properties.
	 *
	 * @param properties
	 *            the properties
	 */
	void setProperties(Map<String, Serializable> properties);

	/**
	 * Gets the revision of the property model.
	 *
	 * @return the revision
	 */
	public Long getRevision();
}
