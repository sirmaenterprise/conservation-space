package com.sirma.itt.emf.scheduler;

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
	 */
	void execute(SchedulerEntry schedulerEntry);

	/**
	 * Execute immediate action ignoring the configuration. This method is called by the
	 * asynchronous observer.
	 * 
	 * @param schedulerEntry
	 *            the scheduler entry
	 * @return <code>true</code>, if successfully executed the action.
	 */
	boolean executeImmediate(SchedulerEntry schedulerEntry);
}
