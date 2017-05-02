/**
 *
 */
package com.sirma.itt.seip;

/**
 * Destroyable interface defines a method that when called the internal resources should be cleared.
 *
 * @author BBonev
 */
@FunctionalInterface
public interface Destroyable {

	/**
	 * Calling the destroy method will call any destroy callback registered and reset the internal state.
	 */
	void destroy();

	/**
	 * Call destroy method if the given instance implements the {@link Destroyable} interface.
	 *
	 * @param toBeDestroyed
	 *            the to be destroyed
	 */
	static void destroy(Object toBeDestroyed) {
		if (toBeDestroyed instanceof Destroyable) {
			((Destroyable) toBeDestroyed).destroy();
		}
	}
}
