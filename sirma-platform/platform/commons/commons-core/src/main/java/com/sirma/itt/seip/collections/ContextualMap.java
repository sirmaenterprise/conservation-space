/**
 *
 */
package com.sirma.itt.seip.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.context.ValidatingContextualReference;

/**
 * Defines a contextual {@link Map} definition.
 *
 * @author BBonev
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
public interface ContextualMap<K, V> extends Contextual<Map<K, V>>, Map<K, V> {

	@Override
	default int size() {
		return getContextValue().size();
	}

	@Override
	default boolean isEmpty() {
		return getContextValue().isEmpty();
	}

	@Override
	default boolean containsKey(Object key) {
		return getContextValue().containsKey(key);
	}

	@Override
	default boolean containsValue(Object value) {
		return getContextValue().containsValue(value);
	}

	@Override
	default V get(Object key) {
		return getContextValue().get(key);
	}

	@Override
	default V put(K key, V value) {
		return getContextValue().put(key, value);
	}

	@Override
	default V remove(Object key) {
		return getContextValue().remove(key);
	}

	@Override
	default void putAll(Map<? extends K, ? extends V> m) {
		getContextValue().putAll(m);
	}

	@Override
	default void clear() {
		getContextValue().clear();
	}

	@Override
	default Set<K> keySet() {
		return getContextValue().keySet();
	}

	@Override
	default Collection<V> values() {
		return getContextValue().values();
	}

	@Override
	default Set<java.util.Map.Entry<K, V>> entrySet() {
		return getContextValue().entrySet();
	}

	/**
	 * Creates the single context map.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the contextual map
	 */
	static <K, V> ContextualMap<K, V> create() {
		return new ContextualReferenceMap<>(CONTEXT_ID_SUPPLIER, HashMap::new);
	}

	/**
	 * Creates new hash map {@link ContextualMap} instances using the given context id supplier.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @return the contextual map
	 */
	static <K, V> ContextualMap<K, V> create(Supplier<String> contextIdSupplier) {
		return new ContextualReferenceMap<>(contextIdSupplier, HashMap::new);
	}

	/**
	 * Creates {@link ContextualMap} instance. The initial contextual value used will be created via the given initial
	 * value supplier. Note that the supplier should not return <code>null</code> or {@link NullPointerException} will
	 * be thrown.
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
	static <K, V> ContextualMap<K, V> create(Supplier<String> contextIdSupplier, Supplier<Map<K, V>> initialValue) {
		return new ContextualReferenceMap<>(contextIdSupplier, initialValue);
	}

	/**
	 * Some basic {@link ContextualMap} implementation using a {@link ContextualReference}.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 */
	class ContextualReferenceMap<K, V> extends ValidatingContextualReference<Map<K, V>>implements ContextualMap<K, V> {

		/**
		 * Instantiates a new contextual reference map.
		 *
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param initialValue
		 *            the initial value
		 */
		public ContextualReferenceMap(Supplier<String> contextIdSupplier, Supplier<Map<K, V>> initialValue) {
			super(contextIdSupplier, initialValue, (m) -> Objects.requireNonNull(m,
					"Tried to initialize with null value when non null initial value is required"));
		}
	}
}
