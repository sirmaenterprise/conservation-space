package com.sirma.itt.seip.tasks;

import java.util.concurrent.Future;

/**
 * Service that handles the {@link SchedulerAction} executions.
 *
 * @author BBonev
 */
public interface SchedulerExecuter {

	/**
	 * Execute the action based on the given configuration.
	 *
	 * @param schedulerEntry
	 *            the scheduler entry
	 * @return a {@link Future} with scheduler entry status as a result: {@link SchedulerEntryStatus#COMPLETED} or
	 *         {@link SchedulerEntryStatus#FAILED} For non asynchronous operations the {@link Future#get()} will not
	 *         block and will return it's result immediately.
	 */
	default Future<SchedulerEntryStatus> execute(SchedulerEntry schedulerEntry) {
		return execute(schedulerEntry, true, true);
	}

	/**
	 * Execute the action based on the given configuration. If parameter {@code allowAsync} is <code>false</code> then
	 * the asynchronous events will be executed immediately. The difference between this method and
	 * {@link #executeImmediate(SchedulerEntry)} is that this method will read the configurations about security and
	 * transaction. This method is intended for invocation from timed executor.
	 *
	 * @param schedulerEntry
	 *            the scheduler entry and it's action that need to be executed in transaction or asynchronously based on
	 *            the it's configuration or the parameters
	 * @param allowAsync
	 *            If asynchronous operations are allowed to be executed asynchronously. This will override the entry
	 *            configuration {@link SchedulerConfiguration#isSynchronous()}
	 * @param allowPersist
	 *            If the method is allowed to perform a persist operation of the given entry after execution. If
	 *            <code>false</code> then the caller should take care of persisting the changes to the entry if needed.
	 * @return a {@link Future} with scheduler entry status as a result: {@link SchedulerEntryStatus#COMPLETED} or
	 *         {@link SchedulerEntryStatus#FAILED} For non asynchronous operations the {@link Future#get()} will not
	 *         block and will return it's result immediately.
	 */
	Future<SchedulerEntryStatus> execute(SchedulerEntry schedulerEntry, boolean allowAsync, boolean allowPersist);

	/**
	 * Execute immediate action ignoring the configuration. This method is called by the asynchronous observer.
	 *
	 * @param schedulerEntry
	 *            the scheduler entry
	 * @return <code>true</code>, if successfully executed the action.
	 */
	boolean executeImmediate(SchedulerEntry schedulerEntry);
}
