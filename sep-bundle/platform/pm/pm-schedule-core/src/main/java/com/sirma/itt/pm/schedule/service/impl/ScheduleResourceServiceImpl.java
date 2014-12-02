/*
 *
 */
package com.sirma.itt.pm.schedule.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.BaseEntityLookupDao;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader.BatchPrimaryKeyEntityLoaderCallback;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.db.DbQueryTemplatesSchedule;
import com.sirma.itt.pm.schedule.event.ScheduleAssignmentAddedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleAssignmentDeletedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleAssignmentUpdatedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleDependencyAddedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleDependencyDeletedEvent;
import com.sirma.itt.pm.schedule.event.ScheduleDependencyUpdatedEvent;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;

/**
 * Default implementation for schedule resource service.
 * 
 * @author BBonev
 */
@Stateless
public class ScheduleResourceServiceImpl implements ScheduleResourceService {

	/** The Constant DEPENDENCY_ENTITY_CACHE. */
	@CacheConfiguration(container = "pm", eviction = @Eviction(maxEntries = 10000), expiration = @Expiration(maxIdle = 6000000, interval = 60000), doc = @Documentation(""
			+ "Cache used to contain the schedule dependency entry for all active users. "
			+ "The cache will have 2 entries for every dependency on the each opened schedule. "
			+ "<br>Minimal value expression: (users * averageScheduleElements) * 2.4"))
	public static final String SCHEDULE_DEPENDENCY_ENTITY_CACHE = "SCHEDULE_DEPENDENCY_ENTITY_CACHE";

	/** The Constant SCHEDULE_ASSIGNMENT_ENTITY_CACHE. */
	@CacheConfiguration(container = "pm", eviction = @Eviction(maxEntries = 20000), expiration = @Expiration(maxIdle = 6000000, interval = 60000), doc = @Documentation(""
			+ "Cache used to contain the schedule assignment between a schedule entry and schedule resource on the opened schedules. "
			+ "<br>Minimal value expression: ((users * 1.5) * averageScheduleElements) * 2.5"))
	public static final String SCHEDULE_ASSIGNMENT_ENTITY_CACHE = "SCHEDULE_ASSIGNMENT_ENTITY_CACHE";

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleResourceServiceImpl.class);

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The dependency callback. */
	private BatchPrimaryKeyEntityLoaderCallback<Long, ScheduleDependency> dependencyCallback;
	/** The assignment callback. */
	private BatchPrimaryKeyEntityLoaderCallback<Long, ScheduleAssignment> assignmentCallback;

	private static final List<Serializable> NO_ID = Arrays.asList((Serializable) (-1l));

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		if (!cacheContext.containsCache(SCHEDULE_ASSIGNMENT_ENTITY_CACHE)) {
			cacheContext.createCache(SCHEDULE_ASSIGNMENT_ENTITY_CACHE,
					new ScheduleAssignmentLookUp());
		}
		if (!cacheContext.containsCache(SCHEDULE_DEPENDENCY_ENTITY_CACHE)) {
			cacheContext.createCache(SCHEDULE_DEPENDENCY_ENTITY_CACHE,
					new ScheduleDependencyLookUp());
		}

		dependencyCallback = new ScheduleDependencyLoader();
		assignmentCallback = new ScheduleAssignmentLoader();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ScheduleAssignment add(ScheduleAssignment assignment) {
		ScheduleAssignment savedAssignment = saveInternal(assignment);

		eventService.fire(new ScheduleAssignmentAddedEvent(savedAssignment));
		return savedAssignment;
	}

	/**
	 * Save internal.
	 * 
	 * @param assignment
	 *            the assignment
	 * @return the schedule assignment
	 */
	private ScheduleAssignment saveInternal(ScheduleAssignment assignment) {
		ScheduleAssignment savedAssignment = dbDao.saveOrUpdate(assignment);
		getAssignmentCache().setValue(savedAssignment.getId(), savedAssignment);
		return savedAssignment;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ScheduleAssignment save(ScheduleAssignment assignment) {
		Pair<Long, ScheduleAssignment> pair = getAssignmentCache().getByValue(assignment);
		if (pair == null) {
			return add(assignment);
		}
		eventService.fire(new ScheduleAssignmentUpdatedEvent(pair.getSecond(), assignment));
		copy(assignment, pair.getSecond());
		// probably good idea is to add cloning of the objects to separate them from the cached
		// instance
		return saveInternal(pair.getSecond());
	}

	/**
	 * Copy.
	 * 
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 */
	private void copy(ScheduleAssignment source, ScheduleAssignment destination) {
		destination.setScheduleId(source.getScheduleId());
		destination.setResourceId(source.getResourceId());
		destination.setTaskId(source.getTaskId());
		destination.setUnits(source.getUnits());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(ScheduleAssignment assignment) {
		deleteByKey(assignment, getAssignmentCache());

		eventService.fire(new ScheduleAssignmentDeletedEvent(assignment));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ScheduleDependency add(ScheduleDependency dependency) {
		ScheduleDependency savedDependency = saveInternal(dependency);

		eventService.fire(new ScheduleDependencyAddedEvent(savedDependency));
		return savedDependency;
	}

	/**
	 * Save internal.
	 * 
	 * @param dependency
	 *            the dependency
	 * @return the schedule dependency
	 */
	private ScheduleDependency saveInternal(ScheduleDependency dependency) {
		ScheduleDependency savedDependency = dbDao.saveOrUpdate(dependency);
		getDependencyCache().setValue(savedDependency.getId(), savedDependency);
		return savedDependency;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ScheduleDependency save(ScheduleDependency dependency) {
		Pair<Long, ScheduleDependency> pair = getDependencyCache().getByValue(dependency);
		if (pair == null) {
			return add(dependency);
		}
		eventService.fire(new ScheduleDependencyUpdatedEvent(pair.getSecond(), dependency));
		copy(dependency, pair.getSecond());
		// probably good idea is to add cloning of the objects to separate them from the cached
		// instance
		return saveInternal(pair.getSecond());
	}

	/**
	 * Copy.
	 * 
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 */
	private void copy(ScheduleDependency source, ScheduleDependency destination) {
		destination.setCssClass(source.getCssClass());
		destination.setFrom(source.getFrom());
		destination.setLag(source.getLag());
		destination.setLagUnit(source.getLagUnit());
		destination.setScheduleId(source.getScheduleId());
		destination.setTo(source.getTo());
		destination.setType(source.getType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(ScheduleDependency dependency) {
		deleteByKey(dependency, getDependencyCache());

		eventService.fire(new ScheduleDependencyDeletedEvent(dependency));
	}

	/**
	 * Deletes an entity from the given cache by primary or secondary key.
	 * 
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param entity
	 *            the entity
	 * @param cache
	 *            the cache to use
	 */
	private <E extends Entity<Long>, S extends Serializable> void deleteByKey(E entity,
			EntityLookupCache<Long, E, ?> cache) {
		Long id = entity.getId();
		if (id == null) {
			Pair<Long, E> pair = cache.getByValue(entity);
			if (pair != null) {
				id = pair.getFirst();
			}
		}
		// element not found or invalid
		if (id != null) {
			cache.deleteByKey(id);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<Resource> getResources(Instance instance, Serializable scheduleId) {
		InstanceReference reference = typeConverter.convert(InstanceReference.class, instance);
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
		args.add(new Pair<String, Object>("sourceId", reference.getIdentifier()));
		args.add(new Pair<String, Object>("sourceType", reference.getReferenceType().getId()));
		args.add(new Pair<String, Object>("scheduleId", scheduleId));
		List<Serializable> list = dbDao.fetchWithNamed(
				DbQueryTemplatesSchedule.QUERY_RESOURCES_KEY, args);
		List<Resource> resources = resourceService.getResources(list);
		return resources;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<ScheduleAssignment> getAssignments(Serializable scheduleId) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("scheduleId", scheduleId));
		List<Long> list = dbDao.fetchWithNamed(
				DbQueryTemplatesSchedule.QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_KEY, args);
		return BatchEntityLoader.batchLoadByPrimaryKey(list, getAssignmentCache(),
				assignmentCallback);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<ScheduleAssignment> getAssignments(List<Long> assignmentIds) {
		if ((assignmentIds == null) || assignmentIds.isEmpty()) {
			return Collections.emptyList();
		}
		return BatchEntityLoader.batchLoadByPrimaryKey(assignmentIds, getAssignmentCache(),
				assignmentCallback);
	}

	@Override
	public List<ScheduleAssignment> getAssignments(ScheduleEntry entry) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("taskId", entry.getId()));
		List<Long> list = dbDao.fetchWithNamed(
				DbQueryTemplatesSchedule.QUERY_ASSIGNMENT_ID_FOR_SCHEDULE_ENTRY_KEY, args);
		return BatchEntityLoader.batchLoadByPrimaryKey(list, getAssignmentCache(),
				assignmentCallback);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<ScheduleDependency> getDependencies(List<Long> dependenciesIds) {
		if ((dependenciesIds == null) || dependenciesIds.isEmpty()) {
			return Collections.emptyList();
		}
		return BatchEntityLoader.batchLoadByPrimaryKey(dependenciesIds, getDependencyCache(),
				dependencyCallback);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<ScheduleDependency> getDependencies(Serializable scheduleId) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("scheduleId", scheduleId));
		List<Long> list = dbDao.fetchWithNamed(
				DbQueryTemplatesSchedule.QUERY_DEPENDENCY_ID_FOR_SCHEDULE_KEY, args);
		return BatchEntityLoader.batchLoadByPrimaryKey(list, getDependencyCache(),
				dependencyCallback);
	}

	@Override
	public List<ScheduleDependency> getDependencies(ScheduleEntry entry, Boolean fromBoolean) {
		List<Pair<String, Object>> args = new ArrayList<>(2);
		List<Serializable> from = Arrays.asList(entry.getId());
		List<Serializable> to = from;
		if (Boolean.TRUE.equals(fromBoolean)) {
			to = NO_ID;
		} else if (Boolean.FALSE.equals(fromBoolean)) {
			from = NO_ID;
		}
		args.add(new Pair<String, Object>("from", from));
		args.add(new Pair<String, Object>("to", to));
		List<Long> list = dbDao.fetchWithNamed(
				DbQueryTemplatesSchedule.QUERY_DEPENDENCIES_FOR_ENTRY_KEY, args);

		return BatchEntityLoader.batchLoadByPrimaryKey(list, getDependencyCache(),
				dependencyCallback);
	}

	/**
	 * Gets the dependency cache.
	 * 
	 * @return the dependency cache
	 */
	private EntityLookupCache<Long, ScheduleDependency, Pair<Long, Long>> getDependencyCache() {
		return cacheContext.getCache(SCHEDULE_DEPENDENCY_ENTITY_CACHE);
	}

	/**
	 * Gets the assignment cache.
	 * 
	 * @return the assignment cache
	 */
	private EntityLookupCache<Long, ScheduleAssignment, Pair<Long, String>> getAssignmentCache() {
		return cacheContext.getCache(SCHEDULE_ASSIGNMENT_ENTITY_CACHE);
	}

	/**
	 * Entity lookup DAO that fetches a {@link ScheduleDependency} entities from the database.
	 * 
	 * @author BBonev
	 */
	public class ScheduleDependencyLookUp extends
			BaseEntityLookupDao<ScheduleDependency, Pair<Long, Long>, Long> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Logger getLogger() {
			return LOGGER;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Class<ScheduleDependency> getEntityClass() {
			return ScheduleDependency.class;
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
		protected Pair<Long, Long> getValueKeyInternal(ScheduleDependency value) {
			return new Pair<Long, Long>(value.getFrom(), value.getTo());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<ScheduleDependency> fetchEntityByValue(Pair<Long, Long> key) {
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
			args.add(new Pair<String, Object>("from", key.getFirst()));
			args.add(new Pair<String, Object>("to", key.getSecond()));
			return getDbDao().fetchWithNamed(DbQueryTemplatesSchedule.QUERY_DEPENDENCY_BY_PAIR_KEY,
					args);
		}
	}

	/**
	 * Entity lookup DAO that fetches a {@link ScheduleAssignment} entities from the database.
	 * 
	 * @author BBonev
	 */
	public class ScheduleAssignmentLookUp extends
			BaseEntityLookupDao<ScheduleAssignment, Pair<Long, String>, Long> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Logger getLogger() {
			return LOGGER;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Class<ScheduleAssignment> getEntityClass() {
			return ScheduleAssignment.class;
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
		protected Pair<Long, String> getValueKeyInternal(ScheduleAssignment value) {
			return new Pair<Long, String>(value.getTaskId(), value.getResourceId());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected List<ScheduleAssignment> fetchEntityByValue(Pair<Long, String> key) {
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
			args.add(new Pair<String, Object>("taskId", key.getFirst()));
			args.add(new Pair<String, Object>("resourceId", key.getSecond()));
			return getDbDao().fetchWithNamed(
					DbQueryTemplatesSchedule.QUERY_ASSIGNMENTS_BY_PAIR_KEY, args);
		}
	}

	/**
	 * The Class ScheduleDependencyLoader. {@link ScheduleDependency} loader callback
	 * 
	 * @author BBonev
	 */
	class ScheduleDependencyLoader implements
			BatchPrimaryKeyEntityLoaderCallback<Long, ScheduleDependency> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long getPrimaryKey(ScheduleDependency entity) {
			return entity.getId();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<ScheduleDependency> findEntitiesByPrimaryKey(Set<Long> secondPass) {
			return dbDao
					.fetchWithNamed(DbQueryTemplatesSchedule.QUERY_DEPENDENCY_BY_IDS_KEY,
							Arrays.asList(new Pair<String, Object>("ids", new ArrayList<Long>(
									secondPass))));
		}

	}

	/**
	 * The Class ScheduleAssignmentLoader. {@link ScheduleAssignment} loader callback
	 * 
	 * @author BBonev
	 */
	class ScheduleAssignmentLoader implements
			BatchPrimaryKeyEntityLoaderCallback<Long, ScheduleAssignment> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Long getPrimaryKey(ScheduleAssignment entity) {
			return entity.getId();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<ScheduleAssignment> findEntitiesByPrimaryKey(Set<Long> secondPass) {
			return dbDao
					.fetchWithNamed(DbQueryTemplatesSchedule.QUERY_ASSIGNMENTS_BY_IDS_KEY,
							Arrays.asList(new Pair<String, Object>("ids", new ArrayList<Long>(
									secondPass))));
		}

	}
}
