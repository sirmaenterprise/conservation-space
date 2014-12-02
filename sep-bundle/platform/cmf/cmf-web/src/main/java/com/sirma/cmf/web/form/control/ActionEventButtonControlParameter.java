package com.sirma.cmf.web.form.control;

/**
 * The Enum ActionEventButtonControlParameter.
 * 
 * @author svelikov
 */
public enum ActionEventButtonControlParameter {

	/** The style class. */
	EVENT_ID("event_id"),

	/** The execute once. */
	EXECUTE_ONCE("execute_once"),

	/** The execute element. */
	EXECUTE_ELEMENT("execute_element"),

	/** The render element. */
	RENDER_ELEMENT("render_element");

	/** The param. */
	private String param;

	/**
	 * Instantiates a new media output ui parameter.
	 * 
	 * @param param
	 *            the param
	 */
	private ActionEventButtonControlParameter(String param) {
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
