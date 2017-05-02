package com.sirma.itt.seip.collections;

import java.util.LinkedHashMap;

/**
 * <p>
 * Linked Hash Map implementation that has {@link String} for key type. Stores its keys in lower case for case
 * insensitivity.
 * </p>
 * <p>
 * The overrided methods {@link #put(String, Object)}, {@link #get(Object)}, {@link #remove(Object)} and
 * {@link #containsKey(Object)} converts the passed key parameter to lower case before invoking the super class's
 * corresponding methods.
 * </p>
 *
 * @param <V>
 *            the type of the mapped values
 * @author Mihail Radkov
 */
public class CaseInsensitiveLinkedHashMap<V> extends LinkedHashMap<String, V> {

	private static final long serialVersionUID = -4814875516613584161L;

	/**
	 * Instantiates a new case insensitive linked hash map.
	 */
	public CaseInsensitiveLinkedHashMap() {
		super();
	}

	/**
	 * Instantiates a new case insensitive linked hash map.
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 * @param loadFactor
	 *            the load factor
	 * @param accessOrder
	 *            the access order
	 */
	public CaseInsensitiveLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
	}

	/**
	 * Instantiates a new case insensitive linked hash map.
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 * @param loadFactor
	 *            the load factor
	 */
	public CaseInsensitiveLinkedHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * Instantiates a new case insensitive linked hash map.
	 *
	 * @param initialCapacity
	 *            the initial capacity
	 */
	public CaseInsensitiveLinkedHashMap(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public V put(String key, V value) {
		return super.put(convert(key), value);
	}

	@Override
	public V get(Object key) {
		return super.get(convert(key));
	}

	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(convert(key));
	}

	@Override
	public V remove(Object key) {
		return super.remove(convert(key));
	}

	/**
	 * Converts the provided key to lower cased string.
	 *
	 * @param key
	 *            - the provided key
	 * @return the key in lower case or null
	 */
	private static String convert(Object key) {
		if (key == null) {
			return null;
		}
		return key.toString().toLowerCase();
	}

}
