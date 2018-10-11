/**
 *
 */
package com.sirma.itt.seip.tenant.audit;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.db.AbstractSecurityContextTenantIdentifierResolver;

/**
 * Current tenant resolver based on the {@link SecurityContext}.
 *
 * @author BBonev
 */
public class AuditTenantIdentifierResolver extends AbstractSecurityContextTenantIdentifierResolver {

	@Override
	public String resolveCurrentTenantIdentifier() {
		// force that when accessing the tenant database to force authenticated user
		return getSecurityContext().getCurrentTenantId() + "_audit";
	}
}
