package com.sirma.itt.emf.cache.lookup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cache.CacheProvider;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAO;

/**
 * Default implementation for the {@link EntityLookupCacheContext}.
 * <p>
 * The implementation used read/write lock to synchronize the access to the caches map. The bean has
 * a field that is used for status check. This is because in integration tests run by arquilian
 * there are unknown cases when the caches disappear. If the bean is notified for container shutdown
 * any method called on the bean instance will throw {@link IllegalStateException}.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class DefaultEntityLookupCacheContext implements EntityLookupCacheContext {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultEntityLookupCacheContext.class);

	/** The cache provider. */
	@Inject
	private CacheProvider cacheProvider;

	/** The cache context. Accessed under lock. */
	@SuppressWarnings("rawtypes")
	private Map<String, EntityLookupCache> cacheContext = new HashMap<>();

	/** The lock used for synchronized access to the internal cache mapping. */
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	/** The state. */
	private volatile boolean state;

	/**
	 * On init.
	 */
	@PostConstruct
	public void onInit() {
		// marked as active
		state = true;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K extends Serializable, V, VK extends Serializable> EntityLookupCache<K, V, VK> createCache(
			String cacheName, EntityLookupCallbackDAO<K, V, VK> lookup) {
		if (lookup == null) {
			// cannot create cache instance without lookup
			return null;
		}
		checkIfActive();
		String cacheRegion = cacheName + (cacheName.endsWith(REGION_SUFFIX) ? "" : REGION_SUFFIX);
		lockForWrite();
		try {
			EntityLookupCache<K, V, VK> lookupCache = new EntityLookupCache<>(
					cacheProvider.createCache(cacheRegion), cacheRegion, lookup);
			cacheContext.put(cacheName, lookupCache);
			LOGGER.debug("Registered cache {}", cacheName);
			return lookupCache;
		} finally {
			unlockForWrite();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V, VK extends Serializable> EntityLookupCache<K, V, VK> getCache(
			String cacheName) {
		checkIfActive();
		lockForRead();
		try {
			EntityLookupCache<K, V, VK> cache = cacheContext.get(cacheName);
			if (cache == null) {
				LOGGER.warn("Looking for not registered cache {} !", cacheName);
			}
			return cache;
		} finally {
			unlockForRead();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getActiveCaches() {
		checkIfActive();
		lockForRead();
		try {
			return new HashSet<>(cacheContext.keySet());
		} finally {
			unlockForRead();
		}
	}

	/**
	 * Check if active.
	 */
	private void checkIfActive() {
		if (!state) {
			throw new IllegalStateException(
					"Bean has been marked for inactive. This method should not be called on destroyed bean!");
		}
	}

	/**
	 * On shutdown.
	 */
	@PreDestroy
	public void onShutdown() {
		lockForWrite();
		try {
			// clear cache on shutdown needed for integration tests
			for (EntityLookupCache<?, ?, ?> cache : cacheContext.values()) {
				cache.clear();
			}
			cacheContext.clear();
		} finally {
			unlockForWrite();
		}
		// marked as shutdown
		state = false;
	}

	/**
	 * Lock for read.
	 */
	private void lockForRead() {
		lock.readLock().lock();
	}

	/**
	 * Lock for write.
	 */
	private void lockForWrite() {
		lock.writeLock().lock();
	}

	/**
	 * Unlock for read.
	 */
	private void unlockForRead() {
		lock.readLock().unlock();
	}

	/**
	 * Unlock for write.
	 */
	private void unlockForWrite() {
		lock.writeLock().unlock();
	}

}
