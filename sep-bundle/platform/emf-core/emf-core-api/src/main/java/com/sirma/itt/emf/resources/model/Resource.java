package com.sirma.itt.emf.resources.model;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceType;

/**
 * Base interface for resource/user in the application.
 *
 * @author bbanchev
 */
public interface Resource extends Instance {

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName();

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	ResourceType getType();
}