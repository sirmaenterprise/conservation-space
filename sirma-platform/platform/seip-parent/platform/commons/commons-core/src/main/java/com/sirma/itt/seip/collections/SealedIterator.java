package com.sirma.itt.seip.collections;

import java.util.Iterator;

import com.sirma.itt.seip.Sealable;

/**
 * Sealable {@link Iterator}. When sealed the collection does not allow data modifications. Any call to method that
 * modifies the content will be ignored without exception.
 *
 * @author BBonev
 * @param <I>
 *            the iterator type
 * @param <E>
 *            the iterator element type
 */
public class SealedIterator<E, I extends Iterator<E>> implements Iterator<E>, Sealable {

	/** The iterator. */
	private final I iterator;
	/** The seal. */
	private boolean seal;

	/**
	 * Instantiates a new already sealed iterator.
	 *
	 * @param it
	 *            the it
	 */
	public SealedIterator(I it) {
		this(it, true);
	}

	/**
	 * Instantiates a new iterator that is can be sealed later
	 *
	 * @param it
	 *            the it
	 * @param sealNow
	 *            the seal now
	 */
	public SealedIterator(I it, boolean sealNow) {
		this.iterator = it;
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
	public boolean hasNext() {
		return getIterator().hasNext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E next() {
		return getIterator().next();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		if (isSealed()) {
			return;
		}
		getIterator().remove();
	}

	/**
	 * Getter method for iterator.
	 *
	 * @return the iterator
	 */
	protected I getIterator() {
		return iterator;
	}

}
