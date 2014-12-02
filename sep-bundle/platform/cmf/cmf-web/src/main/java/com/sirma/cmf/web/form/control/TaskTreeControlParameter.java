package com.sirma.cmf.web.form.control;

/**
 * The Enum TaskTreeControlParameter.
 * 
 * @author svelikov
 */
public enum TaskTreeControlParameter {

	/** The style class. */
	STYLE_CLASS("styleClass"),

	/** The style. */
	STYLE("style");

	/** The param. */
	private String param;

	/**
	 * Instantiates a new picklist control parameter.
	 * 
	 * @param param
	 *            the param
	 */
	private TaskTreeControlParameter(String param) {
		this.param = param;
	}

	/**
	 * Getter method for param.
	 * 
	 * @return the param
	 */
	public String getParam() {
		return param;
	}

	/**
	 * Setter method for param.
	 * 
	 * @param param
	 *            the param to set
	 */
	public void setParam(String param) {
		this.param = param;
	}

	/**
	 * Gets the param enum.
	 * 
	 * @param requestedParam
	 *            the requested param
	 * @return the param enum
	 */
	public static TaskTreeControlParameter getParamEnum(String requestedParam) {
		TaskTreeControlParameter[] params = values();
		for (TaskTreeControlParameter param : params) {
			if (param.name().equals(requestedParam)) {
				return param;
			}
		}

		return null;
	}

	/**
	 * Gets the enum value.
	 * 
	 * @param name
	 *            the name
	 * @return the enum value
	 */
	public static TaskTreeControlParameter getEnumValue(String name) {
		TaskTreeControlParameter enumValue = null;
		enumValue = valueOf(name);
		return enumValue;
	}

}
