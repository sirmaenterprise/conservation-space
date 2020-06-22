package com.sirma.itt.seip.tenant.security.interceptor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.runtime.ComponentValidationException;
import com.sirma.itt.seip.runtime.StartupComponent;
import com.sirma.itt.seip.runtime.boot.StartupException;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

/**
 * Test for {@link RunAsTenantComponentDecorator}
 *
 * @author BBonev
 */
public class RunAsTenantComponentDecoratorTest {

	@Mock
	private StartupComponent component;
	@Mock
	private AnnotatedElement annotatedElement;
	@Mock
	private TenantManager tenantManager;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(component.getAnnotated()).thenReturn(annotatedElement);
		when(annotatedElement.isAnnotationPresent(any())).thenReturn(Boolean.TRUE);
	}

	@Test
	public void runAsActiveTenants() throws Exception {
		mockAnnotation(false, false);

		when(tenantManager.getActiveTenantsInfo(false))
				.then(a -> Stream.of(new TenantInfo("tenant1"), new TenantInfo("tenant2")));

		new RunAsTenantComponentDecorator(component, tenantManager, securityContextManager).execute();

		verify(tenantManager, never()).getAllTenantsInfo(anyBoolean());
		verify(component, times(2)).execute();
		verify(securityContextManager, times(2)).initializeTenantContext(anyString());
		verify(securityContextManager, times(2)).endContextExecution();
	}

	@Test
	public void runAsAllTenants() throws Exception {
		mockAnnotation(true, true);

		when(tenantManager.getAllTenantsInfo(true))
				.then(a -> Stream.of(new TenantInfo("tenant1"), new TenantInfo("tenant2")));

		new RunAsTenantComponentDecorator(component, tenantManager, securityContextManager).execute();

		verify(tenantManager, never()).getActiveTenantsInfo(anyBoolean());
		verify(component, times(2)).execute();
		verify(securityContextManager, times(2)).initializeTenantContext(anyString());
		verify(securityContextManager, times(2)).endContextExecution();
	}

	@Test(expected = StartupException.class)
	public void runAsActiveTenantsWithError() throws Exception {
		mockAnnotation(false, false);

		when(tenantManager.getActiveTenantsInfo(false))
				.then(a -> Stream.of(new TenantInfo("tenant1"), new TenantInfo("tenant2")));
		doThrow(RuntimeException.class).when(component).execute();

		new RunAsTenantComponentDecorator(component, tenantManager, securityContextManager).execute();
	}

	@Test(expected = ComponentValidationException.class)
	public void unsupportedAnnotatedComponent() throws Exception {
		reset(annotatedElement);
		new RunAsTenantComponentDecorator(component, tenantManager, securityContextManager);
	}

	@SuppressWarnings("boxing")
	private void mockAnnotation(boolean parallel, boolean includeInactive) {
		RunAsAllTenantAdmins annotation = mock(RunAsAllTenantAdmins.class);
		when(annotation.parallel()).thenReturn(parallel);
		when(annotation.includeInactive()).thenReturn(includeInactive);
		when(annotatedElement.getAnnotation(any())).thenReturn(annotation);
	}
}