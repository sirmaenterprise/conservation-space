package com.sirma.itt.emf.domain.model;

import java.io.Serializable;

/**
 * Interface that specifies a database identifier. The identifier could have a type depending of the
 * implementations.
 * 
 * @param <E>
 *            the entity ID type
 * @author BBonev
 */
public interface Entity<E extends Serializable> {

	/**
	 * Getter method for id.
	 * 
	 * @return the id
	 */
	public E getId();

	/**
	 * Setter method for id.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(E id);
}
