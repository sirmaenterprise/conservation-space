package com.sirma.itt.emf.instance.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.SemanticDb;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.properties.dao.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.emf.properties.event.PropertiesChangeEvent;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.EqualsHelper.MapValueComparison;

/**
 * Common implementation logic for {@link InstanceDao} realizations.
 * 
 * @param <T>
 *            the instance/entity type
 * @param <P>
 *            the primary key type
 * @param <K>
 *            the secondary key type
 * @param <D>
 *            the target definition type
 * @author BBonev
 */
public abstract class BaseSemanticInstanceDaoImpl<T extends Instance, P extends Serializable, K extends Serializable, D extends DefinitionModel>
		extends BaseInstanceDaoImpl<T, T, P, K, D> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseSemanticInstanceDaoImpl.class);

	@Inject
	@SemanticDb
	protected javax.enterprise.inject.Instance<DbDao> dbDao;

	@Inject
	protected EventService eventService;

	/** The Constant FORBIDDEN_PROPERTIES. */
	private Set<String> forbiddenProperties;

	@Inject
	@ExtensionPoint(value = SemanticNonPersistentPropertiesExtension.TARGET_NAME)
	private Iterable<SemanticNonPersistentPropertiesExtension> semanticNonPersistentProperties;

	/**
	 * Initialize the cache and logger.
	 */
	@Override
	@PostConstruct
	public void initialize() {
		super.initialize();
		trace = LOGGER.isTraceEnabled();

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
	protected void savePropertiesOnPersistChanges(T instance) {
		// disable external properties loading
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void loadProperties(T instance) {
		// disable external properties loading
		loadHeadersInternal(instance, null, true);
	}

	@Override
	protected <E extends Instance> void batchFetchProperties(List<E> toLoadProps, boolean loadAll) {
		// disabled properties loading
		loadHeadersInternal(null, toLoadProps, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void saveProperties(T instance, boolean addOnly) {
		if (addOnly) {
			// REVIEW: partial semantic properties persist
			// when add only operation is perform because the semantic does not support partial
			// properties update we need to ensure the old not modified properties are fetched and
			// added to the modified so that we does not loose some of them.
			// If partial semantic properties updated is implemented the could be removed
			T cached = getEntityCached((P) instance.getId(), null);
			T oldInstance = convert(cached, getInstanceClass());
			oldInstance.getProperties().putAll(instance.getProperties());
			Map<String, Serializable> properties = CollectionUtils.createLinkedHashMap(oldInstance
					.getProperties().size());
			properties.putAll(oldInstance.getProperties());
			properties.putAll(instance.getProperties());
			instance.setProperties(properties);
		}
		persistChanges(instance);
	}

	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> Entity<S> loadEntity(S entityId) {
		return (Entity<S>) getEntityCached((P) entityId, null);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<T> loadInstances(Instance owner, boolean loadProperties) {
		// eventually this could be changed to semantic query sometime
		String query = getOwningInstanceQuery();
		// if no query is provided then we cannot do anything so will return now
		if (!SequenceEntityGenerator.isPersisted(owner) || (query == null)) {
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
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable> void delete(Entity<S> entity) {
		if (getInstanceClass().isInstance(entity)) {
			// delete all children before deleting the current instance
			// REVIEW: define base operations in EMF
			Operation operation = new Operation("delete");
			Iterable<InstanceReference> iterable = getCurrentChildren((T) entity);
			for (InstanceReference instanceReference : iterable) {
				if ((instanceReference != null) && (instanceReference.toInstance() != null)) {
					instanceService.delete(instanceReference.toInstance(), operation, true);
				}
			}
		}
		super.delete(entity);
	}

	/**
	 * Gets the query that should be used for retrieving entity ID by owning instance reference. The
	 * query should have 2 arguments:
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
	 * <b>NOTE:</b>The provided implementation handles basic transformation, persisting and cache
	 * updating
	 * 
	 * @param instance
	 *            the instance
	 * @return the converted entity
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected T convertAndSave(T instance) {
		T entity = convert(instance, getEntityClass());
		T oldCached = null;
		if (SequenceEntityGenerator.isPersisted(instance)) {
			oldCached = getEntityCached((P) instance.getId(), null);
			// disabled so the method could handle newly added objects that are imported into the
			// system and probably non existent in DB
		}
		onBeforeSave(entity, oldCached);
		entity = getDbDao().saveOrUpdate(entity, oldCached);
		if (instance.getId() == null) {
			instance.setId(entity.getId());
		}
		onAfterSave(instance);

		if (entity instanceof OwnedModel) {
			((OwnedModel) entity).setOwningInstance(null);
		}
		// update the cache
		getCache().setValue(entity.getId(), entity);

		// generate properties change event
		Map<String, Serializable> added = CollectionUtils.createLinkedHashMap(entity
				.getProperties().size() << 1);
		int oldSize = 10;
		if (oldCached != null) {
			oldSize = oldCached.getProperties().size();
		}
		Map<String, Serializable> removed = CollectionUtils.createLinkedHashMap(oldSize << 1);

		// check for modified properties and notify for changes
		// REVIEW: this here could be optimization when saving properties to semantic database
		if (calculateModifiedProperties(entity, oldCached, added, removed)) {
			String operation = (String) RuntimeConfiguration
					.getConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			eventService.fire(new PropertiesChangeEvent(entity, added, removed, operation));
		}

		return oldCached;
	}

	/**
	 * {@inheritDoc}
	 * <p />
	 * For semantic implementation: added properties cloning and coping between instances. The
	 * method also removes the forbidden properties from the source and destination
	 */
	@Override
	protected <S, DD> DD convert(S source, Class<DD> dest) {
		/*if (!source.getClass().equals(dest)) {
			// if not the same then use the old convert
			return super.convert(source, dest);
		}*/
		// if same then clone the class but should ensure properties cloning
		DD copy = super.convert(source, dest);
		if (source instanceof PropertyModel) {
			PropertyModel src = (PropertyModel) source;
			// remove all forbidden for persist properties
			// NOTE: this could lead is some cases to a bug when on saving some of the properties
			// disappear
			if (src.getProperties().keySet().removeAll(forbiddenProperties)) {
				LOGGER.trace(
						"Removed some forbidden properties. The full list of forbidden properties is: {}",
						forbiddenProperties);
			}
			PropertyModel destModel = (PropertyModel) copy;
			Map<String, Serializable> cloneProperties = PropertiesUtil.cloneProperties(src
					.getProperties());
			destModel.setProperties(cloneProperties);
		}
		return copy;
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
	protected boolean calculateModifiedProperties(T entity, T oldCached,
			Map<String, Serializable> added, Map<String, Serializable> removed) {
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
					Serializable value = newProps.get(qname);
					added.put(qname, value);
					break;
				case RIGHT_ONLY:
					// We're adding this
					value = newProps.get(qname);
					added.put(qname, value);
					break;
				default:
					throw new IllegalStateException("Unknown MapValueComparison: "
							+ entry.getValue());
			}
		}

		return (removed.size() > 0) || (added.size() > 0);
	}

	/**
	 * Getter method for dbDao.
	 * 
	 * @return the dbDao
	 */
	@Override
	protected DbDao getDbDao() {
		return dbDao.get();
	}
}
