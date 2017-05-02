package com.sirma.itt.emf.audit.observer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Contains helper methods when observing events.
 *
 * @author Mihail Radkov
 */
@ApplicationScoped
public class AuditObserverHelper {

	@Inject
	private SecurityContext securityContext;

	/**
	 * Fetches the currently logged user.
	 *
	 * @return the currently logged user
	 */
	public User getCurrentUser() {
		// this way the authenticated action will be logged not from the name of the user
		// for example operations triggered by the user but executed by the system
		return securityContext.getAuthenticated();
	}

}
