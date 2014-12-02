/*
 *
 */
package com.sirma.itt.cmf.services.impl.dao;

import static com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants.CREATE_IDOC;
import static com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants.MOVE_OTHER_CASE;
import static com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants.MOVE_SAME_CASE;
import static com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants.UPLOAD;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionProxy;
import com.sirma.itt.cmf.beans.entity.SectionEntity;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.concurrent.GenericAsyncTask;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.RelationalDb;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.Lockable;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.LinkIterable;

/**
 * The Class SectionInstanceDao. {@link com.sirma.itt.emf.instance.dao.InstanceDao} implementation
 * for {@link SectionInstance}.
 * 
 * @author BBonev
 * @param <S>
 *            the generic type
 * @param <D>
 *            the generic type
 */
public abstract class BaseSectionInstanceDao<S extends SectionInstance, D extends DefinitionModel>
		extends BaseInstanceDaoImpl<S, SectionEntity, String, String, D> {

	/** The Constant SECTION_ENTITY_CACHE. */
	@CacheConfiguration(transaction = CacheTransactionMode.FULL_XA, container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), doc = @Documentation(""
			+ "Fully transactional cache used to contain the activly loaded section instances. "
			+ "The cache will have at most 2 entries for every section in a case instance for all active users. "
			+ "This means if a there are 10 active users and they perform a search for cases and have a"
			+ " 10 different result pages open then the used entries will be 10 * search page size(25 by default) * average number of sections per case"
			+ " but this value should be 10-20% bigger if search performance is a problem. "
			+ "If the application has enabled projects functionally then the maximum value should correspond to the different active projects."
			+ "<br>Minimal value expression: (((users * searchPageSize * 4) + (users * dashletsPageSize * (projects/4))) * averageSections) * 2.2"))
	static final String SECTION_ENTITY_CACHE = "SECTION_ENTITY_CACHE";

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The service register. */
	@Inject
	private ServiceRegister serviceRegister;

	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	/** The task executor. */
	@Inject
	private TaskExecutor taskExecutor;

	@Inject
	@RelationalDb
	private LinkService linkService;

	@Inject
	private EventService eventService;

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseSectionInstanceDao.class);

	private static final boolean debug = LOGGER.isDebugEnabled();;

	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DbDao getDbDao() {
		return dbDao;
	}

	@Override
	protected void updateCreatorAndModifierInfo(SectionInstance instance) {
		// no need to set anything
	}

	@Override
	protected D getInstanceDefinition(String definitionId, Class<D> definitionClass) {
		DefinitionModel definition = dictionaryService.getDefinition(definitionClass, definitionId);
		if (definition instanceof GenericDefinition) {
			return (D) new SectionDefinitionProxy((GenericDefinition) definition);
		}
		return (D) definition;
	}

	@Override
	protected Iterable<InstanceReference> getCurrentChildren(SectionInstance targetInstance) {
		List<LinkReference> list = linkService.getLinks(targetInstance.toReference(),
				LinkConstantsCmf.SECTION_TO_CHILD);
		return new LinkIterable<>(list);
	}

	@Override
	protected void attachNewChildren(S targetInstance, Operation operation,
			List<Instance> newChildren) {
		for (Instance instance : newChildren) {
			linkService.link(targetInstance, instance, LinkConstantsCmf.SECTION_TO_CHILD, null,
					null);
			if (operation != null) {
				String operationId = operation.getOperation();
				boolean relink = false;
				if (EqualsHelper.nullSafeEquals(operation.getOperation(), MOVE_OTHER_CASE)
						|| EqualsHelper.nullSafeEquals(operation.getOperation(), MOVE_SAME_CASE)) {
					List<LinkReference> links = linkService.getLinks(instance.toReference(),
							LinkConstants.PRIMARY_PARENT);
					// we should relink only if the link does not exists. If exists means we are
					// moving a document out of non primary parent section and we should not created
					// it again
					relink = links.isEmpty();
				}
				if (relink || EqualsHelper.nullSafeEquals(operationId, UPLOAD)
						|| EqualsHelper.nullSafeEquals(operationId, CREATE_IDOC)) {
					linkService.link(instance, targetInstance, LinkConstants.PRIMARY_PARENT, null,
							null);
				}
			}
		}
	}

	/**
	 * Checks if is child allowed.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if is child allowed
	 */
	@Override
	protected boolean isChildAllowed(Instance instance) {
		// we forbid cases to be children to a section
		// also we forbid if not lockable - currently only implementations are documents and objects
		// so only these types of children could be added
		return instance instanceof Lockable;
	}

	@Override
	protected void detachExistingChildren(S sourceInstance, Operation operation,
			List<Instance> oldChildren) {
		for (Instance instance : oldChildren) {
			linkService.unlink(sourceInstance.toReference(), instance.toReference(),
					LinkConstantsCmf.SECTION_TO_CHILD, null);
			// if moving document we should remove the primary parent link if any
			if (EqualsHelper.nullSafeEquals(operation.getOperation(), MOVE_OTHER_CASE)
					|| EqualsHelper.nullSafeEquals(operation.getOperation(), MOVE_SAME_CASE)) {
				// we are going to unlink the primary parent only if moved out of his primary
				// section otherwise the link will be kept. this means when attaching again for the
				// same operation we should check if any primary parent link exists and if so to do
				// nothing
				List<LinkReference> links = linkService.getLinks(instance.toReference(),
						LinkConstants.PRIMARY_PARENT);
				if (!links.isEmpty()) {
					InstanceReference reference = links.get(0).getTo();

					if (sourceInstance.toReference().equals(reference)) {
						linkService.unlink(instance.toReference(), reference,
								LinkConstantsCmf.PRIMARY_PARENT, null);
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onAfterSave(S instance) {
		if (RuntimeConfiguration
				.isConfigurationSet(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN)) {
			// does not update/save children if desired
			return;
		}
		List<GenericAsyncTask> tasks = new ArrayList<GenericAsyncTask>(instance.getContent().size());

		List<LinkReference> oldReferences = linkService.getLinks(instance.toReference(),
				LinkConstantsCmf.SECTION_TO_CHILD);

		List<Instance> notLinked = new LinkedList<Instance>();
		List<Instance> existingEntries = new LinkedList<Instance>();
		Set<Instance> uniqueContent = CollectionUtils.createLinkedHashSet(instance.getContent()
				.size());
		// prepare tasks for parallel persist
		for (Iterator<Instance> it = instance.getContent().iterator(); it.hasNext();) {
			Instance content = it.next();
			// ensure that the same object is not added more then once in the section
			if (uniqueContent.contains(content) || !isChildAllowed(content)) {
				it.remove();
				if (debug) {
					LOGGER.debug("Removed duplicate/forbidden content element "
							+ content.getClass().getSimpleName() + " id=" + content.getId()
							+ " from a section " + instance.getIdentifier() + " id="
							+ instance.getId());
				}
				continue;
			}
			uniqueContent.add(content);

			if (content instanceof OwnedModel) {
				OwnedModel ownedModel = (OwnedModel) content;
				ownedModel.setOwningInstance(instance);
			}
			boolean attached = false;
			if (!SequenceEntityGenerator.isPersisted(content)) {
				notLinked.add(content);
			} else {
				// mark the given entry for linking as is not found in the old references
				boolean found = false;
				for (LinkReference linkReference : oldReferences) {
					if (linkReference.getTo().getReferenceType().getJavaClass().isInstance(content)
							&& linkReference.getTo().getIdentifier()
									.equals(content.getId().toString())) {
						found = true;
						if (linkReference.getProperties().get(LinkConstants.IS_ATTACHED) != null) {
							attached = (Boolean) linkReference.getProperties().get(
									LinkConstants.IS_ATTACHED);
						}
						break;
					}
				}
				if (!found) {
					notLinked.add(content);
				}
				existingEntries.add(content);
			}
			InstancePersistTask task = new InstancePersistTask(content, attached);
			// disabled parallel processing
			if (!(content instanceof DocumentInstance)) {
				task.executeTask();
			} else {
				if (!SequenceEntityGenerator.isPersisted(content)) {
					tasks.add(task);
				} else {
					task.executeTask();
				}
			}
		}
		// the method blocks until all tasks are executed
		taskExecutor.execute(tasks);

		attachNewChildren(instance, null, notLinked);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public S persistChanges(S instance) {
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
	public void synchRevisions(S instance, Long revision) {
		instance.setRevision(revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void populateInstanceForModel(S instance, D model) {
		if (model instanceof Purposable) {
			instance.setPurpose(((Purposable) model).getPurpose());
		}

		// update instance that is created not from a regular definition implementation
		if (model instanceof SectionDefinitionProxy) {
			instance.setStandalone(true);
		}

		populateProperties(instance, model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SectionEntity createEntity(String dmsId) {
		SectionEntity entity = new SectionEntity();
		entity.setDocumentManagementId(dmsId);
		return entity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<SectionEntity> getEntityClass() {
		return SectionEntity.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCacheEntityName() {
		return SECTION_ENTITY_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected EntityLookupCallbackDAOAdaptor<String, SectionEntity, String> getEntityCacheProvider() {
		return new SectionEntityLookupDao();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<String>> List<E> findEntities(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(
				DbQueryTemplates.QUERY_SECTION_ENTITIES_BY_DMS_ID_KEY,
				Arrays.asList(new Pair<String, Object>("documentManagementId", ids)));
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Entity<String>> List<E> findEntitiesByPrimaryKey(Set<String> ids) {
		List<E> list = getDbDao().fetchWithNamed(DbQueryTemplates.QUERY_SECTION_ENTITIES_BY_ID_KEY,
				Arrays.asList(new Pair<String, Object>("id", ids)));
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <E extends Instance> String getSecondaryKey(E instance) {
		return ((SectionInstance) instance).getDmsId();
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
		return DbQueryTemplates.QUERY_SECTIONS_BY_OWN_REF_KEY;
	}

	@Override
	protected <E extends Instance> void loadChildren(E instance, boolean forBatchLoad,
			InstanceDao<E> dao) {
		// does not load children when loading for batch and if standalone folder
		if ((forBatchLoad || ((SectionInstance) instance).isStandalone())
				&& !RuntimeConfiguration
						.isConfigurationSet(RuntimeConfigurationProperties.DO_NOT_LOAD_CHILDREN_OVERRIDE)) {
			return;
		}

		if (instance instanceof SectionInstance) {
			List<LinkReference> list = linkService.getLinks(instance.toReference(),
					LinkConstantsCmf.SECTION_TO_CHILD);
			Map<Serializable, LinkReference> references = CollectionUtils
					.createLinkedHashMap(list.size());
			for (LinkReference reference : list) {
				references.put(reference.getTo().getIdentifier(), reference);
			}
			// no need to load the sections content in parallel. The sections are loaded in parallel
			// the first time.
			List<Instance> children = BatchEntityLoader.loadFromReferences(
					new LinkIterable<>(list), serviceRegister, taskExecutor);

			if (references.size() != children.size()) {
				// we have missing children here and we should update the links so we to stop
				// looking for them
				for (Instance child : children) {
					references.remove(child.getId());
				}
				for (LinkReference linkReference : references.values()) {
					linkService.removeLinkById(linkReference.getId());
				}
			}
			((SectionInstance) instance).setContent(children);
			((SectionInstance) instance).initBidirection();
		}
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
	 * Fork/join task for uploading documents to DMS.
	 *
	 * @author BBonev
	 */
	class InstancePersistTask extends GenericAsyncTask {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -3747987085046032641L;
		private final Instance instance;
		private Instance oldCopy;
		private final boolean attached;

		/**
		 * Instantiates a new document upload task.
		 *
		 * @param instance
		 *            the instance
		 * @param attached
		 *            whether this is not a primary parent-child
		 */
		public InstancePersistTask(Instance instance, boolean attached) {
			super();
			this.instance = instance;
			this.attached = attached;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean executeTask() {
			Instance current = getInstance();
			// load properties of the object if not loaded to prevent properties removal
			if ((current.getProperties() == null) || current.getProperties().isEmpty()) {
				propertiesService.loadProperties(current);
			}
			try {
				if (InstanceUtil.isPersisted(current)) {
					// get a copy of the old data so we can use when firing the persisted event
					// if we are here the instance is probably in the cache so the operation should
					// be relatively cheap
					oldCopy = serviceRegister.getInstanceDao(current).loadInstance(current.getId(),
							null, true);
				}
				Operation operation = null;
				Serializable operationId = RuntimeConfiguration
						.getConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
				if (operationId instanceof String) {
					// this should not propagate delete or any final operation operation for
					// attached instances
					if (attached
							&& (ActionTypeConstants.DELETE.equals(operationId)
									|| ActionTypeConstants.COMPLETE.equals(operationId) || ActionTypeConstants.STOP
										.equals(operationId))) {
						return true;
					}
					operation = new Operation((String) operationId);
				} else {
					operation = new Operation(ActionTypeConstants.EDIT_DETAILS);
				}
				RuntimeConfiguration.setConfiguration(
						RuntimeConfigurationProperties.DO_NOT_FIRE_PERSIST_EVENT, Boolean.TRUE);
				instanceService.save(current, operation);
			} finally {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.DO_NOT_FIRE_PERSIST_EVENT);
			}
			return true;
		}

		@Override
		public void executeOnSuccess() {
			InstanceEventProvider<Instance> eventProvider = serviceRegister
					.getEventProvider(getInstance());
			if (eventProvider != null) {
				InstancePersistedEvent<Instance> persistedEvent = eventProvider
						.createPersistedEvent(getInstance(), oldCopy, (String) RuntimeConfiguration
								.getConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION));
				eventService.fire(persistedEvent);
			}
		}

		/**
		 * Getter method for instance.
		 *
		 * @return the instance
		 */
		public Instance getInstance() {
			return instance;
		}
	}

	/**
	 * Case entity cache lookup DAO.
	 *
	 * @author BBonev
	 */
	class SectionEntityLookupDao extends DefaultEntityLookupDao {

		/**
		 * Gets the value key internal.
		 *
		 * @param value
		 *            the value
		 * @return the value key internal
		 */
		@Override
		protected String getValueKeyInternal(SectionEntity value) {
			return value.getDocumentManagementId();
		}

		/**
		 * Fetch entity by value.
		 *
		 * @param key
		 *            the key
		 * @return the list
		 */
		@Override
		protected List<SectionEntity> fetchEntityByValue(String key) {
			return this.getDbDao().fetchWithNamed(DbQueryTemplates.QUERY_SECTION_BY_DMS_ID_KEY,
					Arrays.asList(new Pair<String, Object>("documentManagementId", key)));
		}
	}
}
