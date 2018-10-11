package com.sirma.itt.seip.tasks;

import java.util.Arrays;
import java.util.Collection;

/**
 * Defines the possible {@code com.sirma.itt.seip.tasks.SchedulerEntity} status.
 *
 * @author BBonev
 */
public enum SchedulerEntryStatus {

	/** The entry is not run, yet. */
	NOT_RUN,
	/** The entry is currently running or waiting on the queue for execution. */
	RUNNING,
	/** The entry has been run and completed successfully. */
	COMPLETED,
	/**
	 * The entry has been run but completed with error and there is a retry count scheduled so it
	 * scheduled for execution again.
	 */
	RUN_WITH_ERROR,
	/**
	 * The schedule entry was processed but there is still needed time for execution so it's
	 * rescheduled.
	 */
	PENDING,
	/** The failed to execute event after multiple retries. */
	FAILED,
	/** The scheduler entry has been canceled. */
	CANCELED,
	/** The skipped. */
	SKIPPED,
	/** The rollbacked. */
	ROLLBACKED,
	/** The rollback failed. */
	ROLLBACK_FAILED,
	/**
	 * The entry has reached it's max retries and has been marked as invalid during the nightly
	 * failed entries rescheduling (see FailedTaskRescheduler). Entries in this status must be
	 * handled manually. Most likely the action has been deleted while the entry has been scheduled.
	 */
	INVALID;

	public static final Collection<SchedulerEntryStatus> ACTIVE_STATES = Arrays.asList(NOT_RUN, PENDING, RUN_WITH_ERROR,
			RUNNING);

	public static final Collection<SchedulerEntryStatus> ALL_STATES = Arrays.asList(NOT_RUN, PENDING, RUN_WITH_ERROR,
			RUNNING, CANCELED, COMPLETED, FAILED, ROLLBACK_FAILED, ROLLBACKED, SKIPPED);
}
