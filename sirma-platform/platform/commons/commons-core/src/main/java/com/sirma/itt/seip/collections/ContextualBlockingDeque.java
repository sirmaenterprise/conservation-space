package com.sirma.itt.seip.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ValidatingContextualReference;

/**
 * Defines a contextual {@link BlockingDeque} definition.
 *
 * @param <E>
 *            the element type
 * @author BBonev
 * @see Contextual
 * @see BlockingDeque
 */
public interface ContextualBlockingDeque<E> extends BlockingDeque<E>, Contextual<BlockingDeque<E>> {

	@Override
	default void putFirst(E e) throws InterruptedException {
		getContextValue().putFirst(e);
	}

	@Override
	default void putLast(E e) throws InterruptedException {
		getContextValue().putLast(e);
	}

	@Override
	default boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return getContextValue().offerFirst(e, timeout, unit);
	}

	@Override
	default boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return getContextValue().offerLast(e, timeout, unit);
	}

	@Override
	default E takeFirst() throws InterruptedException {
		return getContextValue().takeFirst();
	}

	@Override
	default E takeLast() throws InterruptedException {
		return getContextValue().takeLast();
	}

	@Override
	default E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
		return getContextValue().pollFirst(timeout, unit);
	}

	@Override
	default E pollLast(long timeout, TimeUnit unit) throws InterruptedException {
		return getContextValue().pollLast(timeout, unit);
	}

	@Override
	default void put(E e) throws InterruptedException {
		getContextValue().put(e);
	}

	@Override
	default boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return getContextValue().offer(e, timeout, unit);
	}

	@Override
	default E take() throws InterruptedException {
		return getContextValue().take();
	}

	@Override
	default E poll(long timeout, TimeUnit unit) throws InterruptedException {
		return getContextValue().poll(timeout, unit);
	}

	@Override
	default int remainingCapacity() {
		return getContextValue().remainingCapacity();
	}

	@Override
	default int drainTo(Collection<? super E> c) {
		return getContextValue().drainTo(c);
	}

	@Override
	default int drainTo(Collection<? super E> c, int maxElements) {
		return getContextValue().drainTo(c, maxElements);
	}

	// *** Deque methods ***

	@Override
	default void addFirst(E e) {
		getContextValue().addFirst(e);
	}

	@Override
	default void addLast(E e) {
		getContextValue().addLast(e);
	}

	@Override
	default boolean offerFirst(E e) {
		return getContextValue().offerFirst(e);
	}

	@Override
	default boolean offerLast(E e) {
		return getContextValue().offerLast(e);
	}

	@Override
	default E removeFirst() {
		return getContextValue().removeFirst();
	}

	@Override
	default E removeLast() {
		return getContextValue().removeLast();
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
	default E getFirst() {
		return getContextValue().getFirst();
	}

	@Override
	default E getLast() {
		return getContextValue().getLast();
	}

	@Override
	default E peekFirst() {
		return getContextValue().peekFirst();
	}

	@Override
	default E peekLast() {
		return getContextValue().peekLast();
	}

	@Override
	default boolean removeFirstOccurrence(Object o) {
		return getContextValue().removeFirstOccurrence(o);
	}

	@Override
	default boolean removeLastOccurrence(Object o) {
		return getContextValue().removeLastOccurrence(o);
	}

	// *** Queue methods ***

	@Override
	default boolean add(E e) {
		return getContextValue().add(e);
	}

	@Override
	default boolean offer(E e) {
		return getContextValue().offer(e);
	}

	@Override
	default E remove() {
		return getContextValue().remove();
	}

	@Override
	default E poll() {
		return getContextValue().poll();
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
	default void push(E e) {
		getContextValue().push(e);
	}

	@Override
	default E pop() {
		return getContextValue().pop();
	}

	// *** Collection methods ***

	@Override
	default boolean remove(Object o) {
		return getContextValue().remove(o);
	}

	@Override
	default boolean contains(Object o) {
		return getContextValue().contains(o);
	}

	@Override
	default int size() {
		return getContextValue().size();
	}

	@Override
	default Iterator<E> iterator() {
		return getContextValue().iterator();
	}

	@Override
	default Iterator<E> descendingIterator() {
		return getContextValue().descendingIterator();
	}

	@Override
	default boolean isEmpty() {
		return getContextValue().isEmpty();
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
	default boolean containsAll(Collection<?> c) {
		return getContextValue().containsAll(c);
	}

	@Override
	default boolean addAll(Collection<? extends E> c) {
		return getContextValue().addAll(c);
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

	/**
	 * Creates the single context dequeue backed by {@link LinkedList}.
	 *
	 * @param <E>
	 *            the element type
	 * @return the contextual dequeue
	 */
	static <E> ContextualBlockingDeque<E> create() {
		return new ContextualReferenceBlockingDeque<>(CONTEXT_ID_SUPPLIER, LinkedBlockingDeque::new);
	}

	/**
	 * Creates new linked list {@link ContextualBlockingDeque} instance using the given context id supplier.
	 *
	 * @param <E>
	 *            the element type
	 * @param contextIdSupplier
	 *            the context id supplier
	 * @return the contextual dequeue
	 */
	static <E> ContextualBlockingDeque<E> create(Supplier<String> contextIdSupplier) {
		return new ContextualReferenceBlockingDeque<>(contextIdSupplier, LinkedBlockingDeque::new);
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
	static <E> ContextualBlockingDeque<E> create(Supplier<String> contextIdSupplier,
			Supplier<BlockingDeque<E>> initialValue) {
		return new ContextualReferenceBlockingDeque<>(contextIdSupplier, initialValue);
	}

	/**
	 * Some basic {@link ContextualBlockingDeque} implementation using a {@link ValidatingContextualReference}.
	 *
	 * @param <E>
	 *            the element type
	 * @author BBonev
	 */
	class ContextualReferenceBlockingDeque<E> extends ValidatingContextualReference<BlockingDeque<E>>
			implements ContextualBlockingDeque<E> {

		/**
		 * Instantiates a new contextual reference dequeue.
		 *
		 * @param contextIdSupplier
		 *            the context id supplier
		 * @param initialValue
		 *            the initial value
		 */
		public ContextualReferenceBlockingDeque(Supplier<String> contextIdSupplier,
				Supplier<BlockingDeque<E>> initialValue) {
			super(contextIdSupplier, initialValue, dq -> Objects.requireNonNull(dq,
					"Tried to initialize with null value when non null initial value is required"));
		}
	}
}
