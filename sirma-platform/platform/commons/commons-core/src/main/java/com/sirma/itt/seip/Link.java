package com.sirma.itt.seip;

/**
 * Common interface that represents a directional link or relation between 2 objects.
 *
 * @author BBonev
 * @param <F>
 *            the from type
 * @param <T>
 *            the to type
 */
public interface Link<F, T> {

	/**
	 * Gets the beginning of the link
	 *
	 * @return the from
	 */
	F getFrom();

	/**
	 * Gets the end of the link.
	 *
	 * @return the to
	 */
	T getTo();
}
