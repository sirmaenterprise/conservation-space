/*
 *
 */
package com.sirma.itt.cmf.services.impl.dao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.entity.CaseEntity;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.SectionService;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.RelationalDb;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.LinkIterable;
import com.sirma.itt.emf.util.SortableComparator;

/**
 * Case instance specific operations
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.CASE)
public class CaseInstanceDao extends
		BaseInstanceDaoImpl<CaseInstance, CaseEntity, String, String, CaseDefinition>
		implements InstanceDao<CaseInstance> {

	/** The sortable comparator. */
	private static final SortableComparator SORTABLE_COMPARATOR = new SortableComparator();
	/** The Constant CASE_ENTITY_CACHE. */
	@CacheConfiguration(transaction = CacheTransactionMode.FULL_XA, container = "cmf", eviction = @Eviction(maxEntries = 200), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Fully transactional cache used to contain the activly loaded case entities/instances. "
			+ "The cache will have at most 2 entries for every visible case instance for all active users. "
			+ "This means if a there are 10 active users and they perform a search for cases and have a"
			+ " 10 different result pages open then the used entries will be 10 * search page size(25 by default)"
			+ " but this value should be more bigger if search performance is a problem. "
			+ "If the application has enabled projects functionally then the maximum value should correspond to the different active projects."
			+ "<br>Minimal value expression: (users * searchPageSize * 4) + (users * dashletsPageSize * (projects/4)) * 2.2"))
	private static final String CASE_ENTITY_CACHE = "CASE_ENTITY_CACHE";
	/** The Constant LOGGER. */
	@Inject
	private Logger LOGGER;

	@Inject
	protected DbDao dbDao;

	@Inject
	private SectionService sectionService;

	/** The link service. */
	@Inject
	@RelationalDb
	private LinkService linkService;
	/** The service register. */
	@Inject
	private ServiceRegister serviceRegister;
	/** The task executor. */
	@Inject
	private TaskExecutor taskExecutor;

	/** The rendition service. */
	@Inject
	private RenditionService renditionService;

	/**
	 * Initialize the cache.
	 */
	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public CaseInstance persistChanges(CaseInstance instance) {
		return super.persistChanges(instance);
	}

	@Override
	protected void onInstanceUpdated(CaseInstance instance) {
		// on any data change the role should be invalidated
		instance.setCalculatedUserRole(null);
		// force a DMS modification
		// CMF-185, CMF-800
		instance.getProperties().put(CaseProperties.DMS_TOUCH, System.currentTimeMillis());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void synchRevisions(CaseInstance instance, Long revision) {
		if (instance == null) {
			return;
		}
		instance.setRevision(revision);
		for (SectionInstance sectionInstance : instance.getSections()) {
			sectionInstance.setRevision(revision);
			for (Instance documentInstance : sectionInstance.getContent()) {
				// should not change the document revision is already set
				if ((documentInstance.getRevision() == null)
						|| (documentInstance.getRevision() == 0)) {
					documentInstance.setRevision(revision);
				}
			}
		}
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
		// we can throw an exception for just in case
	}

	@Override
	protected <E extends Entity<String>> List<E> findEntities(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(DbQueryTemplates.QUERY_CASE_ENTITIES_BY_DMS_ID_KEY,
				Arrays.asList(new Pair<String, Object>("documentManagementId", ids)));
		return list;
	}

	@Override
	protected <E extends Entity<String>> List<E> findEntitiesByPrimaryKey(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(DbQueryTemplates.QUERY_CASE_ENTITIES_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", ids)));
		return list;
	}

	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return ((CaseInstance) instance).getDmsId();
	}

	@Override
	protected <E extends Entity<?>> String getPrimaryKey(E entity) {
		return (String) entity.getId();
	}



	@Override
	protected CaseEntity createEntity(String dmsId) {
		CaseEntity entity = new CaseEntity();
		entity.setDocumentManagementId(dmsId);
		return entity;
	}

	@Override
	protected Class<CaseInstance> getInstanceClass() {
		return CaseInstance.class;
	}

	@Override
	protected Class<CaseEntity> getEntityClass() {
		return CaseEntity.class;
	}

	@Override
	protected Class<CaseDefinition> getDefinitionClass() {
		return CaseDefinition.class;
	}

	@Override
	protected String getCacheEntityName() {
		return CASE_ENTITY_CACHE;
	}

	@Override
	protected EntityLookupCallbackDAOAdaptor<String, CaseEntity, String> getEntityCacheProvider() {
		return new CaseEntityLookupDao();
	}

	@Override
	protected void populateInstanceForModel(CaseInstance instance, CaseDefinition model) {
		// will stick with the default model
		populateProperties(instance, model);
	}

	@Override
	protected DbDao getDbDao() {
		return dbDao;
	}

	@Override
	protected void onAfterSave(CaseInstance instance) {
		if (RuntimeConfiguration
				.isConfigurationSet(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN)) {
			// does not update/save children if desired
			return;
		}

		List<LinkReference> oldReferences = linkService.getLinks(instance.toReference(),
				LinkConstantsCmf.CASE_TO_SECTION);

		List<Instance> existingEntries = new LinkedList<Instance>();

		boolean isUnsaved = false;
		// convert sections and add them to the given case entity
		for (SectionInstance sectionInstance : instance.getSections()) {
			isUnsaved = !SequenceEntityGenerator.isPersisted(sectionInstance);
			sectionInstance.setOwningReference(instance.toReference());
			sectionService.save(sectionInstance, null);
			if (isUnsaved) {
				linkService.link(instance, sectionInstance, LinkConstantsCmf.CASE_TO_SECTION, null,
						null);
			} else {
				existingEntries.add(sectionInstance);
			}
		}

		// we should detect if any of the objects has been deleted so we could remove it
		for (LinkReference linkReference : oldReferences) {
			boolean found = false;
			for (Instance existing : existingEntries) {
				if (linkReference.getTo().getReferenceType().getJavaClass().isInstance(existing)
						&& linkReference.getTo().getIdentifier()
								.equals(existing.getId().toString())) {
					found = true;
					break;
				}
			}
			// if not found the old link in the current references we should remove it
			if (!found) {
				linkService.removeLink(linkReference);
			}
		}
	}

	@Override
	protected <E extends Instance> void loadChildren(E instance, boolean forBatchLoad, InstanceDao<E> dao) {
		if (forBatchLoad) {
			return;
		}
		if (instance instanceof CaseInstance) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Loadig children for " + instance.getClass().getSimpleName() + " id="
						+ instance.getId());
			}
			List<LinkReference> list = linkService.getLinks(instance.toReference(),
					LinkConstantsCmf.CASE_TO_SECTION);
			// batch loaded sections does not load documents
			// so we disable the check so when loading single case to load it's sections in a single
			// pass
			RuntimeConfiguration.setConfiguration(
					RuntimeConfigurationProperties.DO_NOT_LOAD_CHILDREN_OVERRIDE, Boolean.TRUE);
			try {
				List<Instance> children = BatchEntityLoader.loadFromReferences(new LinkIterable<>(
						list), serviceRegister, taskExecutor);
				List<SectionInstance> sections = cast(children);

				Collections.sort(sections, SORTABLE_COMPARATOR);
				((CaseInstance) instance).setSections(sections);
				((CaseInstance) instance).initBidirection();
				loadThumbnails((CaseInstance) instance);
			} finally {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.DO_NOT_LOAD_CHILDREN_OVERRIDE);
			}
		}
	}

	/**
	 * Load thumbnails for the case contents
	 * 
	 * @param instance
	 *            the instance
	 */
	private void loadThumbnails(CaseInstance instance) {
		Set<Instance> set = new HashSet<Instance>(32);
		set.add(instance);
		for (SectionInstance section : instance.getSections()) {
			set.add(section);
			set.addAll(section.getContent());
		}
		renditionService.loadThumbnails(set);
	}

	/**
	 * Cast.
	 * 
	 * @param list
	 *            the list
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	private List<SectionInstance> cast(List<? extends Instance> list) {
		return (List<SectionInstance>) list;
	}

	@Override
	protected String getOwningInstanceQuery() {
		return DbQueryTemplates.QUERY_CASES_BY_OWN_REF_KEY;
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
	 * Case entity cache lookup DAO
	 *
	 * @author BBonev
	 */
	class CaseEntityLookupDao extends DefaultEntityLookupDao {

		@Override
		protected String getValueKeyInternal(CaseEntity value) {
			return value.getDocumentManagementId();
		}

		@Override
		protected List<CaseEntity> fetchEntityByValue(String key) {
			return getDbDao().fetchWithNamed(DbQueryTemplates.QUERY_CASE_BY_DMS_ID_KEY,
					Arrays.asList(new Pair<String, Object>("documentManagementId", key)));
		}

	}
}
