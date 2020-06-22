/**
 *
 */
package com.sirma.itt.seip.tenant.db;

import java.lang.invoke.MethodHandles;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.CDI;

/**
 * Current tenant resolver based on the {@link SecurityContext}.
 *
 * @author BBonev
 */
public abstract class AbstractSecurityContextTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** The security context. */
	private SecurityContext securityContext;

	/**
	 * Gets the security context.
	 *
	 * @return the security context
	 */
	protected SecurityContext getSecurityContext() {
		if (securityContext == null) {
			securityContext = CDI.instantiateBean(SecurityContext.class, CDI.getCachedBeanManager(),
					CDI.getDefaultLiteral());
			LOGGER.debug("Successfully aquired SecurityContext instance for tenant id resolution!");
		}
		return securityContext;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return false;
	}
}
