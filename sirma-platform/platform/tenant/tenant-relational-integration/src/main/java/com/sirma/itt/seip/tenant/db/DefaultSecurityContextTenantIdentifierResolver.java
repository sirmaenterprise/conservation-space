/**
 *
 */
package com.sirma.itt.seip.tenant.db;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Current tenant resolver based on the {@link SecurityContext}.
 *
 * @author BBonev
 */
public class DefaultSecurityContextTenantIdentifierResolver extends AbstractSecurityContextTenantIdentifierResolver {

	@Override
	public String resolveCurrentTenantIdentifier() {
		// force that when accessing the tenant database to force authenticated user
		if (getSecurityContext().isSystemTenant()) {
			throw new SecurityException("Cannot access system database via tenant datasource!");
		}
		return getSecurityContext().getCurrentTenantId();
	}
}
