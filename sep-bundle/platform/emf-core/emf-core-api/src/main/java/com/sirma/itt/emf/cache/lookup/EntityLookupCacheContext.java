package com.sirma.itt.emf.cache.lookup;

import java.io.Serializable;
import java.util.Set;

import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAO;

/**
 * Defines a context for {@link EntityLookupCache} instances. These instances
 * must contained in {@link javax.enterprise.context.ApplicationScoped} bean.
 *
 * @author BBonev
 */
public interface EntityLookupCacheContext {

	/** The Constant REGION_SUFFIX. */
	String REGION_SUFFIX = "_REGION";

	/**
	 * Checks if the context contains a cache with the given name
	 *
	 * @param cacheName
	 *            the cache name to check for
	 * @return <code>true</code>, if found and <code>false</code> if not
	 */
	boolean containsCache(String cacheName);

	/**
	 * Creates {@link EntityLookupCache} instance using the given callback and
	 * adds it to the context under the given name
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param <VK>
	 *            the keyValue type
	 * @param cacheName
	 *            the cache name
	 * @param lookup
	 *            the callback DAO to use for initializing cache
	 * @return the entity lookup cache
	 */
	<K extends Serializable, V extends Object, VK extends Serializable> EntityLookupCache<K, V, VK> createCache(
			String cacheName, EntityLookupCallbackDAO<K, V, VK> lookup);

	/**
	 * Gets the {@link EntityLookupCache} instance stored under the given name
	 * in the context if any.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param <VK>
	 *            the keyValue type
	 * @param cacheName
	 *            the cache name
	 * @return the cache if found or <code>null</code> if not
	 */
	<K extends Serializable, V extends Object, VK extends Serializable> EntityLookupCache<K, V, VK> getCache(
			String cacheName);

	/**
	 * Gets the active caches.
	 * 
	 * @return the active caches
	 */
	Set<String> getActiveCaches();
}
