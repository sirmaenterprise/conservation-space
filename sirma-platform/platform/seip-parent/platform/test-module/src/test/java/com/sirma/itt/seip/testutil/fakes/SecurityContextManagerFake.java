package com.sirma.itt.seip.testutil.fakes;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.ContextualWrapper;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Mock object for {@link SecurityContextManager}. The mock validates the number for context activation and
 * deactivations. See also {@link #isClean()}
 *
 * @author BBonev
 */
public class SecurityContextManagerFake implements SecurityContextManager {

	private User systemUser = mock(User.class);
	private User adminUser = mock(User.class);
	private int initContext = 0;
	private int activateContext = 0;
	private boolean authenticatedAsAdmin = false;
	private User superAdminUser = mock(User.class);
	private SecurityContext currentContext = mock(SecurityContext.class);

	@Override
	public void initializeFromContext(SecurityContext securityContext) {
		activateContext++;
	}

	@Override
	public boolean initializeExecution(AuthenticationContext authenticationContext) {
		activateContext++;
		return true;
	}

	@Override
	public void initializeTenantContext(String tenantId, String requestId) {
		activateContext++;
	}

	@Override
	public void beginContextExecution(User user) {
		activateContext++;
	}

	@Override
	public void beginContextExecution(String requestId) {
		activateContext++;
	}

	@Override
	public void endContextExecution() {
		activateContext--;
		if (activateContext < 0) {
			fail("endContextExecution() called " + activateContext + " more that begin");
		}
	}

	@Override
	public User getSystemUser() {
		return systemUser;
	}

	@Override
	public User getAdminUser() {
		return adminUser;
	}

	@Override
	@SuppressWarnings("boxing")
	public SecurityContext createTemporaryContext(User principal) {
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthenticated()).thenReturn(principal);
		when(securityContext.getEffectiveAuthentication()).thenReturn(principal);
		when(securityContext.getCurrentTenantId()).thenReturn(principal.getTenantId());
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isAuthenticated()).thenReturn(Boolean.TRUE);
		return securityContext;
	}

	@Override
	public SecurityContext getCurrentContext() {
		return currentContext;
	}

	@Override
	public SecurityContext createTransferableContext() {
		return mock(SecurityContext.class);
	}

	@Override
	public void initializeExecutionAsSystemAdmin() {
		initContext++;
	}

	@Override
	public void endExecution() {
		initContext--;
		if (initContext < 0) {
			fail("endExecution() called " + initContext + " more that beginContextExecution");
		}
	}

	@Override
	public boolean isAuthenticatedAsAdmin() {
		return authenticatedAsAdmin;
	}

	@Override
	public User getSuperAdminUser() {
		return superAdminUser;
	}

	/**
	 * Sets the admin user.
	 *
	 * @param adminUser
	 *            the new admin user
	 */
	public void setAdminUser(User adminUser) {
		this.adminUser = adminUser;
	}

	/**
	 * Sets the authenticated as admin.
	 *
	 * @param authenticatedAsAdmin
	 *            the new authenticated as admin
	 */
	public void setAuthenticatedAsAdmin(boolean authenticatedAsAdmin) {
		this.authenticatedAsAdmin = authenticatedAsAdmin;
	}

	/**
	 * Sets the current context.
	 *
	 * @param currentContext
	 *            the new current context
	 */
	public void setCurrentContext(SecurityContext currentContext) {
		this.currentContext = currentContext;
	}

	/**
	 * Sets the super admin user.
	 *
	 * @param superAdminUser
	 *            the new super admin user
	 */
	public void setSuperAdminUser(User superAdminUser) {
		this.superAdminUser = superAdminUser;
	}

	/**
	 * Sets the system user.
	 *
	 * @param systemUser
	 *            the new system user
	 */
	public void setSystemUser(User systemUser) {
		this.systemUser = systemUser;
	}

	/**
	 * Checks if contexts are closed and clean.
	 *
	 * @return true, if is clean
	 */
	public boolean isClean() {
		return initContext == 0 && activateContext == 0;
	}

	@Override
	public ContextualExecutor executeAsTenant(String tenantId) {
		initializeTenantContext(tenantId);
		try {
			return executeAs();
		} finally {
			endContextExecution();
		}
	}

	@Override
	public ContextualWrapper wrap() {
		return ContextualWrapper.NoConextualWrapper.INSTANCE;
	}

	@Override
	public ContextualExecutor executeAsSystemAdmin() {
		return ContextualExecutor.NoContextualExecutor.INSTANCE;
	}

	@Override
	public ContextualExecutor executeAsSystem() {
		return ContextualExecutor.NoContextualExecutor.INSTANCE;
	}

	@Override
	public ContextualExecutor executeAsAdmin() {
		return ContextualExecutor.NoContextualExecutor.INSTANCE;
	}

}
