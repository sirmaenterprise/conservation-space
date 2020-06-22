package com.sirma.itt.seip;

import java.io.Serializable;

/**
 * Interface that specifies a database identifier. The identifier could have a type depending of the implementations.
 *
 * @param <E>
 *            the entity ID type
 * @author BBonev
 */
public interface Entity<E extends Serializable> {

	/**
	 * Should return the primary database identifier for the current entity instance. May return <code>null</code> if
	 * not generated or set, yet.
	 *
	 * @return the database identifier or <code>null</code>
	 */
	E getId();

	/**
	 * Sets the primary identifier for the current entity instance.
	 *
	 * @param id
	 *            the id to set
	 */
	void setId(E id);
}
