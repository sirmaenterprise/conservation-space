/*
 *
 */
package com.sirma.itt.emf.instance.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.cache.lookup.BaseEntityLookupDao;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.VersionableEntity;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionContextProperties;
import com.sirma.itt.emf.evaluation.ExpressionEvaluator;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.exceptions.StaleDataModificationException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.instance.model.TenantAware;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.ReflectionUtils;

/**
 * Common implementation logic for {@link InstanceDao} realizations.
 * 
 * @param <T>
 *            the instance type
 * @param <C>
 *            the entity type
 * @param <P>
 *            the primary key type
 * @param <K>
 *            the secondary key type
 * @param <D>
 *            the target definition type
 * @author BBonev
 */
@SuppressWarnings("rawtypes")
public abstract class BaseInstanceDaoImpl<T extends Instance, C extends Entity, P extends Serializable, K extends Serializable, D extends DefinitionModel>
		extends BaseInstanceDao<P, K> implements InstanceDao<T> {

	/** The entity cache context. */
	@Inject
	protected EntityLookupCacheContext entityCacheContext;

	/** The db dao. */
	@Inject
	protected DbDao dbDao;

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseInstanceDaoImpl.class);

	/** The debug. */
	private boolean debug;

	/** The trace. */
	boolean trace;

	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;

	/** The default container. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	protected String defaultContainer;

	/** The type converter. */
	@Inject
	protected TypeConverter typeConverter;

	/** The service register. */
	@Inject
	protected ServiceRegister serviceRegister;

	/** The event service. */
	@Inject
	protected EventService eventService;

	/** The instance service. */
	@Inject
	@Proxy
	protected InstanceService<Instance, DefinitionModel> instanceService;

	/**
	 * Initialize the cache and logger.
	 */
	@PostConstruct
	public void initialize() {
		String cacheName = getCacheEntityName();
		if (StringUtils.isNotNullOrEmpty(cacheName) && !entityCacheContext.containsCache(cacheName)) {
			entityCacheContext.createCache(cacheName, getEntityCacheProvider());
		}
		debug = LOGGER.isDebugEnabled();
		trace = LOGGER.isTraceEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public T createInstance(DefinitionModel definitionModel, boolean populateProperties) {
		T instance = createNewInstance();
		if ((definitionModel != null) && (getDefinitionClass() != null)
				&& getDefinitionClass().isInstance(definitionModel)) {
			instance.setIdentifier(definitionModel.getIdentifier());

			populateInstanceForModelInternal(instance, (D) definitionModel, populateProperties);
		}
		return instance;
	}

	            /**
	 * Notify for new instance.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void notifyForNewInstance(T instance) {
		// better not to fire an event for not initialized instance
		if ((instance == null) || (instance.getProperties() == null)) {
			return;
		}
		InstanceEventProvider<Instance> provider = serviceRegister.getEventProvider(instance);
		if (provider != null) {
			InstanceCreateEvent<Instance> event = provider.createCreateEvent(instance);
			if (event != null) {
				eventService.fire(event);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public T createInstance(String definitionId, Class<?> definitionModel,
			boolean populateProperties) {
		T instance = createNewInstance();

		if (definitionId != null) {
			instance.setIdentifier(definitionId);
			// if the instance does not support definitions
			Class<D> definitionClass = getDefinitionClass();
			if (definitionModel != null) {
				definitionClass = (Class<D>) definitionModel;
			}
			if (definitionClass != null) {
				D model = getInstanceDefinition(definitionId, definitionClass);
				populateInstanceForModelInternal(instance, model, populateProperties);
			}

		}
		return instance;
	}

	            /**
	 * Creates the new instance. If the primary type is of type String then new ID is generated
	 * before return.
	 * 
	 * @return a newly created instance.
	 */
	protected T createNewInstance() {
		T instance = ReflectionUtils.newInstance(getInstanceClass());
		// generate instance DB id
		if (getPrimaryIdType().equals(String.class)) {
			SequenceEntityGenerator.generateStringId(instance, true);
		}
		return instance;
	}

	            /**
	 * Update newly created instance with model. When all base logic is called then the methods
	 * {@link #synchRevisions(Instance, Long)} and
	 * {@link #populateInstanceForModel(Instance, DefinitionModel)} is called for concrete
	 * additional implementations to fill any other data.
	 * 
	 * @param instance
	 *            the instance
	 * @param model
	 *            the model
	 * @param populateProperties
	 *            the populate properties
	 */
	protected void populateInstanceForModelInternal(T instance, D model, boolean populateProperties) {
		if ((model != null) && populateProperties) {
			if (instance instanceof TenantAware) {
				if ((model instanceof TenantAware)
						&& (((TenantAware) model).getContainer() != null)) {
					((TenantAware) instance).setContainer(((TenantAware) model).getContainer());
				} else {
					// copy container ID
					String container = SecurityContextManager
							.getCurrentContainer(authenticationService);
					((TenantAware) instance).setContainer(container);
				}
			}
			synchRevisions(instance, model.getRevision());
			populateInstanceForModel(instance, model);
		}
	}

	            /**
	 * Gets the instance definition.
	 * 
	 * @param definitionId
	 *            the definition id
	 * @param definitionClass
	 *            the definition class
	 * @return the instance definition
	 */
	protected D getInstanceDefinition(String definitionId, Class<D> definitionClass) {
		return dictionaryService.getDefinition(definitionClass, definitionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends PropertyDefinition> void populateProperties(PropertyModel model,
			List<E> fields) {
		super.populateProperties(model, fields);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void populateProperties(PropertyModel model, DefinitionModel definitionModel) {
		if (definitionModel != null) {
			super.populateProperties(model, definitionModel.getFields());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void populateProperties(PropertyModel model, RegionDefinitionModel regionDefinitionModel) {
		if (regionDefinitionModel != null) {
			super.populateProperties(model, regionDefinitionModel.getFields());
			for (RegionDefinition regionDefinition : regionDefinitionModel.getRegions()) {
				super.populateProperties(model, regionDefinition.getFields());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void instanceUpdated(T instance, boolean autosave) {
		checkForStaleData(instance);

		onInstanceUpdated(instance);

		boolean auditDisabled = RuntimeConfiguration
				.isConfigurationSet(RuntimeConfigurationProperties.AUDIT_MODIFICATION_DISABLED);
		if (!auditDisabled) {
			updateCreatorAndModifierInfo(instance);
		}

		onAutoSave(instance, autosave);
	}

	            /**
	 * Update creator and modifier info. There is an option to disable setting of modifier info
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void updateCreatorAndModifierInfo(T instance) {
		if (RuntimeConfiguration.isSet(RuntimeConfigurationProperties.OVERRIDE_MODIFIER_INFO)) {
			// disable the default implementation and allow external setting of modifier info
			return;
		}
		// use one date for modified and created
		Date currentDate = new Date();
		updateCreatorInfo(instance, currentDate);

		updateModifierInfo(instance, currentDate);
	}

	/**
	 * Update modifier info.
	 * 
	 * @param instance
	 *            the instance
	 * @param currentDate
	 *            the current date
	 */
	protected void updateModifierInfo(T instance, Date currentDate) {
		// set modifier info
		setCurrentUserTo(instance, DefaultProperties.MODIFIED_BY);
		instance.getProperties().put(DefaultProperties.MODIFIED_ON, currentDate);
	}

	            /**
	 * Update creator info.
	 * 
	 * @param instance
	 *            the instance
	 * @param currentDate
	 *            the current date
	 */
	protected void updateCreatorInfo(T instance, Date currentDate) {
		// if already created does not set them again
		if (instance.getProperties().get(DefaultProperties.CREATED_ON) == null) {
			instance.getProperties().put(DefaultProperties.CREATED_ON, currentDate);
			setCurrentUserTo(instance, DefaultProperties.CREATED_BY);
		}
	}

	            /**
	 * On auto save.
	 * 
	 * @param instance
	 *            the instance
	 * @param autosave
	 *            the auto save
	 */
	protected void onAutoSave(T instance, boolean autosave) {
		// base implementation
		if (autosave) {
			persistChanges(instance);
		}
	}

	            /**
	 * Method call on every all of {@link #instanceUpdated(Instance, boolean)} right after the check
	 * for stale data.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void onInstanceUpdated(T instance) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isModified(T instance) {
		return isModifiedInternal(instance, false);
	}

	            /**
	 * Checks if the given instance is modified the method could optionally throw an exception if
	 * needed on detected modification instead of returning <code>true</code>.
	 * 
	 * @param instance
	 *            the instance
	 * @param throwException
	 *            the throw exception
	 * @return true, if is modified internal
	 * @throws StaleDataModificationException
	 * @see {@link #checkForStaleData(Instance)}
	 */
	protected boolean isModifiedInternal(T instance, boolean throwException)
			throws StaleDataModificationException {
		// get the current (old) timestamp
		Date currentModifiedOn = getModifiedDate(instance.getProperties());
		// get from the DB the latest known properties
		Pair<Date, Serializable> pair = getLatestKnownModifiedDateFor(instance);
		Date oldModifiedOn = pair.getFirst();

		// if we already have a modified date and the incoming instance does not have or is the
		// cache entry is newer that the incoming modified date then we have race condition.

		if ((oldModifiedOn != null)
				&& ((currentModifiedOn == null) || (oldModifiedOn.getTime() > currentModifiedOn
						.getTime()))) {
			String message = "Trying to save " + instance.getClass().getSimpleName() + " with id "
					+ instance.getId() + " that has stale data. Current time "
					+ typeConverter.convert(String.class, currentModifiedOn)
					+ " and the last known is "
					+ typeConverter.convert(String.class, oldModifiedOn);
			// throw exception if desired or print message in log.
			if (throwException) {
				throw new StaleDataModificationException(message, pair.getSecond(), oldModifiedOn);
			}
			LOGGER.info(message);
			return true;
		}
		return false;
	}

	            /**
	 * <b>NOTE:</b> this method will work properly only if called before updating the last modified
	 * date of an instance.
	 * <p>
	 * Checks the given instance if it has stale data. The implementation uses the information
	 * provided from the method {@link #getLatestKnownModifiedDateFor(Instance)} to determine the
	 * latest modified date of the instance and the information in the current instance to compare
	 * them.<br>
	 * There are two supported fail scenarios:
	 * <ol>
	 * <li>The current instance does not have a modified date but the latest from the cache has.
	 * This is considered that the current instance is going to be created but already is.
	 * <li>The second scenario is if the modified date of the current instance is before (less than)
	 * the one from the cache.
	 * </ol>
	 * If any of the cases is met {@link StaleDataModificationException} is thrown
	 * 
	 * @param instance
	 *            the instance to check
	 * @see #getModifiedDate(Map)
	 * @see #getLatestKnownModifiedDateFor(Instance)
	 * @throws StaleDataModificationException
	 */
	protected void checkForStaleData(T instance) throws StaleDataModificationException {
		boolean set = RuntimeConfiguration
				.isSet(RuntimeConfigurationProperties.DISABLE_STALE_DATA_CHECKS);
		isModifiedInternal(instance, !set);
	}

	            /**
	 * Gets the modified by.
	 * 
	 * @param properties
	 *            the properties
	 * @return the modified by
	 */
	protected Serializable getModifiedBy(Map<String, Serializable> properties) {
		Serializable currentModifiedOn = null;
		if (properties != null) {
			currentModifiedOn = properties.get(DefaultProperties.MODIFIED_BY);
		}
		return currentModifiedOn;
	}

	            /**
	 * Gets the modified date from the given properties. Default property returned is
	 * {@link DefaultProperties#MODIFIED_ON}.
	 * 
	 * @param properties
	 *            the instance
	 * @return the modified date or <code>null</code>
	 */
	protected Date getModifiedDate(Map<String, Serializable> properties) {
		Date currentModifiedOn = null;
		if (properties != null) {
			currentModifiedOn = (Date) properties.get(DefaultProperties.MODIFIED_ON);
		}
		return currentModifiedOn;
	}

	            /**
	 * Gets the latest known modified date and user for the given instance. The method should return
	 * the last reliable known last date of modification and the user that did the change. The
	 * default implementation tries to fetch the latest properties using
	 * {@link com.sirma.itt.emf.properties.PropertiesService}. If the concrete instance does not
	 * support {@link com.sirma.itt.emf.properties.PropertiesService} then new instance is loaded
	 * using {@link #loadInstance(Serializable, Serializable, boolean)} and properties are get from
	 * there. The actual field returned is determined by the method {@link #getModifiedDate(Map)}
	 * and {@link #getModifiedBy(Map)}.
	 * 
	 * @param instance
	 *            the instance
	 * @return the latest known modified date and user
	 * @see #getModifiedDate(Map)
	 * @see #getModifiedBy(Map)
	 */
	protected Pair<Date, Serializable> getLatestKnownModifiedDateFor(T instance) {
		Date oldModifiedOn = null;
		Serializable oldModifiedBy = null;
		// the id check is for instances that does not have a DB generated before save
		// for them we can't actually load them or identify that are already persist
		if (propertiesService.isModelSupported(instance) && (instance.getId() != null)) {
			Map<String, Serializable> oldproperties = propertiesService.getEntityProperties(
					instance, instance.getRevision(), instance);
			oldModifiedOn = getModifiedDate(oldproperties);
			oldModifiedBy = getModifiedBy(oldproperties);
		} else {
			T latest = loadInstance(instance.getId(), getSecondaryKey(instance), true);
			if (latest != null) {
				oldModifiedOn = getModifiedDate(latest.getProperties());
				oldModifiedBy = getModifiedBy(latest.getProperties());
			}
		}
		return new Pair<Date, Serializable>(oldModifiedOn, oldModifiedBy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public T persistChanges(T instance) {
		TimeTracker tracker = null;
		if (debug || trace) {
			tracker = new TimeTracker().begin().begin();
		}
		// convert and persists the base case data
		T old = convertAndSave(instance);
		if (trace) {
			LOGGER.trace(getInstanceClass().getSimpleName() + " save took "
					+ tracker.stopInSeconds() + " sec");
			tracker.begin();
		}

		// to persist properties the instance need to be persisted
		savePropertiesOnPersistChanges(instance);
		if (debug) {
			if (trace) {
				LOGGER.trace(getInstanceClass().getSimpleName() + " properties save "
						+ tracker.stopInSeconds() + " sec");
			}
			LOGGER.debug(getInstanceClass().getSimpleName() + " total save time is "
					+ tracker.stopInSeconds() + " sec");
		}
		return old;
	}

	            /**
	 * Save properties on persist changes with default save mode of Replace.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void savePropertiesOnPersistChanges(T instance) {
		boolean addOnly = RuntimeConfiguration
				.isConfigurationSet(RuntimeConfigurationProperties.ADD_ONLY_PROPERTIES);
		saveProperties(instance, addOnly);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void setCurrentUserTo(T model, String key) {
		super.setCurrentUserTo(model, key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public T convertEntity(Entity<?> source, boolean toLoadProps) {
		return super.convertEntity(source, getInstanceClass(), toLoadProps, false, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public T loadInstance(Serializable id, Serializable dmsId, boolean loadProperties) {
		return loadInstanceInternal((P) id, (K) dmsId, loadProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<T> loadInstances(List<S> dmsIds) {
		return loadInstances(dmsIds, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<T> loadInstances(List<S> dmsIds, boolean loadAllProperties) {
		return batchLoad((List<K>) dmsIds, getInstanceClass(), getEntityClass(), this,
				loadAllProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<T> loadInstancesByDbKey(List<S> ids,
			boolean loadAllProperties) {
		return batchLoadByPrimaryId((List<P>) ids, getInstanceClass(), getEntityClass(), this,
				loadAllProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void loadProperties(T instance) {
		propertiesService.loadProperties(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> void batchFetchProperties(List<E> toLoadProps, boolean loadAll) {
		if ((toLoadProps == null) || toLoadProps.isEmpty()) {
			return;
		}
		super.batchFetchProperties(toLoadProps, loadAll);
	}

	            /**
	 * Load headers.
	 * 
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 * @param toLoadProps
	 *            the to load props
	 * @param ifMissing
	 *            to generate the headers only if missing
	 */
	protected <E extends Instance> void loadHeadersInternal(E instance, List<E> toLoadProps,
			boolean ifMissing) {
		TimeTracker tracker = null;
		if (trace) {
			tracker = new TimeTracker().begin();
		}
		InstanceEventProvider<Instance> eventProvider = serviceRegister
				.getEventProvider(getInstanceClass());
		int size = 1;
		if (instance != null) {
			loadHeaders(instance, eventProvider, ifMissing);
		} else if (toLoadProps != null) {
			size = toLoadProps.size();
			for (E e : toLoadProps) {
				loadHeaders(e, eventProvider, ifMissing);
			}
		}
		if (trace) {
			LOGGER.trace("Changed event for " + getInstanceClass().getSimpleName()
					+ " on load handled in : " + tracker.stopInSeconds() + " s for " + size
					+ " instance/s");
		}
	}

	            /**
	 * Load headers.
	 * 
	 * @param instance
	 *            the instance
	 * @param eventProvider
	 *            the event provider
	 * @param ifMissing
	 *            to generate the headers only if missing
	 */
	protected void loadHeaders(Instance instance, InstanceEventProvider<Instance> eventProvider,
			boolean ifMissing) {
		if (ifMissing
				&& instance.getProperties().keySet().containsAll(DefaultProperties.DEFAULT_HEADERS)) {
			// nothing to generate all headers are present
			return;
		}
		if (eventProvider != null) {
			InstanceChangeEvent<Instance> event = eventProvider.createChangeEvent(instance);
			if (event != null) {
				eventService.fire(event);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveProperties(T instance, boolean addOnly) {
		propertiesService.saveProperties(instance, addOnly);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> Entity<S> loadEntity(S entityId) {
		return getEntityCached((P) entityId, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<T> loadInstancesByDbKey(List<S> ids) {
		return loadInstancesByDbKey(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable> void delete(Entity<S> entity) {
		if (getInstanceClass().isInstance(entity) || getEntityClass().isInstance(entity)) {
			getCache().deleteByKey(entity.getId());
		}
		// we can throw an exception for just in case
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable> Entity<S> saveEntity(Entity<S> entity) {
		Entity<P> update = null;
		// if entity nothing to do
		if (getEntityClass().isInstance(entity)) {
			update = (Entity<P>) entity;
			// if instance convert the instance
		} else if (getInstanceClass().isInstance(entity)) {
			update = convert(entity, getEntityClass());
		}
		if (update != null) {
			if (!RuntimeConfiguration
					.isConfigurationSet(RuntimeConfigurationProperties.CACHE_ONLY_OPERATION)) {
				update = getDbDao().saveOrUpdate(update);
			}
			getCache().setValue(update.getId(), update);
		} else {
			update = (Entity<P>) entity;
		}
		return (Entity<S>) update;
	}

	/**
	 * {@inheritDoc}<br>
	 * Sets the owned model before return
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<T> loadInstances(Instance owner, boolean loadProperties) {
		String query = getOwningInstanceQuery();
		// if no query is provided then we cannot do anything so will return now
		if (!SequenceEntityGenerator.isPersisted(owner) || (query == null)) {
			// no allowed children on an instance
			return CollectionUtils.emptyList();
		}

		InstanceReference reference = typeConverter.convert(InstanceReference.class, owner);
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
		args.add(new Pair<String, Object>("sourceId", reference.getIdentifier()));
		args.add(new Pair<String, Object>("sourceType", reference.getReferenceType().getId()));
		List<Long> list = getDbDao().fetchWithNamed(query, args);
		List<T> loadInstancesByDbKey = loadInstancesByDbKey(list, loadProperties);
		// update the instances model
		for (T t : loadInstancesByDbKey) {
			if (t instanceof OwnedModel) {
				((OwnedModel) t).setOwningInstance(owner);
			}
		}
		return loadInstancesByDbKey;
	}

	            /**
	 * Operation should be invoked just before invoking adapter to provide some needed updates on
	 * current model representing the up to date model that would be persisted.
	 * 
	 * @param <PD>
	 *            the generic type
	 * @param model
	 *            the model
	 * @param fields
	 *            the fields for the model
	 * @return the instance updated
	 */
	@Override
	public <PD extends PropertyDefinition> Instance preSaveInstance(PropertyModel model,
			List<PD> fields) {
		for (PD field : fields) {
			if (model.getProperties().containsKey(field.getIdentifier())) {
				continue;
			}
			String expression = field.getDefaultValue();
			ExpressionEvaluator evaluator = evaluatorManager.getEvaluator(expression);
			if (evaluator != null) {
				ExpressionContext context = new ExpressionContext();
				context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) field);
				context.put(ExpressionContextProperties.CURRENT_INSTANCE, (Serializable) model);
				Serializable evaluated = evaluator.evaluate(expression, context,
						(Serializable) model);
				model.getProperties().put(field.getIdentifier(), evaluated);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <I extends Serializable> Class<I> getPrimaryIdType() {
		return (Class<I>) Long.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Instance> attach(T targetInstance, Operation operation, Instance... children) {
		if ((targetInstance == null) || (children == null) || (children.length == 0)) {
			return Collections.emptyList();
		}
		Set<Serializable> childrenIds = getReferenceIds(getCurrentChildren(targetInstance));
		List<Instance> newChildren = new ArrayList<>(children.length);
		for (Instance instance : children) {
			if (!isChildAllowed(instance)) {
				if (trace) {
					LOGGER.trace(
							"Trying to attach an invalid instance with id={} as a child to {}",
							instance.getId(), targetInstance.getId());
				}
				continue;
			}
			if (!childrenIds.contains(instance.getId())) {
				newChildren.add(instance);
			} else {
				if (trace) {
					LOGGER.trace(
							"Trying to add an instance with id={} as a child to {} more then once.",
							instance.getId(), targetInstance.getId());
				}
			}
		}

		attachNewChildren(targetInstance, operation, newChildren);

		return newChildren;
	}

	            /**
	 * Checks if is child allowed.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if is child allowed
	 */
	protected boolean isChildAllowed(Instance instance) {
		return instance != null;
	}

	            /**
	 * Attach new children.
	 * 
	 * @param targetInstance
	 *            the target instance
	 * @param operation
	 *            the operation that triggered the attach
	 * @param newChildren
	 *            the new children
	 */
	protected void attachNewChildren(T targetInstance, Operation operation,
			List<Instance> newChildren) {
		throw new UnsupportedOperationException("Trying to attach children to "
				+ targetInstance.getClass().getSimpleName() + " with id=" + targetInstance.getId()
				+ " when there is no implementation to support it!");
	}

	            /**
	 * Gets the reference ids.
	 * 
	 * @param iterable
	 *            the list
	 * @return the reference ids
	 */
	protected Set<Serializable> getReferenceIds(Iterable<InstanceReference> iterable) {
		Set<Serializable> ids = new LinkedHashSet<>();
		for (InstanceReference instanceReference : iterable) {
			ids.add(instanceReference.getIdentifier());
		}
		return ids;
	}

	            /**
	 * Gets the current children.
	 * 
	 * @param targetInstance
	 *            the target instance
	 * @return the current children
	 */
	protected Iterable<InstanceReference> getCurrentChildren(T targetInstance) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Instance> detach(T sourceInstance, Operation operation, Instance... children) {
		if ((sourceInstance == null) || (children == null) || (children.length == 0)) {
			return Collections.emptyList();
		}
		Set<Serializable> childrenIds = getReferenceIds(getCurrentChildren(sourceInstance));
		List<Instance> oldChildren = new ArrayList<>(children.length);
		for (Instance instance : children) {
			if (childrenIds.contains(instance.getId())) {
				oldChildren.add(instance);
			} else {
				if (trace) {
					LOGGER.trace("Trying to remove an instance with id=" + instance.getId()
							+ " that is not a child of " + sourceInstance.getId());
				}
			}
		}

		detachExistingChildren(sourceInstance, operation, oldChildren);

		return oldChildren;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearInternalCache() {
		if (getCacheEntityName() != null) {
			getCache().clear();
		}
	}

	            /**
	 * Detach existing children.
	 * 
	 * @param sourceInstance
	 *            the source instance
	 * @param operation
	 *            operation that let to children detachment
	 * @param oldChildren
	 *            the old children
	 */
	protected void detachExistingChildren(T sourceInstance, Operation operation,
			List<Instance> oldChildren) {
		throw new UnsupportedOperationException("Trying to remove children from "
				+ sourceInstance.getClass().getSimpleName() + " with id=" + sourceInstance.getId()
				+ " when there is no implementation to support it!");
	}

	            /**
	 * Gets the query that should be used for retrieving entity ID by owning instance reference. The
	 * query should have 2 arguments:
	 * <ul>
	 * <li>sourceId - the id for the instance reference identifier
	 * <li>sourceType - the id for the data type definition id
	 * </ul>
	 * If the query is not supported <code>null</code> should be returned
	 * 
	 * @return the owning instance query or <code>null</code> if not supported.
	 */
	protected abstract String getOwningInstanceQuery();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> void loadChildren(E instance, boolean forBatchLoad,
			InstanceDao<E> dao) {
		// only the needed complex classes should implement this method, so nothing to do here
	}

	            /**
	 * Populate instance properties using the given for model.
	 * 
	 * @param instance
	 *            the instance to populate
	 * @param model
	 *            the model to use
	 */
	protected abstract void populateInstanceForModel(T instance, D model);

	            /**
	 * Gets a cached entity instance by primary or secondary key.
	 * 
	 * @param id
	 *            the id
	 * @param dmsId
	 *            the dms id
	 * @return the case entity cache
	 */
	@SuppressWarnings({ "unchecked" })
	protected C getEntityCached(P id, K dmsId) {
		Pair<Serializable, Entity> pair = null;
		EntityLookupCache<Serializable, Entity, K> lookupCache = getCache();
		if (id != null) {
			// fetch the entity directly from the DB or from the cache
			pair = lookupCache.getByKey(id);
		}
		if ((pair == null) && (dmsId != null)) {
			// first check if we have the dms ID in the cache
			Serializable key = lookupCache.getKey(dmsId);
			// if not check into the DB for the given key
			if (key != null) {
				pair = lookupCache.getByKey(key);
			} else {
				C value = createEntity(dmsId);
				if (value != null) {
					pair = lookupCache.getByValue(value);
				}
			}
		}
		if (pair == null) {
			return null;
		}
		return (C) pair.getSecond();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected <E extends Instance> E loadInstanceInternal(P id, K otherId, boolean loadProperties) {
		C entity = getEntityCached(id, otherId);
		if (entity == null) {
			return null;
		}
		return (E) convertEntity(entity, loadProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityLookupCache<Serializable, Entity, K> getCache() {
		return entityCacheContext.getCache(getCacheEntityName());
	}

	            /**
	 * Creates the entity instance and set the given secondary key. The created instance will be
	 * used for search argument for the secondary key cache lookup.
	 * 
	 * @param dmsId
	 *            the secondary key id
	 * @return the created instance or <code>null</code> if not supported
	 */
	protected abstract C createEntity(K dmsId);

	            /**
	 * Convert and save the given instance as entity.<br>
	 * <b>NOTE:</b>The provided implementation handles basic transformation, persisting and cache
	 * updating
	 * 
	 * @param instance
	 *            the instance
	 * @return the converted entity
	 */
	@SuppressWarnings("unchecked")
	protected T convertAndSave(T instance) {
		C entity = super.convert(instance, getEntityClass());
		C oldCached = null;
		T oldInstance = null;
		// REVIEW: the ID will always be not null so it will generate one additional query for every
		// new instance created
		if (SequenceEntityGenerator.isPersisted(entity)) {
			oldCached = getEntityCached((P) instance.getId(), null);
			if (oldCached != null) {
				// convert and load the old instance
				// NOTE: this will load the children also if we does not require that, something
				// should be changed
				oldInstance = convertEntity(oldCached, getInstanceClass(), true, false, this);
			}
			if ((entity instanceof VersionableEntity) && (oldCached instanceof VersionableEntity)) {
				((VersionableEntity) entity).setVersion(((VersionableEntity) oldCached)
						.getVersion());
			}
		}
		onBeforeSave(entity, oldCached);
		entity = (C) getDbDao().saveOrUpdate(entity);
		if (instance.getId() == null) {
			instance.setId(entity.getId());
		}
		onAfterSave(instance);
		// update the cache
		getCache().setValue(entity.getId(), entity);
		return oldInstance;
	}

	            /**
	 * After the save this method is called for any additional operations to be executed.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void onAfterSave(T instance) {
		// nothing to do here, add the proper implementation in the subclasses
	}

	            /**
	 * The method is called just before saving to DB.
	 * 
	 * @param entity
	 *            the entity
	 * @param oldCached
	 *            the old cached
	 */
	@SuppressWarnings("unchecked")
	protected void onBeforeSave(C entity, C oldCached) {
		if ((entity.getId() == null) && getPrimaryIdType().equals(String.class)) {
			SequenceEntityGenerator.generateStringId(entity, true);
		}
	}

	            /**
	 * Getter method for dbDao.
	 * 
	 * @return the dbDao
	 */
	protected DbDao getDbDao() {
		return dbDao;
	}

	            /**
	 * Gets the instance class for the concrete implementation.
	 * 
	 * @return the instance class
	 */
	protected abstract Class<T> getInstanceClass();

	            /**
	 * Gets the entity class for the concrete implementation.
	 * 
	 * @return the entity class
	 */
	protected abstract Class<C> getEntityClass();

	            /**
	 * Gets the definition class that can be used to represent the current instance or
	 * <code>null</code> if not supported.
	 * 
	 * @return the definition class
	 */
	protected abstract Class<D> getDefinitionClass();

	            /**
	 * Gets the cache entity name or <code>null</code> if the cache is not supported.
	 * 
	 * @return the cache entity name
	 */
	protected abstract String getCacheEntityName();

	            /**
	 * Gets the entity cache provider to be used or <code>null</code> if not supported.
	 * 
	 * @return the entity cache provider
	 */
	protected abstract EntityLookupCallbackDAOAdaptor<P, C, K> getEntityCacheProvider();

	            /**
	 * Base entity cache lookup DAO. The implementation supports fetching by primary and secondary
	 * keys. The implementing class should override methods {@link #getValueKey(Entity)} and
	 * {@link #fetchEntityByValue(Serializable)}.
	 * 
	 * @author BBonev
	 */
	protected abstract class DefaultEntityLookupDao extends BaseEntityLookupDao<C, K, P> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<P, C> createValue(C value) {
			throw new UnsupportedOperationException(getEntityClass() + " is externally persisted");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected org.slf4j.Logger getLogger() {
			return LOGGER;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Class<C> getEntityClass() {
			return BaseInstanceDaoImpl.this.getEntityClass();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected DbDao getDbDao() {
			return BaseInstanceDaoImpl.this.getDbDao();
		}

		                        /**
		 * Gets the value key for the given cache value.
		 * 
		 * @param value
		 *            the value, will never be <code>null</code>
		 * @return the value key internal
		 */
		@Override
		protected abstract K getValueKeyInternal(C value);

		                        /**
		 * Fetch entities by value key.
		 * 
		 * @param key
		 *            the key
		 * @return the list of found entities
		 */
		@Override
		protected abstract List<C> fetchEntityByValue(K key);

	}

	            /**
	 * Default entity cache lookup DAO to handle only primary key cache lookups.
	 * 
	 * @author BBonev
	 */
	public class DefaultPrimaryKeyEntityLookupDao extends DefaultEntityLookupDao {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected K getValueKeyInternal(C value) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<C> fetchEntityByValue(K key) {
			return Collections.emptyList();
		}
	}
}
