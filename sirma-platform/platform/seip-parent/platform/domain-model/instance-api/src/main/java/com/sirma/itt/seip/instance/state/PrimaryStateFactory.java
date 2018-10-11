package com.sirma.itt.seip.instance.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.enterprise.context.ApplicationScoped;

/**
 * A factory for creating PrimaryState objects.
 *
 * @author BBonev
 */
@ApplicationScoped
public class PrimaryStateFactory {

	/** The mapping. */
	protected Map<String, PrimaryStates> mapping = new LinkedHashMap<>(50);
	/** The lock used to synchronize read/write to the cache. */
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * Creates simple implementations for the given key and stores the created objects in memory to reuse them.
	 *
	 * @param key
	 *            the key
	 * @return the primary state type
	 */
	public PrimaryStates create(String key) {
		if (key == null) {
			return create(PrimaryStates.INITIAL_KEY);
		}
		PrimaryStates type = getFromCache(key);
		if (type == null) {
			type = new DefaultPrimaryStateTypeImpl(key);
			addToCache(key, type);
		}
		return type;
	}

	/**
	 * Adds the to cache.
	 *
	 * @param key
	 *            the key
	 * @param type
	 *            the type
	 */
	protected void addToCache(String key, PrimaryStates type) {
		lock.writeLock().lock();
		try {
			mapping.put(key, type);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Gets the from cache.
	 *
	 * @param key
	 *            the key
	 * @return the from cache
	 */
	protected PrimaryStates getFromCache(String key) {
		lock.readLock().lock();
		try {
			return mapping.get(key);
		} finally {
			lock.readLock().unlock();
		}
	}
}
