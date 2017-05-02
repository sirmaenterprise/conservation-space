package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link SchedulerEntryProvider}
 *
 * @author BBonev
 */
@SuppressWarnings("boxing")
public class SchedulerEntryProviderTest {

	@InjectMocks
	private SchedulerEntryProvider entryProvider;
	@Mock
	private SchedulerDao dbDao;
	@Mock
	private SchedulerService schedulerService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	private void mockValidTasks() {
		when(dbDao.getTasksForExecution(anyCollection(), anyCollection(), any()))
				.thenReturn(new HashSet<>(Arrays.asList(1L, 2L, 3L)));

		when(schedulerService.loadByDbId(anyList())).then(a -> {
			List<SchedulerEntry> result = new ArrayList<>();
			List<Long> ids = a.getArgumentAt(0, List.class);
			for (Long id : ids) {
				SchedulerEntry entry = new SchedulerEntry();
				entry.setId(id);
				result.add(entry);
			}
			return result;
		});
	}

	@Test
	public void testGetTasks() throws Exception {
		mockValidTasks();

		when(schedulerService.activate(anyLong())).then(a -> {
			SchedulerEntry entry = new SchedulerEntry();
			entry.setId(a.getArgumentAt(0, Long.class));
			return entry;
		});

		List<SchedulerEntry> tasksForExecution = new LinkedList<>();

		entryProvider.getTasksForExecution(EnumSet.of(SchedulerEntryType.TIMED),
				EnumSet.of(SchedulerEntryStatus.NOT_RUN), 10, entry -> entry.getId() != 2L, tasksForExecution::add);

		assertNotNull(tasksForExecution);
		assertEquals(2, tasksForExecution.size());
	}

	/**
	 * The task verifies if the filter and consumer are called in proper order.<br>
	 * For tasks: task1, task2<br>
	 * The order should be:<br>
	 * filter(task1) -> consumer(task1) -> filter(task2) -> consumer(task2)
	 */
	@Test
	public void testTasksOrderProcessing() throws Exception {
		mockValidTasks();

		when(schedulerService.activate(anyLong())).then(a -> {
			SchedulerEntry entry = new SchedulerEntry();
			entry.setId(a.getArgumentAt(0, Long.class));
			return entry;
		});

		AtomicInteger filterInvocations = new AtomicInteger();
		AtomicInteger consumerInvocations = new AtomicInteger();

		entryProvider.getTasksForExecution(EnumSet.of(SchedulerEntryType.TIMED),
				EnumSet.of(SchedulerEntryStatus.NOT_RUN), 10, entry -> filterInvocations.incrementAndGet() > 0,
				entry -> assertEquals(consumerInvocations.incrementAndGet(), filterInvocations.get()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void failToLoadTasks() throws Exception {
		when(dbDao.getTasksForExecution(anyCollection(), anyCollection(), any())).thenThrow(RuntimeException.class);

		List<SchedulerEntry> tasksForExecution = new LinkedList<>();

		entryProvider.getTasksForExecution(EnumSet.of(SchedulerEntryType.TIMED),
				EnumSet.of(SchedulerEntryStatus.NOT_RUN), 10, entry -> true, tasksForExecution::add);
		assertNotNull(tasksForExecution);
		assertTrue(tasksForExecution.isEmpty());
		verify(schedulerService, never()).activate(anyLong());
	}

	@Test
	public void failToActivateTasks() throws Exception {
		mockValidTasks();
		when(schedulerService.activate(anyLong())).then(a -> {
			SchedulerEntry entry = new SchedulerEntry();
			entry.setId(a.getArgumentAt(0, Long.class));
			if (entry.getId() == 2L) {
				throw new RuntimeException();
			}
			return entry;
		});

		List<SchedulerEntry> tasksForExecution = new LinkedList<>();
		entryProvider.getTasksForExecution(EnumSet.of(SchedulerEntryType.TIMED),
				EnumSet.of(SchedulerEntryStatus.NOT_RUN), 10, entry -> true, tasksForExecution::add);
		assertNotNull(tasksForExecution);
		assertEquals(2, tasksForExecution.size());
	}
}
