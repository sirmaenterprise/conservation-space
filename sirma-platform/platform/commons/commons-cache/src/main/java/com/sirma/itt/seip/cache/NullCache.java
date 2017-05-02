package com.sirma.itt.seip.cache;

import java.io.Serializable;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;

/**
 * A cache that does nothing - always.
 * <p>
 * There are conditions under which code that expects to be caching, should not be. Using this cache, it becomes
 * possible to configure a valid cache in whilst still ensuring that the actual caching is not performed.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author Derek Hulley
 */
@Alternative
public class NullCache<K extends Serializable, V extends Object> implements SimpleCache<K, V> {

	/**
	 * Instantiates a new null cache.
	 */
	public NullCache() {
		// default constructor
	}

	/**
	 * NO-OP.
	 *
	 * @param key
	 *            the key
	 * @return true, if successful
	 */
	@Override
	public boolean contains(K key) {
		return false;
	}

	/**
	 * @return empty stream
	 */
	@Override
	public Stream<K> getKeys() {
		return Stream.empty();
	}

	/**
	 * NO-OP.
	 *
	 * @param key
	 *            the key
	 * @return the v
	 */
	@Override
	public V get(K key) {
		return null;
	}

	/**
	 * NO-OP.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	@Override
	public void put(K key, V value) {
		return;
	}

	/**
	 * NO-OP.
	 *
	 * @param key
	 *            the key
	 */
	@Override
	public void remove(K key) {
		return;
	}

	/**
	 * NO-OP.
	 */
	@Override
	public void clear() {
		return;
	}
}
