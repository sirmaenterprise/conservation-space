package com.sirma.itt.pm.schedule.service.dao;

import java.util.ArrayList;
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
import com.sirma.itt.emf.exceptions.StaleDataModificationException;
import com.sirma.itt.emf.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.db.DbQueryTemplatesSchedule;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryEntity;

/**
 * Instance DAO implementation for accessing schedule entry instances.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
public class ScheduleEntryInstanceDao extends
		BaseInstanceDaoImpl<ScheduleEntry, ScheduleEntryEntity, Long, String, DefinitionModel>
		implements InstanceDao<ScheduleEntry> {

	/** The Constant SCHEDULE_ENTRY_ENTITY_CACHE. */
	@CacheConfiguration(container = "pm", eviction = @Eviction(maxEntries = 10000), expiration = @Expiration(maxIdle = 6000000, interval = 60000), doc = @Documentation(""
			+ "Cache used to contain the schedule entries for all active users. "
			+ "The cache will have 2 entries for every row in every opened schedule. "
			+ "Entries are used even if the schedule is not opened when user works with actual instances that are part of a schedule."
			+ "<br>Minimal value expression: (users * averageScheduleElements) * 2.4"))
	private static final String SCHEDULE_ENTRY_ENTITY_CACHE = "SCHEDULE_ENTRY_ENTITY_CACHE";

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
	protected void updateCreatorAndModifierInfo(ScheduleEntry instance) {
		// no need to set anything
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ScheduleEntry persistChanges(ScheduleEntry instance) {
		return super.persistChanges(instance);
	}

	@Override
	protected void checkForStaleData(ScheduleEntry instance) throws StaleDataModificationException {
		// FIXME - add the schedule modification date
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onBeforeSave(ScheduleEntryEntity entity, ScheduleEntryEntity oldCached) {
		if (oldCached != null) {
			if (oldCached.getScheduleId() != null) {
				entity.setScheduleId(oldCached.getScheduleId());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchRevisions(ScheduleEntry instance, Long revision) {
		instance.setRevision(revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void savePropertiesOnPersistChanges(ScheduleEntry instance) {
		propertiesService.saveProperties(instance, true, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveProperties(ScheduleEntry instance, boolean addOnly) {
		propertiesService.saveProperties(instance, addOnly, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateInstanceForModel(ScheduleEntry instance, DefinitionModel model) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ScheduleEntryEntity createEntity(String dmsId) {
		ScheduleEntryEntity entity = new ScheduleEntryEntity();
		entity.setContentManagementId(dmsId);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ScheduleEntry> getInstanceClass() {
		return ScheduleEntry.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ScheduleEntryEntity> getEntityClass() {
		return ScheduleEntryEntity.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<DefinitionModel> getDefinitionClass() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCacheEntityName() {
		return SCHEDULE_ENTRY_ENTITY_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityLookupCallbackDAOAdaptor<Long, ScheduleEntryEntity, String> getEntityCacheProvider() {
		return new ScheduleEntryLookupDao();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<Long>> List<E> findEntities(Set<String> ids) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("contentManagementIds", ids));
		return getDbDao().fetchWithNamed(
				DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_IDS_KEY, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<Long>> List<E> findEntitiesByPrimaryKey(Set<Long> ids) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
		args.add(new Pair<String, Object>("ids", ids));
		return getDbDao().fetchWithNamed(
				DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_IDS_KEY, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return instance.getIdentifier();
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

	/**
	 * Entity lookup DAO adapter for {@link ScheduleEntryEntity}s.
	 * 
	 * @author BBonev
	 */
	private class ScheduleEntryLookupDao extends DefaultEntityLookupDao {

		/**
		 * Gets the value key internal.
		 * 
		 * @param value
		 *            the value
		 * @return the value key internal
		 */
		@Override
		protected String getValueKeyInternal(ScheduleEntryEntity value) {
			return value.getContentManagementId();
		}

		/**
		 * Fetch entity by value.
		 * 
		 * @param key
		 *            the key
		 * @return the list
		 */
		@Override
		protected List<ScheduleEntryEntity> fetchEntityByValue(String key) {
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
			args.add(new Pair<String, Object>("contentManagementId", key));
			return getDbDao().fetchWithNamed(
					DbQueryTemplatesSchedule.QUERY_SCHEDULE_ENTRY_ENTITY_BY_ACT_ID_KEY, args);
		}

	}

}
