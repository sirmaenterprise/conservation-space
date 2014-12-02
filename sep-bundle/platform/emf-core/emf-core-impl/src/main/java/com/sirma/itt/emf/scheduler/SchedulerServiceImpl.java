/*
 *
 */
package com.sirma.itt.emf.scheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.BaseEntityLookupDao;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.entity.SerializableValue;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader.BatchPrimaryKeyEntityLoaderCallback;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.scheduler.entity.EventTriggerEntity;
import com.sirma.itt.emf.scheduler.entity.SchedulerEntity;
import com.sirma.itt.emf.scheduler.event.SchedulerEntryAddedEvent;
import com.sirma.itt.emf.scheduler.event.SchedulerEntryUpdatedEvent;
import com.sirma.itt.emf.sequence.SequenceGeneratorService;
import com.sirma.itt.emf.serialization.SerializationUtil;
import com.sirma.itt.emf.serialization.kryo.KryoSerializationEngine;
import com.sirma.itt.emf.util.CDI;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.ReflectionUtils;

/**
 * Default scheduler service implementation.
 *
 * @author BBonev
 */
@Stateless
@DependsOn("TimedScheduleExecutor")
public class SchedulerServiceImpl implements SchedulerService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerServiceImpl.class);

	/**
	 * Empty context instance used when saving new actions without context. We can use static
	 * constant because we serialize the context before save and deserialize after load so it no
	 * problem with multiple use.
	 */
	private static final SchedulerContext EMPTY_CONTEXT = new SchedulerContext();

	/** The Constant SCHEDULER_ACTION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 600000, interval = 720000), doc = @Documentation(""
			+ "Cache used to store the information for scheduled actions in the system."
			+ "For every action there are 2 entries in the cache. "
			+ "<br>Minimal value expression: users * 20"))
	private static final String SCHEDULER_ACTION_CACHE = "SCHEDULER_ACTION_CACHE";

	/** The Constant SCHEDULER_ACTION_EVENT_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 600000, interval = 720000), doc = @Documentation(""
			+ "Cache used to store the information for scheduled event actions in the system. The key for this cache is a schedule action event trigger and the value is the schedule action id."
			+ "For every event action there is an entry in the cache. "
			+ "<br>Minimal value expression: users * 20"))
	private static final String SCHEDULER_ACTION_EVENT_CACHE = "SCHEDULER_ACTION_EVENT_CACHE";

	/** The Constant SCHEDULER_ENTRY_SEQUENCE. */
	private static final String SCHEDULER_ENTRY_SEQUENCE = "SCHEDULER_ENTRY_SEQUENCE";

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;

	/** The scheduler executer. */
	@Inject
	private SchedulerExecuter schedulerExecuter;

	/** The bean manager. */
	@Inject
	private BeanManager beanManager;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The primary key entity loader callback. */
	private BatchPrimaryKeyEntityLoaderCallback<Long, SchedulerEntity> primaryKeyEntityLoaderCallback = new SchedulerEntityLoaderCallback();

	/** The generator service. */
	@Inject
	private SequenceGeneratorService generatorService;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		if (!cacheContext.containsCache(SCHEDULER_ACTION_CACHE)) {
			cacheContext.createCache(SCHEDULER_ACTION_CACHE, new SchedulerEntityLookup());
		}
		if (!cacheContext.containsCache(SCHEDULER_ACTION_EVENT_CACHE)) {
			cacheContext
					.createCache(SCHEDULER_ACTION_EVENT_CACHE, new SchedulerActionEventLookup());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SchedulerEntry schedule(Class<? extends SchedulerAction> action,
			SchedulerConfiguration configuration) {
		return scheduleInternal(action, null, configuration, EMPTY_CONTEXT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SchedulerEntry schedule(Class<? extends SchedulerAction> action,
			SchedulerConfiguration configuration, SchedulerContext context) {
		return scheduleInternal(action, null, configuration, context);
	}

	/**
	 * Schedule internal.
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
	private SchedulerEntry scheduleInternal(Class<? extends SchedulerAction> action,
			String actionName, SchedulerConfiguration configuration, SchedulerContext context) {
		if (((action == null) && StringUtils.isNullOrEmpty(actionName)) || (configuration == null)) {
			throw new EmfConfigurationException(
					"Action and scheduler configurations are required to schedule an action");
		}
		SchedulerEntryType type = configuration.getType();
		switch (type) {
			case EVENT:
				return scheduleEvent(action, actionName, configuration, context);
			case TIMED:
				return scheduleTimedEvent(action, actionName, configuration, context);
			case CRON:
				return scheduleCronEvent(action, actionName, configuration, context);
			default:
				throw new EmfRuntimeException("Not supported SchedulerConfigType " + type);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SchedulerEntry schedule(String actionName, SchedulerConfiguration configuration) {
		return scheduleInternal(null, actionName, configuration, EMPTY_CONTEXT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SchedulerEntry schedule(String actionName, SchedulerConfiguration configuration,
			SchedulerContext context) {
		return scheduleInternal(null, actionName, configuration, context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public SchedulerConfiguration buildEmptyConfiguration(SchedulerEntryType type) {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(type);
		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public SchedulerConfiguration buildConfiguration(EmfEvent event) {
		EventTrigger eventTrigger = buildEventTrigger(event);
		SchedulerConfiguration configuration = buildEmptyConfiguration(SchedulerEntryType.EVENT);
		configuration.setEventTrigger(eventTrigger);
		configuration.setSynchronous(true);
		configuration.setInSameTransaction(true);

		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public SchedulerConfiguration buildConfiguration(EmfEvent event, Class<?> targetClass) {
		SchedulerConfiguration configuration = buildConfiguration(event);
		configuration.getEventTrigger().setTargetInstanceClass(targetClass);
		configuration.getEventTrigger().setTargetInstanceIdentifier(null);
		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <I extends Instance> SchedulerConfiguration buildConfiguration(EmfEvent event, I instance) {
		SchedulerConfiguration configuration = buildConfiguration(event);
		if (instance != null) {
			configuration.getEventTrigger().setTargetInstanceClass(instance.getClass());
			configuration.getEventTrigger().setTargetInstanceIdentifier(instance.getId());
		}
		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public SchedulerAction getActionByName(String name) {
		if (StringUtils.isNotNullOrEmpty(name)) {
			try {
				return CDI.instantiateBean(name, SchedulerAction.class, beanManager);
			} catch (Exception e) {
				LOGGER.warn("Failed to initialize bean with name: {}", name, e);
			}
		}
		return null;
	}

	/**
	 * Schedule cron event.
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
	private SchedulerEntry scheduleCronEvent(Class<? extends SchedulerAction> action,
			String actionName, SchedulerConfiguration configuration, SchedulerContext context) {
		if (StringUtils.isNullOrEmpty(configuration.getCronExpression())) {
			throw new EmfConfigurationException(
					"Cannot schedule a timed cron action without setting the CRON expression: "
							+ configuration);
		}
		return convertToScheduleEntry(persistAction(action, actionName, configuration, context),
				false, true);
	}

	/**
	 * Schedule timed event.
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
	private SchedulerEntry scheduleTimedEvent(Class<? extends SchedulerAction> action,
			String actionName, SchedulerConfiguration configuration, SchedulerContext context) {
		if (configuration.getScheduleTime() == null) {
			throw new EmfConfigurationException(
					"Cannot schedule a timed action without confuguring execution time: "
							+ configuration);
		}
		return convertToScheduleEntry(persistAction(action, actionName, configuration, context),
				false, true);
	}

	/**
	 * Schedule event.
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
	private SchedulerEntry scheduleEvent(Class<? extends SchedulerAction> action,
			String actionName, SchedulerConfiguration configuration, SchedulerContext context) {
		EventTrigger eventTrigger = configuration.getEventTrigger();
		if (eventTrigger == null) {
			throw new EmfConfigurationException("Invalid schedule action configuration. "
					+ "Cannot schedule event action without SchedulerEventTrigger configuration!");
		}
		EventTriggerEntity triggerEntity = convertToTriggerEntity(eventTrigger);
		SchedulerEntity entity = getByEntryByEventTrigger(triggerEntity);

		if (entity == null) {
			// no action with the given trigger exists
			return convertToScheduleEntry(
					persistAction(action, actionName, configuration, context), false, true);
		}

		return null;
	}

	/**
	 * Creates new schedule entry form the given arguments and persist it. The method does not throw
	 * an exception but at least one of the arguments should be present: action class or action
	 * name!
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
	private SchedulerEntity persistAction(Class<? extends SchedulerAction> actionClass,
			String actionName, SchedulerConfiguration config, SchedulerContext context) {
		SchedulerEntity entity = createEntity(actionClass, actionName, config, context);

		SchedulerEntity update = dbDao.saveOrUpdate(entity);
		updateCache(update);

		// notify for new object we also instantiate the action class so if the action should be
		// executed now to be able to do so
		eventService.fire(new SchedulerEntryAddedEvent(convertToScheduleEntry(update, true, true)));
		return update;
	}

	/**
	 * Update cache.
	 *
	 * @param entity
	 *            the entity
	 */
	private void updateCache(SchedulerEntity entity) {
		getCache().setValue(entity.getId(), entity);
		if (entity.getEventTrigger() != null) {
			getEventCache().setValue(entity.getEventTrigger().clone(), entity.getId());
		}
	}

	/**
	 * Creates the entity from the given arguments. The method does not throw an exception but at
	 * least one of the arguments should be present: action class or action name!
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
	private SchedulerEntity createEntity(Class<? extends SchedulerAction> actionClass,
			String actionName, SchedulerConfiguration config, SchedulerContext context) {

		SchedulerEntity entity = createEntityAndSetIdentifier(config);

		entity.setType(config.getType());

		// set action and action name
		setActionName(actionClass, entity, actionName);

		Date nextScheduleTime = config.getNextScheduleTime();
		if (nextScheduleTime == null) {
			// if the time for execution has passed we should still be able to execute it at least
			// once
			nextScheduleTime = config.getScheduleTime();
		}
		entity.setNextScheduleTime(nextScheduleTime);

		EventTrigger eventTrigger = config.getEventTrigger();
		if (eventTrigger != null) {
			EventTriggerEntity triggerEntity = convertToTriggerEntity(eventTrigger);
			entity.setEventTrigger(triggerEntity);
		}

		setContextData(config, context, entity);
		return entity;
	}

	/**
	 * Sets the identifier.
	 * 
	 * @param config
	 *            the config
	 * @return the scheduler entity
	 */
	private SchedulerEntity createEntityAndSetIdentifier(SchedulerConfiguration config) {
		SchedulerEntity entity = new SchedulerEntity();
		if (StringUtils.isNullOrEmpty(config.getIdentifier())) {
			// generate identifier
			entity.setIdentifier(generateIdentifier());
		} else {
			// check if identifier-a already exists
			entity.setIdentifier(config.getIdentifier());
			// check using the cache
			Pair<Long, SchedulerEntity> pair = getCache().getByValue(entity);
			if (pair != null) {
				// we will merge the event
				entity = pair.getSecond();
				// reset status
				if ((entity.getStatus() == SchedulerEntryStatus.COMPLETED)
						|| (entity.getStatus() == SchedulerEntryStatus.FAILED)) {
					entity.setStatus(SchedulerEntryStatus.PENDING);
				}
			}
		}
		return entity;
	}

	/**
	 * Gets the action name.
	 *
	 * @param actionClass
	 *            the action class
	 * @param entity
	 *            the entity
	 * @param actionName
	 *            the action name
	 */
	private void setActionName(Class<? extends SchedulerAction> actionClass,
			SchedulerEntity entity, String actionName) {
		String name = actionName;
		if (actionClass != null) {
			entity.setActionClassId(convertToClassIndex(actionClass));
			if (StringUtils.isNullOrEmpty(name)) {
				// if name is not specified try to get the name from the call definition
				Named named = actionClass.getAnnotation(Named.class);
				// we try to get the name of the action event if we have the class because the
				// action could not be registered to Kryo and also the name is preferred when
				// instantiating the action bean
				if (named != null) {
					name = named.value();
					// in case of default name
					if (StringUtils.isNullOrEmpty(name)) {
						String simpleName = actionClass.getSimpleName();
						name = Character.toLowerCase(simpleName.charAt(0))
								+ simpleName.substring(1, simpleName.length());
					}
				}
			}
		}
		entity.setActionName(name);
	}

	/**
	 * Generate identifier.
	 *
	 * @return the string
	 */
	private String generateIdentifier() {
		return "" + generatorService.getNextId(SCHEDULER_ENTRY_SEQUENCE);
	}

	/**
	 * Sets the context and configuration data to the given entity.
	 * 
	 * @param config
	 *            the configuration
	 * @param context
	 *            the context
	 * @param entity
	 *            the target entity
	 */
	private void setContextData(SchedulerConfiguration config, SchedulerContext context,
			SchedulerEntity entity) {
		SchedulerData data = new SchedulerData(config, context);
		Serializable serializableValue = SerializationUtil.serialize(data, null);
		// reuse the context object entry
		if (entity.getContextData() == null) {
			entity.setContextData(new SerializableValue(serializableValue));
		} else {
			entity.getContextData().setSerializable(serializableValue);
		}
	}

	/**
	 * Convert to trigger entity.
	 *
	 * @param eventTrigger
	 *            the event trigger
	 * @return the event trigger entity
	 */
	private EventTriggerEntity convertToTriggerEntity(EventTrigger eventTrigger) {
		EventTriggerEntity triggerEntity = new EventTriggerEntity();
		// for optimization will not be moved to dozer for now
		// REVIEW: move to dozer mapping
		triggerEntity.setEventClassId(eventTrigger.getEventClassRegisterId());
		triggerEntity.setTargetClassId(eventTrigger.getTargetInstanceClassRegisterId());
		if (eventTrigger.getTargetInstanceIdentifier() != null) {
			triggerEntity.setTargetId(eventTrigger.getTargetInstanceIdentifier().toString());
		}
		triggerEntity.setOperation(eventTrigger.getOperation());
		return triggerEntity;
	}

	/**
	 * Convert the given class to class index using the kryo engine.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the class index for the given class
	 * @throws EmfConfigurationException
	 *             the emf configuration exception if the class is not registered
	 */
	protected Integer convertToClassIndex(Class<?> clazz) throws EmfConfigurationException {
		return KryoSerializationEngine.getClassRegistration(clazz);
	}

	/**
	 * Convert the given class index to class using the kryo engine.
	 *
	 * @param classIndex
	 *            the class index
	 * @return the class
	 * @throws EmfConfigurationException
	 *             the emf configuration exception if the class is not registered
	 */
	protected Class<?> convertToClass(int classIndex) throws EmfConfigurationException {
		Class<?> registeredClass = KryoSerializationEngine.getRegisteredClass(classIndex);
		if (registeredClass == null) {
			throw new EmfConfigurationException("The class index " + classIndex
					+ " is not registered in Kryo serialization engine.");
		}
		return registeredClass;
	}

	/**
	 * Gets the cache.
	 *
	 * @return the cache
	 */
	private EntityLookupCache<Long, SchedulerEntity, String> getCache() {
		return cacheContext.getCache(SCHEDULER_ACTION_CACHE);
	}

	/**
	 * Gets the event cache.
	 *
	 * @return the event cache
	 */
	private EntityLookupCache<EventTriggerEntity, Long, Serializable> getEventCache() {
		return cacheContext.getCache(SCHEDULER_ACTION_EVENT_CACHE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void onEvent(EmfEvent event) {
		EventTrigger trigger = buildEventTrigger(event);

		EventTriggerEntity triggerEntity = convertToTriggerEntity(trigger);
		SchedulerEntity schedulerEntity = getByEntryByEventTrigger(triggerEntity);
		if (schedulerEntity == null) {
			// no action scheduled for event
			return;
		}
		updateSchedulerEntryState(schedulerEntity, SchedulerEntryStatus.RUNNING);

		SchedulerEntry entry = convertToScheduleEntry(schedulerEntity, true, true);
		if (entry == null) {
			// probably the data was missing so why here on the first place??
			return;
		}
		schedulerExecuter.execute(entry);
	}

	/**
	 * Gets the by entry by event trigger.
	 *
	 * @param triggerEntity
	 *            the trigger entity
	 * @return the by entry by event trigger
	 */
	private SchedulerEntity getByEntryByEventTrigger(EventTriggerEntity triggerEntity) {
		Pair<EventTriggerEntity, Long> key = getEventCache().getByKey(triggerEntity);
		if (key != null) {
			Pair<Long, SchedulerEntity> pair = getCache().getByKey(key.getSecond());
			if (pair != null) {
				return pair.getSecond();
			}
		}
		return null;
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
	private SchedulerEntry convertToScheduleEntry(SchedulerEntity schedulerEntity,
			boolean instantiateAction, boolean failOnMissingAction) {
		SchedulerData data = extractSchedulerData(schedulerEntity);
		if (data == null) {
			// no configuration -- probably good idea to throw an exception
			return null;
		}
		SchedulerEntry entry = new SchedulerEntry();
		entry.setConfiguration(data.configuration);
		entry.setContext(data.context);
		entry.setId(schedulerEntity.getId());
		entry.setStatus(schedulerEntity.getStatus());
		entry.setIdentifier(schedulerEntity.getIdentifier());
		if (instantiateAction) {
			SchedulerAction action = instantiateAction(schedulerEntity);
			if (action == null) {
				if (failOnMissingAction) {
					throw new EmfConfigurationException("Action not found name="
							+ schedulerEntity.getActionName() + " actionId="
							+ schedulerEntity.getActionClassId());
				}
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
	private void updateSchedulerEntryState(SchedulerEntity schedulerEntity,
			SchedulerEntryStatus newState) {
		schedulerEntity.setStatus(newState);
		getCache().setValue(schedulerEntity.getId(), schedulerEntity);

		dbDao.saveOrUpdate(schedulerEntity);
	}

	/**
	 * Instantiate action bean and return it.
	 *
	 * @param schedulerEntity
	 *            the scheduler entity
	 * @return the scheduler action
	 */
	@SuppressWarnings("unchecked")
	private SchedulerAction instantiateAction(SchedulerEntity schedulerEntity) {
		SchedulerAction action = null;
		if (StringUtils.isNotNullOrEmpty(schedulerEntity.getActionName())) {
			action = getActionByName(schedulerEntity.getActionName());
		}
		if ((action == null) && (schedulerEntity.getActionClassId() != null)) {
			Class<? extends SchedulerAction> actionClass = (Class<? extends SchedulerAction>) convertToClass(schedulerEntity
					.getActionClassId());
			action = CDI.instantiateBean(actionClass, beanManager, CDI.getAnyLiteral());
		}
		return action;
	}

	/**
	 * Extract and deserialize scheduler data.
	 *
	 * @param schedulerEntity
	 *            the scheduler entity
	 * @return the scheduler data
	 */
	private SchedulerData extractSchedulerData(SchedulerEntity schedulerEntity) {
		SerializableValue data = schedulerEntity.getContextData();
		Serializable serializable = data.getSerializable();
		Object object = SerializationUtil.deserialize(serializable, null);
		if (object instanceof SchedulerData) {
			return (SchedulerData) object;
		}
		return null;
	}

	/**
	 * Builds the event trigger from the given event instance.
	 *
	 * @param event
	 *            the event
	 * @return the event trigger that matches the given event
	 */
	@SuppressWarnings("unchecked")
	private EventTrigger buildEventTrigger(EmfEvent event) {
		Class<? extends Instance> targetClass = null;
		Serializable targetId = null;
		String operation = "";
		if (event instanceof AbstractInstanceEvent) {
			Instance instance = ((AbstractInstanceEvent<? extends Instance>) event).getInstance();
			if (instance != null) {
				targetClass = instance.getClass();
				targetId = instance.getId();
			}
		}
		if (event instanceof OperationEvent) {
			operation = ((OperationEvent) event).getOperationId();
		}
		Class<? extends EmfEvent> eventClass = (Class<? extends EmfEvent>) ReflectionUtils
				.getWeldBeanClass(event);
		return new EventTrigger(eventClass, targetClass, targetId, operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SchedulerEntry save(SchedulerEntry entry) {
		if (entry == null) {
			return null;
		}
		if (entry.getId() == null) {
			// TODO: we could persist the action
			// persistAction(action, config, context)
		} else {
			Pair<Long, SchedulerEntity> pair = getCache().getByKey(entry.getId());
			if (pair != null) {
				SchedulerEntity entity = pair.getSecond();
				SchedulerEntry oldEntry = convertToScheduleEntry(entity, false, true);

				// if completed and marked for deletion we should delete the entry
				// if there is some other logic different that deletion it should come here
				if (isForRemoval(entry)) {
					getCache().removeByKey(entity.getId());
				} else {
					// copy status to cached value
					entity.setStatus(entry.getStatus());
					// update the current retry count
					if (entry.getConfiguration().getRetryCount() > 0) {
						entity.setRetries(entry.getConfiguration().getRetryCount());
					}

					Date nextScheduleTime = entry.getConfiguration().getNextScheduleTime();
					if (!EqualsHelper
							.nullSafeEquals(entity.getNextScheduleTime(), nextScheduleTime)) {
						entity.setNextScheduleTime(nextScheduleTime);
					}
					// update entry and store in db and update cache
					setContextData(entry.getConfiguration(), entry.getContext(), entity);

					// the DB save is performed by the cache callback
					getCache().updateValue(entity.getId(), entity);
				}

				// even if the entity is deleted in the DB we are still going to notify for the
				// state transition
				eventService.fire(new SchedulerEntryUpdatedEvent(entry, oldEntry));
			}
		}
		return entry;
	}

	/**
	 * Checks if the entry could be removed. This means to be completed and returns
	 * <code>true</code> from {@link SchedulerConfiguration#isRemoveOnSuccess()}.
	 *
	 * @param entry
	 *            the entry to check
	 * @return true, if the entry could be deleted
	 */
	private boolean isForRemoval(SchedulerEntry entry) {
		return (SchedulerEntryStatus.COMPLETED == entry.getStatus())
				&& entry.getConfiguration().isRemoveOnSuccess();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<SchedulerEntry> loadAndActivateByDbId(List<Long> ids) {
		return loadAndActivateTasksInternal(ids, true);
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
		List<SchedulerEntity> list = BatchEntityLoader.batchLoadByPrimaryKey(ids, getCache(),
				primaryKeyEntityLoaderCallback);

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<SchedulerEntry> loadByDbId(List<Long> ids) {
		return loadAndActivateTasksInternal(ids, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SchedulerEntry getScheduleEntry(String schedulerID) {
		SchedulerEntity value = new SchedulerEntity();
		value.setIdentifier(schedulerID);
		Pair<Long, SchedulerEntity> pair = getCache().getByValue(value);
		if (pair != null) {
			return convertToScheduleEntry(pair.getSecond(), false, true);
		}
		return null;
	}

	/**
	 * Batch entity loader implementation.
	 *
	 * @author BBonev
	 */
	private final class SchedulerEntityLoaderCallback implements
			BatchPrimaryKeyEntityLoaderCallback<Long, SchedulerEntity> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long getPrimaryKey(SchedulerEntity entity) {
			return entity.getId();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<SchedulerEntity> findEntitiesByPrimaryKey(Set<Long> secondPass) {
			List<Pair<String, Object>> args = new ArrayList<>(1);
			args.add(new Pair<String, Object>("ids", secondPass));
			return dbDao.fetchWithNamed(EmfQueries.QUERY_ALL_SCHEDULER_ENTRIES_BY_IDS_KEY, args);
		}
	}

	/**
	 * Entity lookup DAO for {@link SchedulerEntity}.
	 *
	 * @author BBonev
	 */
	public class SchedulerEntityLookup extends BaseEntityLookupDao<SchedulerEntity, String, Long> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Class<SchedulerEntity> getEntityClass() {
			return SchedulerEntity.class;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected DbDao getDbDao() {
			return dbDao;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String getValueKeyInternal(SchedulerEntity value) {
			return value.getIdentifier();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<SchedulerEntity> fetchEntityByValue(String key) {
			List<Pair<String, Object>> args = new ArrayList<>(1);
			args.add(new Pair<String, Object>("identifier", key));
			List<SchedulerEntity> list = dbDao.fetchWithNamed(
					EmfQueries.QUERY_SCHEDULER_ENTRY_BY_UID_KEY, args);
			return list;
		}
	}

	/**
	 * Entity lookup DAO that works with {@link EventTriggerEntity} to
	 * {@link SchedulerEntity#getId()}.
	 * 
	 * @author BBonev
	 */
	public class SchedulerActionEventLookup extends
			EntityLookupCallbackDAOAdaptor<EventTriggerEntity, Long, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<EventTriggerEntity, Long> findByKey(EventTriggerEntity key) {
			List<Pair<String, Object>> args = new ArrayList<>(4);
			String namedQuery = EmfQueries.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_KEY;
			args.add(new Pair<String, Object>("eventClassId", key.getEventClassId()));
			args.add(new Pair<String, Object>("targetClassId", key.getTargetClassId()));
			args.add(new Pair<String, Object>("targetId", key.getTargetId()));
			if (StringUtils.isNotNullOrEmpty(key.getOperation())) {
				args.add(new Pair<String, Object>("operation", key.getOperation()));
				namedQuery = EmfQueries.QUERY_SCHEDULER_ENTRY_ID_BY_EVENT_TRIGGER_OP_KEY;
			}
			List<Long> list = dbDao.fetchWithNamed(namedQuery, args);
			if (list.size() > 1) {
				// .. we have a problem here...
				LOGGER.warn("Found more then one Scheduler entry with same event trigger!!");
			}
			Long id = list.get(0);
			return new Pair<>(key, id);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<EventTriggerEntity, Long> createValue(Long value) {
			throw new UnsupportedOperationException("Cannot create entry using a event trigger");
		}

	}

	/**
	 * Wrapper object to store the action data.
	 *
	 * @author BBonev
	 */
	static class SchedulerData {

		/** Kryo engine index. */
		static final int CLASS_INDEX = 222;

		/** The configuration. */
		@Tag(1)
		protected SchedulerConfiguration configuration;

		/** The context. */
		@Tag(2)
		protected SchedulerContext context;

		/**
		 * Instantiates a new scheduler data.
		 */
		public SchedulerData() {
			// default constructor
		}

		/**
		 * Instantiates a new scheduler data.
		 *
		 * @param configuration
		 *            the configuration
		 * @param context
		 *            the context
		 */
		public SchedulerData(SchedulerConfiguration configuration, SchedulerContext context) {
			this.configuration = configuration;
			this.context = context;
		}
	}

}
