package com.sirma.itt.seip.tasks;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides scheduler entry tasks that need to be executed
 *
 * @author BBonev
 */
class SchedulerEntryProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private SchedulerDao dbDao;
	@Inject
	private SchedulerService schedulerService;

	/**
	 * Gets scheduler entries that need to be run in the time before the current date plus the given offset. The entries
	 * will also match by given types and statuses. Also must match the given filter for additional filtering.
	 *
	 * @param types
	 *            the scheduler types to match
	 * @param status
	 *            the scheduler statuses to match
	 * @param timeOffset
	 *            the time offset to be added based on the current time for the entries
	 * @param taskFilter
	 *            the task filter
	 * @param taskConsumer
	 *            the task consumer
	 */
	@SuppressWarnings("boxing")
	void getTasksForExecution(Set<SchedulerEntryType> types, Set<SchedulerEntryStatus> status, long timeOffset,
			Predicate<SchedulerEntry> taskFilter, Consumer<SchedulerEntry> taskConsumer) {
		List<SchedulerEntry> tasksForExecution;
		try {
			tasksForExecution = getTasksForExecution(types, status, timeOffset);
			LOGGER.trace("Collected {} tasks for execution", tasksForExecution.size());
		} catch (Exception e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Error when collecting tasks for execution: {}", e.getMessage(), e);
			} else {
				LOGGER.warn("Error when collecting tasks for execution: {}", e.getMessage());
			}
			return;
		}

		// the code is supposed to process task by task
		// this means that task must be consumed before the next task stars going down the stream
		// calling the task filter for the next task should happen after the previous task was consumed
		// this way restrictions on task could be applied effectively
		tasksForExecution
				.stream()
					.filter(taskFilter)
					.map(this::activateEntry)
					.filter(Objects::nonNull)
					.forEachOrdered(taskConsumer);
	}

	private SchedulerEntry activateEntry(SchedulerEntry taskToActivate) {
		try {
			return schedulerService.activate(taskToActivate.getId());
		} catch (RuntimeException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Could not add task {} to be executed due to: {}", taskToActivate.getIdentifier(),
						e.getMessage(), e);
			} else {
				LOGGER.warn("Could not add task {} to be executed due to: {}", taskToActivate.getIdentifier(),
						e.getMessage());
			}
		}
		return null;
	}

	private List<SchedulerEntry> getTasksForExecution(Set<SchedulerEntryType> types, Set<SchedulerEntryStatus> status,
			long timeDeviation) {
		Set<Long> set = dbDao.getTasksForExecution(types, status, new Date(System.currentTimeMillis() + timeDeviation));
		// load the actual entries and update their state right away
		return schedulerService.loadByDbId(new ArrayList<>(set));
	}
}
