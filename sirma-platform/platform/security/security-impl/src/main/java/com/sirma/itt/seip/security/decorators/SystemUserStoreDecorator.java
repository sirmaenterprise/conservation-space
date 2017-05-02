package com.sirma.itt.seip.security.decorators;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Delegate that provides access to system user and system admin user when requested by login procedure. This way we may
 * ensure that system user will not be allowed to login
 *
 * @author BBonev
 */
@Decorator
@Priority(Interceptor.Priority.APPLICATION)
public abstract class SystemUserStoreDecorator implements UserStore {

	@Inject
	@Delegate
	private UserStore delegate;

	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public User loadBySystemId(Serializable systemId) {
		if (nullSafeEquals(securityContextManager.getSystemUser().getSystemId(), systemId)) {
			return delegate.wrap(securityContextManager.getSystemUser());
		}
		if (nullSafeEquals(securityContextManager.getSuperAdminUser().getSystemId(), systemId)) {
			return delegate.wrap(securityContextManager.getSuperAdminUser());
		}
		return delegate.loadBySystemId(systemId);
	}

	@Override
	public User loadByIdentityId(String identity) {
		if (nullSafeEquals(securityContextManager.getSystemUser().getIdentityId(), identity, true)) {
			return delegate.wrap(securityContextManager.getSystemUser());
		}
		if (nullSafeEquals(securityContextManager.getSuperAdminUser().getIdentityId(), identity, true)) {
			return delegate.wrap(securityContextManager.getSuperAdminUser());
		}
		return delegate.loadByIdentityId(identity);
	}

	@Override
	public User loadByIdentityId(String identity, String tenantId) {
		if (nullSafeEquals(securityContextManager.getSystemUser().getIdentityId(), identity, true)) {
			return delegate.wrap(securityContextManager.getSystemUser());
		}
		if (nullSafeEquals(securityContextManager.getSuperAdminUser().getIdentityId(), identity, true)) {
			return delegate.wrap(securityContextManager.getSuperAdminUser());
		}
		return delegate.loadByIdentityId(identity, tenantId);
	}
}
