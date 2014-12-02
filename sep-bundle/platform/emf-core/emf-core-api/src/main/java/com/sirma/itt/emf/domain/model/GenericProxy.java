package com.sirma.itt.emf.domain.model;

/**
 * Defines a common proxy interface. The proxy can be cloned with or without the actual object
 *
 * @param <E>
 *            the element type
 * @author BBonev
 */
public interface GenericProxy<E> extends Cloneable {

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	E getTarget();

	/**
	 * Sets the target.
	 *
	 * @param target
	 *            the new target
	 */
	void setTarget(E target);

	/**
	 * Clone proxy but keep the actual object intact
	 *
	 * @return the e
	 */
	E cloneProxy();

	/**
	 * Clone the proxy and the underling target object
	 * 
	 * @return the e
	 */
	E clone();

}
