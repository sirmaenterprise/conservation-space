/**
 *
 */
package com.sirma.itt.seip.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

/**
 * List implementation of the {@link BaseLazyCollection}.
 *
 * @author BBonev
 * @param <E>
 *            the element type
 * @see BaseLazyCollection
 */
public class LazyList<E> extends BaseLazyCollection<E, List<E>>implements List<E> {

	private static final long serialVersionUID = 4331111914202849149L;

	/**
	 * Instantiates a new empty lazy list.
	 */
	public LazyList() {
		this(null, LinkedList::new);
	}

	/**
	 * Instantiates a new lazy list with null initial value
	 *
	 * @param expandedSource
	 *            A supplier that should provide the rest of the elements for the collection data. it should be mutable
	 *            collection. If <code>null</code> or empty then the empty supplier will be called to initialize the
	 *            store.
	 */
	public LazyList(Supplier<List<E>> expandedSource) {
		this(null, expandedSource);
	}

	/**
	 * Instantiates a new lazy list.
	 *
	 * @param initial
	 *            the initial value to provide if any
	 * @param expandedSource
	 *            A supplier that should provide the rest of the elements for the collection data. it should be mutable
	 *            collection. If <code>null</code> or empty then the empty supplier will be called to initialize the
	 *            store.
	 */
	public LazyList(E initial, Supplier<List<E>> expandedSource) {
		super(initial, expandedSource, LinkedList::new, LazyList::listMerge);
	}

	/**
	 * Instantiates a new lazy list from other lazy list.
	 *
	 * @param copyFrom
	 *            the copy from
	 */
	public LazyList(LazyList<E> copyFrom) {
		super(copyFrom);
	}

	/**
	 * Instantiates a new lazy list from other lazy list.
	 *
	 * @param initial
	 *            the initial
	 * @param copyFrom
	 *            the copy from
	 */
	public LazyList(E initial, List<E> copyFrom) {
		this(initial, () -> copyFrom(copyFrom));
	}

	/**
	 * Merges the value and the list by replacing the list element with the given one
	 *
	 * @param <E>
	 *            the element type
	 * @param value
	 *            the value
	 * @param list
	 *            the list
	 * @return the list
	 */
	private static <E> List<E> listMerge(E value, List<E> list) {
		if (value != null) {
			int indexOf = list.indexOf(value);
			if (indexOf > 0) {
				list.set(indexOf, value);
			}
		}
		return list;
	}


	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return getStore().addAll(0, c);
	}

	@Override
	public E get(int index) {
		return getStore().get(index);
	}

	@Override
	public E set(int index, E element) {
		return getStore().set(index, element);
	}

	@Override
	public void add(int index, E element) {
		getStore().add(index, element);
	}

	@Override
	public E remove(int index) {
		return getStore().remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return getStore().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return getStore().lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return getStore().listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return getStore().listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return getStore().subList(fromIndex, toIndex);
	}

	/**
	 * Copy the current collection into new one. If the source collection is {@link LazyList} then new {@link LazyList}
	 * collection will be returned.
	 *
	 * @param <T>
	 *            the generic type
	 * @param list
	 *            the source list to copy
	 * @return new lazy list or new {@link ArrayList} that holds all the elements of the current collection.
	 */
	public static <T> List<T> copyFrom(List<T> list) {
		if (list == null) {
			return new ArrayList<>(0);
		}
		if (list instanceof LazyList) {
			return new LazyList<>((LazyList<T>) list);
		}
		return new ArrayList<>(list);
	}

}
