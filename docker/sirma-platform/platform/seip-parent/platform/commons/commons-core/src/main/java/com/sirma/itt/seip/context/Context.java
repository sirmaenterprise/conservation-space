package com.sirma.itt.seip.context;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Generic context implementation. The provided methods are from the {@link Map} interface. If the class is extended the
 * underling data storage implementation could be changed via overriding the method {@link #createAndSetData(int, Map)}
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author BBonev
 */
public class Context<K, V> implements Map<K, V>, Serializable {
	private static final long serialVersionUID = 4797754957801742634L;

	@SuppressWarnings("rawtypes")
	private static final Context<?, ?> EMPTY_CONTEXT = new Context(0) {
		private static final long serialVersionUID = -1199684732601990015L;

		@Override
		protected Map<?, ?> getData() {
			return Collections.emptyMap();
		}
	};

	/** Default context size. */
	private static final int DEFAUTL_SIZE = 16;
	/** The context data. */
	private Map<K, V> data;

	/**
	 * Return singleton instance of empty unmodifiable context
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the context
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Context<K, V> emptyContext() {
		return (Context<K, V>) EMPTY_CONTEXT;
	}

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
		createAndSetData(source != null ? Math.max(DEFAUTL_SIZE, source.size()) : DEFAUTL_SIZE, source);
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

	@Override
	public void clear() {
		getData().clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return getData().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return getData().containsValue(value);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return getData().entrySet();
	}

	@Override
	public V get(Object key) {
		return getData().get(key);
	}

	@Override
	public boolean isEmpty() {
		return getData().isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return getData().keySet();
	}

	@Override
	public V put(K key, V value) {
		return getData().put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		getData().putAll(m);
	}

	@Override
	public V remove(Object key) {
		return getData().remove(key);
	}

	@Override
	public int size() {
		return getData().size();
	}

	@Override
	public Collection<V> values() {
		return getData().values();
	}

	/**
	 * Returns value from the context by the specified key and checks if the found value matches the given type. If the
	 * type is not compatible (value cannot be cast to the given object type) null is returned.
	 *
	 * @param <E>
	 *            the expected return type
	 * @param key
	 *            the key to fetch
	 * @param target
	 *            the expected return object class. If <code>null</code> the method will return <code>null</code>
	 *            always.
	 * @return the expected value of the specified type or <code>null</code> if not found or the type does not match.
	 */
	public <E> E getIfSameType(K key, Class<E> target) {
		return getIfSameType(key, target, null);
	}

	/**
	 * Converts a value identified by the given key from the context using the given function. If the value is not
	 * present then null is returned.
	 *
	 * @param <R>
	 *            the new mapped type
	 * @param key
	 *            the key to convert
	 * @param function
	 *            the mapping functions to apply on the context value if exists. Cannot be <code>null</code>.
	 * @return the converted value or <code>null</code> if the source value is null
	 */
	public <R> R map(K key, Function<V, R> function) {
		return map(key, function, () -> null);
	}

	/**
	 * Converts a value identified by the given key from the context using the given function. If the value is not
	 * present then the provided supplier will be used to provide the default value to be returned.
	 *
	 * @param <R>
	 *            the new mapped type
	 * @param key
	 *            the key to convert
	 * @param function
	 *            the mapping functions to apply on the context value if exists. Cannot be <code>null</code>.
	 * @param defaultValueSupplier
	 *            the default value supplier to be called if the source value is <code>null</code>. Cannot be
	 *            <code>null</code>.
	 * @return the converted value or the value provided by the default value supplier if the source value is null
	 */
	public <R> R map(K key, Function<V, R> function, Supplier<R> defaultValueSupplier) {
		Objects.requireNonNull(function, "Mapping function could not be null");
		V value = get(key);
		if (value != null) {
			return function.apply(value);
		}
		return Objects.requireNonNull(defaultValueSupplier, "Default value supplier could not be null").get();
	}

	/**
	 * Returns value from the context by the specified key and checks if the found value matches the given type. If the
	 * type is not compatible (value cannot be cast to the given object type) the default value is returned.
	 *
	 * @param <E>
	 *            the expected return type
	 * @param key
	 *            the key to fetch
	 * @param target
	 *            the expected return object class. If <code>null</code> the method will return the default value
	 *            always.
	 * @param defaultValue
	 *            the default value to return
	 * @return the expected value of the specified type or the default value if not found or the type does not match.
	 */
	public <E> E getIfSameType(K key, Class<E> target, E defaultValue) {
		if (target == null) {
			return defaultValue;
		}
		V v = get(key);
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
		setData(CollectionUtils.createLinkedHashMap(preferredSize));
		if (source != null) {
			getData().putAll(source);
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
	 * Sets and override the internal data store
	 *
	 * @param toSet
	 *            the to set
	 */
	protected void setData(Map<K, V> toSet) {
		data = toSet;
	}

	@Override
	public String toString() {
		return getData().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getData() == null ? 0 : getData().hashCode());
		return result;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Context)) {
			return false;
		}
		Context other = (Context) obj;
		return EqualsHelper.nullSafeEquals(getData(), other.getData());
	}

}
