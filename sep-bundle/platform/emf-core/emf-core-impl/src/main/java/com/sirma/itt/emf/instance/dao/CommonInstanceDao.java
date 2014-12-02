package com.sirma.itt.emf.instance.dao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.entity.CommonEntity;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;

/**
 * Common instance specific operations
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.INSTANCE)
public class CommonInstanceDao extends
		BaseInstanceDaoImpl<CommonInstance, CommonEntity, String, String, DefinitionModel>
		implements
		InstanceDao<CommonInstance> {

	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Not transactional cache used to contain the common instances (checkboxes or action buttons). <b>NOTE :</b> the cache should not be transactional or will not work property!"
			+ "The cache will have at most an entry for every instance for opened form with checkbox or action button for all active users. "
			+ "<br>Minimal value expression: (users * (averageCheckboxesPerTaskDefinition + 1)) * 1.2"))
	private static final String COMMON_ENTITY_CACHE = "COMMON_ENTITY_CACHE";

	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
	}

	@Override
	protected void updateCreatorAndModifierInfo(CommonInstance instance) {
		// no need to update anything
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchRevisions(CommonInstance instance, Long revision) {
		instance.setRevision(revision);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<CommonInstance> loadInstances(List<S> dmsIds,
			boolean loadAllProperties) {
		// override the default implementation to disable secondary key searches
		return CollectionUtils.emptyList();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public CommonInstance persistChanges(CommonInstance instance) {
		SequenceEntityGenerator.generateStringId(instance, true);
		return super.persistChanges(instance);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Entity saveEntity(Entity entity) {
		SequenceEntityGenerator.generateStringId(entity, true);
		return super.saveEntity(entity);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void delete(Entity entity) {
		super.delete(entity);
	}

	@Override
	protected <E extends Entity<String>> List<E> findEntities(Set<String> ids) {
		return CollectionUtils.emptyList();
	}

	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return null;
	}

	@Override
	protected <E extends Entity<String>> List<E> findEntitiesByPrimaryKey(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(EmfQueries.QUERY_COMMON_ENTITIES_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", ids)));
		return list;
	}

	@Override
	protected <E extends Entity<?>> String getPrimaryKey(E entity) {
		return (String) entity.getId();
	}

	@Override
	protected void populateInstanceForModel(CommonInstance instance, DefinitionModel model) {
		// nothing to do here
	}

	@Override
	protected CommonEntity createEntity(String dmsId) {
		return new CommonEntity();
	}

	@Override
	protected Class<CommonInstance> getInstanceClass() {
		return CommonInstance.class;
	}

	@Override
	protected Class<CommonEntity> getEntityClass() {
		return CommonEntity.class;
	}

	@Override
	protected Class<DefinitionModel> getDefinitionClass() {
		return null;
	}

	@Override
	protected String getCacheEntityName() {
		return COMMON_ENTITY_CACHE;
	}

	@Override
	protected EntityLookupCallbackDAOAdaptor<String, CommonEntity, String> getEntityCacheProvider() {
		return new DefaultPrimaryKeyEntityLookupDao();
	}

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

}
