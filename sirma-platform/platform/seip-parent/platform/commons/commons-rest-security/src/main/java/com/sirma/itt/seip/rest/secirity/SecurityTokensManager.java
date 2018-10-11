package com.sirma.itt.seip.rest.secirity;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import com.sirma.itt.emf.security.event.BeginLogoutEvent;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.rest.utils.JwtUtil;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Manages tokens for users. Wraps {@link JwtGenerator#generate(User)} and uses {@link SecurityTokensHolder} for storing
 * tokens.
 *
 * @author smustafov
 */
@ApplicationScoped
public class SecurityTokensManager {

	@Inject
	private JwtGenerator jwtGenerator;

	@Inject
	private JwtConfiguration jwtConfig;

	@Inject
	private SecurityTokensHolder tokensHolder;

	@Inject
	private SecurityContext securityContext;

	/**
	 * Generates JWT for given {@link User} and adds to {@link SecurityTokensHolder} JWT token as key and SAML token as
	 * value.
	 *
	 * @param user
	 *            the user to generate JWT for
	 * @return the generated JWT
	 */
	@Transactional
	public String generate(User user) {
		String jwtCompactSerialization = jwtGenerator.generate(user);
		if (user.getTicket() != null) {
			tokensHolder.addToken(jwtCompactSerialization, user.getTicket(),
					user.getProperties().get(JwtUtil.SESSION_INDEX).toString(), user.getIdentityId(),
					extractIdentityProperties(user));
		}
		return jwtCompactSerialization;
	}

	private static Map<String, Serializable> extractIdentityProperties(User user) {
		Map<String, Serializable> identityProperties = CollectionUtils.createHashMap(1);
		Serializable userAgent = user.getProperties().get(UserSessionProperties.USER_AGENT);
		if (userAgent != null) {
			identityProperties.put(UserSessionProperties.USER_AGENT, userAgent);
		}
		return identityProperties;
	}

	/**
	 * Get the JW token for the current logged in user.<br>
	 * This is some kind of workaround because the {@link User#getTicket()} returns the SAML token instead of JWT. When
	 * fixed this method will be obsolete and should be removed.
	 *
	 * @return the found token if any.
	 */
	public Optional<String> getCurrentJwtToken() {
		User authenticatedUser = securityContext.getAuthenticated();
		String samlTicket = authenticatedUser.getTicket();
		if (StringUtils.isBlank(samlTicket)) {
			return Optional.empty();
		}
		return tokensHolder.getJwtToken(samlTicket);
	}

	/**
	 * Gets the SAML token from {@link SecurityTokensHolder} by building {@link JsonWebSignature} for the given claims.
	 *
	 * @param claims
	 *            the claims
	 * @return the SAML token for given JWT claims and configuration
	 * @throws JoseException
	 *             on failed signing JWT
	 */
	public String getSamlToken(JwtClaims claims) throws JoseException {
		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(jwtConfig.getKey());
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		return tokensHolder.getSamlToken(jws.getCompactSerialization()).orElse(null);
	}

	/**
	 * Removes token from the db on user logout. Listens for {@link BeginLogoutEvent}.
	 *
	 * @param event
	 *            the fired event for logout
	 */
	public void onBeginLogout(@Observes BeginLogoutEvent event) {
		User user = event.getAuthenticatedUser();
		tokensHolder.removeBySamlToken(user.getTicket());
	}

}
