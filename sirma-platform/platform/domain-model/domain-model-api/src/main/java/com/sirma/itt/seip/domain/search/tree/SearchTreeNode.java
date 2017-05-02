package com.sirma.itt.seip.domain.search.tree;

/**
 * Common class for search tree nodes.
 * 
 * @author yasko
 */
public abstract class SearchTreeNode {
	
	/**
	 * Possible search tree node types.
	 * 
	 * @author yasko
	 */
	public enum NodeType {
		CONDITION, RULE;
	}
	
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public abstract NodeType getNodeType();
}
