package com.sirma.itt.seip.security.decorators;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Tests for {@link SystemUserStoreDecorator}
 *
 * @author BBonev
 */
public class SystemUserStoreDecoratorTest {

	@InjectMocks
	private Decorator decorator;
	@Mock
	private UserStore delegate;
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private User systemAdmin;
	@Mock
	private User systemUser;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(systemUser.getIdentityId()).thenReturn("System@test.com");
		when(systemUser.getSystemId()).thenReturn("emf:System-test.com");
		when(systemUser.getTenantId()).thenReturn("test.com");

		when(systemAdmin.getIdentityId()).thenReturn("systemadmin");
		when(systemAdmin.getSystemId()).thenReturn("emf:systemadmin");
		when(systemAdmin.getTenantId()).thenReturn(SecurityContext.SYSTEM_TENANT);

		when(securityContextManager.getSystemUser()).thenReturn(systemUser);
		when(securityContextManager.getSuperAdminUser()).thenReturn(systemAdmin);

		when(delegate.wrap(any())).then(a -> a.getArgumentAt(0, User.class));
		when(delegate.unwrap(any())).then(a -> a.getArgumentAt(0, User.class));
	}

	@Test
	public void getUserBySystemId() throws Exception {
		assertEquals(systemUser, decorator.loadBySystemId("emf:System-test.com"));
		verify(delegate, never()).loadBySystemId(any());

		assertEquals(systemAdmin, decorator.loadBySystemId("emf:systemadmin"));
		verify(delegate, never()).loadBySystemId(any());

		decorator.loadBySystemId("emf:test-test.com");
		verify(delegate).loadBySystemId(any());
	}

	@Test
	public void getUserByIdentityId() throws Exception {
		assertEquals(systemUser, decorator.loadByIdentityId("System@test.com"));
		verify(delegate, never()).loadByIdentityId(any());

		assertEquals(systemAdmin, decorator.loadByIdentityId("systemadmin"));
		verify(delegate, never()).loadByIdentityId(any());

		decorator.loadByIdentityId("test@test.com");
		verify(delegate).loadByIdentityId(any());
	}

	@Test
	public void getUserByIdentityIdAndTenant() throws Exception {
		assertEquals(systemUser, decorator.loadByIdentityId("System@test.com", "test.com"));
		verify(delegate, never()).loadByIdentityId(any(), any());

		assertEquals(systemAdmin, decorator.loadByIdentityId("systemadmin", SecurityContext.SYSTEM_TENANT));
		verify(delegate, never()).loadByIdentityId(any(), any());

		decorator.loadByIdentityId("test@test.com", "test.com");
		verify(delegate).loadByIdentityId(any(), any());
	}

	private static class Decorator extends SystemUserStoreDecorator {

		@Override
		public User setUserTicket(User user, String ticket) {
			return null;
		}

		@Override
		public void setRequestProperties(User user, RequestInfo info) {
			// nothing to do
		}
	}
}
