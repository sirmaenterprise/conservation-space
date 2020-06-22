package com.sirma.itt.seip.permissions.sync.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.batch.runtime.context.JobContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link DummyInstancePermissionWriter}
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/07/2017
 */
public class DummyInstancePermissionWriterTest {
	@InjectMocks
	private DummyInstancePermissionWriter permissionWriter;

	@Mock
	private EventService eventService;
	@Mock
	private JobContext jobContext;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(jobContext.getExecutionId()).thenReturn(1L);
	}

	@Test
	public void checkpointInfo_ShouldNotifyWithIntermediateResults() throws Exception {
		ArgumentCaptor<CompletedDryRunJobProcessingEvent> captor = ArgumentCaptor.forClass(CompletedDryRunJobProcessingEvent.class);

		permissionWriter.writeItems(Arrays.asList(createDiff("emf:item1"),
				createDiff("emf:item2")));

		permissionWriter.checkpointInfo();

		verify(eventService).fire(argThat(CustomMatcher.of((CompletedDryRunJobProcessingEvent event) -> {
			List<String> data = event.getData();
			assertEquals(Arrays.asList("emf:item1", "emf:item2"), data);
			assertEquals(1L, event.getExecutionId());
			assertFalse(event.isDone());
		})));

		permissionWriter.writeItems(Arrays.asList(createDiff("emf:item3"),
				createDiff("emf:item4")));

		permissionWriter.checkpointInfo();

		verify(eventService, times(2)).fire(captor.capture());
		List<CompletedDryRunJobProcessingEvent> values = captor.getAllValues();
		assertEquals(2, values.size());

		assertEquals(Arrays.asList("emf:item1", "emf:item2"), values.get(0).getData());
		assertEquals(1L, values.get(0).getExecutionId());
		assertFalse(values.get(0).isDone());

		assertEquals(Arrays.asList("emf:item3", "emf:item4"), values.get(1).getData());
		assertEquals(1L, values.get(1).getExecutionId());
		assertFalse(values.get(1).isDone());
	}

	@Test
	public void close_shouldNotifyWithFinalResults() throws Exception {
		permissionWriter.writeItems(Arrays.asList(createDiff("emf:item1"),
				createDiff("emf:item2")));
		permissionWriter.writeItems(Arrays.asList(createDiff("emf:item3"),
				createDiff("emf:item4")));

		permissionWriter.close();

		verify(eventService).fire(argThat(CustomMatcher.of((CompletedDryRunJobProcessingEvent event) -> {
			List<String> data = event.getData();
			assertEquals(Arrays.asList("emf:item1", "emf:item2", "emf:item3", "emf:item4"), data);
			assertEquals(1L, event.getExecutionId());
			assertTrue(event.isDone());
		})));
	}

	private static PermissionsDiff createDiff(String id) {
		return new PermissionsDiff(id).addRoleChange("authority", "newRole", "oldRole");
	}

}
