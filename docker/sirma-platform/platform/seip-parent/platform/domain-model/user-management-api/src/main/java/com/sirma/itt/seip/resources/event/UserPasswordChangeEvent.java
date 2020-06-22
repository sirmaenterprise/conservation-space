package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.OperationEvent;

/**
 * Event for changing a user's password.
 *
 * @author Mihail Radkov
 */
@Documentation("Event fired after a user has changed its password.")
public class UserPasswordChangeEvent implements OperationEvent {

	private static final String OPERATION_ID = "changePassword";
	private final String username;
	private final String newPassword;

	/**
	 * Constructs new event for the given user and his new password
	 *
	 * @param username the affected user name
	 * @param newPassword the new user's password
	 */
	public UserPasswordChangeEvent(String username, String newPassword) {
		this.username = username;
		this.newPassword = newPassword;
	}

	public String getUsername() {
		return username;
	}

	public String getNewPassword() {
		return newPassword;
	}

	@Override
	public String getOperationId() {
		return OPERATION_ID;
	}

}
