/**
 *
 */
package com.sirma.itt.seip.security.context;

import javax.inject.Inject;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * Default resolver is added at the end of the chain.
 *
 * @author BBonev
 */
@Extension(target = AdminResolver.NAME, order = Double.MAX_VALUE)
public class DefaultAdminResolver implements AdminResolver {

	@Inject
	private SecurityConfiguration securityConfiguration;
	@Inject
	private UserStore userStore;

	@Override
	public boolean isAdmin(SecurityContext securityContext) {
		if (securityContext.isAuthenticated()) {
			return userStore.unwrap(securityConfiguration.getAdminUser().get()).equals(
					userStore.unwrap(securityContext.getEffectiveAuthentication()));
		}
		return false;
	}
}
