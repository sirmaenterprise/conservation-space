package com.sirma.itt.seip.rest.secirity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.rest.utils.JwtUtil;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * JWT generator.
 *
 * @author yasko
 */
@ApplicationScoped
public class JwtGenerator {

	@Inject
	private JwtConfiguration jwtConfig;

	/**
	 * Generates a JWT for a given {@link User}.
	 *
	 * @param user
	 *            {@link User} instance to generate JWT for.
	 * @return The generated JWT.
	 */
	public String generate(User user) {
		JwtClaims claims = new JwtClaims();
		claims.setIssuer(jwtConfig.getIssuer());
		// claims.setExpirationTimeMinutesInTheFuture(10)
		claims.setGeneratedJwtId();
		claims.setIssuedAtToNow();
		claims.setSubject(user.getIdentityId());
		claims.setStringClaim(JwtUtil.SESSION_INDEX, user.getProperties().get(JwtUtil.SESSION_INDEX).toString());

		// TODO: encryption?
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

}
