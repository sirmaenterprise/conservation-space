package com.sirma.itt.seip.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.sirma.itt.seip.Sealable;

/**
 * Generic implementation of {@link Collection} and {@link Sealable} interfaces to implement immutable collections. One
 * sealed the instance ignores all request for modification via method of the current instance or objects that represent
 * the current collection like {@link Iterator}s. Once the collections is sealed the returned iterators will also be
 * sealed to prevent modification on the original collection.
 *
 * @author BBonev
 * @param <C>
 *            the collection type
 * @param <E>
 *            the element type
 */
public class SealedCollection<E, C extends Collection<E>> implements Collection<E>, Sealable, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6253459156369172116L;
	/** The value. */
	private final C value;
	/** The seal. */
	private boolean seal;

	/**
	 * Instantiates a new already sealed collection.
	 *
	 * @param collection
	 *            the collection
	 */
	public SealedCollection(C collection) {
		this(collection, true);
	}

	/**
	 * Instantiates a new collection that can be sealed later.
	 *
	 * @param collection
	 *            the collection
	 * @param sealNow
	 *            the seal now
	 */
	@SuppressWarnings("unchecked")
	public SealedCollection(C collection, boolean sealNow) {
		this.value = collection == null ? (C) Collections.emptyList() : collection;
		this.seal = sealNow;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSealed() {
		return seal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void seal() {
		seal = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return getValue().size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return getValue().isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Object o) {
		return getValue().contains(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() {
		return new SealedIterator<>(getValue().iterator(), isSealed());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return getValue().toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return getValue().toArray(a);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean add(E e) {
		if (isSealed()) {
			return false;
		}
		return getValue().add(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean remove(Object o) {
		if (isSealed()) {
			return false;
		}
		return getValue().remove(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return getValue().containsAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (isSealed()) {
			return false;
		}
		return getValue().addAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		if (isSealed()) {
			return false;
		}
		return getValue().retainAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		if (isSealed()) {
			return false;
		}
		return getValue().removeAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		if (isSealed()) {
			return;
		}
		getValue().clear();
	}

	/**
	 * Getter method for value.
	 *
	 * @return the value
	 */
	protected C getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getValue().toString();
	}
}
