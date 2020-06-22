package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;

/**
 * Tests for {@link TenantFinalizationStep}
 *
 * @author BBonev
 */
public class TenantFinializationStepTest {

	private static final String TENANT_ID = "tenant.com";
	@InjectMocks
	private TenantFinalizationStep finializationStep;
	@Mock
	private TenantManager tenantManager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void executeTenantFinalization() throws Exception {
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo(TENANT_ID));
		TenantStepData data = mock(TenantStepData.class);

		assertTrue(finializationStep.execute(data, context));

		verify(tenantManager).activeTenant(TENANT_ID);
		verify(tenantManager).finishTenantActivation(TENANT_ID);
	}

	@Test
	public void executeTenantFinalization_defaultTenant() throws Exception {
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo(SecurityContext.DEFAULT_TENANT));
		TenantStepData data = mock(TenantStepData.class);

		assertTrue(finializationStep.execute(data, context));

		verify(tenantManager).activeTenant(SecurityContext.DEFAULT_TENANT);
		verify(tenantManager, never()).finishTenantActivation(anyString());
	}

	@Test
	public void executeTenantFinalization_withError() throws Exception {
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo(TENANT_ID));
		TenantStepData data = mock(TenantStepData.class);

		doThrow(Exception.class).when(tenantManager).activeTenant(TENANT_ID);

		assertTrue(finializationStep.execute(data, context));

		verify(tenantManager).activeTenant(TENANT_ID);
		verify(tenantManager, never()).finishTenantActivation(TENANT_ID);
	}
}