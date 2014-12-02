/*
 *
 */
package com.sirma.itt.objects.services.impl.dao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.RelationalDb;
import com.sirma.itt.emf.db.SemanticDb;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.dao.BaseSemanticInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.LinkIterable;
import com.sirma.itt.objects.constants.ObjectLinkConstants;
import com.sirma.itt.objects.constants.ObjectProperties;
import com.sirma.itt.objects.domain.ObjectTypesObject;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.SavedFilter;
import com.sirma.itt.objects.event.ObjectChangeEvent;

/**
 * Default implementation for object instance support.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesObject.SAVED_FILTER)
public class SavedFilterDao extends
		BaseSemanticInstanceDaoImpl<SavedFilter, Serializable, String, ObjectDefinition> implements
		InstanceDao<SavedFilter> {

	private static final Operation OPERATION = new Operation();

	/** The Constant OBJECT_ENTITY_CACHE. */
	@CacheConfiguration(container = "obj", transaction = CacheTransactionMode.FULL_XA, expiration = @Expiration(maxIdle = 1800000), eviction = @Eviction(maxEntries = 500), doc = @Documentation(""
			+ "Fully transactional cache used to contain the activly loaded savedFilters instances. "
			+ "The cache will have at most 2 entries for every created/updated filter in a search page per user. Entity is loaded in the cache after the user creates new filter or one is being updated."
			+ "<br>Minimal value expression: (users * 2.15"))
	private static final String SAVED_FILTER_ENTITY_CACHE = "SAVED_FILTER_ENTITY_CACHE";

	/** The db dao. */
	@Inject
	@SemanticDb
	private DbDao dbDao;

	/** The Constant LOGGER. */
	@Inject
	private Logger LOGGER;

	/** The document service. */
	@Inject
	private DocumentService documentService;

	/** The relational link service. */
	@Inject
	@RelationalDb
	private LinkService relationalLinkService;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The event service. */
	@Inject
	private EventService eventService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DbDao getDbDao() {
		return dbDao;
	}

	@Override
	protected void onInstanceUpdated(SavedFilter instance) {
		setCurrentUserTo(instance, DefaultProperties.MODIFIED_BY);
		Date date = new Date();
		if (instance.getProperties().get(DefaultProperties.CREATED_ON) == null) {
			instance.getProperties().put(DefaultProperties.CREATED_ON, date);
			setCurrentUserTo(instance, DefaultProperties.CREATED_BY);
		}
		instance.getProperties().put(DefaultProperties.MODIFIED_ON, date);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SavedFilter persistChanges(SavedFilter instance) {
		return super.persistChanges(instance);
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
	public void synchRevisions(SavedFilter instance, Long revision) {
		instance.setRevision(revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateInstanceForModel(SavedFilter instance, ObjectDefinition model) {
		instance.setContainer(model.getContainer());
		populateProperties(instance, model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SavedFilter createEntity(String dmsId) {
		SavedFilter entity = new SavedFilter();
		entity.setDmsId(dmsId);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<SavedFilter> getInstanceClass() {
		return SavedFilter.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<SavedFilter> getEntityClass() {
		return SavedFilter.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ObjectDefinition> getDefinitionClass() {
		return ObjectDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCacheEntityName() {
		return SAVED_FILTER_ENTITY_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityLookupCallbackDAOAdaptor<Serializable, SavedFilter, String> getEntityCacheProvider() {
		return new SavedFilterEntityLookupDao();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<Serializable>> List<E> findEntities(Set<String> ids) {
		// note this is the same as calling the findEntitiesByPrimaryKey method
		return getDbDao().fetchWithNamed("SELECT_BY_IDS",
				Arrays.asList(new Pair<String, Object>("URIS", ids)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<Serializable>> List<E> findEntitiesByPrimaryKey(
			Set<Serializable> ids) {
		return getDbDao().fetchWithNamed("SELECT_BY_IDS",
				Arrays.asList(new Pair<String, Object>("URIS", ids)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return ((SavedFilter) instance).getDmsId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<?>> String getPrimaryKey(E entity) {
		return (String) entity.getId();
	}

	@Override
	protected String getOwningInstanceQuery() {
		return null;
	}

	@Override
	protected void onBeforeSave(SavedFilter instance, SavedFilter oldCached) {
		super.onBeforeSave(instance, oldCached);

		DocumentInstance view = (DocumentInstance) instance.getProperties().remove(
				ObjectProperties.DEFAULT_VIEW);
		if (view != null) {
			try {
				RuntimeConfiguration.enable(RuntimeConfigurationProperties.DISABLE_AUTOMATIC_LINKS);
				view.getProperties().put(DefaultProperties.TRANSIENT_SEMANTIC_INSTANCE,
						Boolean.TRUE);
				documentService.save(view, OPERATION);
			} finally {
				RuntimeConfiguration
						.disable(RuntimeConfigurationProperties.DISABLE_AUTOMATIC_LINKS);
				view.getProperties().remove(DefaultProperties.TRANSIENT_SEMANTIC_INSTANCE);
			}
			String dmsId = view.getDmsId();
			if ((dmsId != null) && SequenceEntityGenerator.isPersisted(view)) {
				instance.getProperties().put(ObjectProperties.DEFAULT_VIEW_LOCATION, dmsId);
				// copy the version property of the document
				CollectionUtils.copyValue(view, DocumentProperties.VERSION, instance,
						ObjectProperties.VERSION);
				// the properties will be saved later..
				eventService.fire(new ObjectChangeEvent(instance));
				// link the object and his view
				relationalLinkService.link(instance.toReference(), view.toReference(),
						ObjectLinkConstants.OBJECT_VIEW, null, null);
			}
		}
	}

	@Override
	protected Iterable<InstanceReference> getCurrentChildren(SavedFilter targetInstance) {
		List<LinkReference> list = linkService.getLinks(targetInstance.toReference(),
				LinkConstants.PARENT_TO_CHILD);
		return new LinkIterable<>(list);
	}

	@Override
	protected void attachNewChildren(SavedFilter targetInstance, Operation operation,
			List<Instance> newChildren) {
		// nothing to do here for now
	}

	@Override
	protected void detachExistingChildren(SavedFilter sourceInstance, Operation operation,
			List<Instance> oldChildren) {
		// nothing to do here for now
	}

	/**
	 * Project entity cache lookup dao.
	 * 
	 * @author BBonev
	 */
	class SavedFilterEntityLookupDao extends DefaultEntityLookupDao {

		/**
		 * Fetch entity by value.
		 * 
		 * @param key
		 *            the key
		 * @return the list
		 */
		@Override
		protected List<SavedFilter> fetchEntityByValue(String key) {
			return Collections.emptyList();
		}

		/**
		 * Gets the value key internal.
		 * 
		 * @param value
		 *            the value
		 * @return the value key internal
		 */
		@Override
		protected String getValueKeyInternal(SavedFilter value) {
			return value.getDmsId();
		}
	}

}
