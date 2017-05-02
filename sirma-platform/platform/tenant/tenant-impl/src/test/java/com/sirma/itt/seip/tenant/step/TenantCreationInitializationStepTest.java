package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.sirma.itt.seip.tenant.exception.TenantValidationException;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Tests {@link TenantCreationInitializationStep}.
 * 
 * @author smustafov
 */
public class TenantCreationInitializationStepTest {

	@Mock
	private TenantManager tenantManager;

	@InjectMocks
	private TenantCreationInitializationStep initializationStep = new TenantCreationInitializationStep();

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
		tenant.setActive(true);

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

		assertFalse(tenantArgCaptor.getValue().isActive());
		assertEquals(tenantId, tenantArgCaptor.getValue().getTenantId());
	}

}
