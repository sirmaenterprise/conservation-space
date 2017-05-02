package com.sirma.itt.seip.domain;

/**
 * Interface that represent a node that has methods for finding and traversing tree structures of data
 *
 * @author BBonev
 */
public interface Node extends Identity {

	/**
	 * Checks for children.
	 *
	 * @return true, if the current node has children.
	 */
	boolean hasChildren();

	/**
	 * Gets a child by name.
	 *
	 * @param name
	 *            the name
	 * @return the child
	 */
	Node getChild(String name);

}
