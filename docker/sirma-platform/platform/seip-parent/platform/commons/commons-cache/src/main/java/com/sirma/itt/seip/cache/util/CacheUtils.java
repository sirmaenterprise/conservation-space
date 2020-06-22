package com.sirma.itt.seip.cache.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Helper class that implements optimized batch loading algorithms for entities and instances. Each method requires a
 * concrete callback implementation to work properly.
 *
 * @author BBonev
 */
public class CacheUtils {

	private CacheUtils() {
		// utility class
	}

	/**
	 * Batch load of instances by secondary keys.
	 *
	 * @param <P>
	 *            the generic type
	 * @param <E>
	 *            the concrete instance type
	 * @param ids
	 *            the needed to load
	 * @param cache
	 *            the cache
	 * @param callback
	 *            the callback
	 * @return the list fetched list
	 */
	public static <P extends Serializable, E extends Entity<?>> List<E> batchLoadByPrimaryKey(List<P> ids,
			EntityLookupCache<P, E, ?> cache, BatchPrimaryKeyEntityLoaderCallback<P, E> callback) {
		Set<P> secondPass = new LinkedHashSet<>();
		Map<P, E> result = new LinkedHashMap<>((int) (ids.size() * 1.5));
		// first we check for hits in the cache and filter the cache misses
		if (cache == null) {
			secondPass.addAll(ids);
		} else {
			ids.forEach(id -> {
				// convert the cache entry to instance and schedule
				// properties loading instead of one by one loading
				E entity = cache.getValue(id);
				if (entity != null) {
					result.put(id, entity);
				} else {
					// no cache or not found in cache search later in DB
					secondPass.add(id);
				}
			});
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<E> list = callback.findEntitiesByPrimaryKey(secondPass);
			for (E entity : list) {
				P primaryKey = callback.getPrimaryKey(entity);
				addToCache(cache, primaryKey, entity);
				result.put(primaryKey, entity);
			}
		}

		// sort the results
		List<E> sortedResult = new ArrayList<>(result.size());
		for (P key : ids) {
			CollectionUtils.addNonNullValue(sortedResult, result.get(key));
		}
		return sortedResult;
	}

	/**
	 * Batch load of entities by secondary keys.
	 *
	 * @param <P>
	 *            the generic type
	 * @param <E>
	 *            the concrete instance type
	 * @param <S>
	 *            the key type
	 * @param ids
	 *            the needed to load
	 * @param cache
	 *            the cache
	 * @param callback
	 *            the callback
	 * @return the list fetched list
	 */
	public static <P extends Serializable, E extends Entity<?>, S extends Serializable> List<E> batchLoadBySecondaryKey(
			List<S> ids, EntityLookupCache<P, E, S> cache, BatchSecondaryKeyEntityLoaderCallback<P, E, S> callback) {
		Set<S> secondPass = new LinkedHashSet<>();
		Map<S, E> result = CollectionUtils.createLinkedHashMap(ids.size());
		// first we check for hits in the cache and filter the cache misses

		if (cache == null) {
			secondPass.addAll(ids);
		} else {
			ids.forEach(id -> {
				P key = cache.getKey(id);
				// convert the cache entry to instance and schedule
				// properties loading instead of one by one loading
				// for some reason sometimes the value returned is null
				if (key != null && CollectionUtils.addNonNullValue(result, id, cache.getValue(key))) {
					return;
				}
				// no cache or not found in cache search later in DB
				secondPass.add(id);
			});
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<E> list = callback.findEntitiesBySecondaryKey(secondPass);
			for (E entity : list) {
				// update cache
				addToCache(cache, callback.getPrimaryKey(entity), entity);
				result.put(callback.getSecondaryKey(entity), entity);
			}
		}
		// sort the results
		List<E> sortedResult = new ArrayList<>(result.size());
		ids.forEach(key -> CollectionUtils.addNonNullValue(sortedResult, result.get(key)));
		return sortedResult;
	}

	private static <E extends Entity<?>, P extends Serializable> void addToCache(EntityLookupCache<P, E, ?> cache,
			P primaryKey, E entity) {
		if (cache != null) {
			// update cache
			cache.setValue(primaryKey, entity);
		}
	}

	/**
	 * Callback interface to provide additional information when calling the method.
	 *
	 * @param <R> the primary key type
	 * @param <E> the secondary key type
	 * {@link CacheUtils#batchLoadByPrimaryKey(List, EntityLookupCache, BatchPrimaryKeyEntityLoaderCallback)}
	 * . The implemented class should provide implementation for all defined methods.
	 */
	public interface BatchPrimaryKeyEntityLoaderCallback<R extends Serializable, E extends Entity<? extends Serializable>> {

		/**
		 * Gets the primary key for the given entity.
		 *
		 * @param entity the entity
		 * @return the primary key
		 */
		R getPrimaryKey(E entity);

		/**
		 * The method should perform a search by list primary keys and return the results.
		 *
		 * @param secondPass the list of primary keys that have not been found into provided cache of the batch loader method.
		 * @return the list of found entities
		 */
		List<E> findEntitiesByPrimaryKey(Set<R> secondPass);
	}

	/**
	 * Callback interface to provide additional information when calling the method.
	 *
	 * @param <R> the primary key type
	 * @param <E> the concrete entity type
	 * @param <S> the secondary key type
	 * {@link CacheUtils#batchLoadBySecondaryKey(List, EntityLookupCache, BatchSecondaryKeyEntityLoaderCallback)}
	 * . The implemented class should provide implementation for all defined methods.
	 */
	public interface BatchSecondaryKeyEntityLoaderCallback<R extends Serializable, E extends Entity<? extends Serializable>, S extends Serializable> {

		/**
		 * Gets the primary key for the given entity.
		 *
		 * @param entity the entity
		 * @return the primary key
		 */
		R getPrimaryKey(E entity);

		/**
		 * Gets the secondary key for the given entity.
		 *
		 * @param entity the entity
		 * @return the secondary key
		 */
		S getSecondaryKey(E entity);

		/**
		 * The method should perform a search by list secondary keys and return the results.
		 *
		 * @param secondPass the list of secondary keys that have not been found into provided cache of the batch loader
		 * method.
		 * @return the list of found entities
		 */
		List<E> findEntitiesBySecondaryKey(Set<S> secondPass);
	}
}
