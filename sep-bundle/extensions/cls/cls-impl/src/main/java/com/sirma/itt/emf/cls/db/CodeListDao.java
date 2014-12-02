package com.sirma.itt.emf.cls.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cls.entity.CodeList;
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
 * Data access object that works with CodeLists and the database.
 * 
 * @author Mihail Radkov
 */
@Stateless
@InstanceType(type = "CodeListInstance")
public class CodeListDao extends
		BaseInstanceDaoImpl<CodeList, CodeList, Long, Serializable, DefinitionModel> implements
		InstanceDao<CodeList> {
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Not transactional cache used to contain the code lists. <b>NOTE :</b> the cache should not be transactional or will not work property!"
			+ "<br>Minimal value expression: (users * (averageCheckboxesPerTaskDefinition + 1)) * 1.2"))
	private static final String CODELIST_CACHE = "CODELIST_CACHE";

	@Inject
	@InstanceType(type = "CodeValueInstance")
	private InstanceDao<CodeValue> cvDao;

	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
	}

	@Override
	public CodeList persistChanges(CodeList instance) {
		return super.persistChanges(instance);
	}

	@Override
	protected void savePropertiesOnPersistChanges(CodeList instance) {
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchRevisions(CodeList instance, Long revision) {
		instance.setRevision(revision);
	}

	@Override
	protected String getOwningInstanceQuery() {
		return null;
	}

	@Override
	protected void populateInstanceForModel(CodeList instance, DefinitionModel model) {
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
		return "CODE_LIST_ENTITY_CACHE";
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
	protected <E extends Entity<Long>> List<E> findEntitiesByPrimaryKey(Set<Long> ids) {
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
					List<CodeValue> codeValues = cvDao
							.loadInstancesByDbKey(getCodeValueIds(codeList.getValue()));
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
