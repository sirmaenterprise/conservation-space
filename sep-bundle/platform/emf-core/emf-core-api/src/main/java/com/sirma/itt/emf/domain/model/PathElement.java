package com.sirma.itt.emf.domain.model;


/**
 * Defines methods for navigating into path.
 *
 * @author BBonev
 */
public interface PathElement extends Node {

	String PATH_SEPARATOR = "/";
	/**
	 * Gets the parent element.
	 *
	 * @return the parent element
	 */
	public PathElement getParentElement();

	/**
	 * Constructs the path to the given element
	 *
	 * @return the path
	 */
	public String getPath();

}
