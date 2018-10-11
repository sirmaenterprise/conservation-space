package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

/**
 * Class to represent the camunda condition transition expression. Each transition in camunda could have expression
 * which to validate transition flow. After completion of task each sequence flow condition is evaluated and result of
 * it is boolean value, which idea is to be used to validate task pre-conditions and flow direction.
 */
public class ConditionExpression {
	private String value;
	private String scopeId;

	/**
	 * Instantiates a new condition expression.
	 * 
	 * @param scopeId
	 *            the scope id of expression
	 * @param value
	 *            the value
	 */
	public ConditionExpression(String scopeId, String value) {
		this.scopeId = scopeId;
		this.value = value;
	}

	/**
	 * Gets the condition expression.
	 * 
	 * @return the condition expression
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the condition expression scope id.
	 * 
	 * @return the condition expression scope id
	 */
	public String getScopeId() {
		return scopeId;
	}
}
