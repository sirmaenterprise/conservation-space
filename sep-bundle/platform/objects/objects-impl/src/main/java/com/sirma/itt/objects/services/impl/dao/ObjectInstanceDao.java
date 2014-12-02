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
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.event.ObjectChangeEvent;

/**
 * Default implementation for object instance support.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesObject.OBJECT)
public class ObjectInstanceDao extends
		BaseSemanticInstanceDaoImpl<ObjectInstance, Serializable, String, ObjectDefinition>
		implements InstanceDao<ObjectInstance> {

	private static final Operation OPERATION = new Operation();

	/** The Constant OBJECT_ENTITY_CACHE. */
	@CacheConfiguration(container = "obj", transaction = CacheTransactionMode.FULL_XA, expiration = @Expiration(maxIdle = 1800000), eviction = @Eviction(maxEntries = 500), doc = @Documentation(""
			+ "Fully transactional cache used to contain the activly loaded object instances. "
			+ "The cache will have at most 2 entries for every object in a section in a case instance for all active users. "
			+ "This means if a there are 10 active users and they perform a search for cases and have a"
			+ " 10 different result pages open then the used entries will be 10 * search page size(25 by default) * average number of object sections per case * average number of object per section"
			+ " but this value should be 10-20% bigger if search performance is a problem. "
			+ "If the application has enabled projects functionally then the maximum value should correspond to the different active projects."
			+ "<br>Minimal value expression: (((((users * searchPageSize * 4) + (users * dashletsPageSize * (projects/4))) * averageObjectSections) * 2.15) * averageObjects) * 2.15"))
	private static final String OBJECT_ENTITY_CACHE = "OBJECT_ENTITY_CACHE";

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
	protected void onInstanceUpdated(ObjectInstance instance) {
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
	public ObjectInstance persistChanges(ObjectInstance instance) {
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
	public void synchRevisions(ObjectInstance instance, Long revision) {
		instance.setRevision(revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateInstanceForModel(ObjectInstance instance, ObjectDefinition model) {
		instance.setContainer(model.getContainer());
		populateProperties(instance, model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ObjectInstance createEntity(String dmsId) {
		ObjectInstance entity = new ObjectInstance();
		entity.setDmsId(dmsId);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ObjectInstance> getInstanceClass() {
		return ObjectInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ObjectInstance> getEntityClass() {
		return ObjectInstance.class;
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
		return OBJECT_ENTITY_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityLookupCallbackDAOAdaptor<Serializable, ObjectInstance, String> getEntityCacheProvider() {
		return new ObjectEntityLookupDao();
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
		return ((ObjectInstance) instance).getDmsId();
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
	protected void onBeforeSave(ObjectInstance instance, ObjectInstance oldCached) {
		super.onBeforeSave(instance, oldCached);

		DocumentInstance view = (DocumentInstance) instance.getProperties().remove(
				ObjectProperties.DEFAULT_VIEW);
		if (view != null) {
			try {
				RuntimeConfiguration.enable(RuntimeConfigurationProperties.DISABLE_AUTOMATIC_LINKS);
				RuntimeConfiguration.enable(RuntimeConfigurationProperties.DISABLE_AUDIT_LOG);
				view.getProperties().put(DefaultProperties.TRANSIENT_SEMANTIC_INSTANCE,
						Boolean.TRUE);
				documentService.save(view, OPERATION);
			} finally {
				RuntimeConfiguration
						.disable(RuntimeConfigurationProperties.DISABLE_AUTOMATIC_LINKS);
				RuntimeConfiguration.disable(RuntimeConfigurationProperties.DISABLE_AUDIT_LOG);
				view.getProperties().remove(DefaultProperties.TRANSIENT_SEMANTIC_INSTANCE);
			}
			CollectionUtils.copyValue(view, DocumentProperties.CONTENT, instance,
					ObjectProperties.CONTENT);

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
	protected Iterable<InstanceReference> getCurrentChildren(ObjectInstance targetInstance) {
		List<LinkReference> list = linkService.getLinks(targetInstance.toReference(),
				LinkConstants.PARENT_TO_CHILD);
		return new LinkIterable<>(list);
	}

	@Override
	protected void attachNewChildren(ObjectInstance targetInstance, Operation operation,
			List<Instance> newChildren) {
		// nothing to do here for now
	}

	@Override
	protected void detachExistingChildren(ObjectInstance sourceInstance, Operation operation,
			List<Instance> oldChildren) {
		// nothing to do here for now
	}

	/**
	 * Project entity cache lookup dao.
	 *
	 * @author BBonev
	 */
	class ObjectEntityLookupDao extends DefaultEntityLookupDao {

		/**
		 * Fetch entity by value.
		 *
		 * @param key
		 *            the key
		 * @return the list
		 */
		@Override
		protected List<ObjectInstance> fetchEntityByValue(String key) {
			// FIXME: change to semantic query
			// return getDbDao().fetchWithNamed(DbQueryTemplatesObjects.QUERY_OBJECT_BY_DMS_ID_KEY,
			// Arrays.asList(new Pair<String, Object>("documentManagementId", key)));
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
		protected String getValueKeyInternal(ObjectInstance value) {
			return value.getDmsId();
		}
	}

}
