package com.sirma.itt.seip.domain.search.tree;

import java.util.List;

/**
 * Represent a search criteria e.g title eq "How to Search".
 * The example rule can be divided into the following fields:
 *
 * - field: dcterms:title
 * - operation: equals
 * - type: string
 * - values: How to Search
 *
 *
 * @author yasko
 * @author radoslav
 */
public interface Rule extends SearchNode {

	/**
	 * Get the rule field
	 *
	 * @return The field
	 */
	String getField();

	/**
	 * Get the type of the values
	 *
	 * @return The type
	 */
	String getType();

	/**
	 * Get the rule operation
	 *
	 * @return The operation
	 */
	String getOperation();

	/**
	 * Get the values
	 *
	 * @return the values
	 */
	List<String> getValues();

}
