package com.sirma.sep.keycloak.authentication;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.secirity.JwtParameterAuthenticator;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;

/**
 * Authenticator for Keycloak IdP, which reads access token and tenant from http parameters.
 *
 * @author smustafov
 */
@Extension(target = Authenticator.NAME, order = 2)
public class KeycloakParameterAuthenticator extends KeycloakTokenAuthenticator {

	@Inject
	private JwtConfiguration jwtConfiguration;

	@Override
	public User authenticate(AuthenticationContext context) {
		String tokenString = context.getProperty(JwtParameterAuthenticator.PARAMETER_NAME);
		if (StringUtils.isBlank(tokenString)) {
			tokenString = context.getProperty(jwtConfiguration.getJwtParameterName());
		}
		if (StringUtils.isBlank(tokenString)) {
			return null;
		}

		Optional<String> tenantId = extractTenant(tokenString);
		if (tenantId.isPresent()) {
			return authenticateToken(tokenString, getDeployment(tenantId.get()));
		}
		return null;
	}

}
