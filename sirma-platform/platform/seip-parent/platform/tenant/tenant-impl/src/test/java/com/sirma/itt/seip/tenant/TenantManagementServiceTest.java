package com.sirma.itt.seip.tenant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.exception.TenantOperationException;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantDeletionException;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test the tenant management service.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantManagementServiceTest {

	private Iterable<TenantStep> deletionSteps = Arrays.asList(Mockito.mock(TenantStep.class));

	@Mock
	private TenantManager tenantManager;

	@Mock
	private TenantInitializationStatusService statusService;

	@Spy
	private TransactionSupportFake transactionSupport;

	@Mock
	private ConfigurationManagement configurationManagement;

	@InjectMocks
	private TenantManagementService service;

	/**
	 * Init the tenant steps.
	 */
	@Before
	public void init() {
		ReflectionUtils.setFieldValue(service, "deletionSteps", deletionSteps);
	}

	@Test
	public void should_invokeAllDeletionSteps_onDeletion() throws TenantValidationException {
		service.delete("tenant");
		verify(tenantManager).callTenantRemovedListeners("tenant");
		verify(deletionSteps.iterator().next()).delete(any(TenantStepData.class), any(TenantDeletionContext.class));
		verify(tenantManager).markTenantForDeletion("tenant");
	}

	@Test
	public void should_failOnException() {
		when(deletionSteps.iterator().next().delete(any(TenantStepData.class), any(TenantDeletionContext.class)))
				.thenThrow(new TenantDeletionException());
		service.delete("tenant");
		verify(statusService).setStatus(eq("tenant"), eq(TenantInitializationStatusService.Status.FAILED), anyString());
	}

	@Test
	public void should_BuildCorrectDeletionContext_OnDelete() {
		when(configurationManagement.getAllConfigurations()).thenReturn(buildConfigurations());

		service.delete("tenant");

		ArgumentCaptor<TenantDeletionContext> argumentCaptor = ArgumentCaptor.forClass(TenantDeletionContext.class);
		verify(deletionSteps.iterator().next()).delete(any(TenantStepData.class), argumentCaptor.capture());

		TenantDeletionContext deletionContext = argumentCaptor.getValue();
		assertNotNull(deletionContext);
		assertNotNull(deletionContext.getTenantInfo());
		assertEquals("tenant", deletionContext.getTenantInfo().getTenantId());
		assertEquals("val", deletionContext.getConfigValue("test.config"));
		assertFalse(deletionContext.shouldRollback());
	}

	private Collection<Configuration> buildConfigurations() {
		Configuration tenantConfiguration = new Configuration("test.config", "val", "tenant");
		tenantConfiguration.setRawValue("val");

		Configuration systemConfiguration = new Configuration("test.config", "val2", SecurityContext.SYSTEM_TENANT);
		systemConfiguration.setRawValue("val2");

		return Arrays.asList(tenantConfiguration, systemConfiguration);
	}

	@Test
	public void should_failOn_onTenantManagerException() throws TenantValidationException {
		doThrow(new TenantValidationException()).when(tenantManager).markTenantForDeletion("tenant");
		service.delete("tenant");
		verify(statusService).setStatus(eq("tenant"), eq(TenantInitializationStatusService.Status.FAILED), anyString());
	}

	@Test
	public void should_NotStopDeleteOperation_When_TenantRemoveListenersFail() throws TenantValidationException {
		doThrow(new TenantOperationException()).when(tenantManager).callTenantRemovedListeners("tenant");

		service.delete("tenant");

		verify(tenantManager).markTenantForDeletion("tenant");
	}

}
