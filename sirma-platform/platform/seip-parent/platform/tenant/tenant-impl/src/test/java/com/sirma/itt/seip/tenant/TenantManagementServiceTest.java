package com.sirma.itt.seip.tenant;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;
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
		verify(deletionSteps.iterator().next()).delete(any(TenantStepData.class), any(TenantInfo.class), eq(false));
		verify(tenantManager).markTenantForDeletion("tenant");
	}

	@Test(expected = TenantDeletionException.class)
	public void should_throwDeletionException_onException() {
		when(deletionSteps.iterator().next().delete(any(TenantStepData.class), any(TenantInfo.class), eq(false)))
				.thenThrow(new TenantDeletionException());
		service.delete("tenant");
	}

	@Test(expected = TenantDeletionException.class)
	public void should_throwDeletionException_onTenantManagerException() throws TenantValidationException {
		doThrow(new TenantValidationException()).when(tenantManager).markTenantForDeletion("tenant");
		service.delete("tenant");
	}
}
