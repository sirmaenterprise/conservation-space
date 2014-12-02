package com.sirma.cmf.web.form.control;

/**
 * Constants for DataTableControl builder.
 * 
 * @author svelikov
 */
public enum RadioButtonGroupControlParameter {

	/** The layout. */
	LAYOUT("layout");

	/** The ui param. */
	private String param;

	/**
	 * Instantiates a new data table control parameter.
	 * 
	 * @param param
	 *            the param
	 */
	private RadioButtonGroupControlParameter(String param) {
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

}
