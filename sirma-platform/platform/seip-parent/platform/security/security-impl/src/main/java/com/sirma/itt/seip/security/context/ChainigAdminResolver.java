/**
 *
 */
package com.sirma.itt.seip.security.context;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Default injectable instance that calls {@link AdminResolver} extensions to resolve the identity of the given context
 */
@Singleton
public class ChainigAdminResolver implements AdminResolver, Serializable {

	private static final long serialVersionUID = 6721233236395930300L;

	@Inject
	@ExtensionPoint(AdminResolver.NAME)
	private Iterable<AdminResolver> resolvers;

	@Override
	public boolean isAdmin(SecurityContext securityContext) {
		for (AdminResolver adminResolver : resolvers) {
			if (adminResolver.isAdmin(securityContext)) {
				return true;
			}
		}
		return false;
	}

}
