package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Collection;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;

/**
 * Callback used to batch load an instance using cache and batch queries.
 *
 * @author BBonev
 */
public interface InstanceLoadCallback {

	/**
	 * Gets the id that is handled from the current callback implementation. This is called to extract an id from loaded
	 * entity/instance so that it can be mapped to the result.
	 *
	 * @param entity
	 *            the entity
	 * @return the id
	 */
	Serializable getId(Entity<? extends Serializable> entity);

	/**
	 * Fetch entity by key. If secondary key search is not supported then empty list could be returned. If the method
	 * {@code #getSecondaryKey(Entity)} returns <code>null</code> this method will not be called.
	 *
	 * @param key
	 *            the key to search
	 * @return the found entity/s entities or empty list or <code>null</code>.
	 */
	Object fetchByKey(Serializable key);

	/**
	 * Cache-only operation. Gets the entity from given cache using the provided id. This method will be called for
	 * every id passed for loading. The method should not call any external loading. Only the provided cache should be
	 * used!
	 *
	 * @param id
	 *            the id
	 * @param cache
	 *            the cache
	 * @return the entity from cache or <code>null</code> if not loaded, yet.
	 */
	Entity<? extends Serializable> getFromCacheById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache);

	/**
	 * Gets the entity from given cache or the persistent storage using the provided id. This method should call the
	 * proper cache method that will check the cache or the persistent storage in order to return the value. This method
	 * should be used for single entry loading.
	 *
	 * @param id
	 *            the id to search
	 * @param cache
	 *            the cache
	 * @return the entity from cache or <code>null</code> if not loaded, yet.
	 */
	Entity<? extends Serializable> lookupById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache);

	/**
	 * Load persisted entities using the given identifiers. Here the callback should load the instances from external
	 * source as efficient as possible. Note that the caller will handle query fragmentation.
	 *
	 * @param ids
	 *            the ids to load
	 * @return the collection
	 */
	Collection<Entity<? extends Serializable>> loadPersistedEntities(Collection<? extends Serializable> ids);

	/**
	 * Adds the loaded entity to the given cache.
	 *
	 * @param entity
	 *            the entity
	 * @param cache
	 *            the cache
	 */
	void addEntityToCache(Entity<? extends Serializable> entity,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache);
}