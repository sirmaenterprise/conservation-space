package com.sirma.itt.seip.rest.resources;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Authentication resource. Used by clients to authenticate in the system and get a hold of a JWT.
 *
 * @author yasko
 */
@Singleton
@Path("/auth")
public class AuthenticationResource {

	@Inject
	private Authenticator authenticator;

	@Inject
	private SecurityTokensManager tokensManager;

	@Inject
	private JwtConfiguration jwtConfiguration;

	/**
	 * Authenticates a client and generates a JWT.
	 *
	 * @param header
	 *            The value of the Authentication HTTP header with Basic authentication method.
	 * @return {@code JsonWebToken} instance containing the encoded JWT.
	 */
	@POST
	@Transactional
	@PublicResource
	@Produces({ Versions.V2_JSON, MediaType.APPLICATION_JSON })
	public JsonObject authenticate(@HeaderParam(HttpHeaders.AUTHORIZATION) String header) {
		AuthenticationContext context = createAuthContext(header);

		User user = authenticator.authenticate(context);
		if (user == null) {
			throw new AuthenticationException(null, "Failed to authenticate user");
		}
		if (!user.canLogin()) {
			throw new AuthenticationException(user.getIdentityId(),
					"User " + user.getDisplayName() + " not allow to log in");
		}

		return Json.createObjectBuilder().add(JsonKeys.TOKEN, tokensManager.generate(user)).build();
	}

	/**
	 * Gets the authentication key that can be used for JWT parameter authentication.
	 *
	 * @return the auth key
	 */
	@GET
	@PublicResource
	@Path("/jwt/configuration")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAuthKey() {
		return Json
				.createObjectBuilder()
					.add("parameterName", jwtConfiguration.getJwtParameterName())
					.build()
					.toString();
	}

	/**
	 * Validates the Authorization HTTP header and creates an {@link AuthenticationContext} out of it.
	 *
	 * @param header
	 *            HTTP Authorization header.
	 * @return {@link AuthenticationContext} containing the authorization token
	 */
	private static AuthenticationContext createAuthContext(String header) {
		if (StringUtils.isBlank(header)) {
			throw new AuthenticationException(null, "Authorization header must be provided");
		}
		return AuthenticationContext.create(Collections.singletonMap(HttpHeaders.AUTHORIZATION, header));
	}
}
