package com.sirma.itt.seip.tasks;

import java.util.List;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Service provides means to schedule automatic actions for execution based on different triggers. The possible trigger
 * times are:
 * <ul>
 * <li>Timed - action scheduled to be executed on a given time. Based on the configuration object passed it could be
 * recurring or single executed action or with different time between execution. Depending on the returned value of
 * {@link SchedulerConfiguration#getNextScheduleTime()}.
 * <li>CRON expression - action scheduled based on a CRON expression string
 * <li>System event - action scheduled to be executed when a particular system event occurred over specific instance.
 * </ul>
 * The on schedule time the caller can provide arguments to the action that is going to be executed. The configuration
 * data, should be serializable via {@link com.sirma.itt.seip.serialization.kryo.KryoSerializationEngine}.
 * <p>
 * Action could be referenced by implementation of the interface {@link SchedulerAction} or by name of the named bean
 * that implements the same interface. If the class is used to schedule the action, then the action should be registered
 * in the {@link com.sirma.itt.seip.serialization.kryo.KryoSerializationEngine}.
 *
 * @author BBonev
 * @see SchedulerEntryType
 * @see SchedulerConfiguration
 * @see com.sirma.itt.seip.serialization.kryo.KryoSerializationEngine
 */
public interface SchedulerService {

	/**
	 * Schedules an action using it's class implementation. The action is scheduled using the given configuration. Empty
	 * context data will be passed to the action.
	 *
	 * @param action
	 *            the action
	 * @param configuration
	 *            the configuration
	 * @return created {@link SchedulerEntry}
	 */
	SchedulerEntry schedule(Class<? extends SchedulerAction> action, SchedulerConfiguration configuration);

	/**
	 * Schedules an action using it's class implementation. The action is scheduled using the given configuration. The
	 * given context data will be passed to the action.
	 *
	 * @param action
	 *            the action
	 * @param configuration
	 *            the configuration
	 * @param context
	 *            the context
	 * @return created {@link SchedulerEntry}
	 */
	SchedulerEntry schedule(Class<? extends SchedulerAction> action, SchedulerConfiguration configuration,
			SchedulerContext context);

	/**
	 * Schedules an action for execution using his name and the provided configuration.Empty context data will be passed
	 * to the action.
	 *
	 * @param actionName
	 *            the action name
	 * @param configuration
	 *            the configuration
	 * @return created {@link SchedulerEntry}
	 */
	SchedulerEntry schedule(String actionName, SchedulerConfiguration configuration);

	/**
	 * Schedules an action for execution using his name and the provided configuration.The given context data will be
	 * passed to the action.
	 *
	 * @param actionName
	 *            the action name
	 * @param configuration
	 *            the configuration
	 * @param context
	 *            the context
	 * @return created {@link SchedulerEntry}
	 */
	SchedulerEntry schedule(String actionName, SchedulerConfiguration configuration, SchedulerContext context);

	/**
	 * Reschedule execution using the given context. Before calling this method make sure to update the data in the
	 * context. <br>
	 * <b>NOTE:</b> this method could be called only from {@link SchedulerAction}. If called from any where else a
	 * runtime exception will be thrown.
	 *
	 * @param context
	 *            the updated context to reschedule.
	 */
	void reschedule(SchedulerContext context);

	/**
	 * Same as {@link #reschedule(SchedulerContext)} but uses custom configuration.
	 *
	 * @param context
	 *            the updated context to reschedule.
	 * @param config
	 *            custom new config
	 */
	void reschedule(SchedulerContext context, SchedulerConfiguration config);

	/**
	 * Builds the empty configuration instance using the default implementation and setting the configuration type.
	 *
	 * @param type
	 *            the type
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration buildEmptyConfiguration(SchedulerEntryType type);

	/**
	 * Builds a configuration instance using the given event instance. The returned configuration object will be of type
	 * event based for the given event and instance if provided by the event.
	 *
	 * @param event
	 *            the event
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration buildConfiguration(EmfEvent event);

	/**
	 * Builds a configuration instance of type {@link SchedulerEntryType#EVENT} that matches the given event instance
	 * and target class but no identifier is set.
	 *
	 * @param event
	 *            the event
	 * @param semanticTargetInstance
	 *            the semantic instance target class
	 * @return the scheduler configuration
	 */
	SchedulerConfiguration buildConfiguration(EmfEvent event, String semanticTargetInstance);

	/**
	 * Builds a configuration instance that matches the given event instance and the given concrete {@link Instance}
	 * implementation.
	 *
	 * @param <I>
	 *            the instance type to match
	 * @param event
	 *            the event
	 * @param instance
	 *            the instance
	 * @return the scheduler configuration
	 */
	<I extends Instance> SchedulerConfiguration buildConfiguration(EmfEvent event, I instance);

	/**
	 * Gets the action instance by name.
	 *
	 * @param name
	 *            the name
	 * @return the action by name
	 */
	SchedulerAction getActionByName(String name);

	/**
	 * The method notifies the service that the given event is occurred and to check if action should be executed or
	 * not.
	 *
	 * @param event
	 *            the event that occurred.
	 */
	void onEvent(EmfEvent event);

	/**
	 * Save or updates the given schedule entry
	 *
	 * @param entry
	 *            the entry
	 * @return the scheduler entry
	 */
	SchedulerEntry save(SchedulerEntry entry);

	/**
	 * Loads a schedule instance entries by identifiers.
	 *
	 * @param ids
	 *            the ids
	 * @return the list of found entries
	 */
	List<SchedulerEntry> loadByDbId(List<Long> ids);

	/**
	 * Fetch all {@link SchedulerEntry} with the given status.
	 * 
	 * @param status
	 *            the statusSchedulerService
	 * @return the entries
	 */
	List<SchedulerEntry> loadByStatus(SchedulerEntryStatus status);

	/**
	 * Gets a schedule entry that has been created using the given identifier.
	 *
	 * @param schedulerID
	 *            the scheduler id
	 * @return the schedule entry or <code>null</code> if no entry is found with the given identifier.
	 */
	SchedulerEntry getScheduleEntry(String schedulerID);

	/**
	 * Load and activate schedule instance entry by database identifier. The activation means that the status is changed
	 * to {@link SchedulerEntryStatus#RUNNING} before returning from the method. This should be called when the entries
	 * are loaded for execution.
	 *
	 * @param entryId
	 *            id to load and activate
	 * @return <code>null</code> if not found or the loaded, activated and ready for execution entry.
	 */
	SchedulerEntry activate(Long entryId);

	/**
	 * Validate the entry by trying to load the {@link SchedulerAction} linked to it. If an
	 * exception occurs, invalidate the entry and return false. Otherwise return true.
	 * 
	 * @param entry
	 *            the entry
	 * @return true if valid, false otherwise
	 */
	boolean validate(SchedulerEntry entry);

}
