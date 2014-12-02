package com.sirma.itt.pm.services.impl.dao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.LinkIterable;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.ObjectTypesPm;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.entity.ProjectEntity;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.services.DbQueryTemplatesPm;

/**
 * Default {@link InstanceDao} implementation to support the {@link ProjectInstance} handling.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesPm.PROJECT)
public class ProjectInstanceDao extends
		BasePmInstanceDaoImpl<ProjectInstance, ProjectEntity, String, String, ProjectDefinition>
		implements InstanceDao<ProjectInstance> {

	/** The Constant PROJECT_ENTITY_CACHE. */
	@CacheConfiguration(container = "pm", transaction = CacheTransactionMode.FULL_XA, expiration = @Expiration(maxIdle = 1800000), eviction = @Eviction(maxEntries = 100), doc = @Documentation(""
			+ "Fully transactional cache used to contain the project per for all active users. "
			+ "The cache will have at most 2 entries for every instance for opened by a user."
			+ "<br>Minimal value expression: (users * projects) * 2.2"))
	private static final String PROJECT_ENTITY_CACHE = "PROJECT_ENTITY_CACHE";

	/** The link service. */
	@Inject
	private LinkService linkService;

	@Override
	protected void onInstanceUpdated(ProjectInstance instance) {
		// on any data change the role should be invalidated
		Map<String, Serializable> properties = instance.getProperties();
		// fill owner if not set
		if (properties.get(ProjectProperties.OWNER) == null) {
			properties.put(ProjectProperties.OWNER, properties.get(ProjectProperties.CREATED_BY));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchRevisions(ProjectInstance instance, Long revision) {
		instance.setRevision(revision);
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(Entity entity) {
		super.delete(entity);
		// we can throw an exception for just in case
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected <E extends Entity<String>> List<E> findEntities(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(
				DbQueryTemplatesPm.QUERY_PROJECT_ENTITIES_BY_DMS_ID_KEY,
				Arrays.asList(new Pair<String, Object>("documentManagementId", ids)));
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected <E extends Entity<String>> List<E> findEntitiesByPrimaryKey(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(
				DbQueryTemplatesPm.QUERY_PROJECT_ENTITIES_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", ids)));
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return ((ProjectInstance) instance).getDmsId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<?>> String getPrimaryKey(E entity) {
		return (String) entity.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ProjectEntity createEntity(String dmsId) {
		ProjectEntity entity = new ProjectEntity();
		entity.setDocumentManagementId(dmsId);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ProjectInstance> getInstanceClass() {
		return ProjectInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ProjectEntity> getEntityClass() {
		return ProjectEntity.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ProjectDefinition> getDefinitionClass() {
		return ProjectDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCacheEntityName() {
		return PROJECT_ENTITY_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityLookupCallbackDAOAdaptor<String, ProjectEntity, String> getEntityCacheProvider() {
		return new ProjectEntityLookupDao();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateInstanceForModel(ProjectInstance instance, ProjectDefinition model) {
		instance.setContainer(model.getContainer());
		populateProperties(instance, model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getOwningInstanceQuery() {
		return null;
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
	 * {@inheritDoc}
	 */
	@Override
	protected Iterable<InstanceReference> getCurrentChildren(ProjectInstance targetInstance) {
		List<LinkReference> links = linkService.getLinks(targetInstance.toReference(),
				LinkConstants.PARENT_TO_CHILD);
		return new LinkIterable<>(links);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void attachNewChildren(ProjectInstance targetInstance, Operation operation,
			List<Instance> newChildren) {
		// nothing to do here for now
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void detachExistingChildren(ProjectInstance sourceInstance, Operation operation,
			List<Instance> oldChildren) {
		// nothing to do here for now
	}

	/**
	 * Project entity cache lookup dao.
	 * 
	 * @author BBonev
	 */
	class ProjectEntityLookupDao extends DefaultEntityLookupDao {

		/**
		 * Fetch entity by value.
		 * 
		 * @param key
		 *            the key
		 * @return the list
		 */
		@Override
		protected List<ProjectEntity> fetchEntityByValue(String key) {
			return getDbDao().fetchWithNamed(DbQueryTemplatesPm.QUERY_PROJECT_BY_DMS_ID_KEY,
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
		protected String getValueKeyInternal(ProjectEntity value) {
			return value.getDocumentManagementId();
		}
	}

}
