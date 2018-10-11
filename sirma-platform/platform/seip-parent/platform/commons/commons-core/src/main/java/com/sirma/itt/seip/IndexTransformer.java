/**
 *
 */
package com.sirma.itt.seip;

/**
 * Function that accepts an int value and other argument and produces some other value. This function is intended for
 * array/collection transformations based on the index of the second argument. Mostly used when no other means are
 * available.
 *
 * @author BBonev
 * @param <T>
 *            the collection/array type
 * @param <R>
 *            the result type
 */
@FunctionalInterface
public interface IndexTransformer<T, R> {

	/**
	 * Apply the given function at the given index
	 *
	 * @param index
	 *            the index
	 * @param arg
	 *            the arg
	 * @return the transformed element
	 */
	R apply(int index, T arg);
}
