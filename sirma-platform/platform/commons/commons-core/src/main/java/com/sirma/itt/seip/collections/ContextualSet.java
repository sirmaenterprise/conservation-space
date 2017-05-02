/**
 *
 */
package com.sirma.itt.seip.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.context.ValidatingContextualReference;

/**
 * Contextual {@link Set}.
 *
 * @author BBonev
 * @param <V>
 *            the value type
 */
public interface ContextualSet<V> extends Contextual<Set<V>>, Set<V> {

	@Override
	default int size() {
		return getContextValue().size();
	}

	@Override
	default boolean isEmpty() {
		return getContextValue().isEmpty();
	}

	@Override
	default boolean contains(Object o) {
		return getContextValue().contains(o);
	}

	@Override
	default Iterator<V> iterator() {
		return getContextValue().iterator();
	}

	@Override
	default Object[] toArray() {
		return getContextValue().toArray();
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return getContextValue().toArray(a);
	}

	@Override
	default boolean add(V e) {
		return getContextValue().add(e);
	}

	@Override
	default boolean remove(Object o) {
		return getContextValue().remove(o);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return getContextValue().containsAll(c);
	}

	@Override
	default boolean addAll(Collection<? extends V> c) {
		return getContextValue().addAll(c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return getContextValue().retainAll(c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return getContextValue().removeAll(c);
	}

	@Override
	default void clear() {
		getContextValue().clear();
	}

	@Override
	default void forEach(Consumer<? super V> action) {
		getContextValue().forEach(action);
	}

	@Override
	default Stream<V> parallelStream() {
		return getContextValue().parallelStream();
	}

	@Override
	default Spliterator<V> spliterator() {
		return getContextValue().spliterator();
	}

	@Override
	default Stream<V> stream() {
		return getContextValue().stream();
	}

	@Override
	default boolean removeIf(Predicate<? super V> filter) {
		return getContextValue().removeIf(filter);
	}

	/**
	 * Creates new hash set {@link ContextualSet} instance with single context.
	 *
	 * @param <V>
	 *            the value type
	 * @return the contextual map
	 */
	static <V> ContextualSet<V> create() {
		return new ContextualReferenceSet<>(CONTEXT_ID_SUPPLIER, HashSet::new);
	}

	/**
	 * Creates new hash set {@link ContextualSet} instances using the given context id supplier.
	 *
	 * @param <V>
	 *            the value type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @return the contextual map
	 */
	static <V> ContextualSet<V> create(Supplier<String> contextIdSupplier) {
		return new ContextualReferenceSet<>(contextIdSupplier, HashSet::new);
	}

	/**
	 * Creates {@link ContextualSet} instance. The initial contextual value used will be created via the given initial
	 * value supplier. Note that the supplier should not return <code>null</code> or {@link NullPointerException} will
	 * be thrown.
	 *
	 * @param <V>
	 *            the value type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param initialValue
	 *            the initial value
	 * @return the contextual map
	 */
	static <V> ContextualSet<V> create(Supplier<String> contextIdSupplier, Supplier<Set<V>> initialValue) {
		return new ContextualReferenceSet<>(contextIdSupplier, initialValue);
	}

	/**
	 * Some basic {@link ContextualSet} implementation using a {@link ContextualReference}.
	 *
	 * @param <V>
	 *            the value type
	 */
	class ContextualReferenceSet<V> extends ValidatingContextualReference<Set<V>>implements ContextualSet<V> {

		/**
		 * Instantiates a new contextual reference set.
		 *
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param initialValue
		 *            the initial value
		 */
		public ContextualReferenceSet(Supplier<String> contextIdSupplier, Supplier<Set<V>> initialValue) {
			super(contextIdSupplier, initialValue, (s) -> Objects.requireNonNull(s,
					"Tried to initialize with null value when non null initial value is required"));
		}
	}
}
