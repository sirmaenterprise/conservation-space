package com.sirma.itt.seip.tenant;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.tenant.TenantInitializationStatusService.Status;

/**
 * Test the tenant status service.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantInitializationStatusServiceTest {

	@InjectMocks
	private TenantInitializationStatusService statusService;

	/**
	 * Test the status retrieval when there is and when there isn't a status for the current tenant.
	 */
	@Test
	public void should_getStatus_ifAvailable() {
		statusService.setStatus("tenant", Status.IN_PROGRESS, "msg");
		Pair<Status, String> status = statusService.getStatus("tenant");

		Assert.assertEquals(Status.IN_PROGRESS, status.getFirst());
		Assert.assertEquals("msg", status.getSecond());

		Pair<Status, String> nonExistingStatus = statusService.getStatus("anotherTenant");
		Assert.assertEquals(Pair.NULL_PAIR, nonExistingStatus);
	}

	/**
	 * Test that the tenant creation process is completed functionality.
	 */
	@Test
	public void should_beCompleted_when_tenantIsCreated() {
		// Test it when there is no status for this tenant.
		Assert.assertFalse(statusService.isCompleted("tenant"));

		// Test it when the progress is not completed
		statusService.setStatus("tenant", Status.IN_PROGRESS, "still in progress");
		Assert.assertFalse(statusService.isCompleted("tenant"));

		// Test it when it's completed
		statusService.setStatus("tenant", Status.COMPLETED, "completed");
		Assert.assertTrue(statusService.isCompleted("tenant"));
	}

	/**
	 * Test that the tenant creation process is in progress functionality.
	 */
	@Test
	public void should_beInProgress_when_creatingTenant() {
		// Test it when there is no status for this tenant.
		Assert.assertFalse(statusService.isInProgress("tenant"));

		// Test it when it's not in progress
		statusService.setStatus("tenant", Status.COMPLETED, "completed");
		Assert.assertFalse(statusService.isInProgress("tenant"));

		// Test it with the correct status
		statusService.setStatus("tenant", Status.IN_PROGRESS, "in progress");
		Assert.assertTrue(statusService.isInProgress("tenant"));
	}

	/**
	 * Test that the tenant creation process has failed functionality.
	 */
	@Test
	public void should_fail_when_anErrorHasOccured() {
		// Test it when there is no status for this tenant.
		Assert.assertFalse(statusService.hasFailed("tenant"));

		// Test it when it's not in progress
		statusService.setStatus("tenant", Status.COMPLETED, "completed");
		Assert.assertFalse(statusService.hasFailed("tenant"));

		// Test it with the correct status
		statusService.setStatus("tenant", Status.FAILED, "failed");
		Assert.assertTrue(statusService.hasFailed("tenant"));
	}
}