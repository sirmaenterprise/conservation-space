package com.sirma.itt.seip.cache;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Basic caching interface.
 * <p>
 * All implementations <b>must</b> be thread-safe. Additionally, the use of the <tt>Serializable</tt> for both keys and
 * values ensures that the underlying cache implementations can support both clustered caches as well as persistent
 * caches.
 * <p>
 * All implementations must support <tt>null</tt> values. It therefore follows that
 *
 * <pre>
 * (simpleCache.contains(key) == true) does not imply (simpleCache.get(key) != null)
 * </pre>
 *
 * but
 *
 * <pre>
 * (simpleCache.contains(key) == false) implies (simpleCache.get(key) == null)
 * </pre>
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author Derek Hulley
 */
public interface SimpleCache<K extends Serializable, V extends Object> {

	/**
	 * Contains.
	 *
	 * @param key
	 *            the cache key to check up on
	 * @return Returns <tt>true</tt> if there is a cache entry, regardless of whether the value itself is <tt>null</tt>
	 */
	boolean contains(K key);

	/**
	 * Gets the cache keys as stream. Note that the stream should be closed on end!
	 * <p>
	 * Use something like this
	 *
	 * <pre>
	 * try (Stream&lt;Serializable&gt; keysStream = cache.getKeys()) {
	 * 	// process keys
	 * }
	 * </pre>
	 *
	 * Note that this method is heavy and expensive and memory consuming and should not be used be trivial operations as
	 * by checking size or iterating all keys all the time.
	 *
	 * @return the keys
	 */
	Stream<K> getKeys();

	/**
	 * Gets the.
	 *
	 * @param key
	 *            the key
	 * @return Returns the value associated with the key. It will be <tt>null</tt> if the value is <tt>null</tt> or if
	 *         the cache doesn't have an entry.
	 */
	V get(K key);

	/**
	 * Put.
	 *
	 * @param key
	 *            the key against which to store the value
	 * @param value
	 *            the value to store. <tt>null</tt> is allowed.
	 */
	void put(K key, V value);

	/**
	 * Removes the cache entry whether or not the value stored against it is <tt>null</tt>.
	 *
	 * @param key
	 *            the key value to remove
	 */
	void remove(K key);

	/**
	 * Clear.
	 */
	void clear();
}
