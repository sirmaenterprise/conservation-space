package com.sirma.itt.seip.permissions.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.permissions.sync.batch.CompletedDryRunJobProcessingEvent;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.sep.instance.batch.BatchRequest;
import com.sirma.sep.instance.batch.BatchService;

/**
 * Test for {@link PermissionSynchronizationService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/07/2017
 */
public class PermissionSynchronizationServiceTest {
	@InjectMocks
	private PermissionSynchronizationService service;

	@Mock
	private BatchService batchService;
	@Mock
	private SearchService searchService;
	@Spy
	private Contextual<PermissionSynchronizationService.SyncExecution> execution = ContextualReference.create();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(batchService.execute(any(BatchRequest.class))).thenReturn(1L);

		service.init();
	}

	@Test
	public void triggerPermissionChecking() throws Exception {
		PermissionSynchronizationService.SyncExecution execution = service.triggerPermissionChecking();
		assertNotNull(execution);
		assertEquals(1L, execution.getExecutionId());
		assertFalse(execution.isCancelled());
		assertFalse(execution.isDone());

		verify(batchService).execute(any(BatchRequest.class));
	}

	@Test(expected = SynchronizationAlreadyRunningException.class)
	public void triggerPermissionChecking_ShouldFailIfAlreadyRunningATask() throws Exception {
		service.triggerPermissionChecking();

		service.triggerPermissionChecking();
	}

	@Test(expected = NoSynchronizationException.class)
	public void getCurrentExecution_shouldFailIfNotStarted() throws Exception {
		service.getCurrentExecution();
	}
	@Test
	public void getCurrentExecution() throws Exception {
		PermissionSynchronizationService.SyncExecution syncExecution = service.triggerPermissionChecking();
		PermissionSynchronizationService.SyncExecution currentExecution = service.getCurrentExecution();
		assertEquals(syncExecution, currentExecution);
	}

	@Test
	public void cancelSynchronization() throws Exception {
		PermissionSynchronizationService.SyncExecution syncExecution = service.triggerPermissionChecking();
		service.cancelSynchronization();

		assertTrue(syncExecution.isCancelled());
	}

	@Test(expected = NoSynchronizationException.class)
	public void cancelSynchronization_shouldFailIfNotStarted() throws Exception {
		service.cancelSynchronization();
	}

	@Test(expected = NoSynchronizationException.class)
	public void applySynchronizationChanges_shouldFailIfNotStarted() throws Exception {
		service.applySynchronizationChanges();
	}

	@Test
	public void applySynchronizationChanges_shouldDoNoThingIfNoData() throws Exception {
		service.triggerPermissionChecking();

		service.onFinishedDryRunJob(new CompletedDryRunJobProcessingEvent(1L, Collections.emptyList(), true));
		long id = service.applySynchronizationChanges();
		assertEquals(-1L, id);
	}

	@Test
	public void applySynchronizationChanges() throws Exception {
		service.triggerPermissionChecking();

		service.onFinishedDryRunJob(new CompletedDryRunJobProcessingEvent(1L,
				Arrays.asList("emf:instance1", "emf:instance2"), true));
		long id = service.applySynchronizationChanges();
		assertEquals(1L, id);

		verify(batchService, times(2)).execute(any(BatchRequest.class));
	}

	@Test
	public void syncGivenInstances_doNothingOnEmptyData() throws Exception {
		long executionId = service.syncGivenInstances(Collections.emptyList());
		assertEquals(-1L, executionId);
	}

	@Test
	public void syncGivenInstances() throws Exception {
		long executionId = service.syncGivenInstances(Arrays.asList("emf:instance1", "emf:instance2"));
		assertEquals(1L, executionId);
	}

	@Test
	public void onFinishedDryRunJob_doNothingIfNotSameExecutionId() throws Exception {
		service.triggerPermissionChecking();

		service.onFinishedDryRunJob(new CompletedDryRunJobProcessingEvent(2L,
				Collections.singletonList("emf:instance1"), true));
		PermissionSynchronizationService.SyncExecution execution = service.getCurrentExecution();
		assertTrue(execution.getData().isEmpty());
	}

	@Test
	public void onFinishedDryRunJob_shouldMarkAsDoneOnCompletedEvent() throws Exception {
		service.triggerPermissionChecking();

		service.onFinishedDryRunJob(new CompletedDryRunJobProcessingEvent(1L,
				Collections.singletonList("emf:instance1"), false));
		service.onFinishedDryRunJob(new CompletedDryRunJobProcessingEvent(1L,
				Arrays.asList("emf:instance1", "emf:instance2"), true));
		PermissionSynchronizationService.SyncExecution execution = service.getCurrentExecution();
		assertFalse(execution.getData().isEmpty());
		assertEquals(2, execution.getData().size());
		assertTrue(execution.isDone());
	}

}
