/**
 *
 */
package com.sirma.itt.seip.security.interceptor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.interceptor.InvocationContext;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.mocks.UserMock;

/**
 * The Class RunAsSystemSecurityInterceptorTest.
 *
 * @author BBonev
 */
public class RunAsSystemSecurityInterceptorTest {

	@Mock
	private InvocationContext context;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private SecurityContextManager contextManager;

	@Spy
	private ContextualExecutor superAdminCaller = new ContextualExecutor.NoContextualExecutor();
	@Spy
	private ContextualExecutor systemCaller = new ContextualExecutor.NoContextualExecutor();

	@InjectMocks
	private RunAsSystemSecurityInterceptor interceptor;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		mockContextForMethod("mockMethod");
		when(contextManager.executeAsSystemAdmin()).thenReturn(superAdminCaller);
		when(contextManager.executeAsSystem()).thenReturn(systemCaller);
	}

	private void mockContextForMethod(String methodName) {
		try {
			Mockito.when(context.getMethod()).thenReturn(this.getClass().getMethod(methodName));
		} catch (NoSuchMethodException | SecurityException e) {
			Assert.fail("", e);
		}
	}

	@RunAsSystem
	public void mockMethod() {
	}

	@RunAsSystem(protectCurrentTenant = false)
	public void mockMethodDoNotProtectTenant() {
	}

	/**
	 * Test_not active context.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void test_notActiveContext() throws Exception {
		when(contextManager.getCurrentContext()).thenReturn(securityContext);
		when(securityContext.isActive()).thenReturn(Boolean.FALSE);

		interceptor.manageSecurityContext(context);

		verify(context).proceed();
		verify(superAdminCaller).callable(any());
	}

	/**
	 * Test_active context.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void test_activeContext() throws Exception {
		when(contextManager.getCurrentContext()).thenReturn(securityContext);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);

		UserMock admin = new UserMock("system", "tenant");
		when(contextManager.getSystemUser()).thenReturn(admin);

		interceptor.manageSecurityContext(context);

		verify(systemCaller).callable(any());
	}

	@Test
	public void test_activeContext_outsideTheCurrentTenant() throws Exception {
		reset(context);
		mockContextForMethod("mockMethodDoNotProtectTenant");

		when(contextManager.getCurrentContext()).thenReturn(securityContext);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);

		UserMock admin = new UserMock("system", "tenant");
		when(contextManager.getSystemUser()).thenReturn(admin);

		interceptor.manageSecurityContext(context);

		verify(superAdminCaller).callable(any());
	}

	@Test
	public void test_activeContext_InSystemTenant() throws Exception {
		when(contextManager.getCurrentContext()).thenReturn(securityContext);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.TRUE);

		UserMock admin = new UserMock("system", "tenant");
		when(contextManager.getSystemUser()).thenReturn(admin);

		interceptor.manageSecurityContext(context);

		verify(superAdminCaller).callable(any());
	}

	/**
	 * Test_timeout_not active context.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void test_timeout_notActiveContext() throws Exception {
		when(contextManager.getCurrentContext()).thenReturn(securityContext);
		when(securityContext.isActive()).thenReturn(Boolean.FALSE);

		interceptor.manageTimedSecurityContext(context);

		verify(context).proceed();
	}

	/**
	 * Test_timeout_active context.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void test_timeout_activeContext() throws Exception {
		when(contextManager.getCurrentContext()).thenReturn(securityContext);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);

		UserMock admin = new UserMock("admin", "tenant");
		when(contextManager.getAdminUser()).thenReturn(admin);

		interceptor.manageTimedSecurityContext(context);

		verify(systemCaller).callable(any());
	}
}
