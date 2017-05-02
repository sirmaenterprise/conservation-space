package com.sirma.itt.seip.domain;

import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * The Interface Identity.
 *
 * @author BBonev
 */
public interface Identity {

	/**
	 * Gets the definition identifier. If the object is an {@link InstanceReference} this will return the instance id
	 * instead.
	 *
	 * @return the identifier
	 */
	String getIdentifier();

	/**
	 * Sets the definition identifier.
	 *
	 * @param identifier
	 *            the new identifier
	 */
	void setIdentifier(String identifier);

}