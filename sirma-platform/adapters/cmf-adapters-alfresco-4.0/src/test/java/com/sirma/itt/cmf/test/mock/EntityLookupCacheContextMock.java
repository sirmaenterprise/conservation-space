/*
 *
 */
package com.sirma.itt.cmf.test.mock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sirma.itt.seip.cache.CacheProvider;
import com.sirma.itt.seip.cache.InMemoryCacheProvider;
import com.sirma.itt.seip.cache.NullCacheProvider;
import com.sirma.itt.seip.cache.SimpleCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAO;

/**
 * Default mock implementation for the {@link EntityLookupCacheContext}.
 *
 * @author bbanchev
 */
public class EntityLookupCacheContextMock implements EntityLookupCacheContext {

	/** The Constant REGION_SUFFIX. */
	private static final String REGION_SUFFIX = "_REGION";

	@SuppressWarnings("rawtypes")
	private Map<String, SimpleCache> simpleCacheContext = new HashMap<>();
	/** The lock used for synchronized access to the internal cache mapping. */
	private ReadWriteLock simpleCacheLock = new ReentrantReadWriteLock();
	/** The cache provider. */
	private CacheProvider cacheProvider = new InMemoryCacheProvider();
	private CacheProvider nullCacheProvider = new NullCacheProvider();

	/** The cache context. */
	@SuppressWarnings("rawtypes")
	private Map<String, EntityLookupCache> cacheContext = new HashMap<>();

	@Override
	public boolean containsCache(String cacheName) {
		return cacheContext.containsKey(cacheName);
	}

	@Override
	public <K extends Serializable, V, VK extends Serializable> EntityLookupCache<K, V, VK> createCache(
			String cacheName, EntityLookupCallbackDAO<K, V, VK> lookup) {
		String cacheRegion = cacheName + REGION_SUFFIX;
		EntityLookupCache<K, V, VK> lookupCache = new EntityLookupCache<>(cacheProvider.createCache(cacheRegion),
				cacheRegion, lookup);
		cacheContext.put(cacheName, lookupCache);
		return lookupCache;
	}

	@Override
	public <K extends Serializable, V, X extends Serializable> EntityLookupCache<K, V, X> createNullCache(
			String cacheName, EntityLookupCallbackDAO<K, V, X> lookup) {
		String cacheRegion = cacheName + REGION_SUFFIX;
		EntityLookupCache<K, V, X> lookupCache = new EntityLookupCache<>(nullCacheProvider.createCache(cacheRegion),
				cacheRegion, lookup);
		cacheContext.put(cacheName, lookupCache);
		return lookupCache;
	}

	@Override
	public <K extends Serializable, V, VK extends Serializable> EntityLookupCache<K, V, VK> getCache(String cacheName) {
		return cacheContext.get(cacheName);
	}

	@Override
	public Set<String> getActiveCaches() {
		return new HashSet<>(cacheContext.keySet());
	}

	@Override
	public <K extends Serializable, V> SimpleCache<K, V> getCache(String name, boolean create) {
		simpleCacheLock.readLock().lock();
		try {
			String regionName = getRegionName(name);
			SimpleCache<K, V> cache = simpleCacheContext.get(regionName);
			if (cache == null && create) {
				simpleCacheLock.readLock().unlock();
				simpleCacheLock.writeLock().lock();
				try {
					cache = cacheProvider.createCache(regionName);
					simpleCacheContext.put(regionName, cache);
					simpleCacheLock.readLock().lock();
				} finally {
					simpleCacheLock.writeLock().unlock();
				}
			}
			return cache;
		} finally {
			simpleCacheLock.readLock().unlock();
		}
	}

	/**
	 * Gets the region name.
	 *
	 * @param cacheName
	 *            the cache name
	 * @return the region name
	 */
	private String getRegionName(String cacheName) {
		return cacheName + (cacheName.endsWith(REGION_SUFFIX) ? "" : REGION_SUFFIX);
	}

}
