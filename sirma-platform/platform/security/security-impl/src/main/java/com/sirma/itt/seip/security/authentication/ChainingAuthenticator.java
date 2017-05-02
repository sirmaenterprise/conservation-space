/**
 *
 */
package com.sirma.itt.seip.security.authentication;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Default authenticator that runs all other authenticators as a chain. The first non <code>null</code>
 * {@link AuthenticatedIdentity} will be returned. If no authenticator could handle the request then
 * {@link AuthenticationException} will be thrown.
 *
 * @author BBonev
 */
@Singleton
public class ChainingAuthenticator implements Authenticator {

	@Inject
	@ExtensionPoint(Authenticator.NAME)
	private Iterable<Authenticator> authenticators;

	/**
	 * Execute authentication chain.
	 *
	 * @param authenticationContext
	 *            the authentication context
	 * @return the authenticated identity
	 */
	@Override
	public User authenticate(AuthenticationContext authenticationContext) {
		for (Authenticator authenticator : authenticators) {
			User identity = authenticator.authenticate(authenticationContext);
			if (identity != null) {
				return identity;
			}
		}
		return null;
	}

	@Override
	public Object authenticate(User toAuthenticate) {
		for (Authenticator authenticator : authenticators) {
			Object identity = authenticator.authenticate(toAuthenticate);
			if (identity != null) {
				return identity;
			}
		}
		return null;
	}
}
