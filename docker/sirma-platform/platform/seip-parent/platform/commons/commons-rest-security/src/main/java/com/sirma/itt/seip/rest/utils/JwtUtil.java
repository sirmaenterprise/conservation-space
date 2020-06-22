package com.sirma.itt.seip.rest.utils;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * Contains methods for processing jwt tokens.
 *
 * @author smustafov
 */
public class JwtUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String SESSION_INDEX = "SessionIndex";

	@Inject
	private JwtConfiguration jwtConfig;

	/**
	 * Loads user from jwt token.
	 *
	 * @param securityContextManager
	 *            the security context manager
	 * @param userStore
	 *            the user store
	 * @param jwtToken
	 *            the jwt token
	 * @return the loaded user or null
	 */
	public User readUser(SecurityContextManager securityContextManager, UserStore userStore, String jwtToken) {
		JwtClaims claims = getClaims(jwtToken);
		if (claims == null) {
			return null;
		}

		try {
			String subject = claims.getSubject();
			StringPair userAndTenant = SecurityUtil.getUserAndTenant(subject);
			String tenant = userAndTenant.getSecond();
			return securityContextManager.executeAsTenant(tenant).biFunction(userStore::loadByIdentityId, subject,
					tenant);
		} catch (MalformedClaimException e) {
			LOGGER.error("Error when reading user from jwt token", e);
		}

		return null;
	}

	/**
	 * Extracts user id from the given jwt token.
	 *
	 * @param jwtToken
	 *            with which a user is authenticated
	 * @return the user id in the given jwt token or null if not present
	 */
	public String extractUserId(String jwtToken) {
		JwtClaims claims = getClaims(jwtToken);
		if (claims == null) {
			return null;
		}

		try {
			return claims.getSubject();
		} catch (MalformedClaimException e) {
			LOGGER.error("Error when reading user from jwt token", e);
		}

		return null;
	}

	/**
	 * Extracts session index from the given jwt token.
	 *
	 * @param jwtToken
	 *            the jwt token to extract from session index
	 * @return the extracted session index from the given jwt token
	 */
	public String extractSessionIndex(String jwtToken) {
		JwtClaims claims = getClaims(jwtToken);
		if (claims == null) {
			return null;
		}

		try {
			String sessionIndex = claims.getStringClaimValue(SESSION_INDEX);
			if (StringUtils.isBlank(sessionIndex)) {
				// this means we forgot to set it on login or something else
				throw new SecurityException("Session index not found in jwt token");
			}
			return sessionIndex;
		} catch (MalformedClaimException e) {
			LOGGER.error("Error while extracting session index from jwt token", e);
		}

		return null;
	}

	private JwtClaims getClaims(String jwtToken) {
		JwtConsumer jwtConsumer = getConsumer();
		try {
			return jwtConsumer.processToClaims(jwtToken);
		} catch (InvalidJwtException e) {
			LOGGER.error("Invalid jwt token", e);
		}
		return null;
	}

	/**
	 * Produces consumer for jwt.
	 *
	 * @return the jwt consumer
	 */
	private JwtConsumer getConsumer() {
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
				.setRequireSubject()
				.setRequireJwtId()
				.setExpectedIssuer(jwtConfig.getIssuer())
				.setRequireIssuedAt()
				.setVerificationKey(jwtConfig.getKey())
				.setJwsAlgorithmConstraints(AlgorithmConstraints.DISALLOW_NONE).build();
		return jwtConsumer;
	}

}
