package com.sirma.sep.keycloak.authentication;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang.StringUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.sep.keycloak.authentication.util.KeycloakAuthUtil;

/**
 * Authenticator for Keycloak IdP with HTTP Basic authentication mechanism. It makes auth request to the idp with
 * username and password using password grant type of the UI client.
 * <p>
 * This authentication type should be used only for test purposes or with rest client to invoke standalone rest
 * endpoints, because its slow due to making requests to the idp.
 *
 * @author smustafov
 * @see KeycloakTokenAuthenticator
 */
@Extension(target = Authenticator.NAME, order = 3)
public class KeycloakBasicAuthenticator extends KeycloakTokenAuthenticator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public User authenticate(AuthenticationContext context) {
		String authorization = context.getProperty(HttpHeaders.AUTHORIZATION);
		if (StringUtils.isBlank(authorization)) {
			return null;
		}

		String[] auth = WHITESPACE_PATTERN.split(authorization);
		if (auth.length != 2 || !"Basic".equals(auth[0]) || StringUtils.isBlank(auth[1])) {
			LOGGER.trace("Skipping authenticator. Invalid authorization header value: {}", auth);
			return null;
		}

		String decodedCredentials = decodeCredentials(auth[1]);
		String[] credentials = decodedCredentials.split(":");
		if (credentials.length != 2) {
			LOGGER.trace(
					"Skipping authenticator. Invalid format of credentials provided. Expected format - username:password");
			return null;
		}

		String tenant = SecurityUtil.getUserAndTenant(credentials[0]).getSecond();
		KeycloakDeployment deployment = getDeployment(tenant);

		return authenticateUser(deployment, credentials[0], credentials[1]);
	}

	private static String decodeCredentials(String encoded) {
		return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
	}

	private User authenticateUser(KeycloakDeployment deployment, String username, String password) {
		AccessTokenResponse tokenResponse = KeycloakAuthUtil.loginWithCredentials(deployment, username, password);
		if (tokenResponse == null) {
			LOGGER.error("Basic authentication failed for user: {}", username);
			return null;
		}

		return authenticateToken(tokenResponse.getToken(), deployment);
	}

}
