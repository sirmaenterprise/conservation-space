package com.sirma.itt.cmf.services.impl.dao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.entity.TaskEntity;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
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
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Instance dao implementation for task instances.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.WORKFLOW_TASK)
public class TaskInstanceDao extends
		BaseInstanceDaoImpl<TaskInstance, TaskEntity, String, String, TaskDefinitionRef> implements
		InstanceDao<TaskInstance> {

	/** The Constant PROJECT_ENTITY_CACHE. */
	@CacheConfiguration(transaction = CacheTransactionMode.FULL_XA, container = "cmf", eviction = @Eviction(maxEntries = 5000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Fully transactional cache used to contain the activly loaded workflow task instances. "
			+ "The cache will have at most 2 entries for every workflow task in a case or project for all active users. "
			+ "<br>Minimal value expression: ((users * ((searchPageSize + dashletsPageSize) / 1.5) * 4) + users) * 2.2"))
	private static final String TASK_ENTITY_CACHE = "TASK_ENTITY_CACHE";
	/** The db dao. */
	@Inject
	private DbDao dbDao;

	@Override
	protected DbDao getDbDao() {
		return dbDao;
	}

	@Override
	protected void onInstanceUpdated(TaskInstance instance) {
		if (StringUtils.isNullOrEmpty(instance.getContentManagementId())) {
			instance.setContentManagementId(instance.getTaskInstanceId());
		}
		if (instance.getWorkflowDefinitionId() == null) {
			instance.setWorkflowDefinitionId(instance.getContext().getIdentifier());
		}
		if (instance.getContainer() == null) {
			instance.setContainer(instance.getContext().getContainer());
		}
		if (instance.getWorkflowInstanceId() == null) {
			instance.setWorkflowInstanceId(instance.getContext().getWorkflowInstanceId());
		}
		if ((instance.getState() == TaskState.COMPLETED)
				&& (instance.getProperties().get(TaskProperties.ACTUAL_END_DATE) == null)) {
			instance.getProperties().put(TaskProperties.ACTUAL_END_DATE, new Date());
		}
	}

	@Override
	protected void updateCreatorInfo(TaskInstance instance, Date currentDate) {
		// different from default properties - set from activity engine
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public TaskInstance persistChanges(TaskInstance instance) {
		return super.persistChanges(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchRevisions(TaskInstance instance, Long revision) {
		instance.setRevision(revision);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Entity saveEntity(Entity entity) {
		return super.saveEntity(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<String>> List<E> findEntities(Set<String> ids) {
		List<E> list = dbDao.fetchWithNamed(DbQueryTemplates.QUERY_TASK_ENTITIES_BY_DMS_ID_KEY,
				Arrays.asList(new Pair<String, Object>("documentManagementId", ids)));
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<String>> List<E> findEntitiesByPrimaryKey(Set<String> ids) {
		List<E> list = dbDao.fetchWithNamed(DbQueryTemplates.QUERY_TASK_ENTITIES_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", ids)));
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSecondaryKey(Instance instance) {
		return ((AbstractTaskInstance) instance).getTaskInstanceId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<?>> String getPrimaryKey(E entity) {
		return (String) entity.getId();
	}

	@Override
	protected TaskEntity createEntity(String dmsId) {
		TaskEntity entity = new TaskEntity();
		entity.setDocumentManagementId(dmsId);
		return entity;
	}

	@Override
	protected Class<TaskInstance> getInstanceClass() {
		return TaskInstance.class;
	}

	@Override
	protected Class<TaskEntity> getEntityClass() {
		return TaskEntity.class;
	}

	@Override
	protected String getCacheEntityName() {
		return TASK_ENTITY_CACHE;
	}

	@Override
	protected EntityLookupCallbackDAOAdaptor<String, TaskEntity, String> getEntityCacheProvider() {
		return new TaskEntityLookupDao();
	}

	@Override
	protected void populateInstanceForModel(TaskInstance instance, TaskDefinitionRef model) {
		populateProperties(instance, model);
	}

	@Override
	protected Class<TaskDefinitionRef> getDefinitionClass() {
		return TaskDefinitionRef.class;
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
	 * Task entity cache lookup dao.
	 *
	 * @author BBonev
	 */
	class TaskEntityLookupDao extends DefaultEntityLookupDao {

		/**
		 * Fetch entity by value.
		 *
		 * @param key
		 *            the key
		 * @return the list
		 */
		@Override
		protected List<TaskEntity> fetchEntityByValue(String key) {
			return dbDao.fetchWithNamed(DbQueryTemplates.QUERY_TASK_BY_DMS_ID_KEY,
					Arrays.asList(new Pair<String, Object>("documentManagementId", key)));
		}

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
					new Pair<String, Object>("taskType", TaskType.WORKFLOW_TASK)));
			if (result.isEmpty()) {
				return null;
			}
			return new Pair<String, TaskEntity>(key, result.get(0));
		}
	}

}
