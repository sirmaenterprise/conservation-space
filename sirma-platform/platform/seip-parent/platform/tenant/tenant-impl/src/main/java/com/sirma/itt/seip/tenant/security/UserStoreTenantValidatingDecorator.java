/**
 *
 */
package com.sirma.itt.seip.tenant.security;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.exception.TenantDisabledException;

/**
 * Decorator to {@link UserStore} that checks if tenant is active upon user loading with tenant information.
 *
 * @author BBonev
 */
@Decorator
@Priority(Interceptor.Priority.APPLICATION - 100)
public abstract class UserStoreTenantValidatingDecorator implements UserStore {

	@Inject
	@Delegate
	private UserStore delegate;

	@Inject
	private TenantManager tenantManager;

	@Override
	public User loadByIdentityId(String identity, String tenantId) {
		if (StringUtils.isNotBlank(tenantId) && !SecurityContext.isSystemTenant(tenantId)) {
			if (tenantManager.isTenantActive(tenantId)) {
				return delegate.loadByIdentityId(identity, tenantId);
			}
			throw new TenantDisabledException(SecurityUtil.buildTenantUserId(identity, tenantId),
					"Tenant with id " + tenantId + " is not active");
		}
		return delegate.loadByIdentityId(identity, tenantId);
	}
}
