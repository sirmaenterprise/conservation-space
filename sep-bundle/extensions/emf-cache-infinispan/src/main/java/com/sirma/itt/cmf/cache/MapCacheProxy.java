package com.sirma.itt.cmf.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sirma.itt.emf.cache.SimpleCache;

/**
 * Proxy class for {@link Map} interface to {@link SimpleCache}
 * 
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author BBonev
 */
public class MapCacheProxy<K extends Serializable, V extends Object> implements SimpleCache<K, V> {

	/** The map. */
	private Map<K, V> map;

	/**
	 * Instantiates a new map cache proxy.
	 * 
	 * @param map
	 *            the map
	 */
	public MapCacheProxy(Map<K, V> map) {
		this.map = map;
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
	public Collection<K> getKeys() {
		return map.keySet();
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
