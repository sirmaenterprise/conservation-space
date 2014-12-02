package com.sirma.itt.emf.security.model;

import java.io.Serializable;

/**
 * The RoleIdentifier is base class for any role registered in the system
 */
public interface RoleIdentifier extends Serializable {

	/**
	 * Gets the actual role identifier.
	 *
	 * @return the identifier
	 */
	String getIdentifier();

	/**
	 * Get the priority for this role. Higher value means higher role as administrator
	 *
	 * @return some number
	 */
	int getGlobalPriority();

}
