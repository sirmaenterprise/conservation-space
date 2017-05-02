package com.sirma.itt.emf.cls.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceType;

/**
 * Data access object that works with code values and the database.
 *
 * @author Mihail Radkov
 */
@ApplicationScoped
@InstanceType(type = "CodeValueInstance")
public class CodeValueDao extends OldBaseInstanceDaoImpl<CodeValue, Long, Serializable, DefinitionModel> {

	@CacheConfiguration(eviction = @Eviction(maxEntries = 1000) , expiration = @Expiration(maxIdle = 1800000, interval = -1) , doc = @Documentation(""
			+ "Not transactional cache used to contain the code lists. <b>NOTE :</b> the cache should not be transactional or will not work property!") )
	private static final String CODE_VALUE_ENTITY_CACHE = "CODE_VALUE_ENTITY_CACHE";

	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
	}

	@Override
	public void synchRevisions(Instance instance, Long revision) {
		instance.setRevision(revision);
	}

	@Override
	protected void savePropertiesOnPersistChanges(Instance instance) {
		// doesn't need implementation at that point
	}

	@Override
	protected String getOwningInstanceQuery() {
		return null;
	}

	@Override
	protected void populateInstanceForModel(Instance instance, DefinitionModel model) {
		// doesn't need implementation at that point
	}

	@Override
	protected CodeValue createEntity(Serializable dmsId) {
		return null;
	}

	@Override
	protected Class<CodeValue> getInstanceClass() {
		return CodeValue.class;
	}

	@Override
	protected Class<CodeValue> getEntityClass() {
		return CodeValue.class;
	}

	@Override
	protected Class<DefinitionModel> getDefinitionClass() {
		return DefinitionModel.class;
	}

	@Override
	protected String getCacheEntityName() {
		return CODE_VALUE_ENTITY_CACHE;
	}

	@Override
	protected EntityLookupCallbackDAOAdaptor<Long, CodeValue, Serializable> getEntityCacheProvider() {
		return new DefaultPrimaryKeyEntityLookupDao();
	}

	@Override
	protected <E extends Entity<Long>> List<E> findEntities(Set<Serializable> ids) {
		return Collections.emptyList();
	}

	@Override
	protected <E extends Entity<Long>> List<E> findEntitiesByPrimaryKey(Collection<Long> ids) {
		List<E> list = getDbDao().fetchWithNamed(ClsQueries.QUERY_CODEVALUE_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", ids)));
		return list;
	}

	@Override
	protected <E extends Instance> Serializable getSecondaryKey(E instance) {
		return null;
	}

	@Override
	protected <E extends Entity<?>> Long getPrimaryKey(E entity) {
		return (Long) entity.getId();
	}

	@Override
	protected <E extends Instance> void batchFetchProperties(List<E> toLoadProps, boolean loadAll) {
		// doesn't need implementation at that point
	}

}
