package com.sirma.itt.seip.resources.security;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The UserCredentialService is responsible to manage authentication/authorization data in the underlying systems
 * providing those data.
 */
public interface UserCredentialService extends Plugin, Named {

	String NAME = "userCredentialsService";

	/**
	 * Changes user password.
	 *
	 * @param username    the username to change for the password
	 * @param oldPassword the old password for authentication.
	 * @param newPassword the new password to set
	 * @return true, if successful.
	 * @throws PasswordChangeFailException with specific error for the operation
	 */
	boolean changeUserPassword(String username, String oldPassword, String newPassword);

}
