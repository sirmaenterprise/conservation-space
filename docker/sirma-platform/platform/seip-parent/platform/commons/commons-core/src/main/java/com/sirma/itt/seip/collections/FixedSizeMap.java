package com.sirma.itt.seip.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map implementation that has a fixed size. If added element that will exceed the specified size an element will be
 * removed from it. The removed element will be the one that is longer in the map. This is useful for fixed size caches.
 * REVIEW:BB think about implementation that uses provided map rather a {@link LinkedHashMap}.
 *
 * @author BBonev
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
public class FixedSizeMap<K, V> extends LinkedHashMap<K, V> {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4827310399758994579L;
	/** The max size. */
	private final int maxSize;

	/**
	 * Instantiates a new fixed size map.
	 *
	 * @param maxSize
	 *            the max size
	 */
	public FixedSizeMap(int maxSize) {
		// initialize the super map with optimal capacity
		super((int) (maxSize * 1.1), 0.95F, false);
		this.maxSize = maxSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (m == null) {
			return;
		}
		// make sure when we put new batch values not to loose any of the new values
		if (m.size() > maxSize) {
			// Insufficient storage capacity
			throw new IllegalArgumentException("Insufficient storage capacity. The new data " + m.size()
					+ " is bigger than the max allowed of " + maxSize);
		}
		super.putAll(m);
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		// activates the ability of the linked hash map to act as a cache storage
		return size() > maxSize;
	}
}
