/**
 *
 */
package com.sirma.itt.seip;

/**
 * Instances that implement this interface must support internal state reset by implementing the {@link #reset()}
 * method.
 *
 * @author BBonev
 */
@FunctionalInterface
public interface Resettable {

	/**
	 * Reset the internal state.
	 */
	void reset();

	/**
	 * If the given argument is of type {@link Resettable} then the {@link #reset()} method is called.
	 *
	 * @param <T>
	 *            the generic type
	 * @param toReset
	 *            the to reset
	 * @return the input value
	 */
	static <T> T reset(T toReset) {
		if (toReset instanceof Resettable) {
			((Resettable) toReset).reset();
		}
		return toReset;
	}
}
