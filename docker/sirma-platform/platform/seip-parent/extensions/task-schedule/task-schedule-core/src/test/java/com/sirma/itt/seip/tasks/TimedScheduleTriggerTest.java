package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.concurrent.SimpleFuture;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link TimedScheduleTrigger}
 *
 * @author BBonev
 */
@SuppressWarnings("boxing")
public class TimedScheduleTriggerTest {

	@InjectMocks
	private TimedScheduleTrigger timedTrigger;

	@Mock
	private MainSchedulerTrigger mainSchedulerTrigger;
	@Mock
	private SchedulerTaskExecutor scheduledExecutor;
	@Mock
	private SchedulerEntryProvider dbDao;
	@Mock
	private SchedulerEntryStore schedulerStore;
	@Mock
	private SchedulerExecuter schedulerExecuter;
	@Mock
	private SecurityContext securityContext;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Spy
	private ContextualConcurrentMap<String, SchedulerTask> runningTasks = ContextualConcurrentMap.create();
	@Spy
	private ConfigurationProperty<Long> timedExecutorDelay = new ConfigurationPropertyMock<>(60L);
	@Spy
	private ContextualConcurrentMap<String, AtomicInteger> activateGroups = ContextualConcurrentMap.create();
	@Mock
	private SchedulerService schedulerService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		timedTrigger.initialize();
		when(securityContext.getAuthenticated()).thenReturn(mock(User.class));
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);

		when(mainSchedulerTrigger.scheduleMainChecker(any(), anyLong(), any())).then(a -> {
			a.getArgumentAt(0, Executable.class).execute();
			return mock(Future.class);
		});

		doAnswer(a -> {
			Consumer<SchedulerEntry> consumer = a.getArgumentAt(4, Consumer.class);
			consumer.accept(createEntry(1L));
			consumer.accept(createEntry(2L));
			consumer.accept(createEntry(3L));
			return null;
		}).when(dbDao).getTasksForExecution(anySet(), anySet(), anyLong(), any(), any());
	}

	@Test
	public void testScheduleCoreTasks() throws Exception {
		mockRunTaskSuccessfully();

		timedTrigger.checkAndScheduleTimedTasks();
		verify(dbDao).getTasksForExecution(anySet(), anySet(), anyLong(), any(), any());
		verify(schedulerStore, atLeast(3)).saveChanges(any());
	}

	@Test
	public void testScheduleTenantTasks_canceled() throws Exception {
		mockRunTaskCanceled();

		timedTrigger.checkAndScheduleTenantTasks();
		verify(dbDao, times(1)).getTasksForExecution(anySet(), anySet(), anyLong(), any(), any());
		verify(schedulerStore, never()).saveChanges(any());
	}

	@Test
	public void onTaskAdded() throws Exception {
		SchedulerEntry entry = createEntry(4L);

		mockRunTaskSuccessfully();

		timedTrigger.onTask(mockMessage(entry));

		verify(schedulerStore).saveChanges(any());
	}

	@Test
	public void onCompletedTaskAdded() throws Exception {
		SchedulerEntry entry = createEntry(4L);
		entry.setStatus(SchedulerEntryStatus.COMPLETED);

		mockRunTaskSuccessfully();

		timedTrigger.onTask(mockMessage(entry));

		verify(schedulerStore, never()).saveChanges(any());
	}

	@Test
	public void onTaskAdded_whenShutdown() throws Exception {
		timedTrigger.onShutdown();

		SchedulerEntry entry = createEntry(4L);

		mockRunTaskSuccessfully();

		timedTrigger.onTask(mockMessage(entry));

		verify(schedulerStore, never()).saveChanges(any());
	}

	@Test
	public void testScheduleFailedCoreTasks() throws Exception {
		mockRunTaskFailed();

		timedTrigger.checkAndScheduleTimedTasks();
		verify(dbDao, times(1)).getTasksForExecution(anySet(), anySet(), anyLong(), any(), any());
	}

	private static SchedulerEntry createEntry(Long id) {
		SchedulerEntry entry = new SchedulerEntry();
		entry.setIdentifier(String.valueOf(id));
		entry.setId(id);
		entry.setAction(mock(SchedulerAction.class));
		SchedulerConfiguration configuration = mock(SchedulerConfiguration.class);
		when(configuration.getType()).thenReturn(SchedulerEntryType.TIMED);
		when(configuration.getNextScheduleTime()).thenReturn(new Date());
		when(configuration.getIdentifier()).thenReturn(String.valueOf(id));
		entry.setConfiguration(configuration);
		entry.setStatus(SchedulerEntryStatus.RUNNING);
		return entry;
	}

	private Message mockMessage(SchedulerEntry entry) throws JMSException {
		Message message = mock(Message.class);
		when(message.getStringProperty("time")).thenReturn("2017-12-15T00:00:00.000Z");
		when(message.getLongProperty("id")).thenReturn(entry.getId());
		when(schedulerService.activate(entry.getId())).thenReturn(entry);
		return message;
	}

	private void mockRunTaskSuccessfully() {
		when(schedulerExecuter.execute(any(), eq(false), eq(false)))
				.thenReturn(new SimpleFuture<>(SchedulerEntryStatus.COMPLETED));

		when(scheduledExecutor.submit(any(), anyLong(), eq(timedTrigger))).then(a -> {
			SchedulerTask task = mock(SchedulerTask.class);
			when(task.getEntry()).thenReturn(a.getArgumentAt(0, SchedulerEntry.class));
			when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
			SchedulerTaskCallback callback = a.getArgumentAt(2, SchedulerTaskCallback.class);
			callback.onTimeout(task);
			callback.onExecuteSuccess(task);
			return task;
		});
	}

	@Test
	public void onFail() throws Exception {
		SchedulerEntry entry = createEntry(7L);
		SchedulerTask task = mock(SchedulerTask.class);
		when(task.getEntry()).then(a -> entry);
		when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);

		when(scheduledExecutor.submit(any(), anyLong(), eq(timedTrigger))).then(a -> task);
		try {
			timedTrigger.onTask(mockMessage(entry));
			timedTrigger.onExecuteFail(task);

			verify(schedulerStore).saveChanges(entry);
		} finally {
			timedTrigger.onShutdown();
		}
	}

	@Test
	public void onFail_noMoreInvocations() throws Exception {
		SchedulerEntry entry = createEntry(7L);
		when(entry.getConfiguration().getNextScheduleTime()).thenReturn(null);
		SchedulerTask task = mock(SchedulerTask.class);
		when(task.getEntry()).then(a -> entry);
		when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);

		when(scheduledExecutor.submit(any(), anyLong(), eq(timedTrigger))).then(a -> task);
		try {
			timedTrigger.onTask(mockMessage(entry));
			timedTrigger.onExecuteFail(task);

			verify(schedulerStore).saveChanges(
					argThat(CustomMatcher.of((SchedulerEntry e) -> e.getStatus() == SchedulerEntryStatus.FAILED,
							"The entry should be marked as failed")));
		} finally {
			timedTrigger.onShutdown();
		}
	}

	@Test
	public void onFail_InTheFuture() throws Exception {
		SchedulerEntry entry = createEntry(8L);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, 30);
		when(entry.getConfiguration().getNextScheduleTime()).thenReturn(new Date(), calendar.getTime());
		SchedulerTask task = mock(SchedulerTask.class);
		when(task.getEntry()).then(a -> entry);
		when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);

		when(scheduledExecutor.submit(any(), anyLong(), eq(timedTrigger))).then(a -> task);
		try {
			timedTrigger.onTask(mockMessage(entry));
			timedTrigger.onExecuteFail(task);

			verify(schedulerStore).saveChanges(entry);
			verify(scheduledExecutor, times(2)).submit(eq(entry), anyLong(), eq(timedTrigger));
		} finally {
			timedTrigger.onShutdown();
		}
	}

	@Test
	public void onFail_AlreadyRunningNewTask() throws Exception {
		SchedulerEntry entry = createEntry(8L);
		SchedulerTask newTask = mock(SchedulerTask.class);
		when(newTask.getEntry()).then(a -> entry);
		when(newTask.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);

		when(scheduledExecutor.submit(any(), anyLong(), eq(timedTrigger))).then(a -> {
			SchedulerTask task = mock(SchedulerTask.class);
			when(task.getEntry()).thenReturn(a.getArgumentAt(0, SchedulerEntry.class));
			when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
			return task;
		});
		try {
			timedTrigger.onTask(mockMessage(entry));
			timedTrigger.onExecuteFail(newTask);

			verify(schedulerStore, never()).saveChanges(entry);
		} finally {
			timedTrigger.onShutdown();
		}
	}

	@Test
	public void onEntryChange() throws Exception {
		SchedulerEntry entry = createEntry(9L);
		SchedulerTask newTask = mock(SchedulerTask.class);
		when(newTask.getEntry()).then(a -> entry);
		when(newTask.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);

		when(scheduledExecutor.submit(any(), anyLong(), eq(timedTrigger))).then(a -> {
			SchedulerTask task = mock(SchedulerTask.class);
			when(task.getEntry()).thenReturn(a.getArgumentAt(0, SchedulerEntry.class));
			when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
			return task;
		});
		try {
			timedTrigger.onTask(mockMessage(entry));
			timedTrigger.onTask(mockMessage(entry));
			verify(scheduledExecutor).submit(any(), anyLong(), any());
		} finally {
			timedTrigger.onShutdown();
		}
	}

	@Test
	public void maxElementsInGroup() throws Exception {

		when(scheduledExecutor.submit(any(), anyLong(), eq(timedTrigger))).then(a -> {
			SchedulerTask task = mock(SchedulerTask.class);
			when(task.getEntry()).thenReturn(a.getArgumentAt(0, SchedulerEntry.class));
			when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
			return task;
		});

		reset(dbDao, mainSchedulerTrigger);
		doAnswer(a -> {
			SchedulerEntry entry1 = createEntry(9L);
			when(entry1.getConfiguration().getGroup()).thenReturn("testGroup");
			when(entry1.getConfiguration().getMaxActivePerGroup()).thenReturn(1);
			assertTrue(a.getArgumentAt(3, Predicate.class).test(entry1));
			a.getArgumentAt(4, Consumer.class).accept(entry1);

			SchedulerEntry entry2 = createEntry(10L);
			when(entry2.getConfiguration().getGroup()).thenReturn("testGroup");
			when(entry2.getConfiguration().getMaxActivePerGroup()).thenReturn(1);
			assertFalse(a.getArgumentAt(3, Predicate.class).test(entry2));

			return null;
		}).when(dbDao).getTasksForExecution(anySet(), anySet(), anyLong(), any(), any());

		// the default mock will trigger double task scheduling
		when(mainSchedulerTrigger.scheduleMainChecker(any(), anyLong(), any())).then(a -> {
			a.getArgumentAt(0, Executable.class).execute();
			return mock(Future.class);});

		timedTrigger.checkAndScheduleTimedTasks();

		Map<String, Integer> activeGroupsCount = timedTrigger.getActiveGroupsCount();
		assertNotNull(activeGroupsCount);
		assertTrue(activeGroupsCount.containsKey("testGroup"));
		assertEquals(Integer.valueOf(1), activeGroupsCount.get("testGroup"));
	}

	@Test(expected = EmfRuntimeException.class)
	public void invalidSecurityContext() throws Exception {
		reset(securityContext);
		when(securityContext.isActive()).thenReturn(Boolean.FALSE);

		timedTrigger.onTimeout(mock(SchedulerTask.class));
	}

	@Test
	public void afterShutdown() throws Exception {
		timedTrigger.onShutdown();

		timedTrigger.onExecuteCanceled(null);
		timedTrigger.onExecuteFail(null);
		timedTrigger.onExecuteSuccess(null);
		timedTrigger.onTimeout(null);
	}

	@Test
	public void should_stopTasks_onTenantRemove() {
		SchedulerTask runningTask = mock(SchedulerTask.class);
		runningTasks.put("testId", runningTask);

		timedTrigger.onTenantRemove();

		assertEquals(0, runningTasks.size());
		assertEquals(0, activateGroups.size());
		verify(runningTask, times(1)).cancel();
	}
	
	private void mockRunTaskFailed() {
		when(schedulerExecuter.execute(any(), eq(false), eq(false)))
				.thenReturn(new SimpleFuture<>(SchedulerEntryStatus.FAILED));

		when(scheduledExecutor.submit(any(), anyLong(), eq(timedTrigger))).then(a -> {
			SchedulerTask task = mock(SchedulerTask.class);
			SchedulerEntry entry = a.getArgumentAt(0, SchedulerEntry.class);
			entry.setStatus(SchedulerEntryStatus.RUN_WITH_ERROR);
			when(task.getEntry()).thenReturn(entry);
			when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUN_WITH_ERROR);
			SchedulerTaskCallback callback = a.getArgumentAt(2, SchedulerTaskCallback.class);
			callback.onTimeout(task);
			return task;
		});
	}

	private void mockRunTaskCanceled() {
		when(scheduledExecutor.submit(any(), anyLong(), eq(timedTrigger))).then(a -> {
			SchedulerTask task = mock(SchedulerTask.class);
			SchedulerEntry entry = a.getArgumentAt(0, SchedulerEntry.class);
			when(task.getEntry()).thenReturn(entry);
			when(task.getStatus()).thenReturn(SchedulerEntryStatus.CANCELED);
			SchedulerTaskCallback callback = a.getArgumentAt(2, SchedulerTaskCallback.class);
			callback.onExecuteCanceled(task);
			return task;
		});
	}

	@After
	public void cleanup() throws Exception {
		timedTrigger.onShutdown();
	}
}
