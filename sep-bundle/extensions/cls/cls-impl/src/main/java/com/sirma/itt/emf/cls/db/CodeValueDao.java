package com.sirma.itt.emf.cls.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cls.entity.CodeValue;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Data access object that works with code values and the database.
 * 
 * @author Mihail Radkov
 */
@Stateless
@InstanceType(type = "CodeValueInstance")
public class CodeValueDao extends
		BaseInstanceDaoImpl<CodeValue, CodeValue, Long, Serializable, DefinitionModel> implements
		InstanceDao<CodeValue> {

	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Not transactional cache used to contain the code lists. <b>NOTE :</b> the cache should not be transactional or will not work property!"
			+ "<br>Minimal value expression: (users * (averageCheckboxesPerTaskDefinition + 1)) * 1.2"))
	private static final String CODELIST_CACHE = "CODELIST_CACHE";

	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
	}

	@Override
	public void synchRevisions(CodeValue instance, Long revision) {
		instance.setRevision(revision);
	}

	@Override
	protected void savePropertiesOnPersistChanges(CodeValue instance) {
	}

	@Override
	protected String getOwningInstanceQuery() {
		return null;
	}

	@Override
	protected void populateInstanceForModel(CodeValue instance, DefinitionModel model) {
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
		return "CODE_VALUE_ENTITY_CACHE";
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
	protected <E extends Entity<Long>> List<E> findEntitiesByPrimaryKey(Set<Long> ids) {
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
	}

}
