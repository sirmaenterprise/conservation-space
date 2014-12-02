/*
 *
 */
package com.sirma.itt.emf.link;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Triplet;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.util.CollectionUtils;

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

	/** The task executor. */
	@Inject
	protected TaskExecutor taskExecutor;

	/** The service register. */
	@Inject
	protected ServiceRegister serviceRegister;

	/** The dms adapter intance. */
	@Inject
	protected javax.enterprise.inject.Instance<DMSInstanceAdapterService> dmsAdapterIntance;

	/** The entity cache context. */
	@Inject
	protected EntityLookupCacheContext entityCacheContext;

	/**
	 * Lock for synchronizing the access to the deleted links set
	 */
	private static final Lock DELETED_LINKS_LOCK = new ReentrantLock();

	/**
	 * Set that holds all recently deleted links. This is needed due to the cache organization and
	 * not to clear all of it on link removal.
	 */
	protected static final Set<Serializable> DELETED_LINKS = CollectionUtils
			.createLinkedHashSet(MAX_DELETED_LINKS);

	/**
	 * Map used to synchronize the caches modifications on deletes and new links. The key is any key
	 * used in the caches and the value is the lock object used for synchronization of the given
	 * key. The user should create lock object, lock it and place it in the map and when finished
	 * should remove it and unlock. If the map contains a lock, the user should wait the lock to be
	 * unlocked before continue.
	 */
	protected static Map<Serializable, Lock> activeModifications = new ConcurrentHashMap<>(256,
			0.8f, 32);

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		if ((getMiddleLevelCacheName() != null)
				&& !entityCacheContext.containsCache(getMiddleLevelCacheName())) {
			entityCacheContext.createCache(getMiddleLevelCacheName(), new LinkEntityCacheLookup());
		}
		if ((getTopLevelCacheName() != null)
				&& !entityCacheContext.containsCache(getTopLevelCacheName())) {
			entityCacheContext.createCache(getTopLevelCacheName(), new LinkEntityFullCacheLookup());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Pair<Serializable, Serializable> link(Instance from, Instance to, String mainLinkId,
			String reverseLinkId, Map<String, Serializable> properties) {
		if ((from == null) || (to == null)) {
			LOGGER.warn("Cannot link null instances!");
			return Pair.NULL_PAIR;
		}
		Map<String, Serializable> map = properties;
		if (map == null) {
			map = Collections.emptyMap();
		}

		return linkAndRegisterToCache(from.toReference(), to.toReference(), mainLinkId,
				reverseLinkId, map);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Pair<Serializable, Serializable> link(InstanceReference from, InstanceReference to,
			String mainLinkId, String reverseLinkId, Map<String, Serializable> properties) {
		if ((from == null) || (to == null)) {
			LOGGER.warn("Cannot link null instances!");
			return Pair.NULL_PAIR;
		}
		return linkAndRegisterToCache(from, to, mainLinkId, reverseLinkId, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Secure
	public boolean associate(Instance from, Instance to, String assocName) {
		if (!(from instanceof DMSInstance) || !(to instanceof DMSInstance)) {
			LOGGER.debug("Instances {} and {} are not linked due to incompatible types",
					from == null ? null : from.getClass(), to == null ? null : to.getClass());
			return false;
		}
		try {
			return dmsAdapterIntance.get().linkAsChild((DMSInstance) from, (DMSInstance) to, null,
					assocName);
		} catch (DMSException e) {
			LOGGER.error("Failed to create assosiation between {} and {} with name {} due to {}",
					from.toReference(), to.toReference(), assocName, e.getMessage(), e);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean dissociate(Instance from, Instance to, String assocName) {
		if (!(from instanceof DMSInstance) || !(to instanceof DMSInstance)) {
			LOGGER.debug("Instances {} and {} are not unlinked due to incompatible types",
					from == null ? null : from.getClass(), to == null ? null : to.getClass());
			return false;
		}
		try {
			return dmsAdapterIntance.get().removeLinkAsChild((DMSInstance) from, (DMSInstance) to,
					assocName);
		} catch (DMSException e) {
			LOGGER.error("Failed to remove assosiation between {} and {} with name {} due to {}",
					from.toReference(), to.toReference(), assocName, e.getMessage(), e);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Secure
	public boolean reassociate(Instance from, Instance to, Instance oldFrom, String assocName) {
		if (oldFrom == null) {
			return associate(from, to, assocName);
		}
		if (!(from instanceof DMSInstance)
				|| (!(to instanceof DMSInstance) | !(oldFrom instanceof DMSInstance))) {
			LOGGER.debug("Instances {} and {} are not relinked due to incompatible types",
					from == null ? null : from.getClass(), to == null ? null : to.getClass());
			return false;
		}
		try {
			return dmsAdapterIntance.get().linkAsChild((DMSInstance) from, (DMSInstance) to,
					(DMSInstance) oldFrom, assocName);
		} catch (DMSException e) {
			LOGGER.error("Failed to reassosiate from {} to {} and {} with name {}",
					from.toReference(), oldFrom.toReference(), to.toReference(), assocName);
			LOGGER.trace("Link reassociate failed due to", e);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinksTo(InstanceReference to, String linkId) {
		return getLinksInternal(null, to, Arrays.asList(linkId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinks(InstanceReference from, String linkId,
			Class<? extends Instance> toFilter) {
		List<LinkReference> linksInternal = getLinksInternal(from, null, Arrays.asList(linkId));
		if (toFilter == null) {
			return linksInternal;
		}
		List<LinkReference> filtered = new LinkedList<>();
		for (LinkReference linkReference : linksInternal) {
			if (toFilter.isAssignableFrom(linkReference.getTo().getReferenceType().getJavaClass())) {
				filtered.add(linkReference);
			}
		}
		return filtered;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkInstance> convertToLinkInstance(List<LinkReference> source,
			boolean ignoreMissing) {
		List<LinkInstance> result = new ArrayList<>(source.size());

		Set<InstanceReference> references = CollectionUtils.createLinkedHashSet(source.size() + 10);
		for (LinkReference linkReference : source) {
			references.add(linkReference.getFrom());
			references.add(linkReference.getTo());
		}
		// load all references properly in parallel if needed
		Map<InstanceReference, Instance> map = BatchEntityLoader.loadAsMapFromReferences(
				references, serviceRegister, taskExecutor);

		// build the result instances
		for (LinkReference linkReference : source) {
			InstanceReference fromRef = linkReference.getFrom();
			InstanceReference toRef = linkReference.getTo();
			Instance from = map.get(fromRef);
			Instance to = map.get(toRef);
			if ((from == null) || (to == null)) {
				LOGGER.warn(
						"Failed to load instances for references: from({} , {}) = {} to({}, {}) = {}",
						new Object[] { fromRef.getIdentifier(),
								fromRef.getReferenceType().getName(),
								(from == null ? "NULL" : "NOT_NULL"), toRef.getIdentifier(),
								toRef.getReferenceType().getName(),
								(to == null ? "NULL" : "NOT_NULL") });
				if (!ignoreMissing) {
					continue;
				}
			}
			LinkInstance instance = new LinkInstance();
			instance.setFrom(from);
			instance.setTo(to);
			instance.setId(linkReference.getId());
			instance.setIdentifier(linkReference.getIdentifier());
			instance.setProperties(linkReference.getProperties());
			instance.setPrimary(linkReference.getPrimary());
			instance.setReverse(linkReference.getReverse());

			result.add(instance);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean linkSimple(InstanceReference from, List<InstanceReference> tos, String linkId) {
		for (InstanceReference instance : tos) {
			linkSimple(from, instance, linkId);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isLinked(InstanceReference from, InstanceReference to, String linkId) {
		if ((from == null) || (to == null) || (linkId == null)) {
			return false;
		}
		List<LinkReference> references = getLinksInternal(from, linkId, false);
		for (LinkReference linkReference : references) {
			if (linkReference.getTo().equals(to)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isLinkedSimple(InstanceReference from, InstanceReference to, String linkId) {
		if ((from == null) || (to == null) || (linkId == null)) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public LinkInstance getLinkInstance(Serializable id) {
		return convertToLinkInstance(getLinkReference(id));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public LinkReference getLinkReference(Serializable id) {
		if (id == null) {
			return null;
		}
		return getLinkReferenceById(id, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinks(InstanceReference from) {
		if (from == null) {
			return Collections.emptyList();
		}
		return getLinksInternal(from, null, true);
	}

	/**
	 * Filter deleted.
	 *
	 * @param toFilter
	 *            the references
	 * @param lists
	 *            the list of links to update. The non deleted elements will be added to every list
	 *            in the given array
	 * @return true, if successful
	 */
	private boolean filterDeleted(List<LinkReference> toFilter, List<Object>[] lists) {
		boolean foundDeleted = false;
		for (Iterator<LinkReference> it = toFilter.iterator(); it.hasNext();) {
			LinkReference reference = it.next();
			if (isDeleted(reference.getId())) {
				it.remove();
				foundDeleted = true;
			} else {
				for (int i = 0; i < lists.length; i++) {
					List<Object> list = lists[i];
					list.add(reference);
				}
			}
		}
		return foundDeleted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<LinkReference> getLinks(InstanceReference from, String linkId) {
		if (from == null) {
			return Collections.emptyList();
		}
		return getLinksInternal(from, linkId, true);
	}

	/**
	 * Gets the links internal.
	 *
	 * @param from
	 *            the from
	 * @param linkId
	 *            the link id
	 * @param cloneResult
	 *            if the result should be cloned or not
	 * @return the links internal
	 */
	private List<LinkReference> getLinksInternal(InstanceReference from, String linkId,
			boolean cloneResult) {
		if (linkId == null) {
			return getLinksInternal(from, null, Collections.<String> emptyList());
		}
		return getLinksInternal(from, null, Arrays.asList(linkId));
		//
		// EntityLookupCache<Triplet<String, String, String>, List<LinkReference>, Serializable>
		// cache = getLinkEntityCache();
		//
		// List<LinkReference> result = new LinkedList<>();
		// boolean foundDeleted = false;
		// if (linkId == null) {
		// EntityLookupCache<Pair<String, String>, Set<String>, Serializable> fullCache =
		// getLinkEntityFullCache();
		// Pair<String, String> key = new Pair<String, String>(from.getIdentifier(), from
		// .getReferenceType().getName());
		// Pair<Pair<String, String>, Set<String>> pair = fullCache.getByKey(key);
		// if (pair == null) {
		// return Collections.emptyList();
		// }
		// Set<String> ids = pair.getSecond();
		// Set<String> idCopy = CollectionUtils.createLinkedHashSet(ids.size());
		//
		// Triplet<String, String, String> tkey = new Triplet<String, String, String>(
		// (String) null, key.getFirst(), key.getSecond());
		//
		// List<LinkReference> tempCopy = null;
		// for (String type : ids) {
		// idCopy.add(type);
		// // complete the key
		// tkey.setFirst(type);
		//
		// Pair<Triplet<String, String, String>, List<LinkReference>> byKey = cache
		// .getByKey(tkey);
		// if (byKey != null) {
		// // copy the non deleted links to the temp list that will be used to update the
		// // cache also add the non deleted values to the rest list also - this way we
		// // will not required to copy the contents from the temp list to the result
		// tempCopy = new ArrayList<>(byKey.getSecond().size());
		// boolean deleted = filterDeleted(byKey.getSecond(), new List[] { result,
		// tempCopy });
		// if (deleted) {
		// if (tempCopy.isEmpty()) {
		// idCopy.remove(type);
		// // if all elements are deleted remove the entry from the cache
		// cache.removeByKey(tkey);
		// } else {
		// cache.setValue(tkey, tempCopy);
		// }
		// }
		// foundDeleted |= deleted;
		// }
		// }
		//
		// if (foundDeleted) {
		// if (result.isEmpty()) {
		// fullCache.removeByKey(key);
		// } else {
		// fullCache.setValue(key, idCopy);
		// }
		// }
		// } else {
		// Triplet<String, String, String> tkey = new Triplet<String, String, String>(
		// shrinkLinkIdentifier(linkId), from.getIdentifier(), from.getReferenceType()
		// .getName());
		// Pair<Triplet<String, String, String>, List<LinkReference>> pair = cache.getByKey(tkey);
		// if (pair == null) {
		// return Collections.emptyList();
		// }
		// List<LinkReference> list = pair.getSecond();
		// List<LinkReference> copy = new ArrayList<>(list.size());
		// foundDeleted = filterDeleted(list, new List[] { result, copy });
		// // if deleted link has been found then we update the cache
		// if (foundDeleted) {
		// if (copy.isEmpty()) {
		// cache.removeByKey(tkey);
		// } else {
		// cache.setValue(tkey, copy);
		// }
		// }
		// }
		//
		// if (cloneResult) {
		// // create copy different from the one in the cache
		// List<LinkReference> copy = new ArrayList<>(result.size());
		// for (LinkReference reference : result) {
		// copy.add(SerializationUtil.copy(reference));
		// }
		// return copy;
		// }
		// return result;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean removeLinksFor(InstanceReference reference) {

		List<LinkReference> links = getLinksInternal(reference, null, false);
		for (LinkReference ref : links) {
			removeLink(ref);
		}

		links = getLinksInternal(reference, null, false);
		for (LinkReference ref : links) {
			removeLink(ref);
		}
		return false;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeLink(LinkInstance instance) {
		if ((instance == null) || (instance.getId() == null)) {
			return;
		}
		removeLinkById(instance.getId());
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeLink(LinkReference instance) {
		if ((instance == null) || (instance.getId() == null)) {
			return;
		}
		addDeletedLinkId(instance.getId());
		removeLinkInternal(instance);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean updateLinkProperties(Serializable id, Map<String, Serializable> properties) {
		if ((id == null) || (properties == null)) {
			return false;
		}
		LinkReference linkReference = getLinkReferenceById(id, true);
		if ((linkReference == null) || (linkReference.getFrom() == null)) {
			LOGGER.warn("Relation with id {} was not found."
					+ " No properties will be updated. New properties: {}", id, properties);
			return false;
		}
		// EntityLookupCache<Triplet<String, String, String>, List<LinkReference>, Serializable>
		// cache = getLinkEntityCache();
		// Triplet<String, String, String> key = new Triplet<String, String, String>(
		// shrinkLinkIdentifier(linkReference.getIdentifier()), linkReference.getFrom()
		// .getIdentifier(), linkReference.getFrom().getReferenceType().getName());
		// Pair<Triplet<String, String, String>, List<LinkReference>> pair = cache.getByKey(key);
		// if ((pair == null) || (pair.getSecond() == null)) {
		// return false;
		// }
		// List<LinkReference> list = pair.getSecond();
		// Map<String, Serializable> oldProperties = Collections.emptyMap();
		// for (LinkReference reference : list) {
		// if (reference.getId().equals(id)) {
		// // backup old properties to be able to send them for diff update
		// oldProperties = PropertiesUtil.cloneProperties(reference.getProperties());
		// reference.getProperties().putAll(properties);
		// break;
		// }
		// }
		// if (updatePropertiesInternal(id, properties, linkReference.getProperties())) {
			// update the cache entry with the updated list
			// this is idf the cache is not local and the value has been deserialized
		// cache.setValue(key, list);
		// }
		return updatePropertiesInternal(id, properties, linkReference.getProperties());
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
		arguments.setResult(convertToLinkInstance(result, true));
		arguments.setTotalItems(result.size());
		return arguments;
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
	protected abstract boolean updatePropertiesInternal(Serializable id,
			Map<String, Serializable> properties, Map<String, Serializable> oldProperties);

	/**
	 * Adds the deleted link id.
	 *
	 * @param linkDbId
	 *            the link db id
	 */
	protected void addDeletedLinkId(Serializable linkDbId) {
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
	protected abstract List<LinkReference> getLinksInternal(Object from, Object to,
			Collection<String> linkids);

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
	protected abstract Pair<Serializable, Serializable> linkInternal(Object from, Object to,
			String mainLinkId, String reverseLinkId, Map<String, Serializable> properties);

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
	protected Pair<Serializable, Serializable> linkAndRegisterToCache(InstanceReference from,
			InstanceReference to, String mainLinkId, String reverseLinkId,
			Map<String, Serializable> properties) {
		Pair<Serializable, Serializable> pair = linkInternal(from, to, mainLinkId, reverseLinkId,
				properties);
		if (pair == null) {
			LOGGER.warn("Link {} between {} and {} was NOT created!", new Object[] { mainLinkId,
					from, to });
			return null;
		}
		if (pair.getFirst() != null) {
			LOGGER.trace(
					"Forward link {} was created between {} with id={} to {}. Updating cache.",
					mainLinkId, from.getIdentifier(), pair.getFirst(), to.getIdentifier());
		}
		if (pair.getSecond() != null) {
			LOGGER.trace(
					"Reverse link {} was created between {} with id={} to {}. Updating cache.",
					reverseLinkId, to.getIdentifier(), pair.getSecond(), from.getIdentifier());
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
	protected void updateCache(Serializable dbId, String linkId, InstanceReference from,
			InstanceReference to, Map<String, Serializable> properties) {
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
		Pair<String, String> key = new Pair<String, String>(reference.getFrom().getIdentifier(),
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
		Triplet<String, String, String> tkey = new Triplet<String, String, String>(linkId,
				key.getFirst(), key.getSecond());
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
	 * Gets the link entity full cache. High level cache that holds as key pair of instance id/type
	 * and as key the ids of all link references where the key is the beginning of a link. When the
	 * cache searches the underling layer the returned data is used to populate the other 2 caches.
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
	 * Middle level cache to that store db link ids with a key: linkType, instance id/type where the
	 * instance is the beginning of a link. When cache searches the underling layer the returned
	 * data is used to populate the other lower level cache.
	 *
	 * @author BBonev
	 */
	protected class LinkEntityCacheLookup
			extends
			EntityLookupCallbackDAOAdaptor<Triplet<String, String, String>, List<LinkReference>, Serializable> {

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
			return new Pair<Triplet<String, String, String>, List<LinkReference>>(key, result);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Triplet<String, String, String>, List<LinkReference>> createValue(
				List<LinkReference> value) {
			throw new UnsupportedOperationException("LinkReference is externally persisted");
		}
	}

	/**
	 * High level cache that holds as key pair of instance id/type and as key the ids of all link
	 * references where the key is the beginning of a link. When the cache searches the underling
	 * layer the returned data is used to populate the other 2 caches.
	 *
	 * @author BBonev
	 */
	protected class LinkEntityFullCacheLookup extends
			EntityLookupCallbackDAOAdaptor<Pair<String, String>, Set<String>, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, String>, Set<String>> findByKey(Pair<String, String> key) {
			InstanceReference from = typeConverter
					.convert(InstanceReference.class, key.getSecond());
			from.setIdentifier(key.getFirst());

			List<LinkReference> list = getLinksInternal(from, null,
					Collections.<String> emptyList());

			Set<String> result = CollectionUtils.createLinkedHashSet(32);
			Map<Triplet<String, String, String>, List<LinkReference>> mapping = CollectionUtils
					.createLinkedHashMap(32);
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
						new Triplet<>(identifier, reference.getIdentifier(), reference
								.getReferenceType().getName()), linkReference);
			}

			// update the lower level caches
			EntityLookupCache<Triplet<String, String, String>, List<LinkReference>, Serializable> linkEntityCache = getLinkEntityCache();
			for (Entry<Triplet<String, String, String>, List<LinkReference>> entry : mapping
					.entrySet()) {
				linkEntityCache.setValue(entry.getKey(), entry.getValue());
			}

			return new Pair<Pair<String, String>, Set<String>>(key, result);
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
