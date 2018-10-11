/**
 *
 */
package com.sirma.itt.seip;

import java.util.Objects;

/**
 * Consumer that accepts three arguments.
 *
 * @author BBonev
 * @param <X>
 *            the generic type
 * @param <Y>
 *            the generic type
 * @param <Z>
 *            the generic type
 */
@FunctionalInterface
public interface TriConsumer<X, Y, Z> {

	/**
	 * Accept.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	void accept(X x, Y y, Z z);

	/**
	 * Returns a composed {@code TriConsumer} that performs, in sequence, this operation followed by the {@code after}
	 * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
	 * operation. If performing this operation throws an exception, the {@code after} operation will not be performed.
	 *
	 * @param after
	 *            the operation to perform after this operation
	 * @return a composed {@code TriConsumer} that performs in sequence this operation followed by the {@code after}
	 *         operation
	 * @throws NullPointerException
	 *             if {@code after} is null
	 */
	default TriConsumer<X, Y, Z> andThen(TriConsumer<? super X, ? super Y, ? super Z> after) {
		Objects.requireNonNull(after);

		return (x, y, z) -> {
			accept(x, y, z);
			after.accept(x, y, z);
		};
	}
}