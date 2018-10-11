package com.sirma.itt.seip.tasks;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test the failed task rescheduling.
 * 
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class FailedTaskReschedulerTest {

	@Mock
	private SchedulerService schedulerService;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@InjectMocks
	private FailedTaskRescheduler taskRescheduler;

	@Test
	public void should_reschedule_failed_tasks() {
		SchedulerEntry failedEntry = getSchedulerEntry();
		mockSchedulerService(failedEntry, true);

		taskRescheduler.rescheduleAllTenantTasks();
		Assert.assertNotNull(failedEntry.getExpectedExecutionTime());
		Assert.assertEquals(SchedulerEntryStatus.NOT_RUN, failedEntry.getStatus());
		Assert.assertEquals(0, failedEntry.getConfiguration().getRetryCount());
	}

	@Test
	public void should_not_reschedule_invalid_tasks() {
		SchedulerEntry failedEntry = getSchedulerEntry();
		mockSchedulerService(failedEntry, false);

		taskRescheduler.reschedule();
		Assert.assertEquals(SchedulerEntryStatus.FAILED, failedEntry.getStatus());
	}

	private static SchedulerEntry getSchedulerEntry() {
		SchedulerEntry failedEntry = new SchedulerEntry();
		failedEntry.setStatus(SchedulerEntryStatus.FAILED);
		failedEntry.setConfiguration(new DefaultSchedulerConfiguration());
		return failedEntry;
	}

	private void mockSchedulerService(SchedulerEntry failedEntry, boolean isValid) {
		Mockito.when(schedulerService.validate(Matchers.any(SchedulerEntry.class))).thenReturn(isValid);
		Mockito.when(schedulerService.loadByStatus(SchedulerEntryStatus.FAILED)).thenReturn(Arrays.asList(failedEntry));
	}
}
