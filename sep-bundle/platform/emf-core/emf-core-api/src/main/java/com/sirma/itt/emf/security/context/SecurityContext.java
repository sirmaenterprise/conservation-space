package com.sirma.itt.emf.security.context;

import com.sirma.itt.emf.security.model.User;

/**
 * The Interface SecurityContext.
 *
 * @author BBonev
 */
public interface SecurityContext {

	/**
	 * Gets the real authentication used to for auditing.
	 *
	 * @return the real authentication
	 */
	User getAuthentication();

	/**
	 * Gets the effective authentication used for accessing the subsystems
	 * system and for current user operations.
	 *
	 * @return the effective authentication
	 */
	User getEffectiveAuthentication();

}
