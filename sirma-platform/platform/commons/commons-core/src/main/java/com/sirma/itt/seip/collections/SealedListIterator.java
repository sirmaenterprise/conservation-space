package com.sirma.itt.seip.collections;

import java.util.ListIterator;

/**
 * Sealable {@link ListIterator}. When sealed the collection does not allow data modifications. Any call to methods that
 * modifies the content will be ignored without exception.
 *
 * @author BBonev
 * @param <E>
 *            the iterator element type
 */
public class SealedListIterator<E> extends SealedIterator<E, ListIterator<E>>implements ListIterator<E> {

	/**
	 * Instantiates a new already sealed list iterator.
	 *
	 * @param it
	 *            the it
	 */
	public SealedListIterator(ListIterator<E> it) {
		super(it);
	}

	/**
	 * Instantiates a new list iterator that can be sealed later.
	 *
	 * @param it
	 *            the it
	 * @param sealNow
	 *            the seal now
	 */
	public SealedListIterator(ListIterator<E> it, boolean sealNow) {
		super(it, sealNow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasPrevious() {
		return getIterator().hasPrevious();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E previous() {
		return getIterator().previous();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int nextIndex() {
		return getIterator().nextIndex();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int previousIndex() {
		return getIterator().previousIndex();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(E e) {
		if (isSealed()) {
			return;
		}
		getIterator().set(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(E e) {
		if (isSealed()) {
			return;
		}
		getIterator().add(e);
	}

}
