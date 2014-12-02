package com.sirma.cmf.web.form.control;

/**
 * The Enum DataTableControlParameter.
 * 
 * @author svelikov
 */
public enum DataTableControlParameter {

	/** The table header. */
	TABLE_HEADER(""),

	/** The column title. */
	COLUMN_TITLE(""),

	/** The column value. */
	COLUMN_VALUE(""),

	/** The table value. */
	TABLE_VALUE("value"),

	/** The style class. */
	STYLE_CLASS("styleClass"),

	/** The style. */
	STYLE("style");

	/** The ui param. */
	private String param;

	/**
	 * Instantiates a new data table control parameter.
	 * 
	 * @param param
	 *            the param
	 */
	private DataTableControlParameter(String param) {
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
