package com.sirma.itt.emf.security.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.security.User;

/**
 * Fired when the user is logout from the system.
 *
 * @author Adrian Mitev
 */
@Documentation("Fired when the user has been logged out (and just before session invalidation) within the system.<br/>"
		+ "Useful for cases like audit logging.")
public final class UserLogoutEvent implements EmfEvent {

	/**
	 * Authenticated user.
	 */
	private final User authenticatedUser;

	/**
	 * Initializes the event object.
	 *
	 * @param authenticatedUser
	 *            currently authenticated user.
	 */
	public UserLogoutEvent(User authenticatedUser) {
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
