package com.sirma.itt.emf.security;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Wrapper class for authentication mechanisms related to rest services or servlets. There is option
 * to use username+pass or saml2 token.
 *
 * @author bbanchev
 */
public interface BackendAuthenticator {

	/**
	 * Do an authentication in the system. If some data is not valid, false is returned, otherwise
	 * true
	 *
	 * @param request
	 *            is the http request
	 * @param response
	 *            is the http response
	 * @param username
	 *            is the user to authenticate. Should be provided only if pass is provided as well
	 * @param password
	 *            is the password
	 * @param encryptedToken
	 *            is the saml2 authenication response of an user.
	 * @return true if authentication event is fired
	 */
	boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response,
			final String username, final String password, final String encryptedToken);

	/**
	 * Clear authentication created using the
	 * {@link #doAuthenticate(HttpServletRequest, HttpServletResponse, String, String, String)}
	 * method.
	 * <p>
	 * <b>NOTE: </b>This method should be called when the work that needed the authentication is
	 * finished to clear any authentication data.
	 */
	void clearAuthentication();

	/**
	 * Execute the {@link Callable} in authenticated context. The method will create authentication
	 * context before execution and will clear it at the end. The method will throw
	 * {@link com.sirma.itt.emf.exceptions.EmfRuntimeException} if the callable throws an exception
	 * 
	 * @param <E>
	 *            the expected result type
	 * @param callable
	 *            the callable to execute
	 * @param username
	 *            the username to use. Should be used with password parameter. Not needed if token
	 *            is provided
	 * @param password
	 *            the password to use for the given username. Not needed if token is provided.
	 * @param encryptedToken
	 *            the encrypted token to use. If passed and valid it will overrdide the provided
	 *            username and password.
	 * @return the result from callable execution. If the authentication fail the method will return
	 *         <code>null</code>.
	 */
	<E> E executeAuthenticated(Callable<E> callable, String username, String password,
			String encryptedToken);
}