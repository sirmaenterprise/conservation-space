package com.sirma.itt.seip.domain;

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
	PathElement getParentElement();

	/**
	 * Constructs the path to the given element
	 *
	 * @return the path
	 */
	String getPath();

}
