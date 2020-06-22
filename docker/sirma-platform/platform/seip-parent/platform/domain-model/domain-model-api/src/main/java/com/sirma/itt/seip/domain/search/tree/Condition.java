package com.sirma.itt.seip.domain.search.tree;

import java.util.List;

/**
 * Represents a search junction such as (a AND b) or (a OR b)
 * between {@link Rule rules} or {@link Condition conditions}.
 * {@link Rule Rules} could be other nested conditions.
 * This is the preferred type to be used when constructing the query tree
 * and since it is immutable the {@link ConditionBuilder} class
 * provides default way for creating instances.
 *
 * @author yasko
 * @author radoslav
 */
public interface Condition extends SearchNode {
	/**
	 * Junction types.
	 *
	 * @author yasko
	 */
	enum Junction {
		AND, OR;

		/**
		 * Converts string to {@link Junction}. The conversion is *not* case
		 * sensitive.
		 *
		 * @param from
		 * 		String source to try and convert.
		 * @return An element of {@link Junction}, or {@code null} if no match is found.
		 */
		public static Junction fromString(String from) {
			if (from == null) {
				return null;
			}
			switch (from.toUpperCase()) {
			case "AND":
				return AND;
			case "OR":
				return OR;
			default:
				return null;
			}
		}
	}

	/**
	 * Gets the condition between the rules that might be either {@link Junction#OR} or {@link Junction#AND}
	 *
	 * @return The condition
	 */
	Junction getCondition();

	/**
	 * Gets the list of rules for the search condition
	 *
	 * @return The list
	 */
	List<SearchNode> getRules();

}
