package com.sirma.cmf.web.form.control;

/**
 * The Enum MediaOutputControlParameter.
 * 
 * @author svelikov
 */
public enum MediaOutputControlParameter {

	/** The style class. */
	STYLE_CLASS("styleClass"),

	/** The style. */
	STYLE("style"),

	CONTROL_LABEL("");

	/** The ui param. */
	private String uiParam;

	/**
	 * Instantiates a new media output ui parameter.
	 * 
	 * @param uiParam
	 *            the ui param
	 */
	private MediaOutputControlParameter(String uiParam) {
		this.uiParam = uiParam;
	}

	/**
	 * Getter method for uiParam.
	 * 
	 * @return the uiParam
	 */
	public String getUiParam() {
		return uiParam;
	}

	/**
	 * Setter method for uiParam.
	 * 
	 * @param uiParam
	 *            the uiParam to set
	 */
	public void setUiParam(String uiParam) {
		this.uiParam = uiParam;
	}

}
