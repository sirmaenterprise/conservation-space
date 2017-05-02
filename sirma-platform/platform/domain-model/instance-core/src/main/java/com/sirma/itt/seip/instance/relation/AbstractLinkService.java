/*
 *
 */
package com.sirma.itt.seip.instance.relation;

import static com.sirma.itt.seip.collections.CollectionUtils.addValueToSetMap;
import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.CREATED_ON;
import static com.sirma.itt.seip.util.EqualsHelper.getMapComparison;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Triplet;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.EqualsHelper.MapValueComparison;

/**
 * Basic {@link LinkService} implementation for some of the methods.
 *
 * @author BBonev
 */
public abstract class AbstractLinkService implements LinkService, Serializable {
	private static final int MAX_DELETED_LINKS = 2048;

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1422766464513563845L;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLinkService.class);

	/** The type converter. */
	@Inject
	protected TypeConverter typeConverter;

	/** The dms adapter intance. */
	@Inject
	protected javax.enterprise.inject.Instance<RelationAdapterService> dmsAdapterIntance;

	/** The entity cache context. */
	@Inject
	protected EntityLookupCacheContext entityCacheContext;

	@Inject
	protected InstanceLoadDecorator instanceLoadDecorator;

	@Inject
	protected DictionaryService dictionaryService;

	@Inject
	protected SecurityContext securityContext;

	/**
	 * Lock for synchronizing the access to the deleted links set
	 */
	private static final Lock DELETED_LINKS_LOCK = new ReentrantLock();

	/**
	 * Set that holds all recently deleted links. This is needed due to the cache organization and not to clear all of
	 * it on link removal.
	 */
	protected static final Set<Serializable> DELETED_LINKS = CollectionUtils.createLinkedHashSet(MAX_DELETED_LINKS);

	/**
	 * Map used to synchronize the caches modifications on deletes and new links. The key is any key used in the caches
	 * and the value is the lock object used for synchronization of the given key. The user should create lock object,
	 * lock it and place it in the map and when finished should remove it and unlock. If the map contains a lock, the
	 * user should wait the lock to be unlocked before continue.
	 */
	protected static Map<Serializable, Lock> activeModifications = new ConcurrentHashMap<>(256, 0.8f, 32);

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		if (getMiddleLevelCacheName() != null && !entityCacheContext.containsCache(getMiddleLevelCacheName())) {
			entityCacheContext.createCache(getMiddleLevelCacheName(), new LinkEntityCacheLookup());
		}
		if (getTopLevelCacheName() != null && !entityCacheContext.containsCache(getTopLevelCacheName())) {
			entityCacheContext.createCache(getTopLevelCacheName(), new LinkEntityFullCacheLookup());
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId, String reverseLinkId,
			Map<String, Serializable> properties) {
		if (from == null || to == null) {
			LOGGER.warn("Cannot link null instances!");
			return Pair.NULL_PAIR;
		}
		Map<String, Serializable> map = properties;
		if (map == null) {
			map = Collections.emptyMap();
		}

		return linkAndRegisterToCache(from.toReference(), to.toReference(), mainLinkId, reverseLinkId, map);
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public Pair<Serializable, Serializable> link(InstanceReference from, InstanceReference to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties) {
		if (from == null || to == null) {
			LOGGER.warn("Cannot link null instances!");
			return Pair.NULL_PAIR;
		}
		return linkAndRegisterToCache(from, to, mainLinkId, reverseLinkId, properties);
	}

	@Override
	public boolean associate(Instance from, Instance to, String assocName) {
		if (!(from instanceof DMSInstance) || !(to instanceof DMSInstance)) {
			LOGGER.debug("Instances {} and {} are not linked due to incompatible types",
					from == null ? null : from.getClass(), to == null ? null : to.getClass());
			return false;
		}
		try {
			return dmsAdapterIntance.get().linkAsChild((DMSInstance) from, (DMSInstance) to, null, assocName);
		} catch (Exception e) {
			LOGGER.error("Failed to create assosiation between {} and {} with name {} due to {}", from.toReference(),
					to.toReference(), assocName, e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean dissociate(Instance from, Instance to, String assocName) {
		if (!(from instanceof DMSInstance) || !(to instanceof DMSInstance)) {
			LOGGER.debug("Instances {} and {} are not unlinked due to incompatible types",
					from == null ? null : from.getClass(), to == null ? null : to.getClass());
			return false;
		}
		try {
			return dmsAdapterIntance.get().removeLinkAsChild((DMSInstance) from, (DMSInstance) to, assocName);
		} catch (Exception e) {
			LOGGER.error("Failed to remove assosiation between {} and {} with name {} due to {}", from.toReference(),
					to.toReference(), assocName, e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean reassociate(Instance from, Instance to, Instance oldFrom, String assocName) {
		if (oldFrom == null) {
			return associate(from, to, assocName);
		}
		if (!(from instanceof DMSInstance) || !(to instanceof DMSInstance) || !(oldFrom instanceof DMSInstance)) {
			LOGGER.debug("Instances {} and {} are not relinked due to incompatible types",
					from == null ? null : from.getClass(), to == null ? null : to.getClass());
			return false;
		}
		try {
			return dmsAdapterIntance.get().linkAsChild((DMSInstance) from, (DMSInstance) to, (DMSInstance) oldFrom,
					assocName);
		} catch (Exception e) {
			LOGGER.error("Failed to reassosiate from {} to {} and {} with name {}", from.toReference(),
					oldFrom.toReference(), to.toReference(), assocName);
			LOGGER.trace("Link reassociate failed due to", e);
		}
		return false;
	}

	@Override
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		return getLinksInternal(null, to, Arrays.asList(linkId));
	}

	@Override
	public LinkInstance convertToLinkInstance(LinkReference source) {
		if (source == null) {
			return null;
		}
		LinkInstance instance = new LinkInstance();
		instance.setId(source.getId());
		instance.setIdentifier(source.getIdentifier());
		instance.setPrimary(source.getPrimary());
		instance.setReverse(source.getReverse());
		instance.setProperties(source.getProperties());

		instance.setFrom(source.getFrom().toInstance());
		instance.setTo(source.getTo().toInstance());
		return instance;
	}

	@Override
	public List<LinkInstance> convertToLinkInstance(List<LinkReference> source) {
		List<LinkInstance> instanceLinks = new ArrayList<>(source.size());
		// hold references to the instances that will be filled with data
		Collection<Instance> instances = new ArrayList<>(instanceLinks.size() * 2);

		for (LinkReference linkReference : source) {
			LinkInstance linkInstance = linkReference.toLinkInstance();
			instanceLinks.add(linkInstance);

			instances.add(linkInstance.getFrom());
			instances.add(linkInstance.getTo());
		}

		instanceLoadDecorator.decorateResult(instances);
		return instanceLinks;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean linkSimple(InstanceReference from, List<InstanceReference> tos, String linkId) {
		for (InstanceReference instance : tos) {
			linkSimple(from, instance, linkId);
		}
		return true;
	}

	@Override
	public boolean isLinked(InstanceReference from, InstanceReference to, String linkId) {
		if (from == null || to == null || linkId == null) {
			return false;
		}
		List<LinkReference> references = getLinksInternal(from, linkId);
		for (LinkReference linkReference : references) {
			if (linkReference.getTo().equals(to)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId) {
		if (from == null || to == null || linkId == null) {
			return false;
		}
		List<LinkReference> references = getSimpleLinks(from, linkId);
		for (LinkReference linkReference : references) {
			if (linkReference.getTo().equals(to)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public LinkInstance getLinkInstance(Serializable id) {
		return convertToLinkInstance(getLinkReference(id));
	}

	@Override
	public LinkReference getLinkReference(Serializable id) {
		if (id == null) {
			return null;
		}
		return getLinkReferenceById(id, true);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from) {
		if (from == null) {
			return Collections.emptyList();
		}
		return getLinksInternal(from, null);
	}

	@Override
	public List<LinkReference> getLinks(InstanceReference from, String linkId) {
		if (from == null) {
			return Collections.emptyList();
		}
		return getLinksInternal(from, linkId);
	}

	private List<LinkReference> getLinksInternal(InstanceReference from, String linkId) {
		if (linkId == null) {
			return getLinksInternal(from, null, Collections.<String> emptyList());
		}
		return getLinksInternal(from, null, Arrays.asList(linkId));
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean removeLinksFor(InstanceReference reference) {

		List<LinkReference> links = getLinksInternal(reference, null);
		for (LinkReference ref : links) {
			removeLink(ref);
		}

		links = getLinksInternal(reference, null);
		for (LinkReference ref : links) {
			removeLink(ref);
		}
		return false;
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public void removeLink(LinkInstance instance) {
		if (instance == null || instance.getId() == null) {
			return;
		}
		removeLinkById(instance.getId());
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public void removeLink(LinkReference instance) {
		if (instance == null || instance.getId() == null) {
			return;
		}
		addDeletedLinkId(instance.getId());
		removeLinkInternal(instance);
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public void removeLinkById(Serializable linkDbId) {
		if (linkDbId == null) {
			return;
		}
		LinkReference reference = getLinkReferenceById(linkDbId, false);
		if (reference == null) {
			return;
		}
		addDeletedLinkId(linkDbId);
		removeLinkInternal(reference);
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public boolean updateLinkProperties(Serializable id, Map<String, Serializable> properties) {
		if (id == null || properties == null) {
			return false;
		}
		LinkReference linkReference = getLinkReferenceById(id, true);
		if (linkReference == null || linkReference.getFrom() == null) {
			LOGGER.warn("Relation with id {} was not found." + " No properties will be updated. New properties: {}", id,
					properties);
			return false;
		}
		return updatePropertiesInternal(id, properties, linkReference.getProperties());
	}

	@Override
	public LinkSearchArguments searchLinks(LinkSearchArguments arguments) {
		if (arguments == null) {
			return null;
		}
		// same basic implementation for the method using the existing methods
		List<LinkReference> result = Collections.emptyList();
		if (arguments.getFrom() != null) {
			if (arguments.getLinkId() != null) {
				result = getLinks(arguments.getFrom(), arguments.getLinkId());
			} else {
				result = getLinks(arguments.getFrom());
			}
		} else if (arguments.getTo() != null) {
			if (arguments.getLinkId() != null) {
				result = getLinksTo(arguments.getTo(), arguments.getLinkId());
			} else {
				result = getLinksTo(arguments.getTo());
			}
		}
		arguments.setResult(convertToLinkInstance(result));
		arguments.setTotalItems(result.size());
		return arguments;
	}

	@Override
	public List<LinkReference> getInstanceRelations(Instance instance) {
		return getInstanceRelations(instance, filter -> true);
	}

	@Override
	public List<LinkReference> getInstanceRelations(Instance instance, Predicate<String> relationFilter) {
		if (instance == null) {
			return Collections.emptyList();
		}

		DefinitionModel definition = dictionaryService.getInstanceDefinition(instance);
		if (definition == null) {
			LOGGER.debug("Could not load definition model for instance with id [{}] and definition id [{}]",
					instance.getId(), instance.getIdentifier());
			return Collections.emptyList();
		}
		// will load only non system fields of type URI that have a valid uri and are not filtered
		Set<String> linkIds = definition
					.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty())
					.filter(p -> relationFilter.test(p.getIdentifier()))
					.map(PropertyDefinition.resolveUri())
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

		if (isEmpty(linkIds)) {
			return Collections.emptyList();
		}

		return getSimpleLinks(instance.toReference(), linkIds);
	}

	@Override
	public Pair<List<LinkReference>, List<LinkReference>> getRelationsDiff(InstanceReference source,
			Map<String, ? extends Collection<InstanceReference>> changes) {

		Map<String, Set<InstanceReference>> current = new HashMap<>();
		Map<String, Set<InstanceReference>> currentCopy = new HashMap<>();
		List<LinkReference> currentRelations = getInstanceRelations(source.toInstance());
		for (LinkReference linkReference : currentRelations) {
			addValueToSetMap(current, linkReference.getIdentifier(), linkReference.getTo());
			addValueToSetMap(currentCopy, linkReference.getIdentifier(), linkReference.getTo());
		}

		Map<String, Set<InstanceReference>> incommingChanges = new HashMap<>();
		for (Entry<String, ? extends Collection<InstanceReference>> entry : changes.entrySet()) {
			incommingChanges.put(entry.getKey(), new HashSet<>(0));
			for (InstanceReference reference : entry.getValue()) {
				addValueToSetMap(incommingChanges, entry.getKey(), reference);
			}
		}

		currentCopy.putAll(incommingChanges);

		return createRelationsDiff(source, current, currentCopy);
	}

	@Override
	public void saveRelations(List<LinkReference> toAdd, List<LinkReference> toRemove,
			Consumer<LinkReference> onSuccessAdd, Consumer<LinkReference> onSuccessRemove) {

		Map<String, Serializable> relationProperties = createHashMap(2);
		relationProperties.put(CREATED_BY, securityContext.getAuthenticated());
		relationProperties.put(CREATED_ON, new Date());

		for (LinkReference link : toAdd) {
			Pair<Serializable, Serializable> pair = link(link.getFrom(), link.getTo(), link.getIdentifier(),
					getReverseLinkType(link.getIdentifier()), relationProperties);

			if (pair.getFirst() != null && onSuccessAdd != null) {
				onSuccessAdd.accept(link);
			}
		}

		for (LinkReference link : toRemove) {
			boolean unlinked = unlink(link.getFrom(), link.getTo(), link.getIdentifier(),
					getReverseLinkType(link.getIdentifier()));
			if (unlinked && onSuccessRemove != null) {
				onSuccessRemove.accept(link);
			}
		}
	}

	/**
	 * Gets the reverse link type. If no reverse link is defined in semantic model no reverse link will be created
	 *
	 * @param relationType
	 *            the relation type
	 * @return the reverse link type
	 */
	protected abstract String getReverseLinkType(String relationType);

	private static Pair<List<LinkReference>, List<LinkReference>> createRelationsDiff(InstanceReference source,
			Map<String, Set<InstanceReference>> current, Map<String, Set<InstanceReference>> changes) {

		List<Pair<String, InstanceReference>> toAdd = new LinkedList<>();
		List<Pair<String, InstanceReference>> toRemove = new LinkedList<>();

		for (Entry<String, MapValueComparison> entry : getMapComparison(current, changes).entrySet()) {
			String relationId = entry.getKey();
			switch (entry.getValue()) {
				case LEFT_ONLY:
					for (InstanceReference ref : current.get(relationId)) {
						toRemove.add(new Pair<>(relationId, ref));
					}
					break;
				case RIGHT_ONLY:
					for (InstanceReference ref : changes.get(relationId)) {
						toAdd.add(new Pair<>(relationId, ref));
					}
					break;
				case NOT_EQUAL:
					buildRelationChanges(relationId, current, changes, toAdd, toRemove);
					break;
				default:
					break;
			}
		}

		List<LinkReference> linksToAdd = toLinkReference(source, toAdd);
		List<LinkReference> linksToRemove = toLinkReference(source, toRemove);

		return new Pair<>(linksToAdd, linksToRemove);
	}

	private static List<LinkReference> toLinkReference(InstanceReference source,
			List<Pair<String, InstanceReference>> toTransform) {

		List<LinkReference> result = new ArrayList<>(toTransform.size());

		for (Pair<String, InstanceReference> pair : toTransform) {
			LinkReference reference = new LinkReference();
			reference.setFrom(source);
			reference.setIdentifier(pair.getFirst());
			reference.setTo(pair.getSecond());
			result.add(reference);
		}
		return result;
	}

	private static void buildRelationChanges(String relationId, Map<String, Set<InstanceReference>> current,
			Map<String, Set<InstanceReference>> changes, List<Pair<String, InstanceReference>> toAdd,
			List<Pair<String, InstanceReference>> toRemove) {
		Set<InstanceReference> currentSet = current.get(relationId);
		Set<InstanceReference> changeSet = changes.get(relationId);

		// contain the elements that are common for the both sets
		Set<InstanceReference> common = new HashSet<>(currentSet);
		common.addAll(changeSet);
		common.retainAll(currentSet);
		common.retainAll(changeSet);

		Set<InstanceReference> diffSet = new HashSet<>(currentSet);
		diffSet.removeAll(common);
		for (InstanceReference reference : diffSet) {
			toRemove.add(new Pair<>(relationId, reference));
		}

		diffSet = new HashSet<>(changeSet);
		diffSet.removeAll(common);
		for (InstanceReference reference : diffSet) {
			toAdd.add(new Pair<>(relationId, reference));
		}
	}

	/**
	 * Update properties internal in the undelying implemenatation.
	 *
	 * @param id
	 *            the id
	 * @param properties
	 *            the properties
	 * @param oldProperties
	 *            before delete if present
	 * @return true, if successful
	 */
	protected abstract boolean updatePropertiesInternal(Serializable id, Map<String, Serializable> properties,
			Map<String, Serializable> oldProperties);

	/**
	 * Adds the deleted link id.
	 *
	 * @param linkDbId
	 *            the link db id
	 */
	protected void addDeletedLinkId(Serializable linkDbId) {
		if (linkDbId == null) {
			return;
		}
		DELETED_LINKS_LOCK.lock();
		try {
			if (DELETED_LINKS.size() == MAX_DELETED_LINKS) {
				// delete the first element
				DELETED_LINKS.remove(DELETED_LINKS.iterator().next());
			}
			DELETED_LINKS.add(linkDbId);
		} finally {
			DELETED_LINKS_LOCK.unlock();
		}
	}

	/**
	 * Removes the link internal.
	 *
	 * @param second
	 *            the second
	 */
	protected abstract void removeLinkInternal(LinkReference second);

	/**
	 * Gets the link reference by id.
	 *
	 * @param id
	 *            the id
	 * @param loadProperties
	 *            the load properties
	 * @return the link reference by id
	 */
	protected abstract LinkReference getLinkReferenceById(Serializable id, boolean loadProperties);

	/**
	 * Normalize link identifier.
	 *
	 * @param identifier
	 *            the identifier
	 * @return the string
	 */
	protected abstract String shrinkLinkIdentifier(String identifier);

	/**
	 * Expand link identifier.
	 *
	 * @param identifier
	 *            the identifier
	 * @return the string
	 */
	protected abstract String expandLinkIdentifier(String identifier);

	/**
	 * Gets the links internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param linkids
	 *            the linkids
	 * @return the links internal
	 */
	protected abstract List<LinkReference> getLinksInternal(Object from, Object to, Collection<String> linkids);

	/**
	 * Link internal.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param mainLinkId
	 *            the main link id
	 * @param reverseLinkId
	 *            the reverse link id
	 * @param properties
	 *            the properties
	 * @return the pair
	 */
	protected abstract Pair<Serializable, Serializable> linkInternal(Object from, Object to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties);

	/**
	 * Gets the middle level cache name.
	 *
	 * @return the middle level cache name
	 */
	protected abstract String getMiddleLevelCacheName();

	/**
	 * Gets the top level cache name.
	 *
	 * @return the top level cache name
	 */
	protected abstract String getTopLevelCacheName();

	/**
	 * Link and register to cache.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param mainLinkId
	 *            the main link id
	 * @param reverseLinkId
	 *            the reverse link id
	 * @param properties
	 *            the properties
	 * @return the pair
	 */
	protected Pair<Serializable, Serializable> linkAndRegisterToCache(InstanceReference from, InstanceReference to,
			String mainLinkId, String reverseLinkId, Map<String, Serializable> properties) {
		Pair<Serializable, Serializable> pair = linkInternal(from, to, mainLinkId, reverseLinkId, properties);
		if (pair == null) {
			LOGGER.warn("Link {} between {} and {} was NOT created!", new Object[] { mainLinkId, from, to });
			return null;
		}
		if (pair.getFirst() != null) {
			LOGGER.trace("Forward link {} was created between {} with id={} to {}. Updating cache.", mainLinkId,
					from.getIdentifier(), pair.getFirst(), to.getIdentifier());
		}
		if (pair.getSecond() != null) {
			LOGGER.trace("Reverse link {} was created between {} with id={} to {}. Updating cache.", reverseLinkId,
					to.getIdentifier(), pair.getSecond(), from.getIdentifier());
		}

		return pair;
	}

	/**
	 * Update cache with the given information about the links
	 *
	 * @param dbId
	 *            the db id
	 * @param linkId
	 *            the link id
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param properties
	 *            the properties
	 */
	protected void updateCache(Serializable dbId, String linkId, InstanceReference from, InstanceReference to,
			Map<String, Serializable> properties) {
		LinkReference reference = new LinkReference();
		reference.setId(dbId);
		reference.setIdentifier(expandLinkIdentifier(linkId));
		reference.setFrom(from);
		reference.setTo(to);
		if (properties != null) {
			reference.setProperties(PropertiesUtil.cloneProperties(properties));
		} else {
			reference.setProperties(new HashMap<String, Serializable>(1));
		}

		// update the top level cache
		// adds the link type to the set of types for the given source instance
		Pair<String, String> key = new Pair<>(reference.getFrom().getIdentifier(),
				reference.getFrom().getReferenceType().getName());
		// only fetches from the cache not from the database
		// and what about the links that are not fetched??
		Set<String> set = getLinkEntityFullCache().getValue(key);
		if (set == null) {
			set = CollectionUtils.createLinkedHashSet(32);
		}
		set.add(linkId);
		getLinkEntityFullCache().setValue(key, set);

		// updates the middle level cache where the values are the actual link references
		EntityLookupCache<Triplet<String, String, String>, List<LinkReference>, Serializable> entityCache = getLinkEntityCache();
		Triplet<String, String, String> tkey = new Triplet<>(linkId, key.getFirst(), key.getSecond());
		// only fetches from the cache not from the database
		// and what about the links that are not fetched??
		List<LinkReference> list = entityCache.getValue(tkey);
		if (list == null) {
			list = new LinkedList<>();
		}
		list.add(reference);
		entityCache.setValue(tkey, list);
	}

	/**
	 * Gets the link entity cache.
	 *
	 * @return the link entity cache
	 */
	protected EntityLookupCache<Triplet<String, String, String>, List<LinkReference>, Serializable> getLinkEntityCache() {
		return entityCacheContext.getCache(getMiddleLevelCacheName());
	}

	/**
	 * Gets the link entity full cache. High level cache that holds as key pair of instance id/type and as key the ids
	 * of all link references where the key is the beginning of a link. When the cache searches the underling layer the
	 * returned data is used to populate the other 2 caches.
	 *
	 * @return the link entity full cache
	 */
	protected EntityLookupCache<Pair<String, String>, Set<String>, Serializable> getLinkEntityFullCache() {
		return entityCacheContext.getCache(getTopLevelCacheName());
	}

	/**
	 * Checks if the given link id is deleted.
	 *
	 * @param id
	 *            the id
	 * @return true, if is deleted
	 */
	protected boolean isDeleted(Serializable id) {
		DELETED_LINKS_LOCK.lock();
		try {
			return DELETED_LINKS.contains(id);
		} finally {
			DELETED_LINKS_LOCK.unlock();
		}
	}

	/**
	 * Middle level cache to that store db link ids with a key: linkType, instance id/type where the instance is the
	 * beginning of a link. When cache searches the underling layer the returned data is used to populate the other
	 * lower level cache.
	 *
	 * @author BBonev
	 */
	protected class LinkEntityCacheLookup
			extends EntityLookupCallbackDAOAdaptor<Triplet<String, String, String>, List<LinkReference>, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Triplet<String, String, String>, List<LinkReference>> findByKey(
				Triplet<String, String, String> key) {

			InstanceReference from = typeConverter.convert(InstanceReference.class, key.getThird());
			from.setIdentifier(key.getSecond());
			List<LinkReference> list = getLinksInternal(from, null, Arrays.asList(key.getFirst()));
			List<LinkReference> result = new ArrayList<>(list.size());
			for (LinkReference linkReference : list) {
				if (isDeleted(linkReference.getId())) {
					continue;
				}
				result.add(linkReference);
			}
			return new Pair<>(key, result);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Triplet<String, String, String>, List<LinkReference>> createValue(List<LinkReference> value) {
			throw new UnsupportedOperationException("LinkReference is externally persisted");
		}
	}

	/**
	 * High level cache that holds as key pair of instance id/type and as key the ids of all link references where the
	 * key is the beginning of a link. When the cache searches the underling layer the returned data is used to populate
	 * the other 2 caches.
	 *
	 * @author BBonev
	 */
	protected class LinkEntityFullCacheLookup
			extends EntityLookupCallbackDAOAdaptor<Pair<String, String>, Set<String>, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, String>, Set<String>> findByKey(Pair<String, String> key) {
			InstanceReference from = typeConverter.convert(InstanceReference.class, key.getSecond());
			from.setIdentifier(key.getFirst());

			List<LinkReference> list = getLinksInternal(from, null, Collections.<String> emptyList());

			Set<String> result = CollectionUtils.createLinkedHashSet(32);
			Map<Triplet<String, String, String>, List<LinkReference>> mapping = CollectionUtils.createLinkedHashMap(32);
			for (LinkReference linkReference : list) {
				// ignored deleted links
				if (isDeleted(linkReference.getId())) {
					continue;
				}
				String identifier = shrinkLinkIdentifier(linkReference.getIdentifier());
				result.add(identifier);
				// collect the link references id to fill the lower level cache
				InstanceReference reference = linkReference.getFrom();
				CollectionUtils.addValueToMap(mapping,
						new Triplet<>(identifier, reference.getIdentifier(), reference.getReferenceType().getName()),
						linkReference);
			}

			// update the lower level caches
			EntityLookupCache<Triplet<String, String, String>, List<LinkReference>, Serializable> linkEntityCache = getLinkEntityCache();
			for (Entry<Triplet<String, String, String>, List<LinkReference>> entry : mapping.entrySet()) {
				linkEntityCache.setValue(entry.getKey(), entry.getValue());
			}

			return new Pair<>(key, result);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, String>, Set<String>> createValue(Set<String> value) {
			throw new UnsupportedOperationException("LinkReference is externally persisted");
		}
	}

}
