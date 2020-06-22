package com.sirma.itt.seip.cache.lookup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Destroyable;
import com.sirma.itt.seip.cache.CacheProvider;
import com.sirma.itt.seip.cache.NullCacheProvider;
import com.sirma.itt.seip.cache.SimpleCache;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.context.ValidatingContextualReference;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default implementation for the {@link EntityLookupCacheContext}.
 * <p>
 * The implementation used read/write lock to synchronize the access to the caches map. The bean has a field that is
 * used for status check. This is because in integration tests run by arquilian there are unknown cases when the caches
 * disappear. If the bean is notified for container shutdown any method called on the bean instance will throw
 * {@link IllegalStateException}.
 *
 * @author BBonev
 */
@Singleton
public class DefaultEntityLookupCacheContext implements EntityLookupCacheContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntityLookupCacheContext.class);

	/** The cache provider. */
	@Inject
	private CacheProvider cacheProvider;

	private CacheProvider nullCacheProvider = new NullCacheProvider();

	/** The cache context. Accessed under lock. */
	@SuppressWarnings("rawtypes")
	private Map<String, EntityLookupCache> cacheContext = new HashMap<>(64);
	@SuppressWarnings("rawtypes")
	private Map<String, SimpleCache> simpleCacheContext = new HashMap<>(64);

	/** The lock used for synchronized access to the internal cache mapping. */
	@Inject
	private ContextualReadWriteLock lock;
	@Inject
	private ContextualReadWriteLock simpleCacheLock;

	@Inject
	private SecurityContext securityContext;

	/** The state. */
	private volatile boolean active;

	/**
	 * On init.
	 */
	@PostConstruct
	public void onInit() {
		// marked as active
		active = true;
	}

	@Override
	public boolean containsCache(String cacheName) {
		checkIfActive();
		lockForRead();
		try {
			return cacheContext.containsKey(cacheName);
		} finally {
			unlockForRead();
		}
	}

	@Override
	public <K extends Serializable, V, X extends Serializable> EntityLookupCache<K, V, X> createCache(String cacheName,
			EntityLookupCallbackDAO<K, V, X> lookup) {
		return createCacheInternal(cacheName, lookup, cacheProvider);
	}

	private <X extends Serializable, K extends Serializable, V> EntityLookupCache<K, V, X> createCacheInternal(
			String cacheName, EntityLookupCallbackDAO<K, V, X> lookup, CacheProvider provider) {
		if (lookup == null) {
			// cannot create cache instance without lookup
			return null;
		}
		checkIfActive();
		String cacheRegion = getRegionName(cacheName);
		SimpleCache<Serializable, Object> cache = getOrCreateCache(cacheName, provider, true);
		lockForWrite();
		try {
			EntityLookupCache<K, V, X> lookupCache = new EntityLookupCache<>(cache, cacheRegion, lookup);
			cacheContext.put(cacheName, lookupCache);
			LOGGER.debug("Registered cache {}", cacheName);
			return lookupCache;
		} finally {
			unlockForWrite();
		}
	}

	@Override
	public <K extends Serializable, V, X extends Serializable> EntityLookupCache<K, V, X> createNullCache(
			String cacheName, EntityLookupCallbackDAO<K, V, X> lookup) {
		LOGGER.warn("Creating NULL cache for {}. This means that no caching will be done for it!", cacheName);
		return createCacheInternal(cacheName, lookup, nullCacheProvider);
	}

	/**
	 * Gets the region name.
	 *
	 * @param cacheName
	 *            the cache name
	 * @return the region name
	 */
	private static String getRegionName(String cacheName) {
		return cacheName + (cacheName.endsWith(REGION_SUFFIX) ? "" : REGION_SUFFIX);
	}

	@Override
	public <K extends Serializable, V> SimpleCache<K, V> getCache(String name, boolean create) {
		checkIfActive();
		return getOrCreateCache(name, cacheProvider, create);
	}

	private <K extends Serializable, V> SimpleCache<K, V> getOrCreateCache(String cacheName, CacheProvider provider,
			boolean create) {
		simpleCacheLock.lockForRead();
		try {
			SimpleCache<K, V> cache = simpleCacheContext.get(cacheName);
			if (cache == null && create) {
				simpleCacheLock.unlockForRead();
				simpleCacheLock.lockForWrite();
				try {
					String fullName = getRegionName(cacheName);
					cache = createCacheProxy(fullName, provider);
					simpleCacheContext.put(cacheName, cache);
				} finally {
					simpleCacheLock.unlockForWrite();
					simpleCacheLock.lockForRead();
				}
			}
			return cache;
		} finally {
			simpleCacheLock.unlockForRead();
		}
	}

	private <K extends Serializable, V> SimpleCache<K, V> createCacheProxy(String fullName, CacheProvider provider) {
		SecurityContext security = securityContext;
		return new CacheProxy<>(fullName, security::getCurrentTenantId, () -> provider.createCache(fullName));
	}

	@Override
	public <K extends Serializable, V, X extends Serializable> EntityLookupCache<K, V, X> getCache(String cacheName) {
		checkIfActive();
		lockForRead();
		try {
			EntityLookupCache<K, V, X> cache = cacheContext.get(cacheName);
			if (cache == null) {
				LOGGER.warn("Looking for not registered cache {} !", cacheName);
			}
			return cache;
		} finally {
			unlockForRead();
		}
	}

	@Override
	public Set<String> getActiveCaches() {
		checkIfActive();
		lockForRead();
		try {
			return new HashSet<>(simpleCacheContext.keySet());
		} finally {
			unlockForRead();
		}
	}

	/**
	 * Check if active.
	 */
	private void checkIfActive() {
		if (!active) {
			throw new IllegalStateException(
					"Bean has been marked for inactive. This method should not be called on destroyed bean!");
		}
	}

	/**
	 * On shutdown.
	 */
	@PreDestroy
	public void onShutdown() {
		clearEntityContextCache();
		clearSimpleCache();
		// marked as shutdown
		active = false;
	}

	/**
	 * Clear simple cache.
	 */
	private void clearSimpleCache() {
		simpleCacheContext.values().forEach(Destroyable::destroy);
		simpleCacheContext.clear();
	}

	/**
	 * Clear entity context cache.
	 */
	private void clearEntityContextCache() {
		// clear cache on shutdown needed for integration tests
		// NOTE: for clustered cache this will cause total cluster clear on node shutdown!
		// so this need another approach
		cacheContext.values().forEach(e -> Destroyable.destroy(e));
		cacheContext.clear();
	}

	/**
	 * Lock for read.
	 */
	private void lockForRead() {
		lock.lockForRead();
	}

	/**
	 * Lock for write.
	 */
	private void lockForWrite() {
		lock.lockForWrite();
	}

	/**
	 * Unlock for read.
	 */
	private void unlockForRead() {
		lock.unlockForRead();
	}

	/**
	 * Unlock for write.
	 */
	private void unlockForWrite() {
		lock.unlockForWrite();
	}

	/**
	 * The cache proxy for lazy initialization of the actual caches.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @author BBonev
	 */
	private static class CacheProxy<K extends Serializable, V extends Object>
			extends ValidatingContextualReference<SimpleCache<K, V>>implements SimpleCache<K, V> {

		/**
		 * Instantiates a new cache proxy.
		 *
		 * @param name
		 *            the name
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param initialValue
		 *            the initial value
		 */
		public CacheProxy(String name, Supplier<String> contextIdSupplier, Supplier<SimpleCache<K, V>> initialValue) {
			super(contextIdSupplier, initialValue,
					(c) -> Objects.requireNonNull(c, "Cache instance for " + name + " cannot be null"));
			onDestroy(cache -> cache.clear());
		}

		@Override
		public boolean contains(K key) {
			return getContextValue().contains(key);
		}

		@Override
		public Stream<K> getKeys() {
			return getContextValue().getKeys();
		}

		@Override
		public V get(K key) {
			return getContextValue().get(key);
		}

		@Override
		public void put(K key, V value) {
			getContextValue().put(key, value);
		}

		@Override
		public void remove(K key) {
			getContextValue().remove(key);
		}

		@Override
		public void clear() {
			getContextValue().clear();
		}
	}

}
