package com.sirma.itt.emf.security.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.security.User;

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

	private final boolean initiatedByUser;

	/**
	 * Initializes the event object.
	 *
	 * @param authenticatedUser
	 *            currently authenticated user.
	 */
	public UserAuthenticatedEvent(User authenticatedUser) {
		this.authenticatedUser = authenticatedUser;
		this.initiatedByUser = true;
	}

	/**
	 * Initializes the event object.
	 *
	 * @param authenticatedUser
	 *            currently authenticated user.
	 * @param initiatedByUser
	 *            whether this is a user request. by default is assumed that it is
	 */
	public UserAuthenticatedEvent(User authenticatedUser, boolean initiatedByUser) {
		this.authenticatedUser = authenticatedUser;
		this.initiatedByUser = initiatedByUser;
	}

	/**
	 * Getter method for authenticatedUser.
	 *
	 * @return the authenticatedUser
	 */
	public User getAuthenticatedUser() {
		return authenticatedUser;
	}

	/**
	 * Checks if authentication is initiated by user.
	 *
	 * @return true, if is initiated by user
	 */
	public boolean isInitiatedByUser() {
		return initiatedByUser;
	}

}
