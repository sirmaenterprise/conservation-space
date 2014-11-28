/**
 */
package com.sirma.codelist.cache;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * Base class for implementing a local application cache stored in a {@link Map}
 * object.
 * 
 * @param <K>
 *            is the type of the cache key
 * @param <V>
 *            is the type of the cached value
 * @author B.Bonev
 */
public abstract class BaseCache<K, V> implements Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6326162279878962656L;
	private Map<K, V> cache;

	/**
	 * Method called to initialize the cache.
	 */
	@PostConstruct
	protected abstract void initialize();

	/**
	 * Puts an object to the session cache.
	 * 
	 * @param key
	 *            is the key to store under the given value
	 * @param value
	 *            is the value to store
	 * @return the put value or the old value under this key if any.
	 */
	public V put(K key, V value) {
		return getCache().put(key, value);
	}

	/**
	 * Retrieves a value by key from session cache.
	 * 
	 * @param key
	 *            is the key to look for.
	 * @return the found value or <code>null</code> if not found.
	 */
	public V get(K key) {
		return getCache().get(key);
	}

	/**
	 * Checks the given key if is in the session cache.
	 * 
	 * @param key
	 *            is the value to search for.
	 * @return <code>true</code> if the given key is found in the cache.
	 */
	public boolean contains(K key) {
		return getCache().containsKey(key);
	}

	/**
	 * Setter method for cache.
	 * 
	 * @param cache
	 *            the codelistCache to set
	 */
	protected void setCache(Map<K, V> cache) {
		this.cache = cache;
	}

	/**
	 * Getter method for cache.
	 * 
	 * @return the cache
	 */
	protected Map<K, V> getCache() {
		return cache;
	}
}
