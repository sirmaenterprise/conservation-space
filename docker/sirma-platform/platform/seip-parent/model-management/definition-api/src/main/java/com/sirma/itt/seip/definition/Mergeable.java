package com.sirma.itt.seip.definition;

import com.sirma.itt.seip.domain.Identity;

/**
 * Defines a method for easy merging two objects.
 *
 * @param <E>
 *            the element type
 * @author BBonev
 */
public interface Mergeable<E> extends Identity {

	/**
	 * Merges the data from the given source object to the current instance.
	 *
	 * @param source
	 *            the source object
	 * @return the updated instance
	 */
	E mergeFrom(E source);

	/**
	 * A factory for creating Mergeable objects.
	 *
	 * @param <E>
	 *            the element type
	 */
	interface MergeableInstanceFactory<E> {

		/**
		 * Creates a new MergeableInstance object.
		 *
		 * @return the e
		 */
		Mergeable<E> createInstance();
	}
}
