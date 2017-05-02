package com.sirma.itt.seip.cache.lookup;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Supplier;

import com.sirma.itt.seip.cache.CacheProvider;
import com.sirma.itt.seip.cache.SimpleCache;

/**
 * This class is defined to handle the issue for storing cache instances that are used in stateless beans. Because the
 * nature of the stateless SB the cache instances could not be stored in the bean instances. the decision was made to
 * store the cache instances in a singleton/application scope bean and to be requested when needed. <br>
 * The interface defines method for registering {@link EntityLookupCache} or accessing a simple cache. The cache itself
 * are accessed via the current available {@link CacheProvider}.
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
	 * Creates {@link EntityLookupCache} instance using the given callback and adds it to the context under the given
	 * name
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param <X>
	 *            the value key type
	 * @param cacheName
	 *            the cache name
	 * @param lookup
	 *            the callback DAO to use for initializing cache
	 * @return the entity lookup cache
	 */
	<K extends Serializable, V extends Object, X extends Serializable> EntityLookupCache<K, V, X> createCache(
			String cacheName, EntityLookupCallbackDAO<K, V, X> lookup);

	/**
	 * Creates the cache if absent or returns the current cache. The method checks for cache name validity and if
	 * <code>null</code> just returns. If the given cache is present then it will be returned. If the cache is not
	 * present then it will be created using one of the methods {@link #createCache(String, EntityLookupCallbackDAO)} or
	 * {@link #createNullCache(String, EntityLookupCallbackDAO)} depending on the boolean parameter
	 * {@code withActualCaching}. If value is <code>true</code> then actual caching will be performed otherwise it will
	 * return dummy cache that will not store anything.
	 *
	 * @param <K>
	 *            the primary key type
	 * @param <V>
	 *            the value type
	 * @param <X>
	 *            the secondary key type
	 * @param cacheName
	 *            the cache name
	 * @param withActualCaching
	 *            the actual caching will occur if passed <code>true</code> and will not if passed <code>false</code>.
	 * @param lookup
	 *            the lookup to be used for cache creation if cache should be created.
	 * @return the entity lookup cache or <code>null</code> if cache name was <code>null</code>.
	 * @see #createCache(String, EntityLookupCallbackDAO)
	 * @see #createNullCache(String, EntityLookupCallbackDAO)
	 */
	default <K extends Serializable, V extends Object, X extends Serializable> EntityLookupCache<K, V, X> createCacheIfAbsent(
			String cacheName, boolean withActualCaching, EntityLookupCallbackDAO<K, V, X> lookup) {
		return createCacheIfAbsent(cacheName, withActualCaching, () -> lookup);
	}

	/**
	 * Creates the cache lazily if absent or returns the current cache. The method checks for cache name validity and if
	 * <code>null</code> just returns. If the given cache is present then it will be returned. If the cache is not
	 * present then it will be created using one of the methods {@link #createCache(String, EntityLookupCallbackDAO)} or
	 * {@link #createNullCache(String, EntityLookupCallbackDAO)} depending on the boolean parameter
	 * {@code withActualCaching}. If value is <code>true</code> then actual caching will be performed otherwise it will
	 * return dummy cache that will not store anything.
	 *
	 * @param <K>
	 *            the primary key type
	 * @param <V>
	 *            the value type
	 * @param <X>
	 *            the secondary key type
	 * @param cacheName
	 *            the cache name
	 * @param withActualCaching
	 *            the actual caching will occur if passed <code>true</code> and will not if passed <code>false</code>.
	 * @param lookupProvider
	 *            supplier for the cache lookup dao. It will be used if the cache does not exist and must be created.
	 * @return the entity lookup cache or <code>null</code> if cache name was <code>null</code>.
	 * @see #createCache(String, EntityLookupCallbackDAO)
	 * @see #createNullCache(String, EntityLookupCallbackDAO)
	 */
	default <K extends Serializable, V extends Object, X extends Serializable> EntityLookupCache<K, V, X> createCacheIfAbsent(
			String cacheName, boolean withActualCaching, Supplier<EntityLookupCallbackDAO<K, V, X>> lookupProvider) {
		if (cacheName != null) {
			if (!containsCache(cacheName)) {
				if (withActualCaching) {
					return createCache(cacheName, lookupProvider.get());
				}
				return createNullCache(cacheName, lookupProvider.get());
			}
			return getCache(cacheName);
		}
		return null;
	}

	/**
	 * Creates {@link EntityLookupCache} instance that is not backed up by cache but is valid entity lookup and is
	 * accessible by the given name.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param <X>
	 *            the value key type
	 * @param cacheName
	 *            the cache name
	 * @param lookup
	 *            the callback DAO to use for initializing cache
	 * @return the entity lookup cache
	 */
	<K extends Serializable, V extends Object, X extends Serializable> EntityLookupCache<K, V, X> createNullCache(
			String cacheName, EntityLookupCallbackDAO<K, V, X> lookup);

	/**
	 * Returns a cache instance. If the cache is not registered in the context it will be created if the second argument
	 * is <code>true</code> and stored. Subsequent calls to the method will return the already created cache instance no
	 * matter the second argument.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param name
	 *            the name
	 * @param create
	 *            the create if missing
	 * @return the simple cache
	 */
	<K extends Serializable, V extends Object> SimpleCache<K, V> getCache(String name, boolean create);

	/**
	 * Gets the {@link EntityLookupCache} instance stored under the given name in the context if any.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param <X>
	 *            the value key type
	 * @param cacheName
	 *            the cache name
	 * @return the cache if found or <code>null</code> if not
	 */
	<K extends Serializable, V extends Object, X extends Serializable> EntityLookupCache<K, V, X> getCache(
			String cacheName);

	/**
	 * Gets the active caches created via methods {@link #createCache(String, EntityLookupCallbackDAO)} or
	 * {@link #getCache(String, boolean)}.
	 *
	 * @return the active caches
	 */
	Set<String> getActiveCaches();
}
