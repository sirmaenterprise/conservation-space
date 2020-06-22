package com.sirma.itt.seip.security.context;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.sirma.itt.seip.security.User;

/**
 * Security context implementation that uses suppliers to implement the interface functions.
 *
 * @author BBonev
 */
public class DefaultSecurityContext implements SecurityContext {
	private static final long serialVersionUID = 4047998859698193391L;
	private final BooleanSupplier isAuthenticated;
	private final BooleanSupplier isActive;
	private final Supplier<User> authenticated;
	private final Supplier<User> effectiveAuthentication;
	private final Supplier<String> tenantId;
	private final String requestId;

	/**
	 * Instantiates a new default security context.
	 *
	 * @param realAuthentication
	 *            the authenticated supplier
	 * @param effectiveAuthentication
	 *            the effective authentication
	 * @param tenantId
	 *            the tenant id supplier
	 * @param isActive
	 *            the is active supplier
	 * @param isAuthenticated
	 *            the is authenticated supplier
	 * @param requestId
	 *            the request correlation id
	 */
	public DefaultSecurityContext(Supplier<User> realAuthentication, Supplier<User> effectiveAuthentication,
			Supplier<String> tenantId, BooleanSupplier isActive, BooleanSupplier isAuthenticated, String requestId) {
		this.isAuthenticated = isAuthenticated;
		this.isActive = isActive;
		authenticated = realAuthentication;
		this.effectiveAuthentication = effectiveAuthentication;
		this.tenantId = tenantId;
		this.requestId = requestId;
	}

	@Override
	public User getAuthenticated() {
		return authenticated.get();
	}

	@Override
	public User getEffectiveAuthentication() {
		return effectiveAuthentication.get();
	}

	@Override
	public boolean isAuthenticated() {
		return isAuthenticated.getAsBoolean();
	}

	@Override
	public boolean isActive() {
		return isActive.getAsBoolean();
	}

	@Override
	public String getCurrentTenantId() {
		return tenantId.get();
	}

	@Override
	public String getRequestId() {
		return requestId;
	}
}