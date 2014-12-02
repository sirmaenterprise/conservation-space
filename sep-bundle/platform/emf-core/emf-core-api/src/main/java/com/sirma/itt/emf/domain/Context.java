package com.sirma.itt.emf.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Generic context implementation. The provided methods are from the {@link Map} interface. If the
 * class is extended the underling data storage implementation could be changed via overriding the
 * method {@link #createAndSetData(int, Map)}
 * 
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author BBonev
 */
public class Context<K, V> implements Map<K, V>, Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4797754957801742634L;
	/** The Constant DEFAUTL_SIZE. */
	private static final int DEFAUTL_SIZE = 16;
	/** The data. */
	private Map<K, V> data;

	/**
	 * Instantiates a new empty context with default size
	 */
	public Context() {
		this(DEFAUTL_SIZE);
	}

	/**
	 * Instantiates a new context with default size and copies the given data to it
	 * 
	 * @param <M>
	 *            the concrete map type
	 * @param source
	 *            the source data to copy
	 */
	public <M extends Map<K, V>> Context(M source) {
		createAndSetData(DEFAUTL_SIZE, source);
	}

	/**
	 * Instantiates a new context with the given size and copies the given data to it.
	 * 
	 * @param <M>
	 *            the concrete map type
	 * @param preferredSize
	 *            the preferred size
	 * @param source
	 *            the source data to copy
	 */
	public <M extends Map<K, V>> Context(int preferredSize, M source) {
		createAndSetData(preferredSize, source);
	}

	/**
	 * Instantiates a new empty context with the given size.
	 * 
	 * @param preferredSize
	 *            the preferred size
	 */
	public Context(int preferredSize) {
		createAndSetData(preferredSize, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		getData().clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsKey(Object key) {
		return getData().containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsValue(Object value) {
		return getData().containsValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return getData().entrySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(Object key) {
		return getData().get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return getData().isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<K> keySet() {
		return getData().keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V put(K key, V value) {
		return getData().put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		getData().putAll(m);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V remove(Object key) {
		return getData().remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return getData().size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<V> values() {
		return getData().values();
	}

	/**
	 * Returns value from the context by the specified key and checks if the found value matches the
	 * given type. If the type is not compatible (value cannot be cast to the given object type)
	 * null is returned.
	 * 
	 * @param <E>
	 *            the expected return type
	 * @param key
	 *            the key to fetch
	 * @param target
	 *            the expected return object class. If <code>null</code> the method will return
	 *            <code>null</code> always.
	 * @return the expected value of the specified type or <code>null</code> if not found or the
	 *         type does not match.
	 */
	public <E> E getIfSameType(K key, Class<E> target) {
		return getIfSameType(key, target, null);
	}

	/**
	 * Returns value from the context by the specified key and checks if the found value matches the
	 * given type. If the type is not compatible (value cannot be cast to the given object type) the
	 * default value is returned.
	 * 
	 * @param <E>
	 *            the expected return type
	 * @param key
	 *            the key to fetch
	 * @param target
	 *            the expected return object class. If <code>null</code> the method will return the
	 *            default value always.
	 * @param defaultValue
	 *            the default value to return
	 * @return the expected value of the specified type or the default value if not found or the
	 *         type does not match.
	 */
	public <E> E getIfSameType(K key, Class<E> target, E defaultValue) {
		V v = get(key);
		if (target == null) {
			return defaultValue;
		}
		if (target.isInstance(v)) {
			return target.cast(v);
		}
		return defaultValue;
	}

	/**
	 * Creates the and set data.
	 * 
	 * @param preferredSize
	 *            the preferred size
	 * @param source
	 *            the source
	 */
	protected void createAndSetData(int preferredSize, Map<K, V> source) {
		data = CollectionUtils.createLinkedHashMap(preferredSize);
		if (source != null) {
			data.putAll(source);
		}
	}

	/**
	 * Gets the underline implementations of the data holder-a.
	 * 
	 * @return the data
	 */
	protected Map<K, V> getData() {
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return data.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Context)) {
			return false;
		}
		Context other = (Context) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

}
