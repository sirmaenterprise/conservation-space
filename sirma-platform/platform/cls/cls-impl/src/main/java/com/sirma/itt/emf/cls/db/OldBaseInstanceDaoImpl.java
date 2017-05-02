package com.sirma.itt.emf.cls.db;

/*
 *
 */
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.BaseEntityLookupDao;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.TenantAware;
import com.sirma.itt.seip.domain.VersionableEntity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinitionModel;
import com.sirma.itt.seip.domain.exceptions.StaleDataModificationException;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceLoader;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;
import com.sirma.itt.seip.instance.event.InstanceCreateEvent;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Common implementation logic for {@link InstanceDao} realizations.
 *
 * @author BBonev
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
 * @deprecated This should be replaces with {@link InstanceLoader}
 */
@Deprecated
@SuppressWarnings("rawtypes")
public abstract class OldBaseInstanceDaoImpl<C extends Entity, P extends Serializable, K extends Serializable, D extends DefinitionModel>
		extends OldBaseInstanceDao<P, K> implements InstanceDao {

	/** The entity cache context. */
	@Inject
	protected EntityLookupCacheContext entityCacheContext;

	/** The db dao. */
	@Inject
	protected DbDao dbDao;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** The debug. */
	private boolean debug;

	/** The trace. */
	boolean trace;

	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;

	/** The type converter. */
	@Inject
	protected TypeConverter typeConverter;

	/** The service register. */
	@Inject
	protected ServiceRegistry serviceRegistry;

	/** The event service. */
	@Inject
	protected EventService eventService;


	@Inject
	private DatabaseIdManager idManager;

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

	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <T extends Instance> T createInstance(DefinitionModel definitionModel, boolean populateProperties) {
		T instance = createNewInstance();
		if (definitionModel != null && getDefinitionClass() != null
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
	protected void notifyForNewInstance(Instance instance) {
		// better not to fire an event for not initialized instance
		if (instance == null || instance.getProperties() == null) {
			return;
		}
		InstanceEventProvider<Instance> provider = serviceRegistry.getEventProvider(instance);
		if (provider != null) {
			InstanceCreateEvent<Instance> event = provider.createCreateEvent(instance);
			if (event != null) {
				eventService.fire(event);
			}
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <T extends Instance> T createInstance(String definitionId, boolean populateProperties) {
		T instance = createNewInstance();

		if (definitionId != null) {
			instance.setIdentifier(definitionId);
			// if the instance does not support definitions
			D model = getInstanceDefinition(definitionId);
			populateInstanceForModelInternal(instance, model, populateProperties);
		}
		return instance;
	}

	/**
	 * Creates the new instance. If the primary type is of type String then new ID is generated before return.
	 *
	 * @return a newly created instance.
	 */
	protected <T extends Instance> T createNewInstance() {
		T instance = ReflectionUtils.newInstance(getInstanceClass());
		// generate instance DB id
		if (getPrimaryIdType().equals(String.class)) {
			idManager.generateStringId(instance, true);
		}
		return instance;
	}

	/**
	 * Update newly created instance with model. When all base logic is called then the methods
	 * {@link #synchRevisions(Instance, Long)} and {@link #populateInstanceForModel(Instance, DefinitionModel)} is
	 * called for concrete additional implementations to fill any other data.
	 *
	 * @param instance
	 *            the instance
	 * @param model
	 *            the model
	 * @param populateProperties
	 *            the populate properties
	 */
	protected void populateInstanceForModelInternal(Instance instance, D model, boolean populateProperties) {
		if (model != null && populateProperties) {
			if (instance instanceof TenantAware) {
				((TenantAware) instance).setContainer(securityContext.getCurrentTenantId());
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
	 * @return the instance definition
	 */
	protected D getInstanceDefinition(String definitionId) {
		return (D) dictionaryService.find(definitionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends PropertyDefinition> void populateProperties(PropertyModel model, List<E> fields) {
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
	public void instanceUpdated(Instance instance, boolean autosave) {
		checkForStaleData(instance);

		onInstanceUpdated(instance);

		updateCreatorAndModifierInfo(instance);

		onAutoSave(instance, autosave);
	}

	/**
	 * Update creator and modifier info. There is an option to disable setting of modifier info
	 *
	 * @param instance
	 *            the instance
	 */
	protected void updateCreatorAndModifierInfo(Instance instance) {
		if (Options.AUDIT_MODIFICATION_DISABLED.isEnabled() || Options.OVERRIDE_MODIFIER_INFO.isEnabled()) {
			// disable the default implementation and allow external setting of
			// modifier info
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
	protected void updateModifierInfo(Instance instance, Date currentDate) {
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
	protected void updateCreatorInfo(Instance instance, Date currentDate) {
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
	protected void onAutoSave(Instance instance, boolean autosave) {
		// base implementation
		if (autosave) {
			persistChanges(instance);
		}
	}

	/**
	 * Method call on every all of {@link #instanceUpdated(Instance, boolean)} right after the check for stale data.
	 *
	 * @param instance
	 *            the instance
	 */
	protected void onInstanceUpdated(Instance instance) {
		// override to add functionality. No defaults.
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isModified(Instance instance) {
		return isModifiedInternal(instance, false);
	}

	/**
	 * Checks if the given instance is modified the method could optionally throw an exception if needed on detected
	 * modification instead of returning <code>true</code>.
	 *
	 * @param instance
	 *            the instance
	 * @param throwException
	 *            the throw exception
	 * @return true, if is modified internal
	 * @see {@link #checkForStaleData(Instance)}
	 * @see #getModifiedBy(Map)
	 * @see #getModifiedDate(Map)
	 * @see #checkDatesForChanges(Date, Date)
	 * @see #isLastModificationImportant(Instance, Map, Serializable)
	 */
	protected boolean isModifiedInternal(Instance instance, boolean throwException) {
		// get the current (old) timestamp
		Date currentModifiedOn = getModifiedDate(instance.getProperties());
		// get from the DB the latest known properties
		Date oldModifiedOn = null;
		Serializable oldModifiedBy = null;

		Map<String, Serializable> oldProperties = getLastKnownProperties(instance);
		// if no previous data then we are done
		if (oldProperties == null) {
			return false;
		}

		oldModifiedOn = getModifiedDate(oldProperties);
		oldModifiedBy = getModifiedBy(oldProperties);

		// if we already have a modified date and the incoming instance does not
		// have or is the
		// cache entry is newer that the incoming modified date then we have
		// race condition.

		if (checkDatesForChanges(currentModifiedOn, oldModifiedOn)
				&& !isLastModificationImportant(instance, oldProperties, oldModifiedBy)) {
			StringBuilder builder = new StringBuilder(256)
					.append("Trying to save ")
						.append(instance.getClass().getSimpleName())
						.append(" with id ")
						.append(instance.getId())
						.append(" that has stale data. Last modified by ")
						.append(oldModifiedBy)
						.append(". Current time ")
						.append(typeConverter.convert(String.class, currentModifiedOn))
						.append(" and the last known is ")
						.append(typeConverter.convert(String.class, oldModifiedOn));
			// throw exception if desired or print message in log.
			if (throwException) {
				throw new StaleDataModificationException(builder.toString(), oldModifiedBy, oldModifiedOn);
			}
			LOGGER.info(builder.toString());
			return true;
		}
		return false;
	}

	/**
	 * Check dates for changes.
	 *
	 * @param currentModifiedOn
	 *            the current modified on
	 * @param oldModifiedOn
	 *            the old modified on
	 * @return true, if there are changes in the modified dates
	 */
	protected boolean checkDatesForChanges(Date currentModifiedOn, Date oldModifiedOn) {
		return isAlreadyCreated(currentModifiedOn, oldModifiedOn)
				|| isOldModifiedDateIsAfterCurrent(currentModifiedOn, oldModifiedOn);
	}

	/**
	 * Checks if is old modified date is after current modified date
	 *
	 * @param currentModifiedOn
	 *            the current modified on
	 * @param oldModifiedOn
	 *            the old modified on
	 * @return true, if is old modified date is after current
	 */
	protected boolean isOldModifiedDateIsAfterCurrent(Date currentModifiedOn, Date oldModifiedOn) {
		return currentModifiedOn != null && oldModifiedOn != null
				&& oldModifiedOn.getTime() > currentModifiedOn.getTime();
	}

	/**
	 * Checks if is already created.
	 *
	 * @param currentModifiedOn
	 *            the current modified on
	 * @param oldModifiedOn
	 *            the old modified on
	 * @return true, if is already created
	 */
	protected boolean isAlreadyCreated(Date currentModifiedOn, Date oldModifiedOn) {
		return oldModifiedOn != null && currentModifiedOn == null;
	}

	/**
	 * This method is called when change, made by the given user, is detected in the passed instance. The default
	 * implementation checks if the user is system and marks it as non important and could be overridden.
	 *
	 * @param instance
	 *            the instance modified instance
	 * @param oldProperties
	 *            the old properties if any. Contains the last known properties of the instance before this change.
	 * @param lastModifier
	 *            the last modifier
	 * @return <code>true</code> if the last change should not be allowed to be overridden. If <code>false</code> the
	 *         last change will be lost and overridden.
	 */
	protected boolean isLastModificationImportant(Instance instance, Map<String, Serializable> oldProperties,
			Serializable lastModifier) {
		if (lastModifier == null) {
			return false;
		}
		// this should be changed to check for last user
		return true;
	}

	/**
	 * <b>NOTE:</b> this method will work properly only if called before updating the last modified date of an instance.
	 * <p>
	 * Checks the given instance if it has stale data. The implementation uses the information provided from the method
	 * {@link #getLastKnownProperties(Instance)} to determine the latest modified date and modifier of the instance and
	 * the information in the current instance to compare them.<br>
	 * There are two supported fail scenarios:
	 * <ol>
	 * <li>The current instance does not have a modified date but the latest from the cache has. This is considered that
	 * the current instance is going to be created but already is.
	 * <li>The second scenario is if the modified date of the current instance is before (less than) the one from the
	 * cache.
	 * </ol>
	 * If any of the cases is met {@link StaleDataModificationException} is thrown
	 *
	 * @param instance
	 *            the instance to check
	 * @see #getModifiedDate(Map)
	 * @see #getLastKnownProperties(Instance)
	 * @see isModifiedInternal
	 */
	protected void checkForStaleData(Instance instance) {
		boolean set = Options.DISABLE_STALE_DATA_CHECKS.isEnabled();
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
	 * Gets the latest known properties for the given instance. The method should return the last reliable known
	 * properties before the change. The default implementation tries to fetch the latest properties using
	 * {@link com.sirma.itt.seip.instance.properties.PropertiesService}. If the concrete instance does not support
	 * {@link com.sirma.itt.seip.instance.properties.PropertiesService} then new instance is loaded using
	 * {@link #loadInstance(Serializable, Serializable, boolean)} and properties are get from there.
	 *
	 * @param instance
	 *            the instance
	 * @return the latest known modified date and user
	 */
	protected Map<String, Serializable> getLastKnownProperties(Instance instance) {
		// the id check is for instances that does not have a DB generated
		// before save
		// for them we can't actually load them or identify that are already
		// persist
		if (propertiesService.isModelSupported(instance) && instance.getId() != null) {
			Map<String, Serializable> oldproperties = propertiesService.getEntityProperties(instance, instance);
			return oldproperties;
		} else if (idManager.isPersisted(instance)) {
			// no need to query anything if not persisted
			Instance latest = loadInstance(instance.getId(), getSecondaryKey(instance), true);
			if (latest != null) {
				return latest.getProperties();
			}
		}
		return null;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <I extends Instance> I persistChanges(I instance) {
		TimeTracker tracker = new TimeTracker();
		if (debug || trace) {
			tracker.begin().begin();
		}
		// convert and persists the base case data
		I old = convertAndSave(instance);
		if (trace) {
			LOGGER.trace(getInstanceClass().getSimpleName() + " save took " + tracker.stopInSeconds() + " sec");
			tracker.begin();
		}

		// to persist properties the instance need to be persisted
		savePropertiesOnPersistChanges(instance);
		if (debug) {
			if (trace) {
				LOGGER.trace(
						getInstanceClass().getSimpleName() + " properties save " + tracker.stopInSeconds() + " sec");
			}
			LOGGER.debug(
					getInstanceClass().getSimpleName() + " total save time is " + tracker.stopInSeconds() + " sec");
		}
		return old;
	}

	/**
	 * Save properties on persist changes with default save mode of Replace.
	 *
	 * @param instance
	 *            the instance
	 */
	protected void savePropertiesOnPersistChanges(Instance instance) {
		boolean addOnly = Options.ADD_ONLY_PROPERTIES.isEnabled();
		saveProperties(instance, addOnly);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void setCurrentUserTo(Instance model, String key) {
		super.setCurrentUserTo(model, key);
	}

	/**
	 * Convert entity.
	 *
	 * @param source
	 *            the source
	 * @param toLoadProps
	 *            the to load props
	 * @return the t
	 */
	protected <T extends Instance> T convertEntity(Entity<?> source, boolean toLoadProps) {
		return super.convertEntity(source, getInstanceClass(), toLoadProps, false, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <T extends Instance> T loadInstance(Serializable id, Serializable dmsId, boolean loadProperties) {
		return loadInstanceInternal((P) id, (K) dmsId, loadProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable, I extends Instance> List<I> loadInstances(List<S> dmsIds) {
		return loadInstances(dmsIds, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable, I extends Instance> List<I> loadInstances(List<S> dmsIds,
			boolean loadAllProperties) {
		return batchLoad((List<K>) dmsIds, getInstanceClass(), this, loadAllProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable, I extends Instance> List<I> loadInstancesByDbKey(List<S> ids,
			boolean loadAllProperties) {
		return batchLoadByPrimaryId((List<P>) ids, getInstanceClass(), this, loadAllProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void loadProperties(Instance instance) {
		propertiesService.loadProperties(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> void batchFetchProperties(List<E> toLoadProps, boolean loadAll) {
		if (toLoadProps == null || toLoadProps.isEmpty()) {
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
	protected <E extends Instance> void loadHeadersInternal(E instance, List<E> toLoadProps, boolean ifMissing) {
		TimeTracker tracker = new TimeTracker();
		if (trace) {
			tracker.begin();
		}
		InstanceEventProvider<Instance> eventProvider = serviceRegistry.getEventProvider(getInstanceClass());
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
			LOGGER.trace("Changed event for " + getInstanceClass().getSimpleName() + " on load handled in : "
					+ tracker.stopInSeconds() + " s for " + size + " instance/s");
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
	protected void loadHeaders(Instance instance, InstanceEventProvider<Instance> eventProvider, boolean ifMissing) {
		if (ifMissing && instance.getProperties().keySet().containsAll(DefaultProperties.DEFAULT_HEADERS)) {
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
	public void saveProperties(Instance instance, boolean addOnly) {
		propertiesService.saveProperties(instance, addOnly);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable, I extends Instance> List<I> loadInstancesByDbKey(List<S> ids) {
		return loadInstancesByDbKey(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable> void delete(Entity<S> entity) {
		if (getInstanceClass().isInstance(entity) || getEntityClass().isInstance(entity)) {
			int deleted = getCache().deleteByKey(entity.getId());
			LOGGER.debug("Deleting {} with id [{}]. Actually removed {}.", entity.getClass().getSimpleName(),
					entity.getId(), deleted);
			if (deleted > 0) {
				deleteProperties(entity);
			}
		}
		// we can throw an exception for just in case
	}

	/**
	 * Delete properties.
	 *
	 * @param <S>
	 *            the generic type
	 * @param entity
	 *            the entity
	 */
	protected <S extends Serializable, T extends Instance> void deleteProperties(Entity<S> entity) {
		PathElement path = null;
		boolean supported = false;
		if (getInstanceClass().isInstance(entity)) {
			Instance instance = getInstanceClass().cast(entity);
			path = instance;
			supported = propertiesService.isModelSupported(instance);
		} else if (getEntityClass().isInstance(entity)) {
			T convert = convert(entity, getInstanceClass());
			if (convert != null) {
				path = convert;
				supported = propertiesService.isModelSupported(convert);
			}
		}
		if (supported && path != null) {
			propertiesService.removeProperties(entity, path);
		}
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
			if (!Options.CACHE_ONLY_OPERATION.isEnabled()) {
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
	public <T extends Instance> List<T> loadInstances(Instance owner, boolean loadProperties) {
		String query = getOwningInstanceQuery();
		// if no query is provided then we cannot do anything so will return now
		if (!idManager.isPersisted(owner) || query == null) {
			// no allowed children on an instance
			return CollectionUtils.emptyList();
		}

		InstanceReference reference = typeConverter.convert(InstanceReference.class, owner);
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<String, Object>("sourceId", reference.getIdentifier()));
		args.add(new Pair<String, Object>("sourceType", reference.getReferenceType().getId()));
		List<P> list = getDbDao().fetchWithNamed(query, args);
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
	 * Operation should be invoked just before invoking adapter to provide some needed updates on current model
	 * representing the up to date model that would be persisted.
	 *
	 * @param <X>
	 *            the generic type
	 * @param model
	 *            the model
	 * @param fields
	 *            the fields for the model
	 * @return the instance updated
	 */
	@Override
	public <X extends PropertyDefinition> Instance preSaveInstance(PropertyModel model, Collection<X> fields) {
		for (X field : fields) {
			if (model.getProperties().containsKey(field.getIdentifier())) {
				continue;
			}
			Serializable serializable = evaluatorManager.evaluate(field,
					evaluatorManager.createDefaultContext((Instance) model, field, null));
			if (serializable != null) {
				model.getProperties().put(field.getIdentifier(), serializable);
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
	public void clearInternalCache() {
		if (getCacheEntityName() != null) {
			getCache().clear();
		}
	}

	/**
	 * Gets the query that should be used for retrieving entity ID by owning instance reference. The query should have 2
	 * arguments:
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
	protected <E extends Instance> void loadChildren(E instance, boolean forBatchLoad, InstanceDao dao) {
		// only the needed complex classes should implement this method, so
		// nothing to do here
	}

	/**
	 * Populate instance properties using the given for model.
	 *
	 * @param instance
	 *            the instance to populate
	 * @param model
	 *            the model to use
	 */
	protected abstract void populateInstanceForModel(Instance instance, D model);

	/**
	 * Gets a cached entity instance by primary or secondary key.
	 *
	 * @param id
	 *            the id
	 * @param dmsId
	 *            the dms id
	 * @return the case entity cache
	 */
	protected C getEntityCached(P id, K dmsId) {
		Pair<Serializable, Entity> pair = null;
		EntityLookupCache<Serializable, Entity, K> lookupCache = getCache();
		if (id != null) {
			// fetch the entity directly from the DB or from the cache
			pair = lookupCache.getByKey(id);
		}
		if (pair == null && dmsId != null) {
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
	 * Creates the entity instance and set the given secondary key. The created instance will be used for search
	 * argument for the secondary key cache lookup.
	 *
	 * @param dmsId
	 *            the secondary key id
	 * @return the created instance or <code>null</code> if not supported
	 */
	protected abstract C createEntity(K dmsId);

	/**
	 * Convert and save the given instance as entity.<br>
	 * <b>NOTE:</b>The provided implementation handles basic transformation, persisting and cache updating
	 *
	 * @param instance
	 *            the instance
	 * @return the converted entity
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Instance> T convertAndSave(T instance) {
		C entity = super.convert(instance, getEntityClass());
		C oldCached = null;
		T oldInstance = null;
		// REVIEW: the ID will always be not null so it will generate one
		// additional query for every
		// new instance created
		if (idManager.isPersisted(entity)) {
			oldCached = getEntityCached((P) instance.getId(), null);
			if (oldCached != null) {
				// convert and load the old instance
				// NOTE: this will load the children also if we does not require
				// that, something
				// should be changed
				oldInstance = convertEntity(oldCached, getInstanceClass(), true, false, this);
			}
			if (entity instanceof VersionableEntity && oldCached instanceof VersionableEntity) {
				((VersionableEntity) entity).setVersion(((VersionableEntity) oldCached).getVersion());
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
	protected void onAfterSave(Instance instance) {
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
		if (entity.getId() == null && getPrimaryIdType().equals(String.class)) {
			idManager.generateStringId(entity, true);
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
	protected abstract <T extends Instance> Class<T> getInstanceClass();

	/**
	 * Gets the entity class for the concrete implementation.
	 *
	 * @return the entity class
	 */
	protected abstract Class<C> getEntityClass();

	/**
	 * Gets the definition class that can be used to represent the current instance or <code>null</code> if not
	 * supported.
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
	 * Base entity cache lookup DAO. The implementation supports fetching by primary and secondary keys. The
	 * implementing class should override methods {@link #getValueKey(Entity)} and
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
			return OldBaseInstanceDaoImpl.this.getEntityClass();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected DbDao getDbDao() {
			return OldBaseInstanceDaoImpl.this.getDbDao();
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
