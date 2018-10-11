package com.sirma.itt.seip;

/**
 * Represents classes that can be cloned. Different from {@link Cloneable} in order to not be confused with it.
 *
 * @author Adrian Mitev
 */
public interface Copyable<T> {

	/**
	 * Creates a cloning of the instance.
	 *
	 * @return instance clone.
	 */
	public T createCopy();

}
