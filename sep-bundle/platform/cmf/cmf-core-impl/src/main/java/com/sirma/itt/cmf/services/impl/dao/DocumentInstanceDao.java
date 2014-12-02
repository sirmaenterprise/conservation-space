/*
 *
 */
package com.sirma.itt.cmf.services.impl.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefProxy;
import com.sirma.itt.cmf.beans.entity.DocumentEntity;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.event.cases.CaseChangeEvent;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceContext;
import com.sirma.itt.emf.util.Documentation;

/**
 * Document instance specific operations.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.DOCUMENT)
public class DocumentInstanceDao
		extends
		BaseInstanceDaoImpl<DocumentInstance, DocumentEntity, String, String, DocumentDefinitionRef>
		implements InstanceDao<DocumentInstance> {

	/** The Constant DOCUMENT_ENTITY_CACHE. */
	@CacheConfiguration(transaction = CacheTransactionMode.FULL_XA, container = "cmf", eviction = @Eviction(maxEntries = 10000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Fully transactional cache used to contain the activly loaded document instances. "
			+ "The cache will have at most 2 entries for every document in a section in a case instance for all active users. "
			+ "This means if a there are 10 active users and they perform a search for cases and have a"
			+ " 10 different result pages open then the used entries will be 10 * search page size(25 by default) * average number of sections per case * average number of documents per section"
			+ " but this value should be 10-20% bigger if search performance is a problem. "
			+ "If the application has enabled projects functionally then the maximum value should correspond to the different active projects."
			+ "<br>Minimal value expression: (((((users * searchPageSize * 4) + (users * dashletsPageSize * (projects/4))) * averageSections) * 2.2) * averageDocuments) * 2.2"))
	private static final String DOCUMENT_ENTITY_CACHE = "DOCUMENT_ENTITY_CACHE";
	@Inject
	private Logger LOGGER;

	private boolean debug;

	/** The case event. */
	@Inject
	private EventService eventService;

	@Inject
	DbDao dbDao;

	@Override
	protected DbDao getDbDao() {
		return dbDao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
		debug = LOGGER.isDebugEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public DocumentInstance persistChanges(DocumentInstance instance) {
		return super.persistChanges(instance);
	}

	@Override
	protected void onInstanceUpdated(DocumentInstance instance) {

		Map<String, Serializable> properties = instance.getProperties();
		// set initial version
		if (properties.get(DocumentProperties.VERSION) == null) {
			properties.put(DocumentProperties.VERSION, Double.valueOf(1.0));
		}
	}

	@Override
	protected void onAutoSave(DocumentInstance instance, boolean autosave) {
		InstanceContext context = InstanceUtil.getParent(InstanceContext.class, instance);
		if (autosave) {
			if (context instanceof CaseInstance) {
				eventService.fire(new CaseChangeEvent((CaseInstance) context));
			} else {
				persistChanges(instance);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchRevisions(DocumentInstance instance, Long revision) {
		// update the revision only if not set or invalid
		if ((instance != null)
				&& ((instance.getRevision() == null) || (instance.getRevision() <= 0))) {
			instance.setRevision(revision);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Entity saveEntity(Entity entity) {
		return super.saveEntity(entity);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable> void delete(Entity<S> entity) {
		long revision = 0;
		if (entity instanceof DocumentInstance) {
			revision = ((DocumentInstance) entity).getRevision();
		}
		if (entity instanceof DocumentEntity) {
			revision = ((DocumentEntity) entity).getRevision();
		}
		if (revision != 0) {
			propertiesService.removeProperties(entity, revision, (PathElement) entity);
			getCache().deleteByKey(entity.getId());
		}
	}

	@Override
	protected <E extends Entity<String>> List<E> findEntities(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(
				DbQueryTemplates.QUERY_DOCUMENT_ENTITIES_BY_DMS_KEY,
				Arrays.asList(new Pair<String, Object>("documentDmsId", ids)));
		return list;
	}

	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return ((DocumentInstance) instance).getDmsId();
	}

	@Override
	protected <E extends Entity<String>> List<E> findEntitiesByPrimaryKey(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(
				DbQueryTemplates.QUERY_DOCUMENT_ENTITIES_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", ids)));
		return list;
	}

	@Override
	protected <E extends Entity<?>> String getPrimaryKey(E entity) {
		return (String) entity.getId();
	}

	@Override
	protected void populateInstanceForModel(DocumentInstance instance, DocumentDefinitionRef model) {
		instance.setDocumentRefId(model.getIdentifier());
		instance.setIdentifier(model.getReferenceId());
		instance.setPurpose(model.getPurpose());
		instance.setStructured(model.getStructured());
		// TODO: Need to implement somehow to fetch the concrete document definition not the case
		// definition because we does not now witch document definition to use
		// the only way we know if we have a standalone document or not
		if (model instanceof DocumentDefinitionRefProxy) {
			instance.setStandalone(true);
		}

		// but for now we does not need this functions
		populateProperties(instance, model);
	}

	@Override
	protected DocumentEntity createEntity(String dmsId) {
		DocumentEntity documentEntity = new DocumentEntity();
		documentEntity.setDocumentDmsId(dmsId);
		return documentEntity;
	}

	@Override
	protected Class<DocumentInstance> getInstanceClass() {
		return DocumentInstance.class;
	}

	@Override
	protected Class<DocumentEntity> getEntityClass() {
		return DocumentEntity.class;
	}

	@Override
	protected Class<DocumentDefinitionRef> getDefinitionClass() {
		return DocumentDefinitionRef.class;
	}

	@Override
	protected String getCacheEntityName() {
		return DOCUMENT_ENTITY_CACHE;
	}

	@Override
	protected EntityLookupCallbackDAOAdaptor<String, DocumentEntity, String> getEntityCacheProvider() {
		return new DocumentEntityLookup();
	}

	@Override
	protected String getOwningInstanceQuery() {
		return DbQueryTemplates.QUERY_DOCUMENTS_BY_OWN_REF_KEY;
	}

	@Override
	protected DocumentDefinitionRef getInstanceDefinition(String definitionId,
			Class<DocumentDefinitionRef> definitionClass) {
		DefinitionModel definition = dictionaryService.getDefinition(definitionClass, definitionId);
		if (definition instanceof DocumentDefinitionTemplate) {
			return new DocumentDefinitionRefProxy((DocumentDefinitionTemplate) definition);
		}
		return (DocumentDefinitionRef) definition;
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
	 * Entity lookup class for documents.
	 * 
	 * @author BBonev
	 */
	public class DocumentEntityLookup extends DefaultEntityLookupDao {

		@Override
		protected String getValueKeyInternal(DocumentEntity value) {
			return value.getDocumentDmsId();
		}

		@Override
		protected List<DocumentEntity> fetchEntityByValue(String key) {
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
			args.add(new Pair<String, Object>("documentDmsId", key));
			return getDbDao().fetchWithNamed(DbQueryTemplates.QUERY_DOCUMENT_ENTITY_BY_DMS_KEY,
					args);
		}
	}

}
