package com.sirma.itt.seip.tenant.context;

import javax.validation.constraints.NotNull;

import com.sirma.itt.seip.tenant.exception.TenantNotActiveException;

/**
 * Provides means of querying current tenant info.
 *
 * @author BBonev
 */
public interface TenantContext {

	/**
	 * Checks for active tenant. The method should not throw an exception if there is no active tenant. If this method
	 * returns <code>true</code> then the method {@link #getActiveTenant()} should not throw
	 * {@link TenantNotActiveException}.
	 *
	 * @return <code>true</code>, if there is an active tenant and <code>false</code> otherwise
	 */
	boolean hasActive();

	/**
	 * Resolve the id of the currently active tenant. If no tenant is active an {@link TenantNotActiveException} may be
	 * thrown.
	 *
	 * @return the id of the active tenant
	 */
	String getActiveTenant();

	/**
	 * Returns the tenant manager that controls the tenant contexts.
	 *
	 * @return the tenant manager
	 */
	@NotNull
	TenantManager getTenantManager();
}
