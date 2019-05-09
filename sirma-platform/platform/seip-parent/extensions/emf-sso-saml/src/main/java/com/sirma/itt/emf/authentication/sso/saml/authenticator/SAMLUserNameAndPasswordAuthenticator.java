/**
 *
 */
package com.sirma.itt.emf.authentication.sso.saml.authenticator;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.security.SecurityTokenService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Authenticator that reads the properties username and password and tries to authenticate the user using the
 * {@link SecurityTokenService}.
 *
 * @author BBonev
 */
@Extension(target = Authenticator.NAME, order = 10)
public class SAMLUserNameAndPasswordAuthenticator extends BaseSamlAuthenticator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Instance<SecurityTokenService> securityTokenService;

	@Override
	public User authenticate(AuthenticationContext authenticationContext) {
		StringPair userAndPass = extractUserAndPass(authenticationContext);
		final String username = userAndPass.getFirst();
		final String password = userAndPass.getSecond();
		if (username == null || password == null) {
			return null;
		}
		if (securityTokenService.isUnsatisfied()) {
			throw new EmfApplicationException("No security token service installed!");
		}
		String token;
		try {
			token = securityTokenService.get().requestToken(username, password);
		} catch (Exception e) {
			throw new AuthenticationException(username, "Security token request failed", e);
		}
		if (token == null) {
			LOGGER.warn("Invalid username/password or SSO configuration. Continuing authenticators chain.");
			return null;
		}
		return authenticateWithToken(token.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Extract user and password from the request
	 *
	 * @param authenticationContext
	 *            the authentication context
	 * @return the string pair
	 */
	@SuppressWarnings("static-method")
	protected StringPair extractUserAndPass(AuthenticationContext authenticationContext) {
		final String username = authenticationContext.getProperty(USERNAME);
		final String password = authenticationContext.getProperty(CREDENTIAL);
		return new StringPair(username, password);
	}

	@Override
	public Object authenticate(User toAuthenticate) {
		if (toAuthenticate == null || toAuthenticate.getIdentityId() == null
				|| toAuthenticate.getCredentials() == null) {
			return null;
		}
		if (securityTokenService.isUnsatisfied()) {
			throw new EmfApplicationException("No security token service installed!");
		}
		return authenticateWithTokenAndGetTicket(toAuthenticate, requestToken(toAuthenticate));
	}

	private byte[] requestToken(User toAuthenticate) {
		String decryptedToken;
		try {
			decryptedToken = securityTokenService.get().requestToken(toAuthenticate.getIdentityId(),
					String.valueOf(toAuthenticate.getCredentials()));
		} catch (Exception e) {
			throw new AuthenticationException(toAuthenticate.getIdentityId(), "Security token request failed", e);
		}
		if (decryptedToken == null) {
			throw new AuthenticationException(toAuthenticate.getIdentityId(),
					"Invalid username(" + toAuthenticate.getIdentityId() + ") or SSO configuration");
		}
		return decryptedToken.getBytes(StandardCharsets.UTF_8);
	}
}
