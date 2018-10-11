package com.sirma.itt.seip.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.Sealable;

/**
 * Sealable {@link Map}. When sealed the collection does not allow data modifications. Any call to methods that modifies
 * the content will be ignored without exception. Note that when sealed the returned {@link Set} and {@link Collection}
 * from the methods {@link #keySet()}, {@link #entrySet()}, {@link #values()} will also be sealed.
 *
 * @author BBonev
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
public class SealedMap<K, V> implements Map<K, V>, Sealable, Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2774775723816640572L;
	/** The map. */
	private final Map<K, V> map;
	/** The seal. */
	private boolean seal;

	/**
	 * Instantiates a new sealed map.
	 *
	 * @param map
	 *            the map
	 */
	public SealedMap(Map<K, V> map) {
		this(map, true);
	}

	/**
	 * Instantiates a new sealed map.
	 *
	 * @param map
	 *            the map
	 * @param sealNow
	 *            the seal now
	 */
	public SealedMap(Map<K, V> map, boolean sealNow) {
		this.map = map == null ? Collections.<K, V> emptyMap() : map;
		this.seal = sealNow;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return map.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(Object key) {
		return map.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V put(K key, V value) {
		if (isSealed()) {
			return null;
		}
		return map.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V remove(Object key) {
		if (isSealed()) {
			return null;
		}
		return map.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (isSealed()) {
			return;
		}
		map.putAll(m);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		if (isSealed()) {
			return;
		}
		map.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<K> keySet() {
		return new SealedSet<>(map.keySet(), isSealed());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<V> values() {
		return new SealedCollection<>(map.values(), isSealed());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new SealedSet<>(map.entrySet(), isSealed());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSealed() {
		return seal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void seal() {
		seal = true;
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
