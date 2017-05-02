package com.sirma.itt.seip.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.enterprise.inject.Alternative;

/**
 * A cache backed by a simple <code>HashMap</code>.
 * <p>
 * <b>Note:</b> This cache is not transaction- or thread-safe. Use it for single-threaded tests only.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author Derek Hulley
 * @since 3.2
 */
@Alternative
public class MemoryCache<K extends Serializable, V extends Object> implements SimpleCache<K, V> {

	/** The map. */
	private Map<K, V> map;

	/**
	 * Instantiates a new memory cache.
	 */
	public MemoryCache() {
		map = new ConcurrentHashMap<>(250);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(K key) {
		return map.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Stream<K> getKeys() {
		return map.keySet().stream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(K key) {
		return map.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(K key, V value) {
		map.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(K key) {
		map.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		map.clear();
	}
}
