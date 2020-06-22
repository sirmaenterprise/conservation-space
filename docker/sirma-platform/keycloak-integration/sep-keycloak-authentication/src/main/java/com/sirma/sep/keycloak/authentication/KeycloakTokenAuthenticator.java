package com.sirma.sep.keycloak.authentication;

import static com.sirma.sep.keycloak.ClientProperties.TENANT_MAPPER_NAME;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.rotation.AdapterRSATokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.sep.keycloak.tenant.KeycloakDeploymentRetriever;

/**
 * Authenticator for Keycloak IdP, which reads access token and tenant per request from http headers.
 *
 * @author smustafov
 */
@Extension(target = Authenticator.NAME, order = 1)
public class KeycloakTokenAuthenticator implements Authenticator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String MASTER_REALM = "master";

	/**
	 * Match tenant in decoded token string. For example with the following decoded jwt token:
	 * {"iat":34782,"aud":"clientId",...,"tenant":"tenantId"} the pattern will extract in a group the tenantId.
	 */
	private static final Pattern TENANT_PATTERN = Pattern.compile(TENANT_MAPPER_NAME + "\":\"([^\"]+)");

	/**
	 * JWT token is separated into three parts by '.' separator.
	 */
	private static final Pattern TOKEN_SEPARATOR_PATTERN = Pattern.compile("\\.");

	protected static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

	static final String AUTHORIZATION_METHOD = "Bearer";

	@Inject
	private KeycloakDeploymentRetriever deploymentRetriever;

	@Inject
	protected SecurityContextManager securityContextManager;

	@Inject
	protected UserStore userStore;

	@Override
	public User authenticate(AuthenticationContext context) {
		String authorization = context.getProperty(HttpHeaders.AUTHORIZATION);
		if (StringUtils.isBlank(authorization)) {
			return null;
		}

		String[] auth = WHITESPACE_PATTERN.split(authorization);
		if (auth.length != 2 || !AUTHORIZATION_METHOD.equals(auth[0]) || StringUtils.isBlank(auth[1])) {
			LOGGER.trace("Skipping authenticator. Invalid authorization header value: {}", auth);
			return null;
		}

		String tokenString = auth[1];
		String tenantId = extractTenant(tokenString)
				.orElseThrow(() -> new AuthenticationException("Invalid access token payload. Cannot find tenant identifier property!"));
		return authenticateToken(tokenString, getDeployment(tenantId));
	}

	protected Optional<String> extractTenant(String tokenString) {
		String payload = TOKEN_SEPARATOR_PATTERN.split(tokenString)[1];
		byte[] decoded = Base64.getDecoder().decode(payload);
		String decodedPayload = new String(decoded, StandardCharsets.UTF_8);

		Matcher matcher = TENANT_PATTERN.matcher(decodedPayload);
		if (matcher.find()) {
			return Optional.of(matcher.group(1));
		}
		return Optional.empty();
	}

	protected User authenticateToken(String tokenString, KeycloakDeployment deployment) {
		AccessToken accessToken = verifyAndParseToken(tokenString, deployment);

		String realm = deployment.getRealm();
		if (MASTER_REALM.equals(realm)) {
			realm = SecurityContext.SYSTEM_TENANT;
		}
		String fullUserId = SecurityUtil.buildTenantUserId(accessToken.getPreferredUsername(), realm);

		User user = loadUserIdentity(realm, fullUserId);
		if (user == null) {
			throw new AuthenticationException(fullUserId, "User not found in the system");
		}

		// set token as ticket in user, which will be available in the current security context
		return userStore.setUserTicket(user, tokenString);
	}

	/**
	 * Verifies that access token is valid using Keycloak adapter. Does the following:
	 *
	 * <ol>
	 * <li>Verifies token signature. Tokens are signed using RSA algorithm. Fetches the public key from keycloak (keys
	 * are cached)</li>
	 * <li>Verifies that has correct issuer (same as the realm in the deployment)</li>
	 * <li>Verifies that has subject (username)</li>
	 * <li>Verifies that the token is active ie. both not expired and not used before its validity</li>
	 * </ol>
	 *
	 * @param tokenString access token as string
	 * @param deployment keycloak deployment for tenant
	 * @return parsed AccessToken object
	 */
	private static AccessToken verifyAndParseToken(String tokenString, KeycloakDeployment deployment) {
		try {
			return AdapterRSATokenVerifier.verifyToken(tokenString, deployment);
		} catch (VerificationException e) {
			throw new AuthenticationException("Failed to verify access token", e);
		}
	}

	protected User loadUserIdentity(String tenant, String fullUserId) {
		return securityContextManager.executeAsTenant(tenant).biFunction(userStore::loadByIdentityId, fullUserId,
				tenant);
	}

	protected KeycloakDeployment getDeployment(String tenant) {
		return deploymentRetriever.getDeployment(tenant);
	}

	@Override
	public Object authenticate(User user) {
		// not supported
		return null;
	}

}
