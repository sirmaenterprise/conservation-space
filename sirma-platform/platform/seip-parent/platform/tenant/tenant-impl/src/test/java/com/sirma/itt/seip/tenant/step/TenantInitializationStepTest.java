package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.tenant.context.Tenant;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.context.TenantStatus;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Tests {@link TenantInitializationStep}.
 * 
 * @author smustafov
 */
public class TenantInitializationStepTest {

	@Mock
	private TenantManager tenantManager;

	@InjectMocks
	private TenantInitializationStep initializationStep = new TenantInitializationStep();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = TenantCreationException.class)
	public void testExecute_alreadyExistingActiveTenant() throws TenantValidationException {
		String tenantId = "tenant.com";
		TenantInitializationContext context = new TenantInitializationContext();

		TenantStepData data = TenantStepData.createEmpty(initializationStep.getIdentifier());
		data.getProperties().put("tenantid", tenantId);

		Tenant tenant = new Tenant(tenantId);
		tenant.setStatus(TenantStatus.ACTIVE);

		when(tenantManager.getTenant(tenantId)).thenReturn(Optional.of(tenant));

		initializationStep.execute(data, context);
	}

	@Test
	public void testExecute_shouldMarkNewTenantAsInactive() throws TenantValidationException {
		String tenantId = "tenant.com";
		TenantInitializationContext context = new TenantInitializationContext();

		TenantStepData data = TenantStepData.createEmpty(initializationStep.getIdentifier());
		data.getProperties().put("tenantid", tenantId);

		when(tenantManager.getTenant(tenantId)).thenReturn(Optional.empty());

		initializationStep.execute(data, context);

		ArgumentCaptor<Tenant> tenantArgCaptor = ArgumentCaptor.forClass(Tenant.class);
		verify(tenantManager).addNewTenant(tenantArgCaptor.capture());

		assertTrue(TenantStatus.INACTIVE.equals(tenantArgCaptor.getValue().getStatus()));
		assertEquals(tenantId, tenantArgCaptor.getValue().getTenantId());
	}

}