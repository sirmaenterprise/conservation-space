package com.sirma.itt.emf.authentication.sso.saml.authenticator;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.security.SecurityTokenService;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;

/**
 * Authenticator supports Basic HTTP Authentication to user name and password to produce a token via
 * {@link SecurityTokenService}.
 *
 * @author BBonev
 */
@Extension(target = Authenticator.NAME, order = 12)
public class BasicHttpAuthenticator extends SAMLUserNameAndPasswordAuthenticator {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	protected StringPair extractUserAndPass(AuthenticationContext authenticationContext) {
		String authorization = authenticationContext.getProperty(HttpHeaders.AUTHORIZATION);
		if (StringUtils.trimToNull(authorization) == null) {
			return StringPair.EMPTY_PAIR;
		}
		return readBasicAuthentication(authorization);
	}

	private static StringPair readBasicAuthentication(String authorization) {
		String[] strings = authorization.split(" ");
		if (strings.length > 1 && "Basic".equalsIgnoreCase(strings[0])) {
			try {
				String decoded = new String(Base64.getDecoder().decode(strings[1]), StandardCharsets.UTF_8);
				String[] split = decoded.split(":");
				if (split.length == 2) {
					return new StringPair(split[0], split[1]);
				}
			} catch (IllegalArgumentException e) {
				LOGGER.trace("Invalid format for basic authentication", e);
			}
		}
		return StringPair.EMPTY_PAIR;
	}
}
