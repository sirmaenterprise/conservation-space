package com.sirma.itt.emf.cls.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.cls.entity.CodeList;
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
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceType;

/**
 * Data access object that works with CodeLists and the database.
 *
 * @author Mihail Radkov
 */
@ApplicationScoped
@InstanceType(type = "CodeListInstance")
public class CodeListDao extends OldBaseInstanceDaoImpl<CodeList, Long, Serializable, DefinitionModel> {
	@CacheConfiguration(eviction = @Eviction(maxEntries = 1000) , expiration = @Expiration(maxIdle = 1800000, interval = -1) , doc = @Documentation(""
			+ "Not transactional cache used to contain the code lists. <b>NOTE :</b> the cache should not be transactional or will not work property!") )
	private static final String CODE_LIST_ENTITY_CACHE = "CODE_LIST_ENTITY_CACHE";

	@Inject
	@InstanceType(type = "CodeValueInstance")
	private InstanceDao cvDao;

	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
	}

	@Override
	protected void savePropertiesOnPersistChanges(Instance instance) {
		// doesn't need implementation at that point
	}

	@Override
	public void synchRevisions(Instance instance, Long revision) {
		instance.setRevision(revision);
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
	protected CodeList createEntity(Serializable dmsId) {
		return null;
	}

	@Override
	protected Class<CodeList> getInstanceClass() {
		return CodeList.class;
	}

	@Override
	protected Class<CodeList> getEntityClass() {
		return CodeList.class;
	}

	@Override
	protected Class<DefinitionModel> getDefinitionClass() {
		return DefinitionModel.class;
	}

	@Override
	protected String getCacheEntityName() {
		return CODE_LIST_ENTITY_CACHE;
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
	protected EntityLookupCallbackDAOAdaptor<Long, CodeList, Serializable> getEntityCacheProvider() {
		return new DefaultPrimaryKeyEntityLookupDao();
	}

	@Override
	protected <E extends Entity<Long>> List<E> findEntities(Set<Serializable> ids) {
		return Collections.emptyList();
	}

	@Override
	protected <E extends Entity<Long>> List<E> findEntitiesByPrimaryKey(Collection<Long> ids) {
		List<E> list = getDbDao().fetchWithNamed(ClsQueries.QUERY_CODELIST_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", ids)));
		return list;
	}

	@Override
	protected <E extends Instance> void batchFetchProperties(List<E> toLoadProps, boolean loadAll) {
		if (loadAll) {
			for (E cl : toLoadProps) {
				if (cl instanceof CodeList) {
					CodeList codeList = (CodeList) cl;
					List<CodeValue> codeValues = cvDao.loadInstancesByDbKey(getCodeValueIds(codeList.getValue()));
					codeList.setCodeValues(codeValues);
				}
			}
		}
	}

	/**
	 * Retrieves the code values ids based on certain code list.
	 *
	 * @param codeListValue
	 *            the code list
	 * @return code values ids
	 */
	private List<Long> getCodeValueIds(String codeListValue) {
		List<Long> list = getDbDao().fetchWithNamed(ClsQueries.QUERY_CODEVALUE_BY_CL_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", codeListValue)));
		return list;
	}

}
