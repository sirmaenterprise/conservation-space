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

import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.entity.WorkflowInstanceContextEntity;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
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
 * Workflow instance context DAO for specific operations.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.WORKFLOW)
public class WorkflowInstanceDao
		extends
		BaseInstanceDaoImpl<WorkflowInstanceContext, WorkflowInstanceContextEntity, String, String, WorkflowDefinition>
		implements InstanceDao<WorkflowInstanceContext> {
	/** The Constant WORKFLOW_ENTITY_CACHE. */
	@CacheConfiguration(transaction = CacheTransactionMode.FULL_XA, container = "cmf", eviction = @Eviction(maxEntries = 200), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Fully transactional cache used to contain the activly loaded workflow instances. "
			+ "The cache will have at most 2 entries for every workflow in a case or project for all active users. "
			+ "<br>Minimal value expression: ((users * ((searchPageSize + dashletsPageSize) / 1.5)) + users) * 2.2"))
	private static final String WORKFLOW_ENTITY_CACHE = "WORKFLOW_ENTITY_CACHE";
	@Inject
	DbDao dbDao;

	@Override
	protected DbDao getDbDao() {
		return dbDao;
	}

	/**
	 * Initialize the cache.
	 */
	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
	}

	@Override
	protected void onInstanceUpdated(WorkflowInstanceContext instance) {
		// add properties that need to be updated on instance modification
	}

	@Override
	protected void updateCreatorInfo(WorkflowInstanceContext instance, Date currentDate) {
		// different from default properties - set from activity engine
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public WorkflowInstanceContext persistChanges(WorkflowInstanceContext instance) {
		return super.persistChanges(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void setCurrentUserTo(WorkflowInstanceContext model, String key) {
		super.setCurrentUserTo(model, key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchRevisions(WorkflowInstanceContext instance, Long revision) {
		instance.setRevision(revision);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Entity saveEntity(Entity entity) {
		return super.saveEntity(entity);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(Entity entity) {
		super.delete(entity);
	}

	@Override
	protected <E extends Entity<String>> List<E> findEntities(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(DbQueryTemplates.QUERY_WORKFLOW_ENTITIES_BY_INSTANCE_ID_KEY,
				Arrays.asList(new Pair<String, Object>("workflowInstanceId", ids)));
		return list;
	}

	@Override
	protected <E extends Entity<String>> List<E> findEntitiesByPrimaryKey(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(DbQueryTemplates.QUERY_WORKFLOW_ENTITIES_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", ids)));
		return list;
	}

	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return ((WorkflowInstanceContext) instance).getWorkflowInstanceId();
	}

	@Override
	protected <E extends Entity<?>> String getPrimaryKey(E entity) {
		return (String) entity.getId();
	}

	@Override
	protected void populateInstanceForModel(WorkflowInstanceContext instance,
			WorkflowDefinition model) {
		populateProperties(instance, model);
	}

	@Override
	protected WorkflowInstanceContextEntity createEntity(String dmsId) {
		WorkflowInstanceContextEntity contextEntity = new WorkflowInstanceContextEntity();
		contextEntity.setWorkflowInstanceId(dmsId);
		return contextEntity;
	}

	@Override
	protected Class<WorkflowInstanceContext> getInstanceClass() {
		return WorkflowInstanceContext.class;
	}

	@Override
	protected Class<WorkflowInstanceContextEntity> getEntityClass() {
		return WorkflowInstanceContextEntity.class;
	}

	@Override
	protected Class<WorkflowDefinition> getDefinitionClass() {
		return WorkflowDefinition.class;
	}

	@Override
	protected String getCacheEntityName() {
		return WORKFLOW_ENTITY_CACHE;
	}

	@Override
	protected EntityLookupCallbackDAOAdaptor<String, WorkflowInstanceContextEntity, String> getEntityCacheProvider() {
		return new WorkflowEntityLookupDao();
	}

	@Override
	protected String getOwningInstanceQuery() {
		return DbQueryTemplates.QUERY_WORKFLOW_BY_OWN_REF_KEY;
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
	 * The entity lookup callback for workflow instance entities.
	 *
	 * @author BBonev
	 */
	public class WorkflowEntityLookupDao extends DefaultEntityLookupDao {

		@Override
		protected String getValueKeyInternal(WorkflowInstanceContextEntity value) {
			return value.getWorkflowInstanceId();
		}

		@Override
		protected List<WorkflowInstanceContextEntity> fetchEntityByValue(String key) {
			return getDbDao().fetchWithNamed(
					DbQueryTemplates.QUERY_WORKFLOW_INSTANCE_BY_INSTANCE_ID_KEY,
					Arrays.asList(new Pair<String, Object>("workflowInstanceId", key)));
		}

	}
}
