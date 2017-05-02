/**
 *
 */
package com.sirma.itt.seip.security.context;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Plugin used to provide extension of the {@link SecurityContextManager} so that the extensions could provide
 * additional means to determine if particular user is admin or not.
 *
 * @author BBonev
 */
public interface AdminResolver extends Plugin {
	/** Plugin name. */
	String NAME = "adminResolver";

	/**
	 * Checks if the given {@link SecurityContext} points to a admin or super user.
	 *
	 * @param securityContext
	 *            the security context
	 * @return true, if is admin
	 */
	boolean isAdmin(SecurityContext securityContext);
}
