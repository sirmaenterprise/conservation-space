package com.sirma.itt.emf.authentication.sso.saml;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.security.AuthenticationService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;

/**
 * Authenticator using a session context to get the current user. This authenticator does not read any properties.
 *
 * @author BBonev
 */
@Extension(target = Authenticator.NAME, order = 1)
public class SessionAuthenticator implements Authenticator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private Instance<AuthenticationService> authenticatedSession;

	/**
	 * Authenticate using the current session.
	 *
	 * @param authenticationContext
	 *            the authentication context
	 * @return the authenticated identity
	 */
	@Override
	public User authenticate(AuthenticationContext authenticationContext) {
		if (Boolean.parseBoolean(authenticationContext.getProperty(FORCE_AUTHENTICATION))) {
			return null;
		}
		try {
			return authenticatedSession.get().getCurrentUser();
		} catch (ContextNotActiveException e) {
			LOGGER.trace("Session not active {}", e);
		}
		return null;
	}

	@Override
	public Object authenticate(User toAuthenticate) {
		// not supported
		return null;
	}

}
