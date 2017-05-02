package com.sirma.itt.seip.resources.security;

/**
 * Carries of security token management. Used to request a new and check the validity of an existing security token.
 *
 * @author Adrian Mitev
 */
public interface SecurityTokenService {

	/**
	 * Requests a security token from the Identity Provider.
	 *
	 * @param username
	 *            authentication username.
	 * @param password
	 *            authentication password.
	 * @return the requested token.
	 * @throws Exception
	 *             on any error during token request
	 */
	String requestToken(String username, String password) throws Exception;

	/**
	 * Checks the validity of a security token.
	 *
	 * @param token
	 *            token to verify.
	 * @return true if valid, false otherwise.
	 */
	boolean validateToken(String token);

}
