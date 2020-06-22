package com.sirmaenterprise.sep.jms.security;

import javax.inject.Inject;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Special authenticator used to initialize security context for JMS received messages. The authenticator reads
 * properties only from the {@link JmsAuthenticationContext}. It does not perform any authentication but only loading
 * of user and tenant identifier.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/05/2017
 */
@Extension(target = Authenticator.NAME, order = 50)
public class JmsSecurityAuthenticator implements Authenticator {

	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private UserStore userStore;

	@Override
	public User authenticate(AuthenticationContext authenticationContext) {
		if (authenticationContext instanceof JmsAuthenticationContext) {
			JmsAuthenticationContext context = (JmsAuthenticationContext) authenticationContext;
			String authenticated = context.getAuthenticatedUser();
			String tenantId = context.getTenantId();
			if (authenticated == null || tenantId == null) {
				return null;
			}
			return securityContextManager.executeAsTenant(tenantId)
					.function(userStore::loadBySystemId, authenticated);
		}
		return null;
	}

	@Override
	public Object authenticate(User toAuthenticate) {
		return null;
	}
}
