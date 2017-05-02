package com.sirma.itt.seip.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Sealable {@link List}. When sealed the collection does not allow data modifications. Any call to methods that
 * modifies the content will be ignored without exception.
 *
 * @author BBonev
 * @param <E>
 *            the element type
 */
public class SealedList<E> extends SealedCollection<E, List<E>>implements List<E> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 332296875699468358L;

	/**
	 * Instantiates a new already sealed list.
	 *
	 * @param list
	 *            the list
	 */
	public SealedList(List<E> list) {
		this(list, true);
	}

	/**
	 * Instantiates a new list that can be sealed later.
	 *
	 * @param list
	 *            the list
	 * @param sealNow
	 *            the seal now
	 */
	public SealedList(List<E> list, boolean sealNow) {
		super(list == null ? Collections.<E> emptyList() : list, sealNow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		if (isSealed()) {
			return false;
		}
		return getValue().addAll(index, c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E get(int index) {
		return getValue().get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E set(int index, E element) {
		if (isSealed()) {
			return null;
		}
		return getValue().set(index, element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(int index, E element) {
		if (isSealed()) {
			return;
		}
		getValue().add(index, element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E remove(int index) {
		if (isSealed()) {
			return null;
		}
		return getValue().remove(index);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int indexOf(Object o) {
		return getValue().indexOf(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int lastIndexOf(Object o) {
		return getValue().lastIndexOf(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListIterator<E> listIterator() {
		return new SealedListIterator<>(getValue().listIterator(), isSealed());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListIterator<E> listIterator(int index) {
		return new SealedListIterator<>(getValue().listIterator(index), isSealed());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new SealedList<>(getValue().subList(fromIndex, toIndex), isSealed());
	}

}
