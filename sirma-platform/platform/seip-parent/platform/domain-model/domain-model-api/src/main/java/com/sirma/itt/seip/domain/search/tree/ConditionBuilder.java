package com.sirma.itt.seip.domain.search.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Builder for the {@link Condition condition} instances used in searches.
 * By default the condition operator set to {@link com.sirma.itt.seip.domain.search.tree.Condition.Junction#AND}.
 * It also enforce validation that throws {@link IllegalArgumentException} exception if it fails.
 * The validation enforces the condition of the {@link Condition} to have meaningful value in our case != null.
 *
 * @author radoslav
 */
public class ConditionBuilder {

	private ConditionImpl condition = new ConditionImpl();

	/**
	 * Populates the builder instance with all fields except the rules from the passed {@link Condition condition}
	 * instance as param. Useful for quickly creating a builder from an existing {@link Condition} and reassign modified
	 * or different rules.
	 *
	 * @param condition
	 * 		The condition to copy the data from
	 * @return itself used for method chaining
	 */
	public ConditionBuilder from(Condition condition) {
		setId(condition.getId()).setCondition(condition.getCondition());
		return this;
	}

	/**
	 * Add rule to the list of rules. A rule can be either {@link Rule} or {@link Condition}.
	 *
	 * @param rule
	 * 		The rule
	 * @return {@link ConditionBuilder} in order to chain methods
	 */
	public ConditionBuilder addRule(SearchNode rule) {
		condition.rules.add(rule);
		return this;
	}

	/**
	 * Assigns the provided rules as the condition's. Any rules previously present in the condition will be cleared.
	 *
	 * @param rules
	 * 		- the new rules
	 * @return {@link ConditionBuilder} in order to chain methods
	 */
	public ConditionBuilder setRules(List<SearchNode> rules) {
		condition.rules.clear();
		condition.rules.addAll(rules);
		return this;
	}

	public ConditionBuilder setCondition(Condition.Junction junction) {
		condition.condition = junction;
		return this;
	}

	public ConditionBuilder setId(String id) {
		condition.id = id;
		return this;
	}

	/**
	 * Validates if all fields of the condition are properly filled.
	 * Throws {@link IllegalArgumentException} exception if invalid.
	 */
	private void validate() {
		if (condition.condition == null) {
			String errorMsg = String.format("Validation error on 'operator' with value:%s", condition.condition);
			throw new IllegalArgumentException(errorMsg);
		}
	}

	/**
	 * Indicates the end of building the condition instance
	 *
	 * @return the condition
	 */
	public Condition build() {
		validate();
		if (StringUtils.isBlank(condition.id)) {
			condition.id = UUID.randomUUID().toString();
		}
		condition.rules = Collections.unmodifiableList(condition.rules);
		return condition;
	}

	/**
	 * Immutable realization of the {@link Condition} interface.
	 *
	 * @author radoslav
	 */
	private class ConditionImpl implements Condition {
		private String id;
		private Condition.Junction condition = Junction.AND;
		private List<SearchNode> rules = new ArrayList<>(3);

		@Override
		public String getId() {
			return id;
		}

		@Override
		public NodeType getNodeType() {
			return NodeType.CONDITION;
		}

		@Override
		public Junction getCondition() {
			return condition;
		}

		@Override
		public List<SearchNode> getRules() {
			return rules;
		}
	}

}
