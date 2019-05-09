package com.sirma.itt.seip.rest.secirity;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.rest.utils.JwtUtil;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Manages tokens for authentication of users. The tokens are signed and verified using JsonWebSignature.
 *
 * @author smustafov
 */
@ApplicationScoped
public class SecurityTokensManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private JwtConfiguration jwtConfig;

	private JwtConsumer consumer;

	/**
	 * Generates token for the given user which can be used for authentication.
	 *
	 * @param user the user to generate token for
	 * @return the generated token
	 */
	public String generate(User user) {
		JwtClaims claims = new JwtClaims();
		claims.setIssuer(jwtConfig.getIssuer());
		claims.setGeneratedJwtId();
		claims.setIssuedAtToNow();
		claims.setSubject(user.getIdentityId());

		Map<String, Serializable> properties = user.getProperties();
		if (properties != null && properties.containsKey(JwtUtil.SESSION_INDEX)) {
			claims.setStringClaim(JwtUtil.SESSION_INDEX, properties.get(JwtUtil.SESSION_INDEX).toString());
		}

		// A JWT is a JWS and/or a JWE with JSON claims as the payload.
		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(jwtConfig.getKey());

		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);

		try {
			return jws.getCompactSerialization();
		} catch (JoseException e) {
			throw new AuthenticationException(user.getIdentityId(), "Failed to sign JWT", e);
		}
	}

	/**
	 * Verifies the given token and retrieves username and issued date. If the token cannot be verified this method
	 * returns null.
	 *
	 * @param token which to extract username from
	 * @return a pair of the found username in the given token and the issued date
	 */
	public Pair<String, NumericDate> readUserNameAndDate(String token) {
		try {
			JwtClaims jwtClaims = getConsumer().processToClaims(token);
			return new Pair<>(jwtClaims.getSubject(), jwtClaims.getIssuedAt());
		} catch (MalformedClaimException | InvalidJwtException e) {
			LOGGER.warn("Invalid JWT passed", e);
		}
		return null;
	}

	private JwtConsumer getConsumer() {
		if (consumer == null) {
			consumer = new JwtConsumerBuilder()
					.setRequireSubject()
					.setRequireJwtId()
					.setExpectedIssuer(jwtConfig.getIssuer())
					.setRequireIssuedAt()
					.setVerificationKey(jwtConfig.getKey())
					// none should be disallowed for sure:
					// https://auth0.com/blog/2015/03/31/critical-vulnerabilities-in-json-web-token-libraries/
					.setJwsAlgorithmConstraints(AlgorithmConstraints.DISALLOW_NONE).build();
		}
		return consumer;
	}

	/**
	 * Checks given issued date of a token if its revoked.
	 *
	 * @param issuedDate the date that token is issued
	 * @return true if the token is revoked, otherwise false
	 */
	public boolean isRevoked(NumericDate issuedDate) {
		Date revocationDate = jwtConfig.getRevocationTimeConfig().get();
		if (revocationDate != null) {
			return issuedDate.isBefore(NumericDate.fromMilliseconds(revocationDate.getTime()));
		}
		return false;
	}

}
