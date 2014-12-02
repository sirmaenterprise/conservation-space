package com.sirma.itt.emf.scheduler;


/**
 * Defines the possible {@link SchedulerEntity} status.
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
	ROLLBACK_FAILED;
}
