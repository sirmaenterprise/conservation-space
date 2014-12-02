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

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
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
 * Implementation of the interface {@link DefinitionAccessor} that handles the document template
 * definitions
 *
 * @author BBonev
 */
@Stateless
public class DocumentTemplateDefinitionAccessor extends BaseTemplateDefinitionAccessor implements
		DefinitionAccessor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1207491806823168316L;
	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;
	/** The Constant DOCUMENT_TEMPLATE_DEFINITION_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 600), expiration = @Expiration(maxIdle = 900000, interval = 60000), doc = @Documentation(""
			+ "Cache used to contain the different document template definitions. The cache will have at most an entry for every different document definition per active tenant. "
			+ "<br>Minimal value expression: tenants * documentTemplateDefinitions"))
	private static final String DOCUMENT_TEMPLATE_DEFINITION_CACHE = "DOCUMENT_TEMPLATE_DEFINITION_CACHE";

	private static final Logger LOGGER = Logger.getLogger(DocumentTemplateDefinitionAccessor.class);

	static {
		SUPPORTED_OBJECTS = new HashSet<Class<?>>();
		SUPPORTED_OBJECTS.add(DocumentDefinitionTemplate.class);
		SUPPORTED_OBJECTS.add(DocumentDefinitionImpl.class);
		SUPPORTED_OBJECTS.add(DocumentDefinitionRefProxy.class);
		SUPPORTED_OBJECTS.add(DocumentDefinitionRef.class);
	}

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		if (!cacheContext.containsCache(DOCUMENT_TEMPLATE_DEFINITION_CACHE)) {
			cacheContext.createCache(DOCUMENT_TEMPLATE_DEFINITION_CACHE,
					new DocumentTemplateDefinitionLookup());
		}
	}

	/**
	 * Getter method for documentDefinitionCache.
	 *
	 * @return the documentDefinitionCache
	 */
	private EntityLookupCache<Pair<String, String>, DocumentDefinitionTemplate, Serializable> getDocumentTemplateDefinitionCache() {
		return cacheContext.getCache(DOCUMENT_TEMPLATE_DEFINITION_CACHE);
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
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> List<E> getAllDefinitions(String container) {
		return (List<E>) getTemplateDefinitionInternal(DocumentDefinitionTemplate.class,
				getDocumentTemplateDefinitionCache());
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
		DocumentDefinitionTemplate definition = getDefinitionFromCache(pair,
				getDocumentTemplateDefinitionCache());
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
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> E getDefinition(Instance instance) {
		if (instance instanceof DocumentInstance) {
			DocumentInstance documentInstance = (DocumentInstance) instance;
			if (documentInstance.isStandalone()) {
				DefinitionModel definitionModel = getDefinition(documentInstance.getContainer(),
						documentInstance.getIdentifier(), documentInstance.getRevision());
				return (E) definitionModel;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	protected DataTypeDefinition detectDataTypeDefinition(DefinitionModel topLevelDefinition) {
		if (topLevelDefinition instanceof DocumentDefinitionTemplate) {
			return getDataTypeDefinition(DocumentDefinitionTemplate.class);
		}
		throw new EmfRuntimeException("Not supported object instance: "
				+ topLevelDefinition.getClass());
	}

	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E extends TopLevelDefinition> E saveDefinition(E documentDefinitionTemplate) {
		DocumentDefinitionTemplate definition = saveTemplate(
				(DocumentDefinitionTemplate) documentDefinitionTemplate,
				getDocumentTemplateDefinitionCache(), this);
		DocumentDefinitionImpl clone = ((DocumentDefinitionImpl) definition).clone();
		clone.initBidirection();
		return (E) clone;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public int computeHash(DefinitionModel model) {
		return hashCalculator.computeHash(model);
	}

	/**
	 * The Class DocumentDefinitionLookup.
	 *
	 * @author BBonev
	 */
	class DocumentTemplateDefinitionLookup
	extends
	EntityLookupCallbackDAOAdaptor<Pair<String, String>, DocumentDefinitionTemplate, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Serializable getValueKey(DocumentDefinitionTemplate value) {
			if (value == null) {
				return null;
			}
			String container = null;
			if (value instanceof TopLevelDefinition) {
				container = ((TopLevelDefinition) value).getContainer();
			}
			return new Pair<String, String>(value.getIdentifier(), container);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Pair<Pair<String, String>, DocumentDefinitionTemplate> findByValue(
				DocumentDefinitionTemplate value) {
			Serializable key = getValueKey(value);
			if (key == null) {
				return null;
			}
			return findByKey((Pair<String, String>) key);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, String>, DocumentDefinitionTemplate> findByKey(
				Pair<String, String> key) {
			DataTypeDefinition type = getDataTypeDefinition(DocumentDefinitionTemplate.class);
			List<DocumentDefinitionTemplate> list = getDefinitionsInternal(
					EmfQueries.QUERY_DEFINITION_BY_ID_CONTAINER_REVISION_KEY, key.getFirst(),
					0L, key.getSecond(), type, null, false);
			if (list.isEmpty()) {
				return null;
			}
			if (list.size() > 1) {
				LOGGER.warn("More then one document definition found for " + key);
			}
			DocumentDefinitionTemplate template = list.get(0);
			updateCache(template, true, false);
			return new Pair<Pair<String, String>, DocumentDefinitionTemplate>(key, template);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, String>, DocumentDefinitionTemplate> createValue(
				DocumentDefinitionTemplate value) {
			DefinitionEntry entry = createEntity((TopLevelDefinition) value,
					DocumentTemplateDefinitionAccessor.this);
			// if needed copy the return ID
			getDbDao().saveOrUpdate(entry);
			String container = null;
			if (value instanceof TopLevelDefinition) {
				container = ((TopLevelDefinition) value).getContainer();
			}
			updateCache(value, true, false);
			return new Pair<Pair<String, String>, DocumentDefinitionTemplate>(
					new Pair<String, String>(value.getIdentifier(), container), value);
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
