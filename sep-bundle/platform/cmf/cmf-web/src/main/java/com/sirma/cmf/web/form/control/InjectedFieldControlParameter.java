package com.sirma.cmf.web.form.control;

/**
 * The Enum InjectedFieldControlParameter.
 * 
 * @author svelikov
 */
public enum InjectedFieldControlParameter {

	/** The style class. */
	SOURCE("source"),

	/** The clvalue. */
	CLVALUE("clvalue"),

	/** The source. */
	VISIBILITY("visibility");

	/** The param. */
	private String param;

	/**
	 * Instantiates a new media output ui parameter.
	 * 
	 * @param param
	 *            the param
	 */
	private InjectedFieldControlParameter(String param) {
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
