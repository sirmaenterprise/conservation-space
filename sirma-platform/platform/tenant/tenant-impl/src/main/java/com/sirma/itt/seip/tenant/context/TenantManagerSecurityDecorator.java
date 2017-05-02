/**
 *
 */
package com.sirma.itt.seip.tenant.context;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;

/**
 * Decorator to enforce tenant access security.
 *
 * @author BBonev
 */
@Decorator
public abstract class TenantManagerSecurityDecorator implements TenantManager {

	private static final String NOT_AUTHENTICATED_AS_SYSTEM_ADMIN = "Not authenticated as system admin";

	@Inject
	@Delegate
	private TenantManager delegate;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private SecurityContextManager contextManager;

	@Override
	public Optional<Tenant> getTenant(String tenantId) {
		Objects.requireNonNull(tenantId, "Tenant id is required");
		// force authenticated users only
		if (isAuthenticatedAsSystemAdmin() || securityContext.getCurrentTenantId().equalsIgnoreCase(tenantId)) {
			return delegate.getTenant(tenantId);
		}
		throw new SecurityException("Not authenticated with user from " + tenantId + " to access it's information");
	}

	@Override
	public Collection<Tenant> getAllTenants() {
		if (isAuthenticatedAsSystemAdmin()) {
			return delegate.getAllTenants();
		}
		throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
	}

	private boolean isAuthenticatedAsSystemAdmin() {
		// forces set authentication
		securityContext.getAuthenticated();
		return securityContext.isSystemTenant() && contextManager.isAuthenticatedAsAdmin();
	}

	@Override
	public void addNewTenant(Tenant tenant) throws TenantValidationException {
		if (!isAuthenticatedAsSystemAdmin()) {
			throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
		}
		delegate.addNewTenant(tenant);
	}

	@Override
	public void updateTenant(Tenant tenant) throws TenantValidationException {
		Objects.requireNonNull(tenant, "Cannot update null tenant");
		// forces set authentication
		securityContext.getAuthenticated();
		if (!contextManager.isAuthenticatedAsAdmin() || !(securityContext.isSystemTenant()
				|| securityContext.getCurrentTenantId().equalsIgnoreCase(tenant.getTenantId()))) {
			throw new SecurityException("Not authenticated as system admin or tenant admin of the same tenant");
		}
		delegate.updateTenant(tenant);
	}

	@Override
	public void activeTenant(String tenantId) throws TenantValidationException {
		if (!isAuthenticatedAsSystemAdmin()) {
			throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
		}
		delegate.activeTenant(tenantId);
	}

	@Override
	public void deactivateTenant(String tenantId) throws TenantValidationException {
		if (!isAuthenticatedAsSystemAdmin()) {
			throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
		}
		delegate.deactivateTenant(tenantId);
	}

	@Override
	public Stream<TenantInfo> getActiveTenantsInfo(boolean parallel) {
		if (isAuthenticatedAsSystemAdmin()) {
			return delegate.getActiveTenantsInfo(parallel);
		}
		throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
	}

	@Override
	public Stream<TenantInfo> getAllTenantsInfo(boolean parallel) {
		if (isAuthenticatedAsSystemAdmin()) {
			return delegate.getAllTenantsInfo(parallel);
		}
		throw new SecurityException(NOT_AUTHENTICATED_AS_SYSTEM_ADMIN);
	}

}
