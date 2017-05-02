package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.event.AuditableEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.instance.properties.PropertiesChangeEvent;
import com.sirma.itt.seip.instance.properties.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.seip.util.EqualsHelper.MapValueComparison;

/**
 * Common implementation logic for {@link InstanceDao} realizations.
 *
 * @author BBonev
 * @param <T>
 *            the instance/entity type
 * @param <P>
 *            the primary key type
 * @param <K>
 *            the secondary key type
 * @param <D>
 *            the target definition type
 */
public abstract class BaseSemanticInstanceDaoImpl<T extends Instance, P extends Serializable, K extends Serializable, D extends DefinitionModel>
		extends BaseInstanceDaoImpl<T, P, K, D> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseSemanticInstanceDaoImpl.class);

	@Inject
	@SemanticDb
	protected javax.enterprise.inject.Instance<DbDao> semanticDbDao;

	/** The Constant FORBIDDEN_PROPERTIES. */
	private Set<String> forbiddenProperties;

	@Inject
	@ExtensionPoint(value = SemanticNonPersistentPropertiesExtension.TARGET_NAME)
	private Iterable<SemanticNonPersistentPropertiesExtension> semanticNonPersistentProperties;

	@Inject
	@SemanticDb
	private InstanceLoader instanceLoader;

	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Initialize the cache and logger.
	 */
	@PostConstruct
	public void initialize() {
		forbiddenProperties = new LinkedHashSet<>(50);
		for (SemanticNonPersistentPropertiesExtension extension : semanticNonPersistentProperties) {
			forbiddenProperties.addAll(extension.getNonPersistentProperties());
		}
	}

	/**
	 * Save properties on persist changes with default save mode of Replace.
	 *
	 * @param instance
	 *            the instance
	 */
	@Override
	protected void savePropertiesOnPersistChanges(Instance instance) {
		// disable external properties loading
	}

	@Override
	@SuppressWarnings("unchecked")

	public void loadProperties(Instance instance) {
		Instance copy = loadInstanceInternal((P) instance.getId(), null);
		// update the current instance properties but also protect from concurrent properties update
		synchronized (instance) {
			instance.getProperties().putAll(copy.getProperties());
			// disable external properties loading
			loadHeadersInternal(instance, null, true);
		}
	}

	@Override
	public void saveProperties(Instance instance, boolean addOnly) {
		if (addOnly) {
			// REVIEW: partial semantic properties persist
			// when add only operation is perform because the semantic does not support partial
			// properties update we need to ensure the old not modified properties are fetched and
			// added to the modified so that we does not loose some of them.
			// If partial semantic properties updated is implemented the could be removed
			T cached = getInstanceLoader().find(instance.getId());
			T oldInstance = toInstance(cached);
			oldInstance.getProperties().putAll(instance.getProperties());
			Map<String, Serializable> properties = CollectionUtils
					.createLinkedHashMap(oldInstance.getProperties().size());
			properties.putAll(oldInstance.getProperties());
			properties.putAll(instance.getProperties());
			instance.setProperties(properties);
		}
		persistChanges(instance);
	}

	@Override
	public <I extends Instance> List<I> loadInstances(Instance owner, boolean loadProperties) {
		// eventually this could be changed to semantic query sometime
		String query = getOwningInstanceQuery();
		// if no query is provided then we cannot do anything so will return now
		if (!idManager.isPersisted(owner) || query == null) {
			// no allowed children on an instance
			return CollectionUtils.emptyList();
		}

		InstanceReference reference = typeConverter.convert(InstanceReference.class, owner);
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<String, Object>("sourceId", reference.getIdentifier()));
		args.add(new Pair<String, Object>("sourceType", reference.getReferenceType().getId()));
		List<Long> list = getDbDao().fetchWithNamed(query, args);

		return loadInstancesByDbKey(list, loadProperties);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends Serializable> Class<I> getPrimaryIdType() {
		return (Class<I>) String.class;
	}

	@Override
	public void touchInstance(Object object) {
		Set<Serializable> referencedInstances = getInstanceIds(object).collect(Collectors.toSet());
		if (isEmpty(referencedInstances)) {
			return;
		}
		EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache = getCache();
		for (Serializable instanceId : referencedInstances) {
			cache.removeByKey(instanceId);
		}
	}

	private Stream<Serializable> getInstanceIds(Object object) {
		if (object instanceof String) {
			return Stream.of((Serializable) object);
		} else if (object instanceof Instance) {
			return Stream.of(((Instance) object).getId());
		} else if (object instanceof InstanceReference) {
			return Stream.of(((InstanceReference) object).getIdentifier());
		} else if (object instanceof Collection) {
			return ((Collection<?>) object).stream().flatMap(item -> getInstanceIds(item));
		}
		return Stream.empty();
	}

	/**
	 * Gets the query that should be used for retrieving entity ID by owning instance reference. The query should have 2
	 * arguments:
	 * <ul>
	 * <li>sourceId - the id for the instance reference identifier
	 * <li>sourceType - the id for the data type definition id
	 * </ul>
	 * If the query is not supported <code>null</code> should be returned
	 *
	 * @return the owning instance query or <code>null</code> if not supported.
	 */
	@Override
	protected abstract String getOwningInstanceQuery();

	/**
	 * Convert and save the given instance as entity.<br>
	 * <b>NOTE:</b>The provided implementation handles basic transformation, persisting and cache updating
	 *
	 * @param instance
	 *            the instance
	 * @return the converted entity
	 */
	@Override
	protected <I extends Instance> I convertAndSave(I instance) {
		T entity = toEntity(instance);
		T oldCached = null;
		if (idManager.isPersisted(instance)) {
			oldCached = getInstanceLoader().find(instance.getId());
			// disabled so the method could handle newly added objects that are imported into the
			// system and probably non existent in DB
		}
		onBeforeSave(entity, oldCached);
		Operation currentOperation = (Operation) Options.CURRENT_OPERATION.get();
		eventService.fire(new AuditableEvent(entity, currentOperation.getUserOperationId()));
		entity = getDbDao().saveOrUpdate(entity, oldCached);
		if (instance.getId() == null) {
			instance.setId(entity.getId());
		}
		onAfterSave(instance);

		// update the cache
		// remove cache entry so that external object properties update will be fetched
		removeFromCache(entity, oldCached);

		// generate properties change event
		Map<String, Serializable> added = CollectionUtils.createLinkedHashMap(entity.getProperties().size() << 1);
		int oldSize = 10;
		if (oldCached != null) {
			oldSize = oldCached.getProperties().size();
		}
		Map<String, Serializable> removed = CollectionUtils.createLinkedHashMap(oldSize << 1);

		// check for modified properties and notify for changes
		// REVIEW: this here could be optimization when saving properties to semantic database
		if (calculateModifiedProperties(entity, oldCached, added, removed)) {
			Options.DISABLE_AUDIT_LOG.enable();
			eventService.fire(
					new PropertiesChangeEvent(entity, added, removed, currentOperation));
			Options.DISABLE_AUDIT_LOG.disable();
		}
		// this should not fail as the instance type and the entity type are the same
		return (I) oldCached;
	}

	protected void removeFromCache(T newEntity, T oldEntity) {
		transactionSupport.invokeAfterTransactionCompletion(() -> {
			Set<Serializable> referencedInstances = collectReferencedIds(newEntity);
			referencedInstances.add(newEntity.getId());
			if (newEntity instanceof OwnedModel) {
				// these should be the same, but current implementation does not have a reference
				if (((OwnedModel) newEntity).getOwningReference() != null) {
					referencedInstances.add(((OwnedModel) newEntity).getOwningReference().getIdentifier());
				} else if (((OwnedModel) newEntity).getOwningInstance() != null) {
					referencedInstances.add(((OwnedModel) newEntity).getOwningInstance().getId());
				}
			}
			if (oldEntity != null) {
				referencedInstances.addAll(collectReferencedIds(oldEntity));
			}

			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache = getCache();
			for (Serializable instanceId : referencedInstances) {
				cache.removeByKey(instanceId);
			}
			LOGGER.trace("Entities removed from the cache: {}.", referencedInstances);
		});
	}

	@SuppressWarnings("unchecked")
	private Set<Serializable> collectReferencedIds(T newEntity) {
		return dictionaryService
				.getInstanceDefinition(newEntity)
					.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty())
					.map(field -> newEntity.get(field.getName()))
					.filter(Objects::nonNull)
					.flatMap(value -> value instanceof Collection ? ((Collection<Serializable>) value).stream()
							: Stream.of(value))
					.collect(Collectors.toSet());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <I extends Instance> T toEntity(I source) {
		// remove all forbidden for persist properties
		// NOTE: this could lead is some cases to a bug when on saving some of the properties
		// disappear
		if (source.getProperties().keySet().removeAll(forbiddenProperties)) {
			LOGGER.trace("Removed some forbidden properties. The full list of forbidden properties is: {}",
					forbiddenProperties);
		}
		Instance entity = (Instance) getInstanceLoader()
				.getPersistCallback()
					.getEntityConverter()
					.convertToEntity(source);
		entity.setType(source.type());
		return (T) entity;
	}

	/**
	 * Calculate modified properties.
	 *
	 * @param entity
	 *            the entity
	 * @param oldCached
	 *            the old cached
	 * @param added
	 *            the added
	 * @param removed
	 *            the removed
	 * @return true, if any properties have been found as modified.
	 */
	protected boolean calculateModifiedProperties(T entity, T oldCached, Map<String, Serializable> added,
			Map<String, Serializable> removed) {
		Map<String, Serializable> newProps = entity.getProperties();
		Map<String, Serializable> oldProps = new HashMap<>(1);
		if (oldCached != null) {
			oldProps = oldCached.getProperties();
		}
		// Now find out what's changed
		Map<String, MapValueComparison> diff = EqualsHelper.getMapComparison(oldProps, newProps);
		for (Map.Entry<String, MapValueComparison> entry : diff.entrySet()) {
			String qname = entry.getKey();

			switch (entry.getValue()) {
				case EQUAL:
					// Ignore
					break;
				case LEFT_ONLY:
					// Not in the new properties
					removed.put(qname, oldProps.get(qname));
					break;
				case NOT_EQUAL:
					// Must remove from the LHS
					removed.put(qname, oldProps.get(qname));
					added.put(qname, newProps.get(qname));
					break;
				case RIGHT_ONLY:
					// We're adding this
					added.put(qname, newProps.get(qname));
					break;
				default:
					throw new IllegalStateException("Unknown MapValueComparison: " + entry.getValue());
			}
		}

		return !removed.isEmpty() || !added.isEmpty();
	}

	/**
	 * Getter method for dbDao.
	 *
	 * @return the dbDao
	 */
	@Override
	protected DbDao getDbDao() {
		return semanticDbDao.get();
	}

	/**
	 * Gets the instance loader.
	 *
	 * @return the instance loader
	 */
	@Override
	protected InstanceLoader getInstanceLoader() {
		return instanceLoader;
	}
}
