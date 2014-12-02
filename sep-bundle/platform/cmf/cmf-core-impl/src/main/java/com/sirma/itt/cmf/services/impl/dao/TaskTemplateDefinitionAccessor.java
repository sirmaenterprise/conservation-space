package com.sirma.itt.cmf.services.impl.dao;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionTemplateImpl;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.definition.dao.BaseTemplateDefinitionAccessor;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.DefinitionEntry;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Implementation of the interface {@link DefinitionAccessor} that handles the task template
 * definitions
 * 
 * @author BBonev
 */
@Stateless
public class TaskTemplateDefinitionAccessor extends BaseTemplateDefinitionAccessor implements
DefinitionAccessor {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1769831948229567951L;

	private static final Logger LOGGER = Logger.getLogger(TaskTemplateDefinitionAccessor.class);

	/** The Constant TASK_DEFINITION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 100), doc = @Documentation(""
			+ "Cache used to contain the different workflow task template definitions. The cache will have at most an entry for every different task definition per active tenant. "
			+ "<br>Minimal value expression: tenants * taskTemplateDefinitions"))
	private static final String TASK_DEFINITION_CACHE = "TASK_DEFINITION_TEMPLATE_CACHE";

	/** The Constant SUPPORTED_OBJECTS. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new HashSet<Class<?>>();
		SUPPORTED_OBJECTS.add(TaskDefinitionTemplate.class);
		SUPPORTED_OBJECTS.add(TaskDefinitionTemplateImpl.class);
	}

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		if (!cacheContext.containsCache(TASK_DEFINITION_CACHE)) {
			cacheContext.createCache(TASK_DEFINITION_CACHE, new TaskDefinitionLookup());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * Gets the task definition cache.
	 *
	 * @return the task definition cache
	 */
	private EntityLookupCache<Pair<String, String>, TaskDefinitionTemplate, Serializable> getTaskDefinitionCache() {
		return cacheContext.getCache(TASK_DEFINITION_CACHE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> List<E> getAllDefinitions(String container) {
		return ((List<E>) getTemplateDefinitionInternal(TaskDefinitionTemplate.class,
				getTaskDefinitionCache()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> E getDefinition(String container, String defId) {
		return getDefinition(container, defId, 0L);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> E getDefinition(String container, String defId, Long version) {
		Pair<String, String> pair = getDefinitionPair(container, defId);
		if (pair == null) {
			return null;
		}
		TaskDefinitionTemplate definition = getDefinitionFromCache(pair, getTaskDefinitionCache());
		if (definition == null) {
			return null;
		}
		return (E) definition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> List<E> getDefinitionVersions(String container, String defId) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> E getDefinition(Instance instance) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTypeDefinition detectDataTypeDefinition(DefinitionModel topLevelDefinition) {
		if (topLevelDefinition instanceof TaskDefinitionTemplate) {
			return getDataTypeDefinition(TaskDefinitionTemplate.class);
		}
		throw new EmfRuntimeException("Not supported object instance: "
				+ topLevelDefinition.getClass());
	}

	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E extends TopLevelDefinition> E saveDefinition(E documentDefinitionTemplate) {
		TaskDefinitionTemplate definition = saveTemplate(
				(TaskDefinitionTemplate) documentDefinitionTemplate, getTaskDefinitionCache(), this);
		TaskDefinitionTemplateImpl clone = ((TaskDefinitionTemplateImpl) definition).clone();
		clone.initBidirection();
		return (E) clone;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public int computeHash(DefinitionModel model) {
		return hashCalculator.computeHash(model);
	}

	/**
	 * The Class TaskDefinitionLookup.
	 *
	 * @author BBonev
	 */
	class TaskDefinitionLookup
	extends
	EntityLookupCallbackDAOAdaptor<Pair<String, String>, TaskDefinitionTemplate, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, String>, TaskDefinitionTemplate> findByKey(Pair<String, String> key) {
			DataTypeDefinition type = getDataTypeDefinition(TaskDefinitionTemplate.class);
			List<TaskDefinitionTemplate> list = getDefinitionsInternal(
					EmfQueries.QUERY_DEFINITION_BY_ID_CONTAINER_REVISION_KEY, key.getFirst(),
					0L, key.getSecond(), type, null, false);
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				LOGGER.warn("More then one task definition template found for " + key);
			}
			TaskDefinitionTemplate template = list.get(0);
			return new Pair<Pair<String, String>, TaskDefinitionTemplate>(key, template);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, String>, TaskDefinitionTemplate> createValue(
				TaskDefinitionTemplate value) {
			DefinitionEntry entry = createEntity((TopLevelDefinition) value,
					TaskTemplateDefinitionAccessor.this);
			// if needed copy the return ID
			getDbDao().saveOrUpdate(entry);
			String container = null;
			if (value instanceof TopLevelDefinition) {
				container = ((TopLevelDefinition) value).getContainer();
			}
			return new Pair<Pair<String, String>, TaskDefinitionTemplate>(new Pair<String, String>(
					value.getIdentifier(), container), value);
		}

	}

	@Override
	public Set<String> getActiveDefinitions() {
		return Collections.emptySet();
	}

	@Override
	public int updateDefinitionRevisionToMaxVersion(String... definitionIds) {
		// nothing to migrate - all template definitions are without revision
		return 0;
	}

}
