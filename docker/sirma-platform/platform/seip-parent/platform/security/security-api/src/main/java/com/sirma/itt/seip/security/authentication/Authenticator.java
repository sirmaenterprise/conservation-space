package com.sirma.itt.seip.security.authentication;

import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Attempt to authenticate using the given context. This is intended to be implemented as a authentication chain. So if
 * the called implementation does not recognize it's properties should ignore the request. If the authentication fails
 * {@link AuthenticationException} should be thrown.
 *
 * @author BBonev
 */
public interface Authenticator extends Plugin {
	String NAME = "Authenticator";

	String USERNAME = "username";
	String CREDENTIAL = "password";

	String TOKEN = "ssoToken";

	/** Boolean property to force authentication pass the session authentication. */
	String FORCE_AUTHENTICATION = "reset";

	/**
	 * Retrieve the authentication using the given request.
	 *
	 * @param authenticationContext
	 *            the authentication context to use
	 * @return the authenticated identity or <code>null</code> if the authenticator could not handle the request
	 */
	User authenticate(AuthenticationContext authenticationContext);

	/**
	 * Authenticate the given user instance. This method should be called with user instance that can provide user name
	 * and password for authentication via {@link User#getIdentityId()} and {@link User#getCredentials()}
	 *
	 * @param toAuthenticate
	 *            the to authenticate
	 * @return the object
	 */
	Object authenticate(User toAuthenticate);
}
