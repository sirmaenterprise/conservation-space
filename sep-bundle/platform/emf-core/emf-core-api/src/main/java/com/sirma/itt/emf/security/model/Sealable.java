package com.sirma.itt.emf.security.model;

/**
 * Identifies that the given object can be sealed so it effectively becomes an
 * immutable object. If once changed it cannot be changed back to mutable. The
 * implementation classes must ensure that the object cannot be modified via
 * setter methods. If any of the methods returns a collection that collection
 * have to me immutable as well.
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
}
