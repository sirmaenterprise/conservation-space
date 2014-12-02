package com.sirma.itt.emf.scheduler;

import java.util.Date;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * The Class TaskExecutorTest.
 * 
 * @author BBonev
 */
@Test
public class TaskExecutorTest {

	/**
	 * Test executor call.
	 * 
	 * @throws InterruptedException
	 */
	public void testExecutorCall() throws InterruptedException {

		long fixedDelay = -1;
		SchedulerEntry entry = new SchedulerEntry();

		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(SchedulerEntryType.EVENT).setScheduleTime(new Date())
				.setMaxRetryCount(5).setIncrementalDelay(true).setRetryDelay(2L);

		SchedulerAction action = Mockito.mock(SchedulerAction.class);
		entry.setAction(action);

		entry.setId(1L);
		entry.setConfiguration(configuration);

		TimedScheduleExecutor executorService = createExecutor();
		Mockito.when(executorService.executeAction(Mockito.any(SchedulerEntry.class))).thenReturn(
				Boolean.TRUE);

		TaskExecutor executor = new TaskExecutor(entry, executorService, fixedDelay);

		executor.call();
		Assert.assertEquals(entry.getStatus(), SchedulerEntryStatus.COMPLETED);

		Mockito.when(executorService.executeAction(Mockito.any(SchedulerEntry.class))).thenReturn(
				Boolean.FALSE);

		executor = new TaskExecutor(entry, executorService, fixedDelay);

		executor.call();
		Assert.assertEquals(entry.getStatus(), SchedulerEntryStatus.RUN_WITH_ERROR);
	}

	/**
	 * Creates the executor.
	 * 
	 * @return the timed schedule executor
	 */
	private TimedScheduleExecutor createExecutor() {
		TimedScheduleExecutor executorService = Mockito.mock(TimedScheduleExecutor.class);
		Mockito.when(executorService.getDefaultRetryDelay()).thenReturn(60L);
		Mockito.when(executorService.getTimedExecutorDelay()).thenReturn(60L);
		return executorService;
	}

}
