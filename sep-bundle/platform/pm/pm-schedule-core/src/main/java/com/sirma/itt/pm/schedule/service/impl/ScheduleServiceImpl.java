package com.sirma.itt.pm.schedule.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.LongType;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.dao.AllowedChildrenHelper;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.TenantAware;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.ReflectionUtils;
import com.sirma.itt.pm.schedule.constants.ScheduleRuntimeConfiguration;
import com.sirma.itt.pm.schedule.db.DbQueryTemplatesSchedule;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.event.ScheduleCreatedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryAddedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryCanceledEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryDeletedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryMovedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryStartedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleEntryUpdatedEvent;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.schedule.model.StartInstance;
import com.sirma.itt.pm.schedule.model.StartedInstance;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;
import com.sirma.itt.pm.schedule.service.ScheduleService;
import com.sirma.itt.pm.schedule.service.dao.ScheduleEntryAllowedChildrenProvider;

/**
 * Default implementation for schedule service
 *
 * @author BBonev
 */
@Stateless
public class ScheduleServiceImpl implements ScheduleService {

	/** The Constant SCHEDULE_ACTUAL_INSTANCE_ENTITY_ID_CACHE. */
	@CacheConfiguration(container = "pm", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 12000000, interval = 60000), doc = @Documentation(""
			+ "Cache used to contain the schedule instance ID for the activly opened schedule instance. "
			+ "The cache will have at an entry for every opened schedule instance. "
			+ "<br>Minimal value expression: users"))
	public static final String SCHEDULE_ACTUAL_INSTANCE_ENTITY_ID_CACHE = "SCHEDULE_ACTUAL_INSTANCE_ENTITY_ID_CACHE";
	/** The Constant SCHEDULE_ENTRY_ACTUAL_INSTANCE_ENTITY_ID_CACHE. */
	@CacheConfiguration(container = "pm", eviction = @Eviction(maxEntries = 10000), expiration = @Expiration(maxIdle = 6000000, interval = 60000), doc = @Documentation(""
			+ "Cache used to contain the schedule entry ID for every loaded schedule entry that has actual instance for all active users. "
			+ "The cache will have at an entry with actual instance for every row in every opened schedule. "
			+ "Entries are used even if the schedule is not opened when user works with actual instances that are part of a schedule."
			+ "<br>Minimal value expression: (users * averageScheduleElements) * 1.4"))
	public static final String SCHEDULE_ENTRY_ACTUAL_INSTANCE_ENTITY_ID_CACHE = "SCHEDULE_ENTRY_ACTUAL_INSTANCE_ENTITY_ID_CACHE";

	@Inject
	private Logger logger;

	@Inject
	private DbDao dbDao;

	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE)
	private InstanceDao<ScheduleInstance> scheduleInstanceDao;
	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
	private InstanceDao<ScheduleEntry> scheduleEntryDao;

	@Inject
	private TypeConverter converter;

	@Inject
	private EventService eventService;

	@Inject
	private AllowedChildrenTypeProvider typeProvider;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private PropertiesService propertiesService;

	@Inject
	private EntityLookupCacheContext cacheContext;

	@Inject
	private ResourceService resourceService;

	@Inject
	private ScheduleResourceService scheduleResourceService;

	@PersistenceContext(unitName = DbDao.PERSISTENCE_UNIT_NAME, type = PersistenceContextType.TRANSACTION)
	private EntityManager entityManager;

	private static ReadWriteLock lock = new ReentrantReadWriteLock();

	private List<Long> instanceTypes;

	@Inject
	@Proxy
	protected InstanceService<Instance, DefinitionModel> instanceService;

	/**
	 * Initialize cache.
	 */
	@PostConstruct
	public void initializeCache() {
		if (!cacheContext.containsCache(SCHEDULE_ACTUAL_INSTANCE_ENTITY_ID_CACHE)) {
			cacheContext.createCache(SCHEDULE_ACTUAL_INSTANCE_ENTITY_ID_CACHE,
					new ScheduleActualInstanceReferenceLookup());
		}
		if (!cacheContext.containsCache(SCHEDULE_ENTRY_ACTUAL_INSTANCE_ENTITY_ID_CACHE)) {
			cacheContext.createCache(SCHEDULE_ENTRY_ACTUAL_INSTANCE_ENTITY_ID_CACHE,
					new ScheduleEntryActualInstanceReferenceLookup());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ScheduleInstance getOrCreateSchedule(Instance instance) {
		try {
			lock.writeLock().lock();
			// we added a cache to store the Ids and to minimize the DB calls
			InstanceReference sourceId = converter.convert(InstanceReference.class, instance);
			EntityLookupCache<Pair<String, Long>, Serializable, Serializable> cache = getScheduleByActualInstanceCache();
			Pair<String, Long> key = convertToCachePair(sourceId);
			Pair<Pair<String, Long>, Serializable> pair = cache.getByKey(key);

			if (pair == null) {
				// create new instance
				ScheduleInstance result = new ScheduleInstance();
				result.setIdentifier(instance.getIdentifier());
				result.setRevision(instance.getRevision());
				result.setOwningReference(sourceId);
				if (instance instanceof TenantAware) {
					result.setContainer(((TenantAware) instance).getContainer());
				}
				result.setOwningInstance(instance);
				scheduleInstanceDao.instanceUpdated(result, true);

				// notify for new schedule instance
				eventService.fire(new ScheduleCreatedEvent(result, instance));

				// create entry for the root object and save it
				ScheduleEntry entry = converter.convert(ScheduleEntry.class, instance);
				entry.setScheduleId((Long) result.getId());
				addEntry(entry);

				String createdBy = (String) entry.getProperties().get(DefaultProperties.CREATED_BY);
				if (createdBy != null) {
					Resource resource = resourceService.getResource(createdBy, ResourceType.USER);
					ScheduleAssignment assignment = new ScheduleAssignment();
					assignment.setResourceId((String) resource.getId());
					assignment.setScheduleId((Long) result.getId());
					assignment.setTaskId((Long) entry.getId());
					assignment.setUnits(100);
					scheduleResourceService.add(assignment);
				}

				cache.setValue(key, result.getId());
				// TODO: add lazy schedule entry initialization - this could be handled in the
				// event handler that listen for the operation
				return result;
			}
			ScheduleInstance scheduleInstance = scheduleInstanceDao.loadInstance(pair.getSecond(),
					null, true);
			scheduleInstance.setOwningInstance(instance);
			return scheduleInstance;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ScheduleInstance getSchedule(Instance instance) {
		lock.readLock().lock();
		try {
			InstanceReference sourceId = converter.convert(InstanceReference.class, instance);
			Pair<Pair<String, Long>, Serializable> pair = getScheduleByActualInstanceCache()
					.getByKey(
					convertToCachePair(sourceId));

			if (pair == null) {
				return null;
			}
			return scheduleInstanceDao.loadInstance(pair.getSecond(), null, true);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<ScheduleEntry> getEntries(ScheduleInstance instance) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("scheduleId", instance.getId()));
		// query entity ids that are located on L0 and L1 of the schedule
		List<Long> list = dbDao.fetchWithNamed(
				DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_SCHEDULE_KEY, args);

		// fetch the entities from the cache by IDs
		List<ScheduleEntry> entries = scheduleEntryDao
				.loadInstancesByDbKey(list);
		return entries;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ScheduleEntry getEntryForActualInstance(Instance instance) {
		return getEntryForActualInstance(instance, true);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ScheduleEntry getEntryForActualInstance(Instance instance, boolean autoCreate) {
		if (instance == null) {
			return null;
		}
		// new instance probably we does not have an entry also so we will create one and return it
		if (!SequenceEntityGenerator.isPersisted(instance)) {
			if (autoCreate) {
				return converter.convert(ScheduleEntry.class, instance);
			}
			return null;
		}

		// this is used when starting instance and we need to fix automatic schedule creation not to
		// create empty entries, because they are not yet in the DB.
		if (RuntimeConfiguration
				.isConfigurationSet(ScheduleRuntimeConfiguration.CURRENT_SCHEDULE_ENTRY)) {
			ScheduleEntry entry = (ScheduleEntry) RuntimeConfiguration
					.getConfiguration(ScheduleRuntimeConfiguration.CURRENT_SCHEDULE_ENTRY);
			// check if the instance is the same
			if ((entry.getActualInstanceClass() != null)
					&& instance.getClass().equals(entry.getActualInstanceClass())) {
				return entry;
			}
		}

		// added cache for actual instance to schedule entry mapping
		InstanceReference reference = converter.convert(InstanceReference.class, instance);
		Pair<Pair<String, Long>, Serializable> pair = getScheduleEntryByActualInstanceCache()
				.getByKey(convertToCachePair(reference));
		if (pair != null) {
			// fetch the entry from the cache by ID
			ScheduleEntry entry = scheduleEntryDao.loadInstance(pair.getSecond(), null, true);
			if (entry != null) {
				return entry;
			}
			// entry should should not be null but just in case
		}
		// if we came here we probably does not have a schedule entry for now so we will create new
		// one and return it
		if (autoCreate) {
			return converter.convert(ScheduleEntry.class, instance);
		}
		return null;
	}

	/**
	 * Convert to cache pair.
	 *
	 * @param reference
	 *            the reference
	 * @return the pair
	 */
	private Pair<String, Long> convertToCachePair(InstanceReference reference) {
		return new Pair<String, Long>(reference.getIdentifier(), reference.getReferenceType()
				.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<ScheduleEntry> addEntry(ScheduleEntry entry) {
		RuntimeConfiguration.setConfiguration(
				RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION, Boolean.TRUE);
		try {
			scheduleEntryDao.instanceUpdated(entry, true);

			if ((entry.getInstanceReference() != null)
					&& StringUtils.isNotNullOrEmpty(entry.getInstanceReference().getIdentifier())) {
				Pair<String, Long> pair = convertToCachePair(entry.getInstanceReference());
				getScheduleEntryByActualInstanceCache().setValue(pair, entry.getId());
			}
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);
		}

		eventService.fire(new ScheduleEntryAddedEvent(entry));

		// TODO: should implement merging with the other new elements if needed and returning them
		List<ScheduleEntry> list = new ArrayList<ScheduleEntry>(1);
		list.add(entry);

		return list;
	}

	@Override
	public List<ScheduleEntry> moveEntry(ScheduleEntry entry) {
		ScheduleEntry oldEntry = scheduleEntryDao.loadInstance(entry.getId(), null, false);
		List<Serializable> parents = new ArrayList<>(2);
		parents.add(oldEntry.getParentId());
		parents.add(entry.getParentId());
		List<ScheduleEntry> list = scheduleEntryDao.loadInstancesByDbKey(parents, true);
		if (list.size() != 2) {
			logger.warn("Parent was not foud!!");
		} else {
			eventService.fire(new ScheduleEntryMovedEvent(entry, list.get(0), list.get(1)));
		}
		return updateEntry(entry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<ScheduleEntry> updateEntry(ScheduleEntry entry) {
		ScheduleEntry internal = saveInternal(entry, true);

		// TODO: should implement merging with the other new elements if needed and returning them
		List<ScheduleEntry> list = new ArrayList<ScheduleEntry>(1);
		list.add(internal);

		return list;
	}

	/**
	 * Save internal.
	 *
	 * @param entry
	 *            the entry
	 * @param updateActualInstance
	 *            to update actual instance
	 * @return the schedule entry
	 */
	ScheduleEntry saveInternal(ScheduleEntry entry, boolean updateActualInstance) {
		RuntimeConfiguration.setConfiguration(
				RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION, Boolean.TRUE);
		try {
			ScheduleEntry oldEntry = scheduleEntryDao.loadInstance(entry.getId(), null, true);
			// save the entry
			scheduleEntryDao.instanceUpdated(entry, true);
			// if we have an actual instance we can update it if we want
			if (updateActualInstance && (entry.getActualInstanceClass() != null)
					&& StringUtils.isNotNullOrEmpty(entry.getIdentifier())
					&& (entry.getInstanceReference() != null)
					&& StringUtils.isNotNullOrEmpty(entry.getInstanceReference().getIdentifier())) {
				// convert the entry to concrete instance to sync all properties
				Instance actualInstance = converter.convert(entry.getActualInstanceClass(), entry);
				if (actualInstance != null) {
					// save changes only
					propertiesService.saveProperties(actualInstance, true);
				}
			}
			eventService.fire(new ScheduleEntryUpdatedEvent(oldEntry, entry));
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);
		}
		return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void deleteEntry(ScheduleEntry entry) {
		// perform the deletion
		scheduleEntryDao.delete(entry);
		// notify for deletion
		eventService.fire(new ScheduleEntryDeletedEvent(entry));
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ScheduleEntry cancelEntry(ScheduleEntry entry) {
		if ((entry != null) && (entry.getActualInstance() != null)) {
			Instance instance = instanceService.cancel(entry.getActualInstance());
			entry.setActualInstance(instance);

			eventService.fire(new ScheduleEntryCanceledEvent(entry));
		}
		return entry;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<ScheduleEntry> getChildren(Serializable nodeId) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("parentId", nodeId));
		List<Long> list = dbDao.fetchWithNamed(
				DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_BY_PARENT_KEY, args);

		List<ScheduleEntry> entries = scheduleEntryDao.loadInstancesByDbKey(list);
		return entries;
	}

	@Override
	public Map<String, List<DefinitionModel>> getAllowedChildrenForNode(ScheduleEntry targetNode,
			List<ScheduleEntry> currentChildren) {
		ScheduleEntryAllowedChildrenProvider provider = new ScheduleEntryAllowedChildrenProvider(
				currentChildren, typeProvider, converter, dictionaryService);
		Instance instance = null;
		if (targetNode.getId() != null) {
			ScheduleEntry entity = scheduleEntryDao.loadInstance(targetNode.getId(), null, true);
			if (entity != null) {
				instance = entity.getActualInstance();
			}
			if (instance == null) {
				instance = entity;
			}
		} else {
			// if the concrete type and definition are not set then we can't do anything
			if (StringUtils.isNullOrEmpty(targetNode.getIdentifier())
					|| (targetNode.getActualInstanceClass() == null)) {
				// probably here we can return same default values
				return Collections.emptyMap();
			}
		}
		if (instance == null) {
			instance = targetNode;
		}
		Map<String, List<DefinitionModel>> allowedChildren = AllowedChildrenHelper
				.getAllowedChildren(instance, provider, dictionaryService);
		return allowedChildren;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<ScheduleEntry> startEntries(ScheduleInstance scheduleInstance,
			List<ScheduleEntry> entries) {
		if ((entries == null) || entries.isEmpty()) {
			return Collections.emptyList();
		}

		List<ScheduleEntry> started = new ArrayList<ScheduleEntry>(entries.size());

		for (ScheduleEntry scheduleEntry : entries) {
			ScheduleEntry startedScheduleEntry = dbDao.invokeInNewTx(new ScheduleEntryStarter(scheduleInstance, scheduleEntry));
			if (startedScheduleEntry != null) {
				started.add(startedScheduleEntry);
			}
		}
		return started;
	}

	/**
	 * Gets the schedule by actual instance cache.
	 *
	 * @return the schedule by actual instance cache
	 */
	protected EntityLookupCache<Pair<String, Long>, Serializable, Serializable> getScheduleByActualInstanceCache() {
		return cacheContext.getCache(SCHEDULE_ACTUAL_INSTANCE_ENTITY_ID_CACHE);
	}

	/**
	 * Gets the schedule entry by actual instance cache.
	 *
	 * @return the schedule entry by actual instance cache
	 */
	protected EntityLookupCache<Pair<String, Long>, Serializable, Serializable> getScheduleEntryByActualInstanceCache() {
		return cacheContext.getCache(SCHEDULE_ENTRY_ACTUAL_INSTANCE_ENTITY_ID_CACHE);
	}

	/**
	 * Entity lookup DAO that fetches the schedule entity id by actual instance reference
	 *
	 * @author BBonev
	 */
	public class ScheduleActualInstanceReferenceLookup extends
			EntityLookupCallbackDAOAdaptor<Pair<String, Long>, Serializable, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, Long>, Serializable> findByKey(Pair<String, Long> key) {
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
			args.add(new Pair<String, Object>("sourceId", key.getFirst()));
			args.add(new Pair<String, Object>("sourceTypeId", key.getSecond()));
			List<Long> fetchWithNamed = dbDao.fetchWithNamed(
					DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTITY_BY_REFERENCE_KEY, args);
			if (fetchWithNamed.isEmpty()) {
				return null;
			}
			Long scheduleId = fetchWithNamed.get(0);
			return new Pair<Pair<String, Long>, Serializable>(key, scheduleId);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, Long>, Serializable> createValue(Serializable value) {
			throw new UnsupportedOperationException("Cannot create entry");
		}
	}

	/**
	 * Entity lookup DAO that fetches the schedule entry entity id by actual instance reference
	 *
	 * @author BBonev
	 */
	public class ScheduleEntryActualInstanceReferenceLookup extends
			EntityLookupCallbackDAOAdaptor<Pair<String, Long>, Serializable, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, Long>, Serializable> findByKey(Pair<String, Long> key) {
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
			args.add(new Pair<String, Object>("sourceId", key.getFirst()));
			args.add(new Pair<String, Object>("sourceTypeId", key.getSecond()));
			List<Long> fetchWithNamed = dbDao.fetchWithNamed(
					DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_BY_REFERENCE_KEY, args);
			if (fetchWithNamed.isEmpty()) {
				return null;
			}
			if (fetchWithNamed.size() >= 2) {
				throw new EmfRuntimeException(
						"Found more then one schedule entry for instance reference: " + key);
			}

			Long entryId = fetchWithNamed.get(0);
			return new Pair<Pair<String, Long>, Serializable>(key, entryId);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, Long>, Serializable> createValue(Serializable value) {
			throw new UnsupportedOperationException("Cannot create entry");
		}
	}

	@Override
	public Map<String, List<ScheduleEntry>> searchAssignments(ScheduleInstance instance,
			List<Serializable> resources, DateRange timeFrame) {
		Map<String, List<ScheduleEntry>> result = null;
		List<Long> instanceTypesLocal = getSearchedTaskTypes();
		// the unassigned tasks
		if ((instance != null) && ((resources == null) || resources.isEmpty())) {
			List<ScheduleEntry> fetchedWithNamed = executeNativeQuery(instance, instanceTypesLocal);
			result = new HashMap<String, List<ScheduleEntry>>(1);
			result.put("-1", fetchedWithNamed);
			return result;
		}
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(5);
		args.add(new Pair<String, Object>("instanceTypes", instanceTypesLocal));
		if ((resources != null) && !resources.isEmpty()) {
			// for specific persons, for one or any project
			args.add(new Pair<String, Object>("resourceIds", resources));
			List<Serializable> scheduleIds = null;
			if (instance == null) {
				scheduleIds = Collections.singletonList((Serializable) (-1L));
			} else {
				scheduleIds = Collections.singletonList(instance.getId());
			}
			args.add(new Pair<String, Object>("scheduleIds", scheduleIds));

			if (timeFrame == null) {
				// TODO find better way. now is -100 +100 year gap
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.YEAR, -100);
				timeFrame = new DateRange(calendar.getTime(), null);
				calendar.add(Calendar.YEAR, 200);
				timeFrame.setSecond(calendar.getTime());
			}

			args.add(new Pair<String, Object>("startDate", timeFrame.getFirst()));
			args.add(new Pair<String, Object>("endDate", timeFrame.getSecond()));

			// find the actual dta
			List<Long> assignmentsIds = dbDao.fetchWithNamed(
					DbQueryTemplatesSchedule.QUERY_RESOURCES_ALLOCATIONS_KEY, args);

			// the assignments found. now start grouping by user
			List<ScheduleAssignment> assignments = scheduleResourceService
					.getAssignments(assignmentsIds);
			Map<Serializable, ScheduleEntry> scheduleEntryIds = new LinkedHashMap<>();
			for (ScheduleAssignment scheduleAssignment : assignments) {
				scheduleEntryIds.put(scheduleAssignment.getTaskId(), null);
			}
			// load and cache the entries to be key searchable by id
			List<ScheduleEntry> loadInstancesByDbKey = scheduleEntryDao
					.loadInstancesByDbKey(new ArrayList<>(scheduleEntryIds.keySet()));
			for (ScheduleEntry entry : loadInstancesByDbKey) {
				scheduleEntryIds.put(entry.getId(), entry);
			}
			result = new HashMap<>();
			// do the final grouping
			for (ScheduleAssignment scheduleAssignment : assignments) {
				String resourceId = scheduleAssignment.getResourceId();
				if (!result.containsKey(resourceId)) {
					result.put(resourceId, new LinkedList<ScheduleEntry>());
				}
				result.get(resourceId).add(scheduleEntryIds.get(scheduleAssignment.getTaskId()));
			}
		}
		return result;
	}

	/**
	 * Executes native query. TODO find better way to use joins
	 *
	 * @param instance
	 *            is the schedule instance to include in request
	 * @param instanceTypesParam
	 *            are the instance types searched
	 * @return list of unassigned schedule entries
	 */
	@SuppressWarnings("unchecked")
	private List<ScheduleEntry> executeNativeQuery(ScheduleInstance instance,
			List<Long> instanceTypesParam) {
		String types = instanceTypesParam.toString();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append("select distinct se.id as id from  pmfs_scheduleentryentity  se  left join pmfs_scheduleassignment sa on ");
		stringBuilder.append("(sa.task_id=se.id) where sa.task_id is null and se.schedule_id in (");
		stringBuilder.append(instance.getId());
		stringBuilder.append(") and se.actualinstancetype in (");
		stringBuilder.append(types.substring(1, types.length() - 1));
		stringBuilder.append(") order by se.id");
		SQLQuery createSQLQuery = ((Session) entityManager.getDelegate())
				.createSQLQuery(stringBuilder.toString());
		createSQLQuery.addScalar("id", new LongType());
		List<Long> fetched = createSQLQuery.list();
		List<ScheduleEntry> fetchedWithNamed = scheduleEntryDao
				.loadInstancesByDbKey(fetched);
		return fetchedWithNamed;
	}

	/**
	 * Get all task types that are searchable.
	 *
	 * @return list of ids for searched entry task types
	 */
	private List<Long> getSearchedTaskTypes() {
		if (instanceTypes == null) {
			DataTypeDefinition taskDefinition = dictionaryService
					.getDataTypeDefinition(TaskInstance.class.getSimpleName().toLowerCase());
			DataTypeDefinition standaloneTaskDefinition = dictionaryService
					.getDataTypeDefinition(StandaloneTaskInstance.class.getSimpleName()
							.toLowerCase());
			instanceTypes = new ArrayList<>();
			instanceTypes.add(taskDefinition.getId());
			instanceTypes.add(standaloneTaskDefinition.getId());
		}
		return instanceTypes;
	}

	/**
	 * Starts a schedule entry. Returns the started entry.
	 */
	private class ScheduleEntryStarter implements Callable<ScheduleEntry> {
		private ScheduleEntry scheduleEntry;
		private ScheduleInstance scheduleInstance;

		/**
		 * Instantiates a new schedule entry starter.
		 * Class implements {@link Callable} interface and returns started entries after called.
		 *
		 * @param scheduleInstance the schedule instance
		 * @param scheduleEntry the schedule entry
		 */
		public ScheduleEntryStarter(ScheduleInstance scheduleInstance, ScheduleEntry scheduleEntry) {
			this.scheduleInstance = scheduleInstance;
			this.scheduleEntry = scheduleEntry;
		}

		@Override
		public ScheduleEntry call() throws Exception {

			ScheduleEntry resultScheduleEntry = null;

			if (scheduleEntry.getActualInstanceClass() == null) {
				return null;
			}
			scheduleEntry.setScheduleId((Long) scheduleInstance.getId());
			StartedInstance startedInstance = null;
			try {
				RuntimeConfiguration.setConfiguration(
						ScheduleRuntimeConfiguration.CURRENT_SCHEDULE_ENTRY, scheduleEntry);
				// REVIEW:BB: this should be refactored!!!!
				Instance instance = ReflectionUtils.newInstance(scheduleEntry
						.getActualInstanceClass());
				startedInstance = converter.convert(StartedInstance.class, instance);
				startedInstance = converter.convert(startedInstance.getClass(), new StartInstance(
						scheduleEntry));
			} catch (Exception e) {
				logger.error("Instance not started! Error while starting: " + e.getMessage());
				if (logger.isDebugEnabled()) {
					logger.debug("", e);
				}
			} finally {
				RuntimeConfiguration
						.clearConfiguration(ScheduleRuntimeConfiguration.CURRENT_SCHEDULE_ENTRY);
			}
			if ((startedInstance != null) && (startedInstance.getTarget() != null)) {
				// updated the actual instance reference if any
				InstanceReference reference = converter.convert(InstanceReference.class,
						startedInstance.getTarget());
				// update cache
				getScheduleEntryByActualInstanceCache().setValue(convertToCachePair(reference),
						scheduleEntry.getId());
				scheduleEntry.setInstanceReference(reference);
				scheduleEntry.setActualInstanceId(startedInstance.getTarget().getId());
				scheduleEntry.setActualInstance(startedInstance.getTarget());
				if (scheduleEntry.getActualInstanceClass() == null) {
					scheduleEntry.setActualInstanceClass(startedInstance.getTarget().getClass());
				}
				// fire event for started entry
				eventService.fire(new ScheduleEntryStartedEvent(scheduleEntry));

				// this should not be saved here, because overrides already calculated state
				scheduleEntry.getProperties().remove(ScheduleEntryProperties.STATUS);
				// save changes to the entry
				resultScheduleEntry = saveInternal(scheduleEntry, false);
			}

			return resultScheduleEntry;
		}
	}
}
