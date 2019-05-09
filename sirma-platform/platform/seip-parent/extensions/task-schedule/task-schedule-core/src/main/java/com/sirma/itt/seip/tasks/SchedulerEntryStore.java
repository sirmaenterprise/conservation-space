package com.sirma.itt.seip.tasks;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.lookup.BaseEntityLookupDao;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.CoreDb;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.cache.util.CacheUtils;
import com.sirma.itt.seip.cache.util.CacheUtils.BatchPrimaryKeyEntityLoaderCallback;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.ContextNotActiveException;
import com.sirma.itt.seip.serialization.SerializationHelper;
import com.sirma.itt.seip.tasks.entity.EventTriggerEntity;
import com.sirma.itt.seip.tasks.entity.SchedulerEntity;
import com.sirma.itt.seip.util.DigestUtils;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Provides persistence and caching for scheduler entries
 *
 * @author BBonev
 */
@ApplicationScoped
class SchedulerEntryStore {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@CacheConfiguration(eviction = @Eviction(maxEntries = 1000) , expiration = @Expiration(maxIdle = 600000, interval = 720000) , doc = @Documentation(""
			+ "Cache used to store the information for scheduled actions in the system."
			+ "For every action there are 2 entries in the cache. " + "<br>Minimal value expression: users * 20") )
	private static final String SCHEDULER_ACTION_CACHE = "SCHEDULER_ACTION_CACHE";
	@CacheConfiguration(eviction = @Eviction(maxEntries = 1000) , expiration = @Expiration(maxIdle = 600000, interval = 720000) , doc = @Documentation(""
			+ "Cache used to store the information for scheduled event actions in the system. The key for this cache is a schedule action event trigger and the value is the schedule action id."
			+ "For every event action there is an entry in the cache. " + "<br>Minimal value expression: users * 20") )
	private static final String SCHEDULER_ACTION_EVENT_CACHE = "SCHEDULER_ACTION_EVENT_CACHE";

	/**
	 * The maximum identifier field size
	 */
	private static final int ID_SIZE = 100;

	@Inject
	private DbDao dbDao;
	@Inject
	@CoreDb
	private DbDao coreDbDao;
	@Inject
	private EntityLookupCacheContext cacheContext;
	@Inject
	private SecurityContext securityContext;
	@Inject
	private SchedulerDao schedulerDao;
	@Inject
	private SerializationHelper serializationHelper;

	private BatchPrimaryKeyEntityLoaderCallback<Long, SchedulerEntity> primaryKeyEntityLoaderCallback = new SchedulerEntityLoaderCallback();

	@PostConstruct
	void initialize() {
		cacheContext.createCacheIfAbsent(SCHEDULER_ACTION_CACHE, true, new SchedulerEntityLookup().enableSecondaryKeyManagement());
		cacheContext.createCacheIfAbsent(SCHEDULER_ACTION_EVENT_CACHE, true, new SchedulerActionEventLookup());
	}

	/**
	 * Persist and update cache for the given {@link SchedulerEntity}
	 *
	 * @param entity
	 *            the entity
	 * @return the scheduler entity
	 */
	SchedulerEntity persist(SchedulerEntity entity) {
		SchedulerEntity update = getDbDao().saveOrUpdate(entity);
		update.setTenantId(entity.getTenantId());
		updateCache(update);
		return update;
	}

	/**
	 * Save changes for the given {@link SchedulerEntry}. If the method {@link SchedulerEntry#isForRemoval()} returns
	 * <code>true</code> then the entry will be deleted instead of saved. Otherwise the method will copy any changes to
	 * the database and persist changes and update the cache.
	 *
	 * @param entry
	 *            the entry to save
	 * @return true if entity was saved successfully and false if the entity was not found or deleted by the method
	 */
	boolean saveChanges(SchedulerEntry entry) {
		SchedulerEntity currentEntity = findById(entry.getId());
		if (currentEntity != null) {
			// if completed and marked for deletion we should delete the entry
			// if there is some other logic different that deletion it should come here
			if (entry.isForRemoval()) {
				delete(currentEntity.getId());
			} else {
				// copy status to cached value
				currentEntity.setStatus(entry.getStatus());
				// update the current retry count
				if (entry.getConfiguration().getRetryCount() > 0) {
					currentEntity.setRetries(entry.getConfiguration().getRetryCount());
				}

				Date nextScheduleTime = entry.getConfiguration().getNextScheduleTime();
				if (!EqualsHelper.nullSafeEquals(currentEntity.getNextScheduleTime(), nextScheduleTime)) {
					currentEntity.setNextScheduleTime(nextScheduleTime);
				}

				currentEntity.setContextData(serializationHelper, entry.getConfiguration(), entry.getContext());
				// update entry and store in db and update cache
				// the DB save is performed by the cache callback
				persist(currentEntity);
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieve all scheduler entities with the given status.
	 *
	 * @param status
	 *            the status
	 * @return the scheduler entities with that status
	 */
	List<SchedulerEntity> findByStatus(SchedulerEntryStatus status) {
		if (status == null) {
			return CollectionUtils.emptyList();
		}
		List<Long> entitiesIds = getDbDao().fetchWithNamed(SchedulerEntity.QUERY_SCHEDULER_ENTRY_ID_BY_STATUS_KEY,
				Arrays.asList(new Pair<>("status", status)));
		return findEntities(entitiesIds);
	}

	/**
	 * Delete entity from cache and database
	 *
	 * @param id
	 *            the id
	 */
	void delete(Long id) {
		LOGGER.trace("Deleting scheduler entity with id={}", id);
		getCache().deleteByKey(id);
	}

	/**
	 * Find entity by db id.
	 *
	 * @param id
	 *            the id
	 * @return the scheduler entity
	 */
	SchedulerEntity findById(Long id) {
		Pair<Long, SchedulerEntity> cacheValue = getCache().getByKey(id);
		if (cacheValue == null) {
			return null;
		}
		SchedulerEntity entity = cacheValue.getSecond();
		setTenantId(entity);
		return entity;
	}

	/**
	 * Find by scheduler entry identifier.
	 *
	 * @param schedulerID
	 *            the scheduler id
	 * @return the scheduler entity
	 */
	SchedulerEntity findByIdentifier(String schedulerID) {
		if (schedulerID == null) {
			return null;
		}

		SchedulerEntity value = new SchedulerEntity();
		value.setIdentifier(truncateIdIfNeeded(schedulerID, ID_SIZE));
		Pair<Long, SchedulerEntity> pair = getCache().getByValue(value);
		if (pair != null) {
			SchedulerEntity entity = pair.getSecond();
			setTenantId(entity);
			return entity;
		}
		return null;
	}

	/**
	 * Gets the or create entity for the given identifier. If <code>null</code> identifier is passed then new one will
	 * be generated and return.
	 *
	 * @param identifier
	 *            the identifier to fetch entity for or use to generate new entry if not found. If <code>null</code>
	 *            random id will be generated.
	 * @return the found entity for identifier or newly created one. Never <code>null</code>.
	 */
	SchedulerEntity getOrCreateEntityForIdentifier(String identifier) {
		SchedulerEntity entity = new SchedulerEntity();
		setTenantId(entity);
		if (StringUtils.isBlank(identifier)) {
			// generate identifier
			entity.setIdentifier(generateIdentifier());
		} else {
			// check if identifier-a already exists
			entity.setIdentifier(truncateIdIfNeeded(identifier, ID_SIZE));
			// check using the cache
			Pair<Long, SchedulerEntity> pair = getCache().getByValue(entity);
			if (pair != null) {
				// we will merge the event
				entity = pair.getSecond();
				setTenantId(entity);
				// reset status
				if (entity.getStatus() == SchedulerEntryStatus.COMPLETED
						|| entity.getStatus() == SchedulerEntryStatus.FAILED) {
					entity.setStatus(SchedulerEntryStatus.PENDING);
				}
			}
		}
		return entity;
	}

	private static String generateIdentifier() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Find entities for the given primary ids. Checks the cache first.
	 *
	 * @param ids
	 *            the ids to look for
	 * @return the list of found entities
	 */
	List<SchedulerEntity> findEntities(List<Long> ids) {
		List<SchedulerEntity> list = CacheUtils.batchLoadByPrimaryKey(ids, getCache(), primaryKeyEntityLoaderCallback);
		setTenantId(list);
		return list;
	}

	/**
	 * Truncate id if needed so that it can be inserted into the table. If the id is greater that the limit a digest
	 * will be build and used as a prefix to the actual returned id. The remaining characters to the maximum will be
	 * filled with the end of the passed string.
	 *
	 * @param identifier
	 *            the identifier
	 * @return the string
	 */
	static String truncateIdIfNeeded(String identifier, int limit) {
		return DigestUtils.truncateWithDigest(identifier, limit);
	}

	/**
	 * Gets a entity that matches the given event trigger. Checks the cache first.
	 *
	 * @param triggerEntity
	 *            the trigger entity
	 * @return the entry that matches the trigger or <code>null</code> if not found any matching
	 */
	SchedulerEntity getByEntryByEventTrigger(EventTriggerEntity triggerEntity) {
		Pair<EventTriggerEntity, Long> key = getEventCache().getByKey(triggerEntity);
		if (key != null) {
			Pair<Long, SchedulerEntity> pair = getCache().getByKey(key.getSecond());
			if (pair != null) {
				return pair.getSecond();
			}
		}
		return null;
	}

	private DbDao getDbDao() {
		if (!securityContext.isActive()) {
			throw new ContextNotActiveException();
		}
		if (securityContext.isSystemTenant()) {
			return coreDbDao;
		}
		return dbDao;
	}

	private void updateCache(SchedulerEntity entity) {
		getCache().setValue(entity.getId(), entity);
		if (entity.getEventTrigger() != null) {
			getEventCache().setValue(entity.getEventTrigger().createCopy(), entity.getId());
		}
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

	private void setTenantId(SchedulerEntity entity) {
		entity.setTenantId(securityContext.getCurrentTenantId());
	}

	private void setTenantId(List<SchedulerEntity> list) {
		list.forEach(entity -> entity.setTenantId(securityContext.getCurrentTenantId()));
	}

	/**
	 * Batch entity loader implementation.
	 *
	 * @author BBonev
	 */
	private final class SchedulerEntityLoaderCallback
			implements BatchPrimaryKeyEntityLoaderCallback<Long, SchedulerEntity> {

		@Override
		public Long getPrimaryKey(SchedulerEntity entity) {
			return entity.getId();
		}

		@Override
		public List<SchedulerEntity> findEntitiesByPrimaryKey(Set<Long> secondPass) {
			List<SchedulerEntity> list = schedulerDao.findEntitiesByPrimaryKey(secondPass);
			setTenantId(list);
			return list;
		}
	}

	/**
	 * Entity lookup DAO that works with {@link EventTriggerEntity} to {@link SchedulerEntity#getId()}.
	 *
	 * @author BBonev
	 */
	class SchedulerActionEventLookup
			extends EntityLookupCallbackDAOAdaptor<EventTriggerEntity, Long, Serializable> {

		@Override
		public Pair<EventTriggerEntity, Long> findByKey(EventTriggerEntity key) {
			List<Long> list = schedulerDao.findEntitiesForTrigger(key);
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				// .. we have a problem here...
				LOGGER.warn("Found more then one Scheduler entry with same event trigger!!");
			}
			Long id = list.get(0);
			return new Pair<>(key, id);
		}

		@Override
		public Pair<EventTriggerEntity, Long> createValue(Long value) {
			throw new UnsupportedOperationException("Cannot create entry using a event trigger");
		}

	}

	/**
	 * Entity lookup DAO for {@link SchedulerEntity}.
	 *
	 * @author BBonev
	 */
	class SchedulerEntityLookup extends BaseEntityLookupDao<SchedulerEntity, String, Long> {

		@Override
		public Pair<Long, SchedulerEntity> findByKey(Long key) {
			Pair<Long, SchedulerEntity> pair = super.findByKey(key);
			if (pair != null && pair.getSecond() != null) {
				setTenantId(pair.getSecond());
			}
			return pair;
		}

		@Override
		protected Class<SchedulerEntity> getEntityClass() {
			return SchedulerEntity.class;
		}

		@Override
		protected DbDao getDbDao() {
			return SchedulerEntryStore.this.getDbDao();
		}

		@Override
		protected String getValueKeyInternal(SchedulerEntity value) {
			return value.getIdentifier();
		}

		@Override
		protected List<SchedulerEntity> fetchEntityByValue(String key) {
			List<SchedulerEntity> list = schedulerDao.findEntriesForIdentifier(key);
			setTenantId(list);
			return list;
		}
	}
}
