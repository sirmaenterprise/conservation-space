package com.sirma.itt.commons.utils.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A special implementation of {@link java.util.LinkedHashMap} to provide LRU
 * functionality.
 * 
 * @author Adrian Mitev
 */
public class LRUMap<K, V> extends LinkedHashMap<K, V> {

	/**
	 * Maximum capacity of the map.
	 */
	private int maxCapacity;

	/**
	 * Initializes the map.
	 * 
	 * @param maxCapacity
	 *            maximum capacity of the map
	 * @testedBy {@link LRUMapTest#testLRU()}
	 */
	public LRUMap(int maxCapacity) {
		// set the load factor to 1 not allowing the map to expand
		super(maxCapacity, 1.0f, true);
		this.maxCapacity = maxCapacity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxCapacity;
	}

}
