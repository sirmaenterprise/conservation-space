/**
 * Copyright (c) 2009 23.01.2009 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.utils.collections;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Hristo Iliev
 */
public final class CollectionUtils {
	/**
	 * Hide utility constructor
	 */
	private CollectionUtils() {
		// Hide default constructor
	}

	/**
	 * Create new {@link Iterable} object which combine the first and the second
	 * parameters.
	 * 
	 * @param <T>
	 *            Type of the items in the array of the first specified
	 *            parameter, this type also specify the type of the arrays of
	 *            the which will be returned by the returned {@link Iterable}
	 * @param <E>
	 *            Type of the second {@link Iterable} arrays. It may be the same
	 *            type as the first {@link Iterable}, or array of any other
	 *            subclass
	 * @param firstIterable
	 *            {@link Iterable}, the first {@link Iterable} which contains
	 *            items
	 * @param secondIterable
	 *            {@link Iterable}, the second {@link Iterable} which contains
	 *            items
	 * @return {@link Iterable}, object which will return {@link Iterator}s
	 *         which will iterate over the second {@link Iterable} parameter as
	 *         many times as there are items in the first {@link Iterable}
	 *         parameter. The returned arrays will contains every possible
	 *         combination between items in the first {@link Iterable}, and the
	 *         second {@link Iterable}. For example: if the first
	 *         {@link Iterable} contains <code>{1, 2}</code> and the second
	 *         {@link Iterable} contains <code>{'a', 'b', 'c'}</code> the
	 *         returned {@link Iterable} object will dynamically combine the
	 *         {@link Iterable}s and provide following combination in specified
	 *         order:
	 *         <code>{1, 'a'}, {1, 'b'}, {1, 'c'}, {2, 'a'}, {2, 'b'}, {2, 'c'}</code>
	 */
	public static <T, E extends T> Iterable<T[]> combineIterables(
			final Iterable<T[]> firstIterable,
			final Iterable<E[]> secondIterable) {
		return new Iterable<T[]>() {

			/**
			 * {@inheritDoc}
			 */
			public Iterator<T[]> iterator() {

				return new Iterator<T[]>() {
					private Iterator<T[]> firstArrayIterator = firstIterable
							.iterator();
					private final Iterator<E[]> secondArrayIterator = secondIterable
							.iterator();
					private T[] appendArray = secondArrayIterator.next();

					/**
					 * {@inheritDoc}
					 */
					public boolean hasNext() {
						return firstArrayIterator.hasNext()
								|| secondArrayIterator.hasNext();
					}

					/**
					 * {@inheritDoc}
					 */
					public T[] next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						if (!firstArrayIterator.hasNext()) {
							firstArrayIterator = firstIterable.iterator();
							appendArray = secondArrayIterator.next();
						}
						T[] streamsItem = firstArrayIterator.next();
						@SuppressWarnings("unchecked")
						T[] result = (T[]) Array.newInstance(streamsItem
								.getClass().getComponentType(),
								streamsItem.length + appendArray.length);
						System.arraycopy(streamsItem, 0, result, 0,
								streamsItem.length);
						System.arraycopy(appendArray, 0, result,
								streamsItem.length, appendArray.length);
						return result;
					}

					/**
					 * {@inheritDoc}
					 */
					public void remove() {
						throw new UnsupportedOperationException();
					}

				};
			}
		};
	}

	/**
	 * Finds the index where an object should be inserted in a collection based
	 * on the following algorithm: if the list is empty the insertion index is 0 <br/>
	 * If equal element exists, the insertion index is the index of the existing
	 * element+1. If multiple equal elements exist, the insertion index is after
	 * the elements.
	 * 
	 * @param <E>
	 *            type of the object
	 * @param list
	 *            list where the element should be inserted.
	 * @param value
	 *            element that should be inserted.
	 * @param comparator
	 *            comparator used to compare objects
	 * @return index (place) where the element should be inserted.
	 */
	public static <E> int findInsertionIndex(List<E> list, E object,
			Comparator<E> comparator) {
		// call binary search
		int binarySearchResult = Collections.binarySearch(list, object,
				comparator);

		int index;
		if (binarySearchResult < 0) {
			// if the result is negative turn it to positive and decrease it by
			// one (see binarySearch doc)
			index = Math.abs(binarySearchResult) - 1;
		} else {
			// if the result is positive, increase it by one
			index = binarySearchResult + 1;

			// if there are multiple equal elements iterate to find the latest
			while (index < list.size()
					&& comparator.compare(list.get(index), object) == 0) {
				index++;
			}
		}

		return index;
	}

	/**
	 * Constructs a comparator and calls findInsertionIndex(List<E> , E,
	 * Comparator<E>) to find the insertion index.
	 * 
	 * @param <E>
	 *            type of the object
	 * @param list
	 *            list where the element should be inserted.
	 * @param value
	 *            element that should be inserted.
	 * @return index (place) where the element should be inserted.
	 */
	public static <E extends Comparable<E>> int findInsertionIndex(
			List<E> list, E object) {
		Comparator<E> comparator = new Comparator<E>() {

			@Override
			public int compare(E o1, E o2) {
				return o1.compareTo(o2);
			}
		};

		return findInsertionIndex(list, object, comparator);
	}

}
