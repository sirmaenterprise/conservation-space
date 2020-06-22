package com.sirma.itt.seip.rest.utils;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationResponseDecorator;

/**
 * Used to decorate a saml response. More specifically the ReplayState by appending the generated JWT token.
 *
 * @author yasko
 */
@Singleton
public class JwtAuthResponseDecorator implements AuthenticationResponseDecorator {

	@Inject
	private SecurityTokensManager tokensManager;

	@Override
	public void decorate(Map<String, Object> request) {
		String redirect = (String) request.get(AuthenticationResponseDecorator.RELAY_STATE);
		User user = (User) request.get(AuthenticationResponseDecorator.USER);
		if (StringUtils.isNotBlank(redirect) && user != null) {
			String token = tokensManager.generate(user);
			if (redirect.contains("?")) {
				redirect += "&";
			} else {
				redirect += "?";
			}
			request.put(AuthenticationResponseDecorator.RELAY_STATE, redirect + "jwt=" + token);
		}
	}

}
