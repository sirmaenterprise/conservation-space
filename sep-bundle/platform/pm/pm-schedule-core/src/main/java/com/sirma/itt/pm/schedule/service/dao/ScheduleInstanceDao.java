package com.sirma.itt.pm.schedule.service.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.db.DbQueryTemplatesSchedule;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleEntity;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;
import com.sirma.itt.pm.services.impl.dao.BasePmInstanceDaoImpl;

/**
 * Instance DAO to support the schedule instance operations for loading and cache managing.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesPms.SCHEDULE)
public class ScheduleInstanceDao
		extends
		BasePmInstanceDaoImpl<ScheduleInstance, ScheduleEntity, Long, Serializable, DefinitionModel>
		implements InstanceDao<ScheduleInstance> {

	/** The Constant SCHEDULE_ENTITY_CACHE. */
	@CacheConfiguration(container = "pm", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 12000000, interval = 60000), doc = @Documentation(""
			+ "Cache used to contain the schedule instances for all active users. "
			+ "The cache will have at most an entry for every opened schedule per user."
			+ "<br>Minimal value expression: users"))
	private static final String SCHEDULE_ENTITY_CACHE = "SCHEDULE_ENTITY_CACHE";

	/** The db dao. */
	@Inject
	private DbDao dbDao;
	/**
	 * {@inheritDoc}
	 */
	@Override
	@PostConstruct
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void initialize() {
		super.initialize();
	}

	@Override
	protected void updateCreatorAndModifierInfo(ScheduleInstance instance) {
		// no need to set anything
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ScheduleInstance persistChanges(ScheduleInstance instance) {
		return super.persistChanges(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Entity saveEntity(Entity entity) {
		return super.saveEntity(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void savePropertiesOnPersistChanges(ScheduleInstance instance) {
		propertiesService.saveProperties(instance, true, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveProperties(ScheduleInstance instance, boolean addOnly) {
		propertiesService.saveProperties(instance, addOnly, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void synchRevisions(ScheduleInstance instance, Long revision) {
		instance.setRevision(revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCacheEntityName() {
		return SCHEDULE_ENTITY_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityLookupCallbackDAOAdaptor<Long, ScheduleEntity, Serializable> getEntityCacheProvider() {
		return new DefaultPrimaryKeyEntityLookupDao();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<Long>> List<E> findEntitiesByPrimaryKey(Set<Long> ids) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("ids", ids));
		return getDbDao().fetchWithNamed(DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTITY_BY_IDS_KEY, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<?>> Long getPrimaryKey(E entity) {
		return (Long) entity.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ScheduleInstance> getInstanceClass() {
		return ScheduleInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ScheduleEntity> getEntityClass() {
		return ScheduleEntity.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<DefinitionModel> getDefinitionClass() {
		// no definition is supported
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ScheduleEntity createEntity(Serializable dmsId) {
		// not supported for now
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> Serializable getSecondaryKey(E instance) {
		// no secondary key is supported
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateInstanceForModel(ScheduleInstance instance, DefinitionModel model) {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<Long>> List<E> findEntities(Set<Serializable> ids) {
		return Collections.emptyList();
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
	protected String getOwningInstanceQuery() {
		return null;
	}
}
