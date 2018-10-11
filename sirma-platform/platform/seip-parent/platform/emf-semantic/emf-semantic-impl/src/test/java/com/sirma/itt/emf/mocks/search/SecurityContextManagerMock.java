/**
 *
 */
package com.sirma.itt.emf.mocks.search;

import static org.mockito.Mockito.mock;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * @author BBonev
 */
public class SecurityContextManagerMock implements SecurityContextManager {

	@Override
	public boolean initializeExecution(AuthenticationContext authenticationContext) {
		return false;
	}

	@Override
	public void endExecution() {
		// nothing to do
	}

	@Override
	public User getSystemUser() {
		return mock(User.class);
	}

	@Override
	public User getAdminUser() {
		return mock(User.class);
	}

	@Override
	public SecurityContext createTemporaryContext(User principal) {
		return getCurrentContext();
	}

	@Override
	public void beginContextExecution(User user) {
		// nothing to do
	}

	@Override
	public void beginContextExecution(String requestId) {
		// nothing to do
	}

	@Override
	public void endContextExecution() {
		// nothing to do
	}

	@Override
	public SecurityContext getCurrentContext() {
		return mock(SecurityContext.class);
	}

	@Override
	public SecurityContext createTransferableContext() {
		return getCurrentContext();
	}

	@Override
	public void initializeFromContext(SecurityContext securityContext) {
		// nothing to do
	}

	@Override
	public boolean isAuthenticatedAsAdmin() {
		return false;
	}

	@Override
	public void initializeTenantContext(String tenantId, String requestId) {
		// nothing to do
	}

	@Override
	public User getSuperAdminUser() {
		return mock(User.class);
	}

	@Override
	public void initializeExecutionAsSystemAdmin() {

	}

	@Override
	public ContextualExecutor executeAsTenant(String tenantId) {
		return ContextualExecutor.NoContextualExecutor.INSTANCE;
	}

	@Override
	public ContextualExecutor executeAsSystemAdmin() {
		return ContextualExecutor.NoContextualExecutor.INSTANCE;
	}

	@Override
	public ContextualExecutor executeAsSystem() {
		return ContextualExecutor.NoContextualExecutor.INSTANCE;
	}

}
