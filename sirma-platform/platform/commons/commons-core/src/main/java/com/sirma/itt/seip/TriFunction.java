/**
 *
 */
package com.sirma.itt.seip;

import java.util.Objects;
import java.util.function.Function;

/**
 * Function that accepts three arguments and produce a result
 *
 * @author BBonev
 * @param <X>
 *            the generic type
 * @param <Y>
 *            the generic type
 * @param <Z>
 *            the generic type
 * @param <R>
 *            the generic type
 */
@FunctionalInterface
public interface TriFunction<X, Y, Z, R> {

	/**
	 * Apply.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 * @return the result
	 */
	R apply(X x, Y y, Z z);

	/**
	 * Returns a composed function that first applies this function to its input, and then applies the {@code after}
	 * function to the result. If evaluation of either function throws an exception, it is relayed to the caller of the
	 * composed function.
	 *
	 * @param <V>
	 *            the type of output of the {@code after} function, and of the composed function
	 * @param after
	 *            the function to apply after this function is applied
	 * @return a composed function that first applies this function and then applies the {@code after} function
	 * @throws NullPointerException
	 *             if after is null
	 */
	default <V> TriFunction<X, Y, Z, V> andThen(Function<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (X x, Y y, Z z) -> after.apply(apply(x, y, z));
	}
}