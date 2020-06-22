package com.sirma.itt.seip.domain.search.tree;

/**
 * Factory class for Rule and Condition builders
 *
 * @author radoslav
 */
public class SearchCriteriaBuilder {

	/**
	 * Make the constructor private because the class is only used as static factory
	 */
	private SearchCriteriaBuilder() {

	}

	/**
	 * Factory method for {@link RuleBuilder rule builder} instances used for creating {@link Rule instances}
	 *
	 * @return The new instance
	 */
	public static RuleBuilder createRuleBuilder() {
		return new RuleBuilder();
	}

	/**
	 * Factory method for {@link ConditionBuilder condtion builder} instance used for the creation of {@Link Condition
	 * instances}
	 *
	 * @return The builder instance
	 */
	public static ConditionBuilder createConditionBuilder() {
		return new ConditionBuilder();
	}

}
