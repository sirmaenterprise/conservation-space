package com.sirma.itt.emf.security.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.Documentation;

/**
 * Fired when the user is authenticated in the system.
 * 
 * @author Adrian Mitev
 */
@Documentation("Fired when the user has been authenticated within the system.<br/>"
		+ "Could be used for setting additional properties and audit logging.")
public final class UserAuthenticatedEvent implements EmfEvent {

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
	public UserAuthenticatedEvent(User authenticatedUser) {
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
