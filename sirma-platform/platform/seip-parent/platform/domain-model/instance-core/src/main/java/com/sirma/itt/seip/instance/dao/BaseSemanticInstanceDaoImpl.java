package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.PropertyChange;
import com.sirma.itt.seip.Trackable;
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
 * @param <T> the instance/entity type
 * @param <P> the primary key type
 * @param <K> the secondary key type
 * @param <D> the target definition type
 * @author BBonev
 */
public abstract class BaseSemanticInstanceDaoImpl<T extends Instance, P extends Serializable, K extends Serializable, D extends DefinitionModel>
		extends BaseInstanceDaoImpl<T, P, K, D> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseSemanticInstanceDaoImpl.class);

	@Inject
	@SemanticDb
	protected javax.enterprise.inject.Instance<DbDao> semanticDbDao;

	private Set<String> forbiddenProperties;

	@Inject
	@ExtensionPoint(value = SemanticNonPersistentPropertiesExtension.TARGET_NAME)
	private Iterable<SemanticNonPersistentPropertiesExtension> semanticNonPersistentProperties;

	@Inject
	@SemanticDb
	private InstanceLoader instanceLoader;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private ChangedInstancesBuffer changedInstancesBuffer;

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
	 * @param instance the instance
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
		instance.getProperties().putAll(copy.getProperties());
		// disable external properties loading
		loadHeadersInternal(instance, null, true);
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
			return Stream.of(((InstanceReference) object).getId());
		} else if (object instanceof Collection) {
			return ((Collection<?>) object).stream().flatMap(this::getInstanceIds);
		}
		return Stream.empty();
	}

	/**
	 * Convert and save the given instance as entity.<br>
	 * <b>NOTE:</b>The provided implementation handles basic transformation, persisting and cache updating
	 *
	 * @param instance the instance
	 * @return the converted entity
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected <I extends Instance> I convertAndSave(I instance) {
		T entity = toEntity(instance);
		onBeforeSave(entity);
		Operation currentOperation = (Operation) Options.CURRENT_OPERATION.get();
		eventService.fire(new AuditableEvent(entity, currentOperation.getUserOperationId()));
		T oldCached = instanceLoader.getPersistCallback().persistAndUpdateCache(entity);
		onAfterSave(instance);

		// remove referenced entities from the cache so their relations to be updated as well
		removeFromCache((T) instance);

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
			PropertiesChangeEvent changeEvent = new PropertiesChangeEvent(entity, added, removed, currentOperation);
			Options.DISABLE_AUDIT_LOG.wrap(() -> eventService.fire(changeEvent)).execute();
		}
		// this should not fail as the instance type and the entity type are the same
		return (I) oldCached;
	}

	protected void removeFromCache(T entity) {
		EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache = getCache();
		List<Serializable> referencedIds = collectReferencedIds(entity).collect(Collectors.toList());
		// skip the current instance from cache removal as we updated it's cache value in the same transaction
		referencedIds.remove(entity.getId());

		changedInstancesBuffer.addAll(referencedIds);
		// the clear should happen in the before transaction, because it causes problems with instance save
		// the cache is cleared before computing the diff for the instance save in the semantic database that causes the
		// new values not the be written in the database, but only in the cache and the next save will save them
		// this could be changed only if semantic persist is changed as well not to depend on the cache
		// this is conclusion from the observation of the behaviour of the system after changing this to before TX completion
		// Issues: CMF-27907,  CMF-27784
		// As suggested here https://blog.infinispan.org/2009/07/increase-transactional-throughput-with.html
		// sorting is applied to the keys before cache removal in order to minimize the possible deadlocks
		transactionSupport.invokeBeforeTransactionCompletion(() -> {
			Collection<Serializable> buffer = changedInstancesBuffer.drainBuffer();
			if (buffer.isEmpty()) {
				return;
			}
			LOGGER.debug("Removing {} cache entries: {}.", buffer.size(), buffer);
			buffer.stream().map(Object::toString).sorted().forEach(cache::removeByKey);
		});
	}

	@SuppressWarnings("unchecked")
	private Stream<Serializable> collectReferencedIds(T entity) {
		Set<String> objectProperties = definitionService
				.getInstanceDefinition(entity)
				.fieldsStream()
				.filter(PropertyDefinition.isObjectProperty())
				.flatMap(prop -> Stream.of(prop.getName(), PropertyDefinition.resolveUri().apply(prop)))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		// we will process with the new way only if the instance is tracked when it entered here
		// if the instance is just created without tracking and saved then it will execute the old logic bellow
		if (entity instanceof Trackable && ((Trackable) entity).isTracked()) {
			return ((Trackable<Serializable>) entity).changes()
					.filter(change -> objectProperties.contains(change.getProperty()))
					.flatMap(BaseSemanticInstanceDaoImpl::changedValues);
		}
		return objectProperties.stream()
				.map(entity::get)
				.filter(Objects::nonNull)
				.flatMap(BaseSemanticInstanceDaoImpl::streamValue);
	}

	private static Stream<String> changedValues(PropertyChange<Serializable> change) {
		switch (change.getChangeType()) {
			case ADD:
				return streamValue(change.getNewValue());
			case REMOVE:
				return streamValue(change.getOldValue());
			case UPDATE:
				// find out the unchanged value and filter them out
				Set<String> newValue = streamValue(change.getNewValue()).collect(Collectors.toSet());
				Set<String> oldValue = streamValue(change.getOldValue()).collect(Collectors.toSet());
				Set<String> intersection = CollectionUtils.intersection(newValue, oldValue);
				Predicate<String> unchanged = id -> !intersection.contains(id);
				return Stream.concat(newValue.stream(), oldValue.stream()).filter(unchanged);
			default:
				throw new UnsupportedOperationException();
		}
	}

	private static Stream<String> streamValue(Serializable value) {
		if (value instanceof Collection) {
			return ((Collection<?>) value).stream().map(Object::toString);
		} else if (value == null) {
			return Stream.empty();
		}
		return Stream.of(value.toString());
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
		Instance entity = (Instance) getInstanceLoader().getPersistCallback().getEntityConverter().convertToEntity(
				source);
		entity.setType(source.type());
		return (T) entity;
	}

	/**
	 * Calculate modified properties.
	 *
	 * @param entity the entity
	 * @param oldCached the old cached
	 * @param added the added
	 * @param removed the removed
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
