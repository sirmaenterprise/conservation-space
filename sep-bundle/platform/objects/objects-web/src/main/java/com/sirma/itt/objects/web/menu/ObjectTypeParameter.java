package com.sirma.itt.objects.web.menu;

/**
 * ObjectTypeParameter.
 * 
 * @author svelikov
 */
public class ObjectTypeParameter {

	/**
	 * Instantiates a new object type parameter.
	 * 
	 * @param parameter
	 *            the parameter
	 * @param value
	 *            the value
	 */
	public ObjectTypeParameter(String parameter, String value) {
		this.parameter = parameter;
		this.value = value;
	}

	/** The parameter. */
	private String parameter;

	/** The value. */
	private String value;

	/**
	 * Getter method for parameter.
	 * 
	 * @return the parameter
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 * Setter method for parameter.
	 * 
	 * @param parameter
	 *            the parameter to set
	 */
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	/**
	 * Getter method for value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Setter method for value.
	 * 
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
