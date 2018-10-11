package com.sirma.itt.seip.tasks;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Message;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.event.AbstractInstanceEvent;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.serialization.SerializationHelper;
import com.sirma.itt.seip.serialization.kryo.KryoHelper;
import com.sirma.itt.seip.tasks.entity.EventTriggerEntity;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirmaenterprise.sep.jms.api.JmsMessageInitializer;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Default scheduler service implementation.
 *
 * @author BBonev
 */
@Transactional
public class SchedulerServiceImpl implements SchedulerService, Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Empty context instance used when saving new actions without context. We can use static constant because we
	 * serialize the context before save and deserialize after load so it no problem with multiple use.
	 */
	private static final SchedulerContext EMPTY_CONTEXT = new SchedulerContext();

	@Inject
	private SchedulerExecuter schedulerExecuter;
	@Inject
	private BeanManager beanManager;
	@Inject
	private transient SecurityValidator securityValidator;

	@Inject
	private transient KryoHelper kryoHelper;
	@Inject
	private transient SerializationHelper serializationHelper;

	@Inject
	private transient SchedulerEntryStore schedulerStore;

	@Inject
	private SenderService senderService;

	@Override
	public SchedulerEntry schedule(Class<? extends SchedulerAction> action, SchedulerConfiguration configuration) {
		return scheduleInternal(action, null, configuration, EMPTY_CONTEXT);
	}

	@Override
	public SchedulerEntry schedule(Class<? extends SchedulerAction> action, SchedulerConfiguration configuration,
			SchedulerContext context) {
		return scheduleInternal(action, null, configuration, context);
	}

	private SchedulerEntry scheduleInternal(Class<? extends SchedulerAction> action, String actionName,
			SchedulerConfiguration configuration, SchedulerContext context) {
		// check arguments
		checkRequiredDataToSchedule(action, actionName, configuration);

		SchedulerEntryType type = configuration.getType();
		switch (type) {
			case EVENT:
				return scheduleEvent(action, actionName, configuration, context);
			case TIMED:
				return scheduleTimedEvent(action, actionName, configuration, context);
			case CRON:
				return scheduleCronEvent(action, actionName, configuration, context);
			case IMMEDIATE:
				return scheduleImmediate(action, actionName, configuration, context);
			default:
				throw new EmfRuntimeException("Not supported SchedulerEntryType " + type);
		}
	}

	/**
	 * Schedule the action immediately
	 *
	 * @param action
	 *            the action
	 * @param actionName
	 *            the action name
	 * @param configuration
	 *            the configuration
	 * @param context
	 *            the context
	 * @return the scheduler entry
	 */
	private SchedulerEntry scheduleImmediate(Class<? extends SchedulerAction> action, String actionName,
			SchedulerConfiguration configuration, SchedulerContext context) {
		configuration.setType(
				configuration.getCronExpression() != null ? SchedulerEntryType.CRON : SchedulerEntryType.TIMED);
		// for non cron dates the schedule time is required in order for the retry mechanism to work
		if (configuration.getType() == SchedulerEntryType.TIMED && configuration.getScheduleTime() == null) {
			configuration.setScheduleTime(new Date());
		}
		SchedulerEntity entity = createEntity(action, actionName, configuration, context);
		if (configuration.isSynchronous()) {
			if (!schedulerExecuter.executeImmediate(convertToScheduleEntry(entity, true, true))) {
				throw new EmfApplicationException("Could not execute action " + (action == null ? actionName : action));
			}
		} else {
			// if not synchronized process normally
			return notifyForNewEntry(convertToScheduleEntry(persist(entity), true, true));
		}

		if (configuration.isPersistent()) {
			return scheduleImmediatePersistentEntity(configuration, entity);
		}
		// when non persistent just return
		return convertToScheduleEntry(entity, false, true);
	}

	private SchedulerEntry scheduleImmediatePersistentEntity(SchedulerConfiguration configuration,
			SchedulerEntity entity) {
		Date nextScheduleTime = configuration.getNextScheduleTime();
		if (nextScheduleTime != null && nextScheduleTime.getTime() > System.currentTimeMillis()) {
			// in the future
			return notifyForNewEntry(convertToScheduleEntry(persist(entity), false, true));
		}
		// if in the past(no more executions or not needed) and should be removed
		if (configuration.isRemoveOnSuccess()) {
			return convertToScheduleEntry(entity, false, true);
		}
		// otherwise persist and return
		return convertToScheduleEntry(persist(entity), false, true);
	}

	private void checkRequiredDataToSchedule(Class<? extends SchedulerAction> action, String actionName,
			SchedulerConfiguration configuration) {
		if (action == null && StringUtils.isBlank(actionName) || configuration == null) {
			throw new EmfConfigurationException(
					"Action and scheduler configurations are required to schedule an action");
		}
		securityValidator.onNewEntry(configuration);
	}

	@Override
	public SchedulerEntry schedule(String actionName, SchedulerConfiguration configuration) {
		return scheduleInternal(null, actionName, configuration, EMPTY_CONTEXT);
	}

	@Override
	public SchedulerEntry schedule(String actionName, SchedulerConfiguration configuration, SchedulerContext context) {
		return scheduleInternal(null, actionName, configuration, context);
	}

	@Override
	public SchedulerConfiguration buildEmptyConfiguration(SchedulerEntryType type) {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(type);
		return configuration;
	}

	@Override
	public SchedulerConfiguration buildConfiguration(EmfEvent event) {
		EventTrigger eventTrigger = buildEventTrigger(event);
		SchedulerConfiguration configuration = buildEmptyConfiguration(SchedulerEntryType.EVENT);
		configuration.setEventTrigger(eventTrigger);
		configuration.setSynchronous(true);
		configuration.setTransactionMode(TransactionMode.REQUIRED);
		return configuration;
	}

	@Override
	public SchedulerConfiguration buildConfiguration(EmfEvent event, String semanticTargetInstance) {
		SchedulerConfiguration configuration = buildConfiguration(event);
		configuration.getEventTrigger().setSemanticTargetInstanceClass(semanticTargetInstance);
		configuration.getEventTrigger().setTargetInstanceIdentifier(null);
		return configuration;
	}

	@Override
	public <I extends Instance> SchedulerConfiguration buildConfiguration(EmfEvent event, I instance) {
		SchedulerConfiguration configuration = buildConfiguration(event);
		if (instance != null) {
			configuration.getEventTrigger().setSemanticTargetInstanceClass(instance.type().getId().toString());
			configuration.getEventTrigger().setTargetInstanceIdentifier(instance.getId());
		}
		return configuration;
	}

	@Override
	public SchedulerAction getActionByName(String name) {
		return instantiateBeanByName(name);
	}

	private SchedulerAction instantiateBeanByName(String name) {
		if (StringUtils.isNotBlank(name)) {
			try {
				return CDI.instantiateBean(name, SchedulerAction.class, beanManager);
			} catch (Exception e) {
				LOGGER.warn("Failed to initialize bean with name: {}", name, e);
			}
		}
		return null;
	}

	private SchedulerEntry scheduleCronEvent(Class<? extends SchedulerAction> action, String actionName,
			SchedulerConfiguration configuration, SchedulerContext context) {
		if (StringUtils.isBlank(configuration.getCronExpression())) {
			throw new EmfConfigurationException(
					"Cannot schedule a timed cron action without setting the CRON expression: " + configuration);
		}
		return convertToScheduleEntry(persistAction(action, actionName, configuration, context), false, true);
	}

	private SchedulerEntry scheduleTimedEvent(Class<? extends SchedulerAction> action, String actionName,
			SchedulerConfiguration configuration, SchedulerContext context) {
		if (configuration.getScheduleTime() == null) {
			// the schedule time is required for timed actions
			configuration.setScheduleTime(new Date());
		}
		return convertToScheduleEntry(persistAction(action, actionName, configuration, context), false, true);
	}

	private SchedulerEntry scheduleEvent(Class<? extends SchedulerAction> action, String actionName,
			SchedulerConfiguration configuration, SchedulerContext context) {
		EventTrigger eventTrigger = configuration.getEventTrigger();
		if (eventTrigger == null) {
			throw new EmfConfigurationException("Invalid schedule action configuration. "
					+ "Cannot schedule event action without SchedulerEventTrigger configuration!");
		}
		EventTriggerEntity triggerEntity = convertToTriggerEntity(eventTrigger);
		SchedulerEntity entity = schedulerStore.getByEntryByEventTrigger(triggerEntity);

		if (entity == null) {
			// no action with the given trigger exists
			return convertToScheduleEntry(persistAction(action, actionName, configuration, context), false, true);
		}
		entity.setContextData(serializationHelper, configuration, context);
		return convertToScheduleEntry(persist(entity), false, true);
	}

	/**
	 * Creates new schedule entry form the given arguments and persist it. The method does not throw an exception but at
	 * least one of the arguments should be present: action class or action name!
	 *
	 * @param actionClass
	 *            the action
	 * @param actionName
	 *            the action name
	 * @param config
	 *            the configuration to use
	 * @param context
	 *            the context
	 * @return the scheduler entity
	 */
	private SchedulerEntity persistAction(Class<? extends SchedulerAction> actionClass, String actionName,
			SchedulerConfiguration config, SchedulerContext context) {
		SchedulerEntity entity = createEntity(actionClass, actionName, config, context);

		SchedulerEntity update = persist(entity);

		// notify for new object we also instantiate the action class so if the action should be
		// executed now to be able to do so
		notifyForNewEntry(convertToScheduleEntry(update, true, true));
		return update;
	}

	private SchedulerEntry notifyForNewEntry(SchedulerEntry schedulerEntry) {
		if (schedulerEntry == null) {
			return null;
		}
		LOGGER.trace("Sending notification for new scheduler entity action={}, id={}, context={}",
				schedulerEntry.getActionName(), schedulerEntry.getId(), schedulerEntry.getContext());
		senderService.send(SchedulerQueues.TASK_QUEUE, buildMessage(schedulerEntry), SendOptions.create().asTenantAdmin());
		return schedulerEntry;
	}

	private JmsMessageInitializer<Message> buildMessage(SchedulerEntry schedulerEntry) {
		return message -> {
			message.setLongProperty("id", schedulerEntry.getId());
			Date executionTime = schedulerEntry.getExpectedExecutionTime();
			if (executionTime == null) {
				// the execution time has passed, so assign now to executed ASAP
				executionTime = new Date();
			}
			message.setStringProperty("time", ISO8601DateFormat.format(executionTime));
			message.setStringProperty("type", schedulerEntry.getConfiguration().getType().toString());
		};
	}

	/**
	 * Persist the given entity and update cache
	 *
	 * @param entity
	 *            the entity
	 * @return the scheduler entity
	 */
	private SchedulerEntity persist(SchedulerEntity entity) {
		securityValidator.checkOnPersistNew(entity);
		return schedulerStore.persist(entity);
	}

	/**
	 * Creates the entity from the given arguments. The method does not throw an exception but at least one of the
	 * arguments should be present: action class or action name!
	 *
	 * @param actionClass
	 *            the action class
	 * @param actionName
	 *            the action name
	 * @param config
	 *            the configuration passed by user
	 * @param context
	 *            the context to be passed to the action upon execution.
	 * @return the created scheduler entity
	 */
	private SchedulerEntity createEntity(Class<? extends SchedulerAction> actionClass, String actionName,
			SchedulerConfiguration config, SchedulerContext context) {

		SchedulerEntity entity = schedulerStore.getOrCreateEntityForIdentifier(config.getIdentifier());

		entity.setType(config.getType());

		// set action and action name
		setActionName(actionClass, entity, actionName);

		Date nextScheduleTime = config.getNextScheduleTime();
		if (nextScheduleTime == null) {
			// if the time for execution has passed we should still be able to execute it at least once
			nextScheduleTime = config.getScheduleTime();
		}
		entity.setNextScheduleTime(nextScheduleTime);

		EventTrigger eventTrigger = config.getEventTrigger();
		if (eventTrigger != null) {
			EventTriggerEntity triggerEntity = convertToTriggerEntity(eventTrigger);
			entity.setEventTrigger(triggerEntity);
		}

		entity.setContextData(serializationHelper, config, context);
		return entity;
	}

	private void setActionName(Class<? extends SchedulerAction> actionClass, SchedulerEntity entity,
			String actionName) {
		String name = actionName;
		if (actionClass != null) {
			entity.setActionClassId(convertToClassIndex(actionClass));
			if (StringUtils.isBlank(name)) {
				// if name is not specified try to get the name from the call definition
				Named named = actionClass.getAnnotation(Named.class);
				// we try to get the name of the action event if we have the class because the
				// action could not be registered to Kryo and also the name is preferred when
				// instantiating the action bean
				if (named != null) {
					name = named.value();
					// in case of default name
				}
				if (StringUtils.isBlank(name)) {
					name = getCdiNameFromClass(actionClass);
				}
			}
		}
		entity.setActionName(name);
	}

	/**
	 * Gets the cdi name from class.
	 *
	 * @param actionClass
	 *            the action class
	 * @return the cdi name from class
	 */
	private static String getCdiNameFromClass(Class<? extends SchedulerAction> actionClass) {
		String simpleName = actionClass.getSimpleName();
		return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1, simpleName.length());
	}

	/**
	 * Convert to trigger entity.
	 *
	 * @param eventTrigger
	 *            the event trigger
	 * @return the event trigger entity
	 */
	private static EventTriggerEntity convertToTriggerEntity(EventTrigger eventTrigger) {
		EventTriggerEntity triggerEntity = new EventTriggerEntity();
		// for optimization will not be moved to dozer for now
		triggerEntity.setEventClassId(eventTrigger.getEventClassRegisterId());
		triggerEntity.setSemanticTargetClass(eventTrigger.getTargetSemanticInstanceClass());
		if (eventTrigger.getTargetInstanceIdentifier() != null) {
			triggerEntity.setTargetId(eventTrigger.getTargetInstanceIdentifier().toString());
		}
		triggerEntity.setServerOperation(eventTrigger.getServerOperation());
		triggerEntity.setUserOperation(eventTrigger.getUserOperation());
		return triggerEntity;
	}

	/**
	 * Convert the given class to class index using the kryo engine.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the class index for the given class
	 */
	private Integer convertToClassIndex(Class<?> clazz) {
		return kryoHelper.getClassRegistration(clazz);
	}

	/**
	 * Convert the given class index to class using the kryo engine.
	 *
	 * @param classIndex
	 *            the class index
	 * @return the class
	 */
	private Class<?> convertToClass(int classIndex) {
		Class<?> registeredClass = kryoHelper.getRegisteredClass(classIndex);
		if (registeredClass == null) {
			throw new EmfConfigurationException(
					"The class index " + classIndex + " is not registered in Kryo serialization engine.");
		}
		return registeredClass;
	}

	/**
	 * Convert the given class index to {@link SchedulerAction} implementation class.
	 *
	 * @param classIndex
	 *            the class index
	 * @return the class that implements scheduler action or <code>null</code> if the index is <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends SchedulerAction> convertToActionClass(Integer classIndex) {
		if (classIndex != null) {
			return (Class<? extends SchedulerAction>) convertToClass(classIndex);
		}
		return null;
	}

	@Override
	public void onEvent(EmfEvent event) {
		EventTrigger trigger = buildEventTrigger(event);

		EventTriggerEntity triggerEntity = convertToTriggerEntity(trigger);
		SchedulerEntity schedulerEntity = schedulerStore.getByEntryByEventTrigger(triggerEntity);
		if (schedulerEntity == null) {
			// no action scheduled for event
			LOGGER.trace("Could not get scheduler entry by the given event trigger {}. Task will not be executed.",
						event.getClass().getName());
			return;
		}
		updateSchedulerEntryState(schedulerEntity, SchedulerEntryStatus.RUNNING);

		SchedulerEntry entry = convertToScheduleEntry(schedulerEntity, true, true);
		if (entry == null) {
			// probably the data was missing so why here on the first place??
			return;
		}
		LOGGER.info("Notifying that the event {} has occurred. Triggering the schedule task",
					event.getClass().getName());
		schedulerExecuter.execute(entry);
	}

	/**
	 * Convert to schedule entry and instantiate the action if requested.
	 *
	 * @param schedulerEntity
	 *            the scheduler entity
	 * @param instantiateAction
	 *            the instantiate action
	 * @param failOnMissingAction
	 *            if the method should fail it the action is was not found or <code>null</code>
	 * @return the scheduler entry
	 */
	private SchedulerEntry convertToScheduleEntry(SchedulerEntity schedulerEntity, boolean instantiateAction,
			boolean failOnMissingAction) {
		SchedulerEntry entry = schedulerEntity.toSchedulerEntry(serializationHelper);
		if (entry == null) {
			return null;
		}
		if (instantiateAction) {
			SchedulerAction action = instantiateAction(schedulerEntity);
			if (action == null) {
				if (failOnMissingAction) {
					throw new EmfConfigurationException("Action not found name=" + schedulerEntity.getActionName()
							+ " actionId=" + schedulerEntity.getActionClassId());
				}
				LOGGER.warn("Action not found: name={}, actionId={}", schedulerEntity.getActionName(),
						schedulerEntity.getActionClassId());
				return null;
			}
			entry.setAction(action);
		}
		return entry;
	}

	/**
	 * Update scheduler entry state.
	 *
	 * @param schedulerEntity
	 *            the scheduler entity
	 * @param newState
	 *            the new state
	 */
	private void updateSchedulerEntryState(SchedulerEntity schedulerEntity, SchedulerEntryStatus newState) {
		schedulerEntity.setStatus(newState);
		schedulerStore.persist(schedulerEntity);
	}

	/**
	 * Instantiate action bean and return it.
	 *
	 * @param schedulerEntity
	 *            the scheduler entity
	 * @return the scheduler action
	 */
	private SchedulerAction instantiateAction(SchedulerEntity schedulerEntity) {
		SchedulerAction action = null;
		if (StringUtils.isNotBlank(schedulerEntity.getActionName())) {
			action = instantiateBeanByName(schedulerEntity.getActionName());
		}
		if (action == null && schedulerEntity.getActionClassId() != null) {
			Class<? extends SchedulerAction> actionClass = convertToActionClass(schedulerEntity.getActionClassId());
			action = CDI.instantiateBean(actionClass, beanManager, CDI.getQualifers(actionClass, beanManager));
		}
		return action;
	}

	/**
	 * Builds the event trigger from the given event instance.
	 *
	 * @param event
	 *            the event
	 * @return the event trigger that matches the given event
	 */
	@SuppressWarnings("unchecked")
	private static EventTrigger buildEventTrigger(EmfEvent event) {
		String targetClass = null;
		Serializable targetId = null;
		String serverOperation = "";
		String userOperation = "";
		Object instance = null;
		if (event instanceof AbstractInstanceEvent) {
			instance = ((AbstractInstanceEvent<?>) event).getInstance();
		} else if (event instanceof AbstractInstanceTwoPhaseEvent) {
			instance = ((AbstractInstanceTwoPhaseEvent<?, ?>) event).getInstance();
		}
		if (instance instanceof Instance) {
			targetClass = ((Instance) instance).type().getId().toString();
			targetId = ((Instance) instance).getId();
		} else if (instance instanceof InstanceReference) {
			targetClass = ((InstanceReference) instance).getType().getId().toString();
			targetId = ((InstanceReference) instance).getId();
		}
		if (event instanceof OperationEvent) {
			serverOperation = ((OperationEvent) event).getOperationId();
			Operation operationObj = ((OperationEvent) event).getOperation();
			if (operationObj != null) {
				userOperation = operationObj.getUserOperationId();
			}
		}
		Class<? extends EmfEvent> eventClass = (Class<? extends EmfEvent>) ReflectionUtils.getWeldBeanClass(event);
		return new EventTrigger(eventClass, targetClass, targetId, serverOperation, userOperation);
	}

	@Override
	@Transactional(TxType.SUPPORTS)
	public void reschedule(SchedulerContext context) {
		SchedulerEntry entity = context.getIfSameType(SchedulerContext.SCHEDULER_ENTRY, SchedulerEntry.class);
		if (entity == null) {
			throw new EmfRuntimeException("Cannot reschedule if not called from SchedulerAction!");
		}
		reschedule(context, entity.getConfiguration());
	}

	@Override
	@Transactional(TxType.SUPPORTS)
	public void reschedule(SchedulerContext context, SchedulerConfiguration config) {
		SchedulerEntry entity = context.getIfSameType(SchedulerContext.SCHEDULER_ENTRY, SchedulerEntry.class);
		if (entity == null) {
			throw new EmfRuntimeException("Cannot reschedule if not called from SchedulerAction!");
		}
		// create a copy of the context but remove the current schedule entry not to be persisted
		context.remove(SchedulerContext.SCHEDULER_ENTRY);
		SchedulerEntity schedulerEntity = schedulerStore.findById(entity.getId());
		scheduleInternal(convertToActionClass(schedulerEntity.getActionClassId()), schedulerEntity.getActionName(),
				config, new SchedulerContext(context));
	}

	@Override
	@Transactional
	public SchedulerEntry save(SchedulerEntry entry) {
		if (entry == null) {
			return null;
		}
		if (entry.getId() == null) {
			// this method should not be called directly to save new entry but via schedule method
			return entry;
		}

		securityValidator.checkOnSave(entry);

		if (schedulerStore.saveChanges(entry)) {
			notifyForNewEntry(entry);
		}
		return entry;
	}

	/**
	 * Load and activate tasks internal.
	 *
	 * @param ids
	 *            the ids
	 * @param activate
	 *            the activate
	 * @return the list
	 */
	private List<SchedulerEntry> loadAndActivateTasksInternal(List<Long> ids, boolean activate) {
		List<SchedulerEntity> list = schedulerStore.findEntities(ids);

		List<SchedulerEntry> result = new ArrayList<>(list.size());
		for (SchedulerEntity entity : list) {
			if (activate) {
				updateSchedulerEntryState(entity, SchedulerEntryStatus.RUNNING);
			}
			SchedulerEntry entry = convertToScheduleEntry(entity, true, false);
			if (entry != null) {
				result.add(entry);
			}
		}
		return result;
	}

	@Override
	@Transactional
	public SchedulerEntry activate(Long entryId) {
		SchedulerEntity entity = schedulerStore.findById(entryId);
		if (entity == null) {
			return null;
		}

		updateSchedulerEntryState(entity, SchedulerEntryStatus.RUNNING);
		return convertToScheduleEntry(entity, true, true);
	}

	@Override
	public List<SchedulerEntry> loadByDbId(List<Long> ids) {
		return loadAndActivateTasksInternal(ids, false);
	}

	@Override
	public SchedulerEntry getScheduleEntry(String schedulerID) {
		SchedulerEntity value = schedulerStore.findByIdentifier(schedulerID);
		if (value != null) {
			return convertToScheduleEntry(value, false, true);
		}
		return null;
	}

	@Override
	public List<SchedulerEntry> loadByStatus(SchedulerEntryStatus status) {
		List<SchedulerEntity> entities = schedulerStore.findByStatus(status);
		return entities.stream().map(entity -> convertToScheduleEntry(entity, false, false)).collect(
				Collectors.toList());
	}

	@Override
	public boolean validate(SchedulerEntry entry) {
		try {
			convertToScheduleEntry(schedulerStore.findById(entry.getId()), true, true);
			return true;
		} catch (Exception e) {
			LOGGER.warn("Scheduler entry {} can't be rescheduled and will be invalidated.", entry.getId());
			LOGGER.trace("Scheduler entry {} can't be rescheduled and will be invalidated.", entry.getId(), e);
			entry.setStatus(SchedulerEntryStatus.INVALID);
			schedulerStore.saveChanges(entry);
			return false;
		}
	}
}
