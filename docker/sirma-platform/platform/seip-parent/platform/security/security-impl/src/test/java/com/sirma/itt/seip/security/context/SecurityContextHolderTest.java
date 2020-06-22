package com.sirma.itt.seip.security.context;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.security.User;

/**
 * The Class SecurityContextHolderTest.
 *
 * @author BBonev
 */
@Test
public class SecurityContextHolderTest {

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		SecurityContextHolder.clear();
	}

	/**
	 * Test context set get.
	 */
	public void testContextSetGet() {
		assertNull(SecurityContextHolder.getContext());
		SecurityContextHolder.setContext(new SecurityContextMock());

		assertNotNull(SecurityContextHolder.getContext());
		assertEquals(SecurityContextHolder.getContext().getCurrentTenantId(), SecurityContext.SYSTEM_TENANT);

		SecurityContextHolder.setContext(new SecurityContextMock("test"));
		assertNotNull(SecurityContextHolder.getContext());
		assertEquals(SecurityContextHolder.getContext().getCurrentTenantId(), "test");
	}

	/**
	 * Test context push pop.
	 */
	public void testContextPushPop() {
		assertNull(SecurityContextHolder.getContext());
		SecurityContextHolder.pushContext(new SecurityContextMock());
		SecurityContextHolder.pushContext(new SecurityContextMock("test1"));
		SecurityContextHolder.pushContext(new SecurityContextMock("test2"));

		assertEquals(SecurityContextHolder.popContext().getCurrentTenantId(), "test2");
		assertEquals(SecurityContextHolder.popContext().getCurrentTenantId(), "test1");
		assertEquals(SecurityContextHolder.popContext().getCurrentTenantId(), SecurityContext.SYSTEM_TENANT);
		assertNull(SecurityContextHolder.popContext());
	}

	/**
	 * The Class SecurityContextMock.
	 */
	static class SecurityContextMock implements SecurityContext {
		private static final long serialVersionUID = -7932755756512014084L;
		private String tenant;
		private String requestId;

		/**
		 * Instantiates a new security context mock.
		 */
		public SecurityContextMock() {
			this(SecurityContext.SYSTEM_TENANT);
		}

		/**
		 * Instantiates a new security context mock.
		 *
		 * @param tenant
		 *            the tenant
		 */
		public SecurityContextMock(String tenant) {
			this.tenant = tenant;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isActive() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getCurrentTenantId() {
			return tenant;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public User getAuthenticated() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isAuthenticated() {
			return false;
		}

		@Override
		public String getRequestId() {
			return requestId;
		}

		public void setRequestId(String requestId) {
			this.requestId = requestId;
		}
	}
}
