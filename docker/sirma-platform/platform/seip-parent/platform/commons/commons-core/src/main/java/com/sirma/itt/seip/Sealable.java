package com.sirma.itt.seip;

import java.util.Collection;

/**
 * Identifies that the given object can be sealed so it effectively becomes an immutable object. If once sealed it
 * cannot be unsealed. The implementation classes must ensure that the object cannot be modified via setter methods. If
 * any of the methods returns a collection that collection have to me immutable as well.
 *
 * @author BBonev
 */
public interface Sealable {

	/**
	 * Checks if the current object is sealed.
	 *
	 * @return true, if is sealed
	 */
	boolean isSealed();

	/**
	 * Seals the current object and prevents it from farther modifications.
	 */
	void seal();

	/**
	 * Seal the provided instance if of type {@link Sealable}
	 *
	 * @param <E>
	 *            the element type
	 * @param toSeal
	 *            the to seal
	 * @return the same instance sealed if supported.
	 */
	static <E> E seal(E toSeal) {
		if (toSeal instanceof Sealable) {
			((Sealable) toSeal).seal();
		} else if (toSeal instanceof Collection) {
			Collection<?> collection = (Collection<?>) toSeal;
			collection.forEach(Sealable::seal);
		}
		return toSeal;
	}
}
