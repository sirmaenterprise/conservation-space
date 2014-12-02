package com.sirma.itt.emf.scheduler;

import java.util.Date;

import com.sirma.itt.emf.domain.model.Identity;

/**
 * Defines a configuration for scheduler entry. The configuration could be one of the types
 * <ul>
 * <li>timed based - with defined exact execution time with or without repetitions
 * <li>cron job - defined using a CRON expression string for scheduling regular occurring tasks
 * <li>event based - based on an event in the system. Triggered by internal event that extends
 * {@link com.sirma.itt.emf.event.EmfEvent}
 * </ul>
 *
 * @author BBonev
 */
public interface SchedulerConfiguration extends Identity {

	/**
	 * Gets the type of the configuration that corresponds to the given instance. Should never be
	 * <code>null</code>.
	 *
	 * @return the type
	 */
	SchedulerEntryType getType();

	/**
	 * Sets the scheduler entry type.
	 *
	 * @param type
	 *            the type
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setType(SchedulerEntryType type);

	/**
	 * Gets the current retry count. If the action has not been failed then the method should return
	 * 0. On first fail the method should return 1 and so on.
	 *
	 * @return the retry count
	 */
	int getRetryCount();

	/**
	 * Updates the current retry count.
	 *
	 * @param retryCount
	 *            the retry count
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setRetryCount(int retryCount);

	/**
	 * Gets the max allowed retry count. If the returned value is equal or less then 0 then on the
	 * first error the action will be marked as failed. If the returned value is greater than 1 then
	 * the executor will try to repeat the action at least maxRetryCount times until give up and
	 * mark the action as failed.
	 *
	 * @return the max retry count
	 */
	int getMaxRetryCount();

	/**
	 * Sets the max retry count for the executor before giving up on error while executing the
	 * action.
	 * <p>
	 * If not set the default value is 0.
	 *
	 * @param maxRetryCount
	 *            the max retry count
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setMaxRetryCount(int maxRetryCount);

	/**
	 * Gets initial schedule time. This is the time when the timed/cron actions should execute for
	 * the first time. This time should not change during the life cycle of the schedule action.
	 *
	 * @return the schedule time
	 */
	Date getScheduleTime();

	/**
	 * Sets the first scheduled execution time. This is required configuration for the timed
	 * actions.
	 *
	 * @param scheduleTime
	 *            the schedule time
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setScheduleTime(Date scheduleTime);

	/**
	 * Gets the next schedule time. This is the time for the next scheduled execution of the action.
	 * If the action has not been run, yet, then this is the same as the {@link #getScheduleTime()}.
	 * If the return value is <code>null</code> then the action will not execute again and will be
	 * marked as completed. The method should take into account {@link #getScheduleTime()},
	 * {@link #getCronExpression()} (for CRON configurations only), {@link #getRetryCount()},
	 * {@link #getRetryDelay()} and {@link #isIncrementalDelay()} when calculating the next
	 * execution time.
	 * 
	 * @return the next schedule time or <code>null</code> if not needed.
	 */
	Date getNextScheduleTime();

	/**
	 * Gets the cron expression used to configure the timed event if any.
	 *
	 * @return the cron expression
	 */
	String getCronExpression();

	/**
	 * Sets the cron expression to be used as a trigger for the CRON configured events
	 *
	 * @param cronExpression
	 *            the cron expression
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setCronExpression(String cronExpression);

	/**
	 * Checks if the action should be executed synchronous or not. Default value is
	 * <code>true</code>.
	 * <p>
	 * <b>NOTE 1:</b> The timed and cron events are always executed as asynchronous operations. This
	 * is valid only for event operations and will be ignored for the other type of operations.<br>
	 * <b>NOTE 2:</b> The retries on an asynchronous operation will not be asynchronous but will be
	 * run on a single thread.
	 * 
	 * @return true, if the execution should be synchronous and <code>false</code> for asynchronous.
	 */
	boolean isSynchronous();

	/**
	 * Sets the preferred execution context. If not set the execution will be synchronous (default
	 * value is <code>true</code>).
	 * 
	 * @param synchronous
	 *            the synchronous execution or not
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setSynchronous(boolean synchronous);

	/**
	 * Is the execution should be performed in the same transaction or in a separate for synchronous
	 * executions. <br>
	 * If executed in the same transaction and an error occur and while executing then the current
	 * transaction will be rolled back. <br>
	 * Default value is <code>true</code>.
	 * <p>
	 * REVIEW:BB What about in no transaction?
	 * 
	 * @return true, if should be executed in the same transaction and <code>false</code> to be
	 *         executed in a separate transaction.
	 * @see #isSynchronous()
	 */
	boolean inSameTransaction();

	/**
	 * Sets the preferred execution transaction context. Default value is <code>true</code> so it
	 * will be executed in the current transaction.
	 * 
	 * @param inSameTx
	 *            the in same transaction
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setInSameTransaction(boolean inSameTx);

	/**
	 * Gets the event trigger configuration.
	 *
	 * @return the event trigger
	 */
	EventTrigger getEventTrigger();

	/**
	 * Sets the event trigger configuration.
	 *
	 * @param trigger
	 *            the trigger
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setEventTrigger(EventTrigger trigger);

	/**
	 * Checks if the scheduler action that is added for schedule should be persistent or not If not
	 * persistent when server is restarted the scheduled action will disappear. Default value is
	 * <code>true</code>.
	 *
	 * @return <code>true</code>, if should be persisted
	 */
	boolean isPersistent();

	/**
	 * Sets the persistent scheduler action.
	 *
	 * @param persistent
	 *            the persistent
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setPersistent(boolean persistent);

	/**
	 * Get the delay for failed operation retry. Should be in seconds. By default is null
	 *
	 * @return the retry delay
	 */
	Long getRetryDelay();

	/**
	 * Updates the delay to the specified time in seconds.
	 * 
	 * @param delay
	 *            is the new time to set
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setRetryDelay(Long delay);

	/**
	 * Checks if the delay is incremental or not. If <code>true</code> when calculating the delay
	 * between retries the delay time will be multiplied by the number of the current retry count.
	 * In other words the delay will grow with each retry.
	 * 
	 * @return true, if is incremental delay
	 */
	boolean isIncrementalDelay();

	/**
	 * Sets the incremental delay. If <code>true</code> when calculating the delay between retries
	 * the delay time will be multiplied by the number of the current retry count. In other words
	 * the delay will grow with each retry.
	 * 
	 * @param incremental
	 *            the incremental
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setIncrementalDelay(boolean incremental);

	/**
	 * Checks if scheduled entry should be deleted after successful completion. This option is
	 * ignored if the configuration is not persistent. Default value is <code>false</code>.
	 * 
	 * @return true, if the entry should be removed on success and <code>false</code> to keep it.
	 */
	boolean isRemoveOnSuccess();

	/**
	 * Sets the remove on success. The option could be used to define configurations that does not
	 * require keeping track of persistent actions. It's useful when used with for actions that
	 * occur frequently.Default behavior is to keep all entries in the scheduled table.
	 * <p>
	 * Setting this option to <code>true</code> for a particular scheduler entry is that after
	 * completion there will be no record in the database for his execution and will be hard to
	 * debug.
	 * <p>
	 * Default value is <code>false</code>.
	 * 
	 * @param removeOnSuccess
	 *            the remove on success
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setRemoveOnSuccess(boolean removeOnSuccess);
}
