package com.sirma.itt.cmf.services.impl.dao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.entity.TaskEntity;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Instance DAO implementation for {@link StandaloneTaskInstance} support.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.STANDALONE_TASK)
public class StandaloneTaskInstanceDao extends
		BaseInstanceDaoImpl<StandaloneTaskInstance, TaskEntity, String, String, TaskDefinition> {

	/** The Constant STANDALONE_TASK_ENTITY_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), transaction = CacheTransactionMode.FULL_XA, doc = @Documentation(""
			+ "Fully transactional cache used to contain the activly loaded standalone task instances. "
			+ "The cache will have at most 2 entries for every standalone task in a case or project for all active users. "
			+ "<br>Minimal value expression: ((users * ((searchPageSize + dashletsPageSize) / 1.5) * 4) + users) * 2.2"))
	private static final String STANDALONE_TASK_ENTITY_CACHE = "STANDALONE_TASK_ENTITY_CACHE";

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DbDao getDbDao() {
		return dbDao;
	}

	@Override
	protected void onInstanceUpdated(StandaloneTaskInstance instance) {
		if (StringUtils.isNullOrEmpty(instance.getContentManagementId())) {
			instance.setContentManagementId(instance.getTaskInstanceId());
		}

		if ((instance.getState() == TaskState.COMPLETED)
				&& (instance.getProperties().get(TaskProperties.ACTUAL_END_DATE) == null)) {
			instance.getProperties().put(TaskProperties.ACTUAL_END_DATE, new Date());
		}
	}

	@Override
	protected void updateCreatorInfo(StandaloneTaskInstance instance, Date currentDate) {
		// different from default properties - set from activity engine
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public StandaloneTaskInstance persistChanges(StandaloneTaskInstance instance) {
		return super.persistChanges(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Entity saveEntity(Entity entity) {
		return super.saveEntity(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void synchRevisions(StandaloneTaskInstance instance, Long revision) {
		instance.setRevision(revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateInstanceForModel(StandaloneTaskInstance instance, TaskDefinition model) {
		populateProperties(instance, model);
		instance.setDmsTaskType(model.getDmsType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskEntity createEntity(String dmsId) {
		TaskEntity entity = new TaskEntity();
		entity.setDocumentManagementId(dmsId);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<StandaloneTaskInstance> getInstanceClass() {
		return StandaloneTaskInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<TaskEntity> getEntityClass() {
		return TaskEntity.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<TaskDefinition> getDefinitionClass() {
		return TaskDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCacheEntityName() {
		return STANDALONE_TASK_ENTITY_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityLookupCallbackDAOAdaptor<String, TaskEntity, String> getEntityCacheProvider() {
		return new StandaloneTaskInstanceLookupDao();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<String>> List<E> findEntities(Set<String> ids) {
		List<E> list = dbDao.fetchWithNamed(DbQueryTemplates.QUERY_STASK_ENTITIES_BY_DMS_ID_KEY,
				Arrays.asList(new Pair<String, Object>("documentManagementId", ids),
						new Pair<String, Object>("taskType", TaskType.STANDALONE_TASK)));
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<String>> List<E> findEntitiesByPrimaryKey(Set<String> ids) {
		List<E> list = dbDao.fetchWithNamed(DbQueryTemplates.QUERY_STASK_ENTITIES_BY_ID_KEY, Arrays
				.asList(new Pair<String, Object>("id", ids), new Pair<String, Object>("taskType",
						TaskType.STANDALONE_TASK)));
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return ((StandaloneTaskInstance) instance).getTaskInstanceId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<?>> String getPrimaryKey(E entity) {
		return (String) entity.getId();
	}

	@Override
	protected String getOwningInstanceQuery() {
		return DbQueryTemplates.QUERY_TASKS_BY_OWN_REF_KEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <I extends Serializable> Class<I> getPrimaryIdType() {
		return (Class<I>) String.class;
	}

	/**
	 * Entity lookup DAO for fetching standalone tasks. NOTE: this is the same implementation as the
	 * DAO class used in {@link TaskInstanceDao}.
	 * 
	 * @author BBonev
	 */
	class StandaloneTaskInstanceLookupDao extends DefaultEntityLookupDao {

		/**
		 * Gets the value key internal.
		 * 
		 * @param value
		 *            the value
		 * @return the value key internal
		 */
		@Override
		protected String getValueKeyInternal(TaskEntity value) {
			return value.getDocumentManagementId();
		}

		/**
		 * Fetch entity by value.
		 * 
		 * @param key
		 *            the key
		 * @return the list
		 */
		@Override
		protected List<TaskEntity> fetchEntityByValue(String key) {
			return dbDao.fetchWithNamed(DbQueryTemplates.QUERY_STASK_BY_DMS_ID_KEY, Arrays.asList(
					new Pair<String, Object>("documentManagementId", key),
					new Pair<String, Object>("taskType", TaskType.STANDALONE_TASK)));
		}

		/**
		 * Find an entity for a given key.
		 * 
		 * @param key
		 *            the key (ID) used to identify the entity (never <tt>null</tt>)
		 * @return Return the entity or <tt>null</tt> if no entity is exists for the ID
		 */
		@Override
		public Pair<String, TaskEntity> findByKey(String key) {
			List<TaskEntity> result = dbDao.fetchWithNamed(DbQueryTemplates.QUERY_TASK_ENTITY_BY_ID_AND_TYPE_KEY, Arrays.asList(
					new Pair<String, Object>("id", key),
					new Pair<String, Object>("taskType", TaskType.STANDALONE_TASK)));
			if (result.isEmpty()) {
				return null;
			}
			return new Pair<String, TaskEntity>(key, result.get(0));
		}
	}

}
