package com.sirma.itt.seip.security.authentication;

import java.util.Map;

/**
 * Decorator called after successful authentication.
 *
 * @author yasko
 */
public interface AuthenticationResponseDecorator {
	String RELAY_STATE = "RelayState";
	String USER = "user";

	/**
	 * Decorates the authentication request.
	 *
	 * @param request
	 *            Auth request.
	 */
	void decorate(Map<String, Object> request);
}
