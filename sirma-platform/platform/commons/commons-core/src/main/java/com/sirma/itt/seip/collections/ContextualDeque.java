package com.sirma.itt.seip.collections;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Supplier;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ValidatingContextualReference;

/**
 * Defines a contextual {@link Deque} definition.
 *
 * @param <E>
 *            the element type
 * @author BBonev
 * @see Contextual
 * @see Deque
 */
public interface ContextualDeque<E> extends Deque<E>, Contextual<Deque<E>> {
	@Override
	default void addLast(E e) {
		getContextValue().addLast(e);
	}

	@Override
	default void addFirst(E e) {
		getContextValue().addFirst(e);
	}

	@Override
	default boolean offerLast(E e) {
		return getContextValue().offerLast(e);
	}

	@Override
	default boolean offerFirst(E e) {
		return getContextValue().offerFirst(e);
	}

	@Override
	default E removeLast() {
		return getContextValue().removeLast();
	}

	@Override
	default E removeFirst() {
		return getContextValue().removeFirst();
	}

	@Override
	default E pollFirst() {
		return getContextValue().pollFirst();
	}

	@Override
	default E pollLast() {
		return getContextValue().pollLast();
	}

	@Override
	default E getLast() {
		return getContextValue().getLast();
	}

	@Override
	default E getFirst() {
		return getContextValue().getFirst();
	}

	@Override
	default E peekLast() {
		return getContextValue().peekLast();
	}

	@Override
	default E peekFirst() {
		return getContextValue().peekFirst();
	}

	@Override
	default boolean removeLastOccurrence(Object o) {
		return getContextValue().removeLastOccurrence(o);
	}

	// *** Queue methods ***

	@Override
	default boolean removeFirstOccurrence(Object o) {
		return getContextValue().removeFirstOccurrence(o);
	}

	@Override
	default boolean offer(E e) {
		return getContextValue().offer(e);
	}

	// *** Queue methods ***
	
	@Override
	default boolean add(E e) {
		return getContextValue().add(e);
	}

	@Override
	default E poll() {
		return getContextValue().poll();
	}

	@Override
	default E remove() {
		return getContextValue().remove();
	}

	@Override
	default E element() {
		return getContextValue().element();
	}

	@Override
	default E peek() {
		return getContextValue().peek();
	}

	// *** Stack methods ***

	@Override
	default E pop() {
		return getContextValue().pop();
	}

	// *** Collection methods ***

	// *** Stack methods ***
	
	@Override
	default void push(E e) {
		getContextValue().push(e);
	}

	@Override
	default boolean contains(Object o) {
		return getContextValue().contains(o);
	}

	@Override
	default boolean remove(Object o) {
		return getContextValue().remove(o);
	}

	@Override
	default int size() {
		return getContextValue().size();
	}

	@Override
	default Iterator<E> descendingIterator() {
		return getContextValue().descendingIterator();
	}

	@Override
	default Iterator<E> iterator() {
		return getContextValue().iterator();
	}

	@Override
	default boolean isEmpty() {
		return getContextValue().isEmpty();
	}

	@Override
	default <T> T[] toArray(T[] a) {
		return getContextValue().toArray(a);
	}

	@Override
	default Object[] toArray() {
		return getContextValue().toArray();
	}

	@Override
	default boolean addAll(Collection<? extends E> c) {
		return getContextValue().addAll(c);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return getContextValue().containsAll(c);
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

	/**
	 * Creates the single context dequeue backed by {@link LinkedList}.
	 *
	 * @param <E>
	 *            the element type
	 * @return the contextual dequeue
	 */
	static <E> ContextualDeque<E> create() {
		return new ContextualReferenceDeque<>(CONTEXT_ID_SUPPLIER, LinkedList::new);
	}

	/**
	 * Creates new linked list {@link ContextualDeque} instance using the given context id supplier.
	 *
	 * @param <E>
	 *            the element type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @return the contextual dequeue
	 */
	static <E> ContextualDeque<E> create(Supplier<String> contextIdSupplier) {
		return new ContextualReferenceDeque<>(contextIdSupplier, LinkedList::new);
	}

	/**
	 * Creates {@link ContextualConcurrentMap} instance. The initial contextual value used will be created via the given
	 * initial value supplier. Note that the supplier should not return <code>null</code> or
	 * {@link NullPointerException} will be thrown.
	 *
	 * @param <E>
	 *            the element type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @param initialValue
	 *            the initial value
	 * @return the contextual dequeue
	 */
	static <E> ContextualDeque<E> create(Supplier<String> contextIdSupplier, Supplier<Deque<E>> initialValue) {
		return new ContextualReferenceDeque<>(contextIdSupplier, initialValue);
	}

	/**
	 * Some basic {@link ContextualDeque} implementation using a {@link ValidatingContextualReference}.
	 *
	 * @param <E>
	 *            the element type
	 * @author BBonev
	 */
	class ContextualReferenceDeque<E> extends ValidatingContextualReference<Deque<E>>implements ContextualDeque<E> {

		/**
		 * Instantiates a new contextual reference dequeue.
		 *
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param initialValue
		 *            the initial value
		 */
		public ContextualReferenceDeque(Supplier<String> contextIdSupplier, Supplier<Deque<E>> initialValue) {
			super(contextIdSupplier, initialValue, dq -> Objects.requireNonNull(dq,
					"Tried to initialize with null value when non null initial value is required"));
		}
	}
}
