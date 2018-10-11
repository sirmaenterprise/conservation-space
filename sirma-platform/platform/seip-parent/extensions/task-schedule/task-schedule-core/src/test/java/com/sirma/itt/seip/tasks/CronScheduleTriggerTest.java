package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.concurrent.SimpleFuture;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link CronScheduleTrigger}
 *
 * @author BBonev
 */
@SuppressWarnings("boxing")
public class CronScheduleTriggerTest {

	@InjectMocks
	private CronScheduleTrigger cronTrigger;

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
	@Mock
	private SchedulerService schedulerService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		cronTrigger.initialize();
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
	
	/**
	 * Test method onCronTaskUpdate scenario with cron timer test just invocation of transactionSupport method.
	 */
	@Test
	public void testOnCronTaskUpdateCronTask() throws JMSException {

		SchedulerEntry entry = Mockito.mock(SchedulerEntry.class);

		SchedulerConfiguration newEntryConfiguration = Mockito.mock(SchedulerConfiguration.class);
		when(newEntryConfiguration.getType()).thenReturn(SchedulerEntryType.CRON);
		when(newEntryConfiguration.getCronExpression()).thenReturn("cron_expression_value");
		when(entry.getConfiguration()).thenReturn(newEntryConfiguration);

		when(entry.getIdentifier()).thenReturn("testId");
		SchedulerTask oldSchedulerTask = Mockito.mock(SchedulerTask.class);
		when(oldSchedulerTask.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
		when(oldSchedulerTask.getEntry()).thenReturn(entry);
		mockRunTaskSuccessfully();

		runningTasks.put("testId", oldSchedulerTask);

		//execution of tested method
		cronTrigger.onTask(mockMessage(entry));
	}

	private Message mockMessage(SchedulerEntry entry) throws JMSException {
		Message message = mock(Message.class);
		when(message.getStringProperty("time")).thenReturn("2017-12-15T00:00:00.000Z");
		when(message.getLongProperty("id")).thenReturn(1L);
		when(schedulerService.activate(1L)).thenReturn(entry);
		return message;
	}

	@Test
	public void testOnUpdateEvent_Completed_None_Running() throws JMSException {
		SchedulerEntry newEntry = Mockito.mock(SchedulerEntry.class);
		when(newEntry.getStatus()).thenReturn(SchedulerEntryStatus.COMPLETED);
		SchedulerConfiguration newEntryConfiguration = Mockito.mock(SchedulerConfiguration.class);
		when(newEntryConfiguration.getType()).thenReturn(SchedulerEntryType.CRON);
		when(newEntryConfiguration.getCronExpression()).thenReturn("cron_expression_value");
		when(newEntry.getConfiguration()).thenReturn(newEntryConfiguration);

		when(newEntry.getIdentifier()).thenReturn("testId");
		SchedulerTask oldSchedulerTask = Mockito.mock(SchedulerTask.class);
		when(oldSchedulerTask.getStatus()).thenReturn(SchedulerEntryStatus.COMPLETED);
		runningTasks.put("testId", oldSchedulerTask);

		//execution of tested method
		cronTrigger.onTask(mockMessage(newEntry));

		//verification
		verify(runningTasks).remove("testId");
		verify(oldSchedulerTask).cancel();
	}

	@Test
	public void testOnUpdateEvent_Completed_Old_Cancel() throws JMSException {
		SchedulerEntry newEntry = Mockito.mock(SchedulerEntry.class);
		when(newEntry.getStatus()).thenReturn(SchedulerEntryStatus.COMPLETED);
		SchedulerConfiguration newEntryConfiguration = Mockito.mock(SchedulerConfiguration.class);
		when(newEntryConfiguration.getType()).thenReturn(SchedulerEntryType.CRON);
		when(newEntryConfiguration.getCronExpression()).thenReturn("cron_expression_value");
		when(newEntry.getConfiguration()).thenReturn(newEntryConfiguration);

		when(newEntry.getIdentifier()).thenReturn("testId");
		SchedulerTask oldSchedulerTask = Mockito.mock(SchedulerTask.class);
		when(oldSchedulerTask.getStatus()).thenReturn(SchedulerEntryStatus.COMPLETED);
		runningTasks.put("testId", oldSchedulerTask);

		//execution of tested method
		cronTrigger.onTask(mockMessage(newEntry));

		//verification
		verify(runningTasks).remove("testId");
	}

	@Test
	public void testScheduleTask_Already_Running() {
		SchedulerEntry newEntry = Mockito.mock(SchedulerEntry.class);
		SchedulerContext schedulerContext = mock(SchedulerContext.class);
		when(newEntry.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
		when(newEntry.getIdentifier()).thenReturn("testId");
		SchedulerConfiguration newEntryConfiguration = Mockito.mock(SchedulerConfiguration.class);
		when(newEntryConfiguration.getType()).thenReturn(SchedulerEntryType.CRON);
		when(newEntryConfiguration.getCronExpression()).thenReturn("cron_expression_value");
		when(newEntry.getConfiguration()).thenReturn(newEntryConfiguration);
		when(newEntry.getContext()).thenReturn(schedulerContext);

		SchedulerTask oldSchedulerTask = Mockito.mock(SchedulerTask.class);
		when(oldSchedulerTask.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
		when(oldSchedulerTask.getEntry()).thenReturn(newEntry);
		runningTasks.put("testId", oldSchedulerTask);

		cronTrigger.scheduleTask(newEntry);

		verify(scheduledExecutor, never()).submit(any(), anyLong(), eq(cronTrigger));
	}

	@Test
	public void testScheduleTask_Cancelled() {
		SchedulerEntry newEntry = Mockito.mock(SchedulerEntry.class);
		SchedulerContext schedulerContext = mock(SchedulerContext.class);
		when(newEntry.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
		when(newEntry.getIdentifier()).thenReturn("testId");
		SchedulerConfiguration newEntryConfiguration = Mockito.mock(SchedulerConfiguration.class);
		when(newEntryConfiguration.getType()).thenReturn(SchedulerEntryType.CRON);
		when(newEntryConfiguration.getCronExpression()).thenReturn("cron_expression_value");
		when(newEntry.getConfiguration()).thenReturn(newEntryConfiguration);
		when(newEntry.getContext()).thenReturn(schedulerContext);

		SchedulerTask oldSchedulerTask = Mockito.mock(SchedulerTask.class);
		when(oldSchedulerTask.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
		when(oldSchedulerTask.getEntry()).thenReturn(newEntry);
		when(oldSchedulerTask.isCanceled()).thenReturn(Boolean.TRUE);
		runningTasks.put("testId", oldSchedulerTask);

		mockRunTaskSuccessfully();

		cronTrigger.scheduleTask(newEntry);

		verify(schedulerStore).saveChanges(any());
	}

	@Test
	public void testOnUpdateEvent_Running() throws JMSException {
		SchedulerEntry entry = Mockito.mock(SchedulerEntry.class);
		when(entry.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
		SchedulerConfiguration newEntryConfiguration = Mockito.mock(SchedulerConfiguration.class);
		when(newEntryConfiguration.getType()).thenReturn(SchedulerEntryType.CRON);
		when(newEntryConfiguration.getCronExpression()).thenReturn("cron_expression_value");
		when(entry.getConfiguration()).thenReturn(newEntryConfiguration);

		when(entry.getIdentifier()).thenReturn("testId");
		SchedulerTask schedulerTask = Mockito.mock(SchedulerTask.class);
		when(schedulerTask.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
		when(schedulerTask.getEntry()).thenReturn(entry);
		runningTasks.put("testId", schedulerTask);
		mockRunTaskSuccessfully();

		//execution of tested method
		cronTrigger.onTask(mockMessage(entry));

		//verification
		verify(schedulerStore).saveChanges(entry);
	}

	@Test
	public void testScheduleCoreTasks() throws Exception {
		mockRunTaskSuccessfully();

		cronTrigger.checkAndScheduleCoreTasks();
		verify(dbDao, times(1)).getTasksForExecution(anySet(), anySet(), anyLong(), any(), any());
		verify(schedulerStore, atLeast(3)).saveChanges(any());
	}

	@Test
	public void testScheduleTenantTasks_canceled() throws Exception {
		mockRunTaskFailed();

		cronTrigger.checkAndScheduleTenantTasks();
		verify(dbDao, times(1)).getTasksForExecution(anySet(), anySet(), anyLong(), any(), any());
		verify(schedulerStore, atLeast(3)).saveChanges(any());
	}

	@Test
	public void onCronTaskAdded() throws Exception {
		SchedulerEntry entry = createEntry(4L);
		when(entry.getConfiguration().getCronExpression()).thenReturn("* * * * * *");

		mockRunTaskSuccessfully();

		cronTrigger.onTask(mockMessage(entry));

		verify(schedulerStore).saveChanges(any());
	}

	@Test
	public void onCronTaskAdded_RemoveCompleted() throws Exception {
		Long identifier = 4L;
		SchedulerEntry entry = createEntry(identifier);
		entry.setStatus(SchedulerEntryStatus.COMPLETED);
		when(entry.getConfiguration().getCronExpression()).thenReturn("* * * * * *");
		SchedulerTask schedulerTask = mock(SchedulerTask.class);
		when(schedulerTask.getStatus()).thenReturn(SchedulerEntryStatus.COMPLETED);
		runningTasks.put(String.valueOf(identifier), schedulerTask);
		mockRunTaskSuccessfully();
		cronTrigger.onTask(mockMessage(entry));

		verify(runningTasks).remove(String.valueOf(identifier));
		verify(schedulerTask).cancel();
	}

	@Test
	public void onCompletedCronTaskAdded() throws Exception {
		SchedulerEntry entry = createEntry(4L);
		when(entry.getConfiguration().getCronExpression()).thenReturn("* * * * * *");
		entry.setStatus(SchedulerEntryStatus.COMPLETED);

		mockRunTaskSuccessfully();

		cronTrigger.onTask(mockMessage(entry));

		verify(schedulerStore, never()).saveChanges(any());
	}

	@Test
	public void onCronTaskAdded_whenShutdown() throws Exception {
		cronTrigger.onShutdown();

		SchedulerEntry entry = createEntry(4L);
		when(entry.getConfiguration().getCronExpression()).thenReturn("* * * * * *");

		mockRunTaskSuccessfully();

		cronTrigger.onTask(mockMessage(entry));

		verify(schedulerStore, never()).saveChanges(any());
	}

	@Test
	public void should_stopTasks_onTenantRemove() {
		SchedulerTask runningTask = mock(SchedulerTask.class);
		runningTasks.put("testId", runningTask);

		cronTrigger.onTenantRemove();

		assertEquals(0, runningTasks.size());
		verify(runningTask, times(1)).cancel();
	}
	
	@Test
	public void onTaskCancel() throws Exception {
		SchedulerTask task = mock(SchedulerTask.class);
		when(task.getEntry()).then(a -> createEntry(5L));
		when(task.getStatus()).thenReturn(SchedulerEntryStatus.CANCELED);

		cronTrigger.onExecuteCanceled(task);
	}

	@Test
	public void onTaskFail() throws Exception {
		SchedulerTask task = mock(SchedulerTask.class);
		when(task.getEntry()).then(a -> createEntry(5L));
		when(task.getStatus()).thenReturn(SchedulerEntryStatus.CANCELED);

		cronTrigger.onExecuteFail(task);
	}

	private static SchedulerEntry createEntry(Long id) {
		SchedulerEntry entry = new SchedulerEntry();
		entry.setIdentifier(String.valueOf(id));
		entry.setId(id);
		entry.setAction(mock(SchedulerAction.class));
		SchedulerConfiguration configuration = mock(SchedulerConfiguration.class);
		when(configuration.getType()).thenReturn(SchedulerEntryType.CRON);
		when(configuration.getNextScheduleTime()).thenReturn(new Date());
		when(configuration.getIdentifier()).thenReturn(String.valueOf(id));
		entry.setConfiguration(configuration);
		entry.setStatus(SchedulerEntryStatus.RUNNING);
		return entry;
	}

	private void mockRunTaskSuccessfully() {
		when(schedulerExecuter.execute(any(), eq(false), eq(false)))
				.thenReturn(new SimpleFuture<>(SchedulerEntryStatus.COMPLETED));

		when(scheduledExecutor.submit(any(), anyLong(), eq(cronTrigger))).then(a -> {
			SchedulerTask task = mock(SchedulerTask.class);
			when(task.getEntry()).thenReturn(a.getArgumentAt(0, SchedulerEntry.class));
			when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUNNING);
			SchedulerTaskCallback callback = a.getArgumentAt(2, SchedulerTaskCallback.class);
			callback.onTimeout(task);
			callback.onExecuteSuccess(task);
			return task;
		});
	}

	private void mockRunTaskFailed() {
		when(schedulerExecuter.execute(any(), eq(false), eq(false)))
		.thenReturn(new SimpleFuture<>(SchedulerEntryStatus.FAILED));

		when(scheduledExecutor.submit(any(), anyLong(), eq(cronTrigger))).then(a -> {
			SchedulerTask task = mock(SchedulerTask.class);
			SchedulerEntry entry = a.getArgumentAt(0, SchedulerEntry.class);
			entry.setStatus(SchedulerEntryStatus.RUN_WITH_ERROR);
			when(task.getEntry()).thenReturn(entry);
			when(task.getStatus()).thenReturn(SchedulerEntryStatus.RUN_WITH_ERROR);
			SchedulerTaskCallback callback = a.getArgumentAt(2, SchedulerTaskCallback.class);
			callback.onTimeout(task);
			callback.onExecuteSuccess(task);
			return task;
		});
	}
}
