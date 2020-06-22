/**
 *
 */
package com.sirma.itt.emf.authentication.sso.saml.authenticator;

import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;

/**
 * Authenticator that reads the property ssoToken to perform the authentication
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = Authenticator.NAME, order = 11)
public class SAMLTokenAuthenticator extends BaseSamlAuthenticator {

	@Override
	public User authenticate(AuthenticationContext authenticationContext) {
		final String headerToken = authenticationContext.getProperty(TOKEN);
		if (headerToken == null) {
			return null;
		}
		return authenticateWithToken(headerToken.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public Object authenticate(User toAuthenticate) {
		// not supported
		return null;
	}

}
