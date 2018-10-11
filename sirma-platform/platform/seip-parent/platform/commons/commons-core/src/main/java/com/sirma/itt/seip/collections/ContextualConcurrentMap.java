/**
 *
 */
package com.sirma.itt.seip.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.context.ValidatingContextualReference;

/**
 * Defines a contextual {@link ConcurrentMap} definition.
 *
 * @author BBonev
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @see ConcurrentMap
 * @see ConcurrentHashMap
 */
public interface ContextualConcurrentMap<K, V> extends Contextual<ConcurrentMap<K, V>>, ConcurrentMap<K, V> {

	@Override
	default boolean isEmpty() {
		return getContextValue().isEmpty();
	}

	@Override
	default int size() {
		return getContextValue().size();
	}

	@Override
	default boolean containsValue(Object value) {
		return getContextValue().containsValue(value);
	}

	@Override
	default boolean containsKey(Object key) {
		return getContextValue().containsKey(key);
	}

	@Override
	default V put(K key, V value) {
		return getContextValue().put(key, value);
	}

	@Override
	default V get(Object key) {
		return getContextValue().get(key);
	}

	@Override
	default void putAll(Map<? extends K, ? extends V> m) {
		getContextValue().putAll(m);
	}

	@Override
	default V remove(Object key) {
		return getContextValue().remove(key);
	}

	@Override
	default Collection<V> values() {
		return getContextValue().values();
	}

	@Override
	default Set<K> keySet() {
		return getContextValue().keySet();
	}

	@Override
	default Set<java.util.Map.Entry<K, V>> entrySet() {
		return getContextValue().entrySet();
	}

	@Override
	default void clear() {
		getContextValue().clear();
	}

	@Override
	default V putIfAbsent(K key, V value) {
		return getContextValue().putIfAbsent(key, value);
	}

	@Override
	default boolean remove(Object key, Object value) {
		return getContextValue().remove(key, value);
	}

	@Override
	default boolean replace(K key, V oldValue, V newValue) {
		return getContextValue().replace(key, oldValue, newValue);
	}

	@Override
	default V replace(K key, V value) {
		return getContextValue().replace(key, value);
	}

	/**
	 * Creates the single context concurrent map.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the contextual map
	 */
	static <K, V> ContextualConcurrentMap<K, V> create() {
		return new ContextualReferenceConcurrentMap<>(CONTEXT_ID_SUPPLIER, ConcurrentHashMap::new);
	}

	/**
	 * Creates new hash map {@link ContextualConcurrentMap} instances using the given context id supplier.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @return the contextual map
	 */
	static <K, V> ContextualConcurrentMap<K, V> create(Supplier<String> contextIdSupplier) {
		return new ContextualReferenceConcurrentMap<>(contextIdSupplier, ConcurrentHashMap::new);
	}

	/**
	 * Creates {@link ContextualConcurrentMap} instance. The initial contextual value used will be created via the given
	 * initial value supplier. Note that the supplier should not return <code>null</code> or
	 * {@link NullPointerException} will be thrown.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param initialValue
	 *            the initial value
	 * @return the contextual map
	 */
	static <K, V> ContextualConcurrentMap<K, V> create(Supplier<String> contextIdSupplier,
			Supplier<ConcurrentMap<K, V>> initialValue) {
		return new ContextualReferenceConcurrentMap<>(contextIdSupplier, initialValue);
	}

	/**
	 * Some basic {@link ContextualConcurrentMap} implementation using a {@link ContextualReference}.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 */
	class ContextualReferenceConcurrentMap<K, V> extends ValidatingContextualReference<ConcurrentMap<K, V>>
			implements ContextualConcurrentMap<K, V> {

		/**
		 * Instantiates a new contextual reference concurrent map.
		 *
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param initialValue
		 *            the initial value
		 */
		public ContextualReferenceConcurrentMap(Supplier<String> contextIdSupplier,
				Supplier<ConcurrentMap<K, V>> initialValue) {
			super(contextIdSupplier, initialValue, (m) -> Objects.requireNonNull(m,
					"Tried to initialize with null value when non null initial value is required"));
		}
	}
}
