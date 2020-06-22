package com.sirma.itt.emf.security.event;

import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.security.User;

/**
 * Fired before user logout from the system.
 *
 * @author smustafov
 */
public class BeginLogoutEvent implements EmfEvent {

	private User authenticatedUser;

	/**
	 * Initializes the user in the event.
	 *
	 * @param authenticatedUser
	 *            currently authenticated user
	 */
	public BeginLogoutEvent(User authenticatedUser) {
		this.authenticatedUser = authenticatedUser;
	}

	/**
	 * Getter method for authenticatedUser.
	 *
	 * @return the authenticatedUser
	 */
	public User getAuthenticatedUser() {
		return authenticatedUser;
	}

}
