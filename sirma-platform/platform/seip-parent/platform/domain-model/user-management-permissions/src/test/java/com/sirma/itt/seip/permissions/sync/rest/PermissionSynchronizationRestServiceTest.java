package com.sirma.itt.seip.permissions.sync.rest;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.permissions.sync.PermissionSynchronizationService;

/**
 * Test for {@link PermissionSynchronizationRestService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/07/2017
 */
public class PermissionSynchronizationRestServiceTest {
	@InjectMocks
	private PermissionSynchronizationRestService restService;

	@Mock
	private PermissionSynchronizationService synchronizationService;
	@Mock
	private PermissionSynchronizationService.SyncExecution execution;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(synchronizationService.triggerPermissionChecking()).thenReturn(execution);
		when(synchronizationService.getCurrentExecution()).thenReturn(execution);

		when(execution.waitForData()).thenReturn(Optional.empty());
		when(execution.getData()).thenReturn(Collections.singletonList("emf:instance"));
	}

	@Test
	public void syncAllDryRun() throws Exception {
		Response response = restService.syncAllDryRun();
		assertNotNull(response);

		verify(synchronizationService).triggerPermissionChecking();
	}

	@Test
	public void getData() throws Exception {
		restService.getData();
		verify(synchronizationService).getCurrentExecution();
	}

	@Test
	public void waitForData() throws Exception {
		restService.waitForData();
		verify(synchronizationService).getCurrentExecution();
	}

	@Test
	public void cancelProcessing() throws Exception {
		restService.cancelProcessing();
		verify(synchronizationService).cancelSynchronization();
	}

	@Test
	public void confirmExecution() throws Exception {
		restService.confirmExecution();
		verify(synchronizationService).applySynchronizationChanges();
	}

	@Test
	public void confirmExecution_whenCanceledDoNothing() throws Exception {
		when(synchronizationService.applySynchronizationChanges()).thenReturn(-1L);
		restService.confirmExecution();
	}

	@Test
	public void syncGivenInstances() throws Exception {
		restService.syncGivenInstances(Arrays.asList("emf:instance"));
		verify(synchronizationService).syncGivenInstances(anyList());
	}

}
