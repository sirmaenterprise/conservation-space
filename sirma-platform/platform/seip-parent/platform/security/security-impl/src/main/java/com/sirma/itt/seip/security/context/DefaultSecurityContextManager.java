package com.sirma.itt.seip.security.context;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.exception.AccountDisabledException;
import com.sirma.itt.seip.security.exception.ContextNotActiveException;
import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Default implementation for the {@link SecurityContextManager}
 *
 * @author BBonev
 */
@ApplicationScoped
class DefaultSecurityContextManager implements SecurityContextManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Pattern DASH_PATTERN = Pattern.compile("-", Pattern.LITERAL);

	@Inject
	private Authenticator authenticator;

	@Inject
	private Instance<SecurityConfiguration> securityConfiguration;

	@Inject
	private Instance<AdminResolver> adminResolver;

	@Override
	public void beginContextExecution(User user) {
		if (!SecurityContextHolder.isSet()) {
			throw new ContextNotActiveException(
					"Security context not initialized. Call any of SecurityContextManager.initializeExecution() method "
							+ "first before calling SecurityContextManager.beginContextExecution(User)!");
		}
		SecurityContext currentContext = SecurityContextHolder.getContext();

		SecurityContext newContext = new DefaultSecurityContext(currentContext::getAuthenticated, () -> user,
				currentContext::getCurrentTenantId, currentContext::isActive, currentContext::isAuthenticated,
				currentContext.getRequestId());
		SecurityContextHolder.pushContext(newContext);

		setLoggingContext(newContext);
	}

	@Override
	public void beginContextExecution(String requestId) {
		if (StringUtils.isBlank(requestId)) {
			throw new SecurityException("Cannot set empty security identifier");
		}
		if (!SecurityContextHolder.isSet()) {
			throw new ContextNotActiveException(
					"Security context not initialized. Call any of SecurityContextManager.initializeExecution() method "
							+ "first before calling SecurityContextManager.beginContextExecution(String)!");
		}
		SecurityContext currentContext = SecurityContextHolder.getContext();

		SecurityContext newContext = new DefaultSecurityContext(currentContext::getAuthenticated, currentContext::getEffectiveAuthentication,
				currentContext::getCurrentTenantId, currentContext::isActive, currentContext::isAuthenticated,
				requestId);
		SecurityContextHolder.pushContext(newContext);

		setLoggingContext(newContext);
	}

	@Override
	public void endContextExecution() {
		LOGGER.trace("Ending security context for tenant: {}", getCurrentContext().getCurrentTenantId());
		SecurityContextHolder.popContext();
		// restore logging context if any
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			setLoggingContext(context);
		} else {
			clearLoggingContext();
		}
	}

	@Override
	public SecurityContext getCurrentContext() {
		return ThreadLocalSecurityContext.getInstance();
	}

	@Override
	public SecurityContext createTemporaryContext(User principal) {
		Objects.requireNonNull(principal, "Cannot create context for null User");
		return createContext(principal, principal, getOrGenerateRequestId());
	}

	private static SecurityContext createContext(User realAuthentication, User effectiveAuthentication,
			String requestId) {
		return new DefaultSecurityContext(() -> realAuthentication, () -> effectiveAuthentication,
				effectiveAuthentication::getTenantId, () -> true, () -> true, requestId);
	}

	@Override
	public boolean initializeExecution(AuthenticationContext authenticationContext) {
		User authenticated = authenticator.authenticate(authenticationContext);
		if (authenticated == null) {
			return false;
		}

		SecurityContext currentContext = SecurityContextHolder.getContext();
        if (currentContext != null) {
            // this check here is to prevent problems for double context initialization. this happens with more than one
            // security filter and web listener
            if (!nullSafeEquals(authenticated, currentContext.getAuthenticated())) {
                throw new SecurityException(String.format("Secure context already active for %s. Cannot activate again for %s!",
                        currentContext.getAuthenticated().getIdentityId(), authenticated.getIdentityId()));
            }
            LOGGER.warn("Requested authentication for {} user twice", authenticated.getIdentityId());
        }

		checkIfAllowedToLogin(authenticated);

		String requestId = authenticationContext.getProperty(AuthenticationContext.CORRELATION_REQUEST_ID);
		if (requestId == null) {
			requestId = generateRequestId();
		}
		SecurityContextHolder.setContext(createContext(authenticated, authenticated, requestId));
		setLoggingContext(authenticated.getTenantId(), authenticated.getIdentityId(), requestId);
		LOGGER.trace("New security session for user: {}@{}", authenticated.getIdentityId(),
				authenticated.getTenantId());
		return true;
	}

	private static void checkIfAllowedToLogin(User authenticated) {
		if (!authenticated.canLogin()) {
			throw new AccountDisabledException(authenticated,
					"User " + authenticated.getDisplayName() + " not allowed to log in!");
		}
	}

	@Override
	public void endExecution() {
		if (!SecurityContextHolder.isSet()) {
			// if this happens is indication that there is some bug that needs fixing
			throw new ContextNotActiveException(
					"Trying to end security context but the context was not activate at the first place");
		}

		try {
			SecurityContext currentContext = null;
			do {
				if (currentContext != null) {
					LOGGER.warn("When ending security session more then one context found: {}@{}",
							currentContext.getAuthenticated().getIdentityId(),
							currentContext.getAuthenticated().getTenantId());
				}
				currentContext = SecurityContextHolder.popContext();
			} while (SecurityContextHolder.isSet());
		} finally {
			clearLoggingContext();
		}
	}

	@Override
	public boolean isAuthenticatedAsAdmin() {
		return adminResolver.get().isAdmin(getCurrentContext());
	}

	@Override
	public User getSystemUser() {
		return securityConfiguration.get().getSystemUser().get();
	}

	@Override
	public User getAdminUser() {
		return securityConfiguration.get().getAdminUser().get();
	}

	@Override
	public User getSuperAdminUser() {
		return securityConfiguration.get().getSystemAdminUser();
	}

	@Override
	public void initializeExecutionAsSystemAdmin() {
		String tenantId = SecurityContext.SYSTEM_TENANT;
		if (!canChangeContext(tenantId)) {
			return;
		}
		User superAdminUser = getSuperAdminUser();
		String requestId = getOrGenerateRequestId();

		setLoggingContext(tenantId, superAdminUser.getIdentityId(), requestId);
		SecurityContextHolder.pushContext(new FixedSecurityContext(
				// lazy fetch system user and admin user
				superAdminUser, superAdminUser, tenantId, true, // active
				true, requestId)); // authenticated
	}

	@Override
	public void initializeTenantContext(String tenantId, String requestId) {
		if (!canChangeContext(tenantId)) {
			return;
		}
		String request = requestId;
		if (request == null) {
			request = getOrGenerateRequestId();
		}
		LOGGER.trace("Activating tenant context for {}", tenantId);
		setLoggingContext(tenantId, "System@" + tenantId, request);
		// create active security context with the system user and lazy initializing the admin user.
		// the admin user will be resolved when requested but after the tenant id has been set
		SecurityContextHolder.pushContext(new DefaultSecurityContext(
				// lazy fetch system user and admin user
				this::getSystemUser, this::getAdminUser, () -> tenantId, () -> true, // active
				() -> true, request)); // authenticated
	}



	private boolean canChangeContext(String tenantId) {
		SecurityContext securityContext = getCurrentContext();
		if (securityContext.isActive() && securityContext.getCurrentTenantId() != null
				&& !securityContext.isSystemTenant()) {
			// tenant already initialized
			// push the same context again to provide method consistency
			// should not print the warning if the tenants are same
			if (!SecurityContext.isSystemTenant(tenantId)
					&& nullSafeEquals(tenantId, securityContext.getCurrentTenantId())) {
				// this allows execute as system to be called using execute as tenant, but only if the tenant is the
				// same as the authenticated, but this excludes system tenant
				return true;
			}
			LOGGER.warn("Tried to change tenant from {} to {}. Request ignored!", securityContext.getCurrentTenantId(),
					tenantId);
			SecurityContext context = SecurityContextHolder.getContext();
			setLoggingContext(context);
			SecurityContextHolder.pushContext(context);
			return false;
		}
		return true;
	}

	private static void setLoggingContext(SecurityContext context) {
		setLoggingContext(context.getCurrentTenantId(), context.getAuthenticated().getIdentityId(),
				context.getRequestId());
	}

	private static void setLoggingContext(String tenantId, String user, String requestId) {
		MDC.put("user", user);
		MDC.put("tenantId", tenantId);
		MDC.put("rqId", requestId);
	}

	private static void clearLoggingContext() {
		MDC.remove("tenantId");
		MDC.remove("user");
		MDC.remove("rqId");
	}

	private String getOrGenerateRequestId() {
		String requestId = getCurrentContext().getRequestId();
		if (requestId == null) {
			requestId = generateRequestId();
		}
		return requestId;
	}

	private static String generateRequestId() {
		return DASH_PATTERN.matcher(UUID.randomUUID().toString()).replaceAll("");
	}

	@Override
	public SecurityContext createTransferableContext() {
		SecurityContext currentContext = getCurrentContext();
		if (!currentContext.isActive()) {
			return null;
		}
		return new FixedSecurityContext(currentContext);
	}

	@Override
	public void initializeFromContext(SecurityContext securityContext) {
		Objects.requireNonNull(securityContext, "Cannot initialize from null context");

		if (!(securityContext instanceof FixedSecurityContext)) {
			throw new SecurityException(
					"Cannot initialize context not produced by the method SecurityContextManager.createTransferableContext()");
		}
		if (!securityContext.isActive()) {
			throw new SecurityException("Cannot initialize from inactive security context!");
		}
		setLoggingContext(securityContext);
		SecurityContextHolder.pushContext(securityContext);
	}

	@Override
	public ContextualExecutor executeAsTenant(String tenantId) {
		return new TenantContextualExecutor(this, tenantId);
	}

	@Override
	public ContextualExecutor executeAsSystem() {
		SecurityContext securityContext = getCurrentContext();
		if (!securityContext.isActive()) {
			throw new ContextNotActiveException("Cannot execute as system without active tenant!");
		}
		if (securityContext.isSystemTenant()) {
			throw new SecurityException(
					"Cannot execute as system in system tenant scope. Use SecurityContextManager.executeAsSystemAdmin() instead");
		}

		// this will create new context where the authenticated user is the system and the operations are run with the
		// admin permissions for that tenant
		return executeAsTenant(securityContext.getCurrentTenantId());
	}

	@Override
	public ContextualExecutor executeAsSystemAdmin() {
		return new SystemAdminContextualExecutor(this);
	}

	/**
	 * Security context used to direct authentication
	 *
	 * @author BBonev
	 */
	private static final class FixedSecurityContext implements SecurityContext {

		private static final long serialVersionUID = -6730247186729306878L;
		private final User realPrincipal;
		private final boolean isAuthenticated;
		private final boolean isActive;
		private final User effectivePrincipal;
		private final String tenantId;
		private final String requestId;

		/**
		 * Instantiates a new fixed security context.
		 *
		 * @param realPrincipal
		 *            the real principal
		 * @param effectivePrincipal
		 *            the effective principal
		 * @param tenantId
		 *            the tenant id supplier
		 * @param isActive
		 *            the is active
		 * @param isAuthenticated
		 *            the is authenticated
		 * @param requestId
		 *            the request id
		 */
		FixedSecurityContext(User realPrincipal, User effectivePrincipal, String tenantId, boolean isActive,
				boolean isAuthenticated, String requestId) {
			this.realPrincipal = realPrincipal;
			this.effectivePrincipal = effectivePrincipal;
			this.tenantId = tenantId;
			this.isActive = isActive;
			this.isAuthenticated = isAuthenticated;
			this.requestId = requestId;
		}

		/**
		 * Instantiates a new fixed security context.
		 *
		 * @param copyFrom
		 *            the copy from
		 */
		FixedSecurityContext(SecurityContext copyFrom) {
			this(copyFrom.getAuthenticated(), copyFrom.getEffectiveAuthentication(), copyFrom.getCurrentTenantId(),
					copyFrom.isActive(), copyFrom.isAuthenticated(), copyFrom.getRequestId());
		}

		@Override
		public boolean isActive() {
			return isActive;
		}

		@Override
		public User getAuthenticated() {
			return realPrincipal;
		}

		@Override
		public User getEffectiveAuthentication() {
			return effectivePrincipal;
		}

		@Override
		public boolean isAuthenticated() {
			return isAuthenticated;
		}

		@Override
		public String getCurrentTenantId() {
			return tenantId;
		}

		@Override
		public String getRequestId() {
			return requestId;
		}

	}
}
