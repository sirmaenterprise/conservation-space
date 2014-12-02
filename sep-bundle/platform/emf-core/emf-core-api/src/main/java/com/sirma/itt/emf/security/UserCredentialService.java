package com.sirma.itt.emf.security;

/**
 * The UserCredentialService is responsible to manage authentication/authorization data in the
 * underlying systems providing those data.
 */
public interface UserCredentialService {

	/**
	 * Change user password
	 *
	 * @param username
	 *            the username to change for the password
	 * @param oldPassword
	 *            the old password for authentication.
	 * @param newPassword
	 *            the new password to set
	 * @return true, if successful.
	 * @throws Exception
	 *             with specific error for the operation
	 */
	boolean changeUserPassword(String username, String oldPassword, String newPassword)
			throws Exception;

}
