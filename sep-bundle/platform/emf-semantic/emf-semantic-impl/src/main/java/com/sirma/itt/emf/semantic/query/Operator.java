/**
 * 
 */
package com.sirma.itt.emf.semantic.query;

/**
 * @author kirq4e
 * 
 */
public enum Operator {

	LIKE("~", Type.STRING), NOT_LIKE("!~", Type.STRING), EQUALS("=",
			Type.AS_FIELD), NOT_EQUALS("!=", Type.AS_FIELD), IN("in", Type.LIST), NOT_IN(
			"not in", Type.LIST), LESS_THAN("<", Type.AS_FIELD), LESS_THAN_EQUALS(
			"<=", Type.AS_FIELD), GREATER_THAN(">", Type.AS_FIELD), GREATER_THAN_EQUALS(
			">=", Type.AS_FIELD), BEFORE("before", Type.DATE), AFTER("after",
			Type.DATE), FROM("from", Type.DATE), TO("to", Type.DATE), DURING(
			"during", Type.DATE);

	private String displayName;
	private Type type;

	/**
	 * Initialize constructor
	 * 
	 * @param displayName
	 *            The name of the Operator
	 * @param type Type of the value of the operator
	 */
	private Operator(String displayName, Type type) {
		this.displayName = displayName;
		this.type = type;
	}

	/**
	 * 
	 * @return The name of the Operator
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

}
