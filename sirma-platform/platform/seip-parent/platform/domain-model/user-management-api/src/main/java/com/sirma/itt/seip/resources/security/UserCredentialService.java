package com.sirma.itt.seip.resources.security;

/**
 * The UserCredentialService is responsible to manage authentication/authorization data in the underlying systems
 * providing those data.
 */
public interface UserCredentialService {

	/**
	 * Changes user password. First validates the password by invoking
	 * {@link UserCredentialService#validatePassword(String, String)}.
	 *
	 * @param username
	 *            the username to change for the password
	 * @param oldPassword
	 *            the old password for authentication.
	 * @param newPassword
	 *            the new password to set
	 * @return true, if successful.
	 * @throws PasswordChangeFailException
	 *             with specific error for the operation
	 */
	boolean changeUserPassword(String username, String oldPassword, String newPassword);

	/**
	 * Validates the new password.
	 *
	 * @param oldPassword
	 *            the old password
	 * @param newPassword
	 *            the new password
	 * @throws PasswordChangeFailException
	 *             if the new password does not pass the validation
	 */
	void validatePassword(String oldPassword, String newPassword);

}
