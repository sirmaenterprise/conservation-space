/*
 *
 */
package com.sirma.itt.cmf.test.mock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.emf.cache.CacheProvider;
import com.sirma.itt.emf.cache.InMemoryCacheProvider;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAO;

/**
 * Default mock implementation for the {@link EntityLookupCacheContext}.
 *
 * @author bbanchev
 */
public class EntityLookupCacheContextMock implements EntityLookupCacheContext {

	/** The Constant REGION_SUFFIX. */
	private static final String REGION_SUFFIX = "_REGION";

	/** The cache provider. */
	private CacheProvider cacheProvider = new InMemoryCacheProvider();

	/** The cache context. */
	@SuppressWarnings("rawtypes")
	private Map<String, EntityLookupCache> cacheContext = new HashMap<String, EntityLookupCache>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsCache(String cacheName) {
		return cacheContext.containsKey(cacheName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K extends Serializable, V, VK extends Serializable> EntityLookupCache<K, V, VK> createCache(
			String cacheName, EntityLookupCallbackDAO<K, V, VK> lookup) {
		String cacheRegion = cacheName + REGION_SUFFIX;
		EntityLookupCache<K, V, VK> lookupCache = new EntityLookupCache<K, V, VK>(
				cacheProvider.createCache(cacheRegion), cacheRegion, lookup);
		cacheContext.put(cacheName, lookupCache);
		return lookupCache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V, VK extends Serializable> EntityLookupCache<K, V, VK> getCache(
			String cacheName) {
		return cacheContext.get(cacheName);
	}

	/* (non-Javadoc)
	 * @see com.sirma.itt.cmf.cache.lookup.EntityLookupCacheContext#getActiveCaches()
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getActiveCaches() {
		return new HashSet<String>(cacheContext.keySet());
	}

}
