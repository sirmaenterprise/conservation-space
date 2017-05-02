package com.sirma.itt.emf.semantic.search.operation;

/**
 * Enum representing the major and basic arithmetic operators
 * 
 * @author svetlozar.iliev
 */
public enum ArithmeticOperators {

	EQUALS("="), DOES_NOT_EQUAL("!="), GREATER_THAN(">"), LESS_THAN("<"), GREATER_THAN_OR_EQUAL(
			">="), LESS_THAN_OR_EQUAL("<=");

	private final String operator;

	private ArithmeticOperators(String operator) {
		this.operator = operator;
	}

	@Override
	public String toString() {
		return operator;
	}

	/**
	 * Returns the raw value of the operator as a string
	 * 
	 * @return the raw value of the operator
	 */
	public String value() {
		return operator;
	}
}
