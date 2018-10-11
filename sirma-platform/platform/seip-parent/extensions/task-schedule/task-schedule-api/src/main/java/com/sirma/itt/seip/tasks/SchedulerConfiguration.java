package com.sirma.itt.seip.tasks;

import java.io.Serializable;
import java.util.Date;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Defines a configuration for scheduler entry. The configuration could be one of the types
 * <ul>
 * <li>timed based - with defined exact execution time with or without repetitions
 * <li>cron job - defined using a CRON expression string for scheduling regular occurring tasks
 * <li>event based - based on an event in the system. Triggered by internal event that extends
 * {@link com.sirma.itt.seip.event.EmfEvent}
 * </ul>
 * <p>
 * An active security context should be present when scheduling operations. If nothing is specified than the current
 * security context will be used for the task execution. To change this behavior one of the methods
 * {@link #setRunAs(RunAs)} or {@link #setRunAs(String)} should be called. Note that both methods a mutually exclusive.
 *
 * @author BBonev
 */
public interface SchedulerConfiguration extends Identity, Serializable {

	/**
	 * Gets the type of the configuration that corresponds to the given instance. Should never be <code>null</code>.
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
	 * Gets the current retry count. If the action has not been failed then the method should return 0. On first fail
	 * the method should return 1 and so on.
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
	 * Gets the max allowed retry count. If the returned value is equal or less then 0 then on the first error the
	 * action will be marked as failed. If the returned value is greater than 1 then the executor will try to repeat the
	 * action at least maxRetryCount times until give up and mark the action as failed.
	 *
	 * @return the max retry count
	 */
	int getMaxRetryCount();

	/**
	 * Sets the max retry count for the executor before giving up on error while executing the action.
	 * <p>
	 * If not set the default value is 0.
	 *
	 * @param maxRetryCount
	 *            the max retry count
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setMaxRetryCount(int maxRetryCount);

	/**
	 * Gets initial schedule time. This is the time when the timed/cron actions should execute for the first time. This
	 * time should not change during the life cycle of the schedule action.
	 *
	 * @return the schedule time
	 */
	Date getScheduleTime();

	/**
	 * Sets the first scheduled execution time. This is required configuration for the timed actions.
	 *
	 * @param scheduleTime
	 *            the schedule time
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setScheduleTime(Date scheduleTime);

	/**
	 * Gets the next schedule time. This is the time for the next scheduled execution of the action. If the action has
	 * not been run, yet, then this is the same as the {@link #getScheduleTime()}. If the return value is
	 * <code>null</code> then the action will not execute again and will be marked as completed. The method should take
	 * into account {@link #getScheduleTime()}, {@link #getCronExpression()} (for CRON configurations only),
	 * {@link #getRetryCount()}, {@link #getRetryDelay()} and {@link #isIncrementalDelay()} when calculating the next
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
	 * Checks if the action should be executed synchronous or not. Default value is <code>true</code>.
	 * <p>
	 * <b>NOTE 1:</b> The timed and cron events are always executed as asynchronous operations. This is valid only for
	 * event operations and will be ignored for the other type of operations.<br>
	 * <b>NOTE 2:</b> The retries on an asynchronous operation will not be asynchronous but will be run on a single
	 * thread.
	 *
	 * @return true, if the execution should be synchronous and <code>false</code> for asynchronous.
	 */
	boolean isSynchronous();

	/**
	 * Sets the preferred execution context. If not set the execution will be synchronous (default value is
	 * <code>true</code>).
	 * <p>
	 * <b>NOTE 1:</b> The timed and cron events are always executed as asynchronous operations. This is valid only for
	 * event operations and will be ignored for the other type of operations.<br>
	 * <b>NOTE 2:</b> The retries on an asynchronous operation will not be asynchronous but will be run on a single
	 * thread.
	 *
	 * @param synchronous
	 *            the synchronous execution or not
	 * @return the scheduler configuration instance for method chaining
	 */
	SchedulerConfiguration setSynchronous(boolean synchronous);

	/**
	 * Gets the transaction mode in which the entity should be run.
	 *
	 * @return the transaction mode
	 */
	TransactionMode getTransactionMode();

	/**
	 * Sets the desired transaction mode in which the execution should occur. Note that asynchronous executions are run
	 * in new transaction so setting the mode to {@link TransactionMode#REQUIRED} or
	 * {@link TransactionMode#REQUIRES_NEW} does not change anything. If not set {@link TransactionMode#REQUIRED} will
	 * be used.
	 *
	 * @param transactionMode
	 *            the transaction mode to use when executing
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setTransactionMode(TransactionMode transactionMode);

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
	 * Checks if the scheduler action that is added for schedule should be persistent or not If not persistent when
	 * server is restarted the scheduled action will disappear. Default value is <code>true</code>.
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
	 * Checks if the delay is incremental or not. If <code>true</code> when calculating the delay between retries the
	 * delay time will be multiplied by the number of the current retry count. In other words the delay will grow with
	 * each retry.
	 *
	 * @return true, if is incremental delay
	 */
	boolean isIncrementalDelay();

	/**
	 * Sets the incremental delay. If <code>true</code> when calculating the delay between retries the delay time will
	 * be multiplied by the number of the current retry count. In other words the delay will grow with each retry.
	 *
	 * @param incremental
	 *            the incremental
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setIncrementalDelay(boolean incremental);

	/**
	 * Checks if scheduled entry should be deleted after successful completion. This option is ignored if the
	 * configuration is not persistent. Default value is <code>false</code>.
	 *
	 * @return true, if the entry should be removed on success and <code>false</code> to keep it.
	 */
	boolean isRemoveOnSuccess();

	/**
	 * Sets the remove on success. The option could be used to define configurations that does not require keeping track
	 * of persistent actions. It's useful when used with for actions that occur frequently.Default behavior is to keep
	 * all entries in the scheduled table.
	 * <p>
	 * Setting this option to <code>true</code> for a particular scheduler entry is that after completion there will be
	 * no record in the database for his execution and will be hard to debug.
	 * <p>
	 * Default value is <code>false</code>.
	 *
	 * @param removeOnSuccess
	 *            the remove on success
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setRemoveOnSuccess(boolean removeOnSuccess);

	/**
	 * Controls if the execution should continue no matter if the execution fails. This is useful from cron jobs where
	 * the execution should continue no matter if there is configured retry or not. This will be valid only if there is
	 * next schedule time. Default value if not set is <code>false</code>.
	 *
	 * @param continueOrError
	 *            the continue or error
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setContinueOnError(boolean continueOrError);

	/**
	 * Should continue on error. Checks if the execution should be rescheduled as normal on error.
	 *
	 * @return true, if successful
	 */
	boolean shouldContinueOnError();

	/**
	 * Gets the execution context.
	 *
	 * @return the execution context
	 * @see SchedulerConfiguration
	 * @see RunAs
	 */
	RunAs getRunAs();

	/**
	 * Sets the execution context using one of the predefined options.
	 *
	 * @param as
	 *            the as
	 * @return the scheduler configuration
	 * @see SchedulerConfiguration
	 * @see RunAs
	 */
	SchedulerConfiguration setRunAs(RunAs as);

	/**
	 * Sets the execution context for the given user The tenant is resolved based on the user that can be found in the
	 * system. Note that a {@link SecurityException} can be thrown if the tenant id of the specified user is different
	 * from the tenant id of the currently logged in user.
	 *
	 * @param systemId
	 *            the system id that identifies the user uniquely.
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setRunAs(String systemId);

	/**
	 * Gets the run user id.
	 *
	 * @return the run user id or <code>null</code> if not specified
	 */
	String getRunUserId();

	/**
	 * Gets the group identifier that can be used for grouping same or different actions using a particular resource
	 *
	 * @return the group identifier or <code>null</code> to indicate no grouping. Default value is <code>null</code>
	 * @see #setMaxActivePerGroup(String, int)
	 */
	String getGroup();

	/**
	 * Gets the number of maximum active instances.
	 *
	 * @return the max active instances. Zero or less than zero means unlimited. Default value is Zero
	 * @see #setMaxActivePerGroup(String, int)
	 */
	int getMaxActivePerGroup();

	/**
	 * Sets the group identifier and maximum active instances that can exists per node for the given action group. This
	 * is useful when one or more operations uses a limited resources and should restrict the maximum active instances
	 * of the job in the group. If multiple configurations register for the same group then the configuration with the
	 * higher limit will be used. For example:
	 * <ul>
	 * <li>Configuration 1 for group 1 has limit 10 and configuration 2 for group 1 has limit 5 then the used limit will
	 * be 10.
	 * <li>Configuration 1 for group 1 has limit 10 and configuration 2 for group 1 does not have a limit then the used
	 * limit will be 10.
	 * <li>Configuration 1 for group 1 has limit 10 and configuration 2 for group 2 has limit 5 then group 1 limit will
	 * be 10 and group 2 limit will be 5
	 * </ul>
	 *
	 * @param group
	 *            the group identifier
	 * @param maxActive
	 *            the max active if less than or equal to zero it will be considered as unlimited
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setMaxActivePerGroup(String group, int maxActive);

	/**
	 * Set the time zone id, used when calculating the next schedule time from cron expressions.
	 *
	 * @param timeZoneID
	 *            the ID for a TimeZone, either an abbreviation such as "PST", a full name such as "Europe/Sofia", or a
	 *            custom ID such as "GMT-8:00". Note that no daylight saving time transition schedule can be specified
	 *            with a custom time zone ID.
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration setTimeZoneID(String timeZoneID);

	/**
	 * Get the timezone id.
	 *
	 * @return the timezone id
	 */
	String getTimeZoneID();
}
