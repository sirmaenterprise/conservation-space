package com.sirma.itt.emf.domain.model;

/**
 * Forces the implementing classes to provide common means for ordering and
 * sorting
 *
 * @author BBonev
 */
public interface Sortable {

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public Integer getOrder();

}
