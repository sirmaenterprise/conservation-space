package com.sirma.cmf.web.search;


/**
 * Abstraction for search filter argument. The method {@link #getType()} provides the sorting field
 * value, and the ui label
 * 
 * @author bbanchev
 */
public interface SortAction {
	/**
	 * Getter method for sort type.
	 * 
	 * @return the sort key
	 */
	String getType();

	/**
	 * Getter method for label.
	 * 
	 * @return the label
	 */
	public String getLabel();
}
