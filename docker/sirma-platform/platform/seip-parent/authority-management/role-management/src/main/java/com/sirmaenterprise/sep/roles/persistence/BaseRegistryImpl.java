package com.sirmaenterprise.sep.roles.persistence;

import java.io.Serializable;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAO;
import com.sirma.itt.seip.provider.ProviderRegistry;

/**
 * Base implementation of a provider registry with caching
 *
 * @param <K>
 *            registry key type
 * @param <C>
 *            cache key type
 * @param <V>
 *            registry value
 * @since 2017-03-29
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
abstract class BaseRegistryImpl<K, C extends Serializable, V> implements ProviderRegistry<K, V> {

	private EntityLookupCacheContext cacheContext;

	/**
	 * Initialize the class with the cache lookup context. After calling the super constructor the method
	 * {@link #configure()} must be call
	 *
	 * @param cacheContext
	 */
	protected BaseRegistryImpl(EntityLookupCacheContext cacheContext) {
		this.cacheContext = cacheContext;
	}

	/**
	 * Configure the cache lookup. This method should be called before using any of the other methods
	 */
	protected void configure() {
		cacheContext.createCacheIfAbsent(getCacheName(), true, this::buildCacheCallback);
	}

	@Override
	public V find(K key) {
		if (key == null) {
			return null;
		}
		Pair<C, V> pair = getCache().getByKey(convertKey(key));
		if (pair != null) {
			return pair.getSecond();
		}
		return null;
	}

	@Override
	public void reload() {
		getCache().clear();
	}

	/**
	 * The cache name to use
	 *
	 * @return the cache name, not <code>null</code>
	 */
	protected abstract String getCacheName();

	/**
	 * Build a {@link EntityLookupCallbackDAO} instance to use for the cache entry resolving
	 *
	 * @return the lookup callback instance
	 */
	protected abstract EntityLookupCallbackDAO<C, V, Serializable> buildCacheCallback();

	/**
	 * Convert the input key to cache key type
	 *
	 * @param sourceKey
	 *            the source key
	 * @return the converted cache key
	 */
	protected abstract C convertKey(K sourceKey);

	/**
	 * The cache instance associated with the current registry
	 *
	 * @return a cache instance
	 */
	protected EntityLookupCache<C, V, Serializable> getCache() {
		return cacheContext.getCache(getCacheName());
	}
}
