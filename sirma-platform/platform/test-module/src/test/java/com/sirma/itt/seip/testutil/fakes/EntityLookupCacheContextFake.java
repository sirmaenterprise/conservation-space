package com.sirma.itt.seip.testutil.fakes;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.cache.CacheProvider;
import com.sirma.itt.seip.cache.InMemoryCacheProvider;
import com.sirma.itt.seip.cache.NullCacheProvider;
import com.sirma.itt.seip.cache.SimpleCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAO;

/**
 * {@link EntityLookupCacheContext} implementation that provides basic functionality for tests
 *
 * @author BBonev
 */
public class EntityLookupCacheContextFake implements EntityLookupCacheContext {

	@SuppressWarnings("rawtypes")
	private Map<String, EntityLookupCache> cacheLookups = new HashMap<>();
	@SuppressWarnings("rawtypes")
	private Map<String, SimpleCache> caches = new HashMap<>();
	private CacheProvider cacheProvider;

	/**
	 * Instantiates a new entity lookup cache context fake.
	 *
	 * @param cacheProvider
	 *            the cache provider
	 */
	public EntityLookupCacheContextFake(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}

	/**
	 * Creates fake that stores the cached data in memory
	 *
	 * @return the entity lookup cache context fake
	 */
	public static EntityLookupCacheContextFake createInMemory() {
		return new EntityLookupCacheContextFake(new InMemoryCacheProvider());
	}

	/**
	 * Creates fake that does not store any data in cache.
	 *
	 * @return the entity lookup cache context fake
	 */
	public static EntityLookupCacheContextFake createNoCache() {
		return new EntityLookupCacheContextFake(NullCacheProvider.INSTANCE);
	}

	@Override
	public boolean containsCache(String cacheName) {
		return cacheLookups.containsKey(cacheName);
	}

	@Override
	public <K extends Serializable, V, X extends Serializable> EntityLookupCache<K, V, X> createCache(String cacheName,
			EntityLookupCallbackDAO<K, V, X> lookup) {
		return cacheLookups.computeIfAbsent(cacheName, name -> new EntityLookupCache<>(getCache(name, true), lookup));
	}

	@Override
	public <K extends Serializable, V, X extends Serializable> EntityLookupCache<K, V, X> createNullCache(
			String cacheName, EntityLookupCallbackDAO<K, V, X> lookup) {
		return cacheLookups.computeIfAbsent(cacheName,
				name -> new EntityLookupCache<>(getOrCreate(name, NullCacheProvider.INSTANCE), lookup));
	}

	@Override
	public <K extends Serializable, V> SimpleCache<K, V> getCache(String name, boolean create) {
		if (create) {
			return getOrCreate(name, cacheProvider);
		}
		return caches.get(name);
	}

	private <K extends Serializable, V> SimpleCache<K, V> getOrCreate(String name, CacheProvider provider) {
		return caches.computeIfAbsent(name, cacheName -> provider.createCache(cacheName));
	}

	@Override
	public <K extends Serializable, V, X extends Serializable> EntityLookupCache<K, V, X> getCache(String cacheName) {
		return cacheLookups.get(cacheName);
	}

	@Override
	public Set<String> getActiveCaches() {
		return Collections.unmodifiableSet(caches.keySet());
	}

	/**
	 * Removes all stored caches and their data. This reset the class to a state as after instance creation.
	 */
	public void reset() {
		caches.clear();
		cacheLookups.clear();
	}

	/**
	 * Purges all cached data if any from all caches. This is only valid for fake instantiated with
	 * {@link #createInMemory()}. If created via {@link #createNoCache()} this method does nothing.
	 */
	public void resetCacheData() {
		caches.forEach((name, cache) -> cache.clear());
	}

}
