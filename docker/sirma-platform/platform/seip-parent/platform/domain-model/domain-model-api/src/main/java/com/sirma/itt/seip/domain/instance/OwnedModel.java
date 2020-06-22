package com.sirma.itt.seip.domain.instance;

/**
 * Model interface to mark a node that it has a parent that owns him.
 *
 * @author BBonev
 * @param <T>
 *            is the owning model type
 */
public interface OwnedModel<T> {

	/**
	 * Gets the owning/parent reference.
	 *
	 * @return the owning reference
	 */
	default T getOwning() {
		return null;
	}

	/**
	 * Sets the owning reference.
	 *
	 * @param owning
	 *            the new owning reference
	 */
	default void setOwning(@SuppressWarnings("unused") T owning) {
		// nothing to do
	}

}
