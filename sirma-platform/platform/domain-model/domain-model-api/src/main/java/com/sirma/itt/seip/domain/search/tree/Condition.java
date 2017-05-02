package com.sirma.itt.seip.domain.search.tree;

import java.util.List;

/**
 * Represents a search junction between {@link Rule rules}.
 * {@link Rule Rules} could be other nested conditions.
 * @author yasko
 */
public class Condition extends SearchTreeNode {
	/**
	 * Junction types.
	 *
	 * @author yasko
	 */
	public enum Junction {
		AND, OR;

		/**
		 * Converts string to {@link Junction}. The conversion is *not* case
		 * sensitive.
		 *
		 * @param from
		 *            String source to try and convert.
		 * @return An element of {@link Junction}, or {@code null} if no match
		 *         is found.
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

	private Junction condition;
	private List<SearchTreeNode> rules;

	/**
	 * Default constructor which initializes the condition type to
	 * {@link Junction#AND}.
	 */
	public Condition() {
		this(Junction.AND);
	}

	/**
	 * Constructor for a specific {@link Junction junction} type.
	 * @param type Junction type to use for the condition.
	 */
	public Condition(Junction type) {
		condition = type;
	}


	public Junction getCondition() {
		return condition;
	}

	public void setCondition(Junction condition) {
		this.condition = condition;
	}

	public List<SearchTreeNode> getRules() {
		return rules;
	}

	public void setRules(List<SearchTreeNode> rules) {
		this.rules = rules;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.CONDITION;
	}

}
