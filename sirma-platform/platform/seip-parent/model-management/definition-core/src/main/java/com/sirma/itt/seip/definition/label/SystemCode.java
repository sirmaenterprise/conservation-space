package com.sirma.itt.seip.definition.label;

/**
 * Represents a system code, for which a label has to be retrieved. Contains the code, an indicator (retrieval method)
 * and the value of the indicator.
 *
 * @author Vilizar Tsonev
 */
public class SystemCode {

	/** The system code, for which a label to be retrieved. Can also be a user URI **/
	private String code;

	private String field;

	/**
	 * Indicates the retrieval method - ex. codelist, usernamebyuri, or any other.
	 **/
	private String indicator;

	/**
	 * The value of the indicator - for example, a codelist number, or just 0 when retrieving user display names.
	 **/
	private String indicatorValue;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public String getIndicatorValue() {
		return indicatorValue;
	}

	public void setIndicatorValue(String indicatorValue) {
		this.indicatorValue = indicatorValue;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
