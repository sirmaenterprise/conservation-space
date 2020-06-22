package com.sirma.sep.model.management.meta;

import java.util.List;
import java.util.Objects;

/**
 * Describes rule expression.
 *
 * @author Stella Djulgerova
 */
public class RuleExpression {

	private String field;

	private String operation;

	private List<Object> values;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RuleExpression)) {
			return false;
		}
		RuleExpression that = (RuleExpression) o;
		return Objects.equals(field, that.field) && Objects.equals(operation, that.operation)
				&& Objects.equals(values, that.values);
	}

	@Override
	public int hashCode() {
		return Objects.hash(field, operation, values);
	}
}
