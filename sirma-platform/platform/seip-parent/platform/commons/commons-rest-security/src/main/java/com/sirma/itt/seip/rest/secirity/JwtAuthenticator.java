package com.sirma.itt.seip.rest.secirity;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang.StringUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.session.SessionManager;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * {@link Authenticator} implementation for handling JWT authentications.
 *
 * @author yasko
 */
@Extension(target = Authenticator.NAME, order = 1)
public class JwtAuthenticator implements Authenticator {
	private static final String AUTHORIZATION_METHOD = "Bearer";

	@Inject
	private JwtConfiguration jwtConfig;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private UserStore userStore;

	@Inject
	private SecurityTokensManager securityTokensManager;

	@Inject
	private SessionManager sessionManager;

	private JwtConsumer consumer;

	@Override
	public User authenticate(AuthenticationContext context) {
		String header = context.getProperty(HttpHeaders.AUTHORIZATION);
		if (StringUtils.isBlank(header)) {
			return null;
		}

		String[] split = header.split(" ");
		if (split.length != 2 || !AUTHORIZATION_METHOD.equals(split[0]) || StringUtils.isBlank(split[1])) {
			return null;
		}
		return readUser(split[1], getConsumer());
	}

	protected JwtConsumer getConsumer() {
		if (consumer == null) {
			JwtConsumer jwtConsumer = new JwtConsumerBuilder()
					.setRequireSubject()
						.setRequireJwtId()
						.setExpectedIssuer(jwtConfig.getIssuer())
						.setRequireIssuedAt()
						.setVerificationKey(jwtConfig.getKey())
						// TODO: now we support only one alg (HS256), maybe only that should be allowed
						// none should be disallowed for sure:
						// https://auth0.com/blog/2015/03/31/critical-vulnerabilities-in-json-web-token-libraries/
						.setJwsAlgorithmConstraints(AlgorithmConstraints.DISALLOW_NONE)
						.build();
			consumer = jwtConsumer;
		}
		return consumer;
	}

	protected User readUser(String token, JwtConsumer jwtConsumer) {
		try {
			JwtClaims claims = jwtConsumer.processToClaims(token);
			String subject = claims.getSubject();
			StringPair userAndTenant = SecurityUtil.getUserAndTenant(subject);
			String tenant = userAndTenant.getSecond();
			User user = securityContextManager.executeAsTenant(tenant).biFunction(userStore::loadByIdentityId, subject,
					tenant);

			if (user == null) {
				throw new AuthenticationException(subject, "User not found in the system");
			}

			setUserTicket(user, claims);
			sessionManager.updateLoggedUser(token, user.getIdentityId());
			return user;
		} catch (InvalidJwtException | MalformedClaimException e) {
			throw new AuthenticationException("Invalid JWT", e);
		}
	}

	/**
	 * Sets ticket (SAML token) for the user, if its {@code null}, by retrieving it from {@link SecurityTokensManager}.
	 *
	 * @param user
	 *            the user for which will be set ticket
	 * @param claims
	 *            the jwt claims
	 */
	private void setUserTicket(User user, JwtClaims claims) {
		if (user.getTicket() == null) {
			try {
				String samlToken = securityTokensManager.getSamlToken(claims);
				if (samlToken == null) {
					throw new AuthenticationException(user.getIdentityId(), "SAML token not found for user: " + user.getIdentityId());
				}
				userStore.setUserTicket(user, samlToken);
			} catch (JoseException e) {
				throw new AuthenticationException(user.getIdentityId(), "Failed to sign JWT", e);
			}
		}
	}

	@Override
	public Object authenticate(User user) {
		// not supported
		return null;
	}

}
