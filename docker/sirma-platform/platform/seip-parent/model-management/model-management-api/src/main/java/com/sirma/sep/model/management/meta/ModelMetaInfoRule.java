package com.sirma.sep.model.management.meta;

import java.util.List;
import java.util.Objects;

/**
 * Describes possible rules for a {@link com.sirma.sep.model.management.ModelAttribute}.
 *
 * @author Stella Djulgerova
 */
public class ModelMetaInfoRule {

	private List<Object> values;

	private String condition = "AND";

	private List<RuleExpression> expressions;

	private String errorLabel;

	private RuleOutcome outcome;

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public List<RuleExpression> getExpressions() {
		return expressions;
	}

	public void setExpressions(List<RuleExpression> expressions) {
		this.expressions = expressions;
	}

	public String getErrorLabel() {
		return errorLabel;
	}

	public void setErrorLabel(String errorLabel) {
		this.errorLabel = errorLabel;
	}

	public RuleOutcome getOutcome() {
		return outcome;
	}

	public void setOutcome(RuleOutcome outcome) {
		this.outcome = outcome;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModelMetaInfoRule)) {
			return false;
		}
		ModelMetaInfoRule that = (ModelMetaInfoRule) o;
		return Objects.equals(values, that.values) && Objects.equals(condition, that.condition)
				&& Objects.equals(expressions, that.expressions) && Objects.equals(errorLabel, that.errorLabel)
				&& Objects.equals(outcome, that.outcome);
	}

	@Override
	public int hashCode() {
		return Objects.hash(values, condition, expressions, errorLabel, outcome);
	}
}
