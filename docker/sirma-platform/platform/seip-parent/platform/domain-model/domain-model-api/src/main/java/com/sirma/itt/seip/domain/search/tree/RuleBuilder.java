package com.sirma.itt.seip.domain.search.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Class that provides methods for {@link Rule rule} creation.
 * It also enforce validation that throws {@link IllegalArgumentException} exception if fails.
 * The validation checks whether the mandatory fields have proper values which are:
 * `field` has value, type and operation to not be null.
 *
 * @author radoslav
 */
public class RuleBuilder {
	private RuleImpl rule = new RuleImpl();

	/**
	 * Populates the rule builder with the data of the passed rule except the values. Useful for quickly creating a
	 * builder from an existing {@link Rule} and reassign modified or different values.
	 *
	 * @param baseRule
	 * 		The passed rule
	 * @return itself in order to provide method chaining
	 */
	public RuleBuilder from(Rule baseRule) {
		setId(baseRule.getId()).setOperation(baseRule.getOperation())
				.setField(baseRule.getField())
				.setType(baseRule.getType());
		return this;
	}

	public RuleBuilder setId(String id) {
		rule.id = id;
		return this;
	}

	public RuleBuilder setField(String field) {
		rule.field = field;
		return this;
	}

	public RuleBuilder setType(String type) {
		rule.type = type;
		return this;
	}

	public RuleBuilder setOperation(String operation) {
		rule.operation = operation;
		return this;
	}

	/**
	 * Adds a value to the rule's values.
	 *
	 * @param value
	 * 		The value
	 * @return itself in order to provide method chaining
	 */
	public RuleBuilder addValue(String value) {
		rule.values.add(value);
		return this;
	}

	/**
	 * Assigns the provided values as the rule's. Any values previously present will be cleared.
	 *
	 * @param values
	 * 		- the new values
	 * @return itself in order to provide method chaining
	 */
	public RuleBuilder setValues(List<String> values) {
		rule.values.clear();
		rule.values.addAll(values);
		return this;
	}

	private void validate() {
		boolean operationInvalid = rule.operation == null;
		if (operationInvalid) {
			throw new IllegalArgumentException("Validation error on 'operation' with value: " + rule.operation);
		}
	}

	/**
	 * Method indicating the end of Rule creation
	 *
	 * @return immutable {@link Rule} instance
	 */
	public Rule build() {
		if (StringUtils.isBlank(rule.id)) {
			rule.id = UUID.randomUUID().toString();
		}
		if (StringUtils.isBlank(rule.field)) {
			rule.field = CriteriaWildcards.ANY_FIELD;
		}
		validate();
		rule.values = Collections.unmodifiableList(rule.values);
		return rule;
	}

	/**
	 * Inner class that holds an immutable {@link Rule} implementation.
	 *
	 * @author radoslav
	 */
	private class RuleImpl implements Rule {
		private String id;
		private String field;
		private String type;
		private String operation;
		private List<String> values = new ArrayList<>(3);

		@Override
		public String getField() {
			return field;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public String getOperation() {
			return operation;
		}

		@Override
		public List<String> getValues() {
			return values;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public NodeType getNodeType() {
			return NodeType.RULE;
		}
	}

}
