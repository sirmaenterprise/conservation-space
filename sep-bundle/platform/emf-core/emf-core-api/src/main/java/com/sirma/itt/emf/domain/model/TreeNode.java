package com.sirma.itt.emf.domain.model;

/**
 * Interface that defines methods that helps defining a tree structure of various times. The
 * implementation should provide the current Identifier ({@link #getId()} and and the parent
 * identifier ({@link #getParentId()} in order to build full tree structure.
 * 
 * @param <I>
 *            the identifier type
 * @author BBonev
 */
public interface TreeNode<I> extends Node {

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	I getId();

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	void setId(I id);

	/**
	 * Gets the parent id.
	 * 
	 * @return the parent id
	 */
	I getParentId();

	/**
	 * Sets the parent id.
	 * 
	 * @param parentId
	 *            the new parent id
	 */
	void setParentId(I parentId);

}
