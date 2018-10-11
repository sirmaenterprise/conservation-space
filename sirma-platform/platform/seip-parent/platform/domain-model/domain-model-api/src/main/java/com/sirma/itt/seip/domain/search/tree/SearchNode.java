package com.sirma.itt.seip.domain.search.tree;

/**
 * Common interface for search tree nodes.
 *
 * @author yasko
 */
public interface SearchNode {

	/**
	 * Possible search tree node types.
	 *
	 * @author yasko
	 */
	enum NodeType {
		CONDITION, RULE;
	}

	/**
	 * Gets the id of the node
	 *
	 * @return The id
	 */
	String getId();

	/**
	 * Gets the node type
	 *
	 * @return The node type
	 */
	NodeType getNodeType();
}
