/**
 *
 */
package com.sirma.itt.seip.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ValidatingContextualReference;

/**
 * Contextual implementation for {@link List}.
 *
 * @param <T>
 *            the element type
 * @author BBonev
 */
public interface ContextualList<T> extends Contextual<List<T>>, List<T> {

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
	default Iterator<T> iterator() {
		return getContextValue().iterator();
	}

	@Override
	default Object[] toArray() {
		return getContextValue().toArray();
	}

	@Override
	default <E> E[] toArray(E[] a) {
		return getContextValue().toArray(a);
	}

	@Override
	default boolean add(T e) {
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
	default boolean addAll(Collection<? extends T> c) {
		return getContextValue().addAll(c);
	}

	@Override
	default boolean addAll(int index, Collection<? extends T> c) {
		return getContextValue().addAll(index, c);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return getContextValue().removeAll(c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return getContextValue().retainAll(c);
	}

	@Override
	default void clear() {
		getContextValue().clear();
	}

	@Override
	default T get(int index) {
		return getContextValue().get(index);
	}

	@Override
	default T set(int index, T element) {
		return getContextValue().set(index, element);
	}

	@Override
	default void add(int index, T element) {
		getContextValue().add(index, element);
	}

	@Override
	default T remove(int index) {
		return getContextValue().remove(index);
	}

	@Override
	default int indexOf(Object o) {
		return getContextValue().indexOf(o);
	}

	@Override
	default int lastIndexOf(Object o) {
		return getContextValue().lastIndexOf(o);
	}

	@Override
	default ListIterator<T> listIterator() {
		return getContextValue().listIterator();
	}

	@Override
	default ListIterator<T> listIterator(int index) {
		return getContextValue().listIterator(index);
	}

	@Override
	default List<T> subList(int fromIndex, int toIndex) {
		return getContextValue().subList(fromIndex, toIndex);
	}

	@Override
	default Spliterator<T> spliterator() {
		return Spliterators.spliterator(getContextValue(), 0);
	}

	/**
	 * Creates the single context map.
	 *
	 * @param <E>
	 *            the element type
	 * @return the contextual list
	 */
	static <E> ContextualList<E> create() {
		return create(CONTEXT_ID_SUPPLIER);
	}

	/**
	 * Creates new linked list {@link ContextualList} instances using the given context id supplier.
	 *
	 * @param <E>
	 *            the element type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @return the contextual list
	 */
	static <E> ContextualList<E> create(Supplier<String> contextIdSupplier) {
		return new ContextualReferenceList<>(contextIdSupplier, LinkedList::new);
	}

	/**
	 * Creates {@link ContextualList} instance. The initial contextual value used will be created via the given initial
	 * value supplier. Note that the supplier should not return <code>null</code> or {@link NullPointerException} will
	 * be thrown.
	 *
	 * @param <E>
	 *            the element type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param initialValue
	 *            the initial value
	 * @return the contextual list
	 */
	static <E> ContextualList<E> create(Supplier<String> contextIdSupplier, Supplier<List<E>> initialValue) {
		return new ContextualReferenceList<>(contextIdSupplier, initialValue);
	}

	/**
	 * Some basic {@link ContextualList} implementation using a {@link ValidatingContextualReference}.
	 *
	 * @param <E>
	 *            the element type
	 */
	class ContextualReferenceList<E> extends ValidatingContextualReference<List<E>>implements ContextualList<E> {

		/**
		 * Instantiates a new contextual reference map.
		 *
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param initialValue
		 *            the initial value
		 */
		public ContextualReferenceList(Supplier<String> contextIdSupplier, Supplier<List<E>> initialValue) {
			super(contextIdSupplier, initialValue, (l) -> Objects.requireNonNull(l,
					"Tried to initialize with null value when non null initial value is required"));
		}
	}

}
