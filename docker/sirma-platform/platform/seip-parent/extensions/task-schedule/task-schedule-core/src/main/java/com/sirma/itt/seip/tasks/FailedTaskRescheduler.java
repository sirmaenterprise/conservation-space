package com.sirma.itt.seip.tasks;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * A failed task rescheduler responsible for rescheduling failed scheduler tasks on a given period
 * of time.
 * 
 * @author nvelkov
 */
@ApplicationScoped
public class FailedTaskRescheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String RESCHEDULE_CRON = "0 0 0 1/1 * ? *";

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Reschedule the failed system tenant tasks.
	 */
	@Schedule(expression = RESCHEDULE_CRON)
	public void reschedule() {
		rescheduleTasks();
	}

	/**
	 * Reschedule all tenants failed tasks.
	 */
	@Schedule(expression = RESCHEDULE_CRON, system = false)
	public void rescheduleAllTenantTasks() {
		rescheduleTasks();
	}

	/**
	 * Split the failed scheduler tasks in batches and update them.
	 */
	private void rescheduleTasks() {
		LOGGER.info("Going to reschedule failed tasks.");
		List<SchedulerEntry> failedEntries = schedulerService.loadByStatus(SchedulerEntryStatus.FAILED);
		FragmentedWork.doWork(failedEntries, 1024,
				tasks -> transactionSupport.invokeConsumerInNewTx(this::changeForReschedulingAndSave, tasks));
		LOGGER.info("Finished failed task rescheduling. {} Entries updated.", failedEntries.size());
	}

	/**
	 * Change the scheduler entries' date, status and retries so they can be rescheduled by the
	 * scheduler.
	 */
	private void changeForReschedulingAndSave(Collection<SchedulerEntry> entries) {
		for (SchedulerEntry entry : entries) {
			if (schedulerService.validate(entry)) {
				entry.setStatus(SchedulerEntryStatus.NOT_RUN);
				entry.setExpectedExecutionTime(new Date());
				entry.getConfiguration().setRetryCount(0);
				schedulerService.save(entry);
			}
		}
	}
}
