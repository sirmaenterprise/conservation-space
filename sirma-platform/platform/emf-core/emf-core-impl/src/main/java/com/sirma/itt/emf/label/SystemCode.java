package com.sirma.itt.emf.label;

import com.sirma.itt.emf.label.retrieve.FieldId;

// TODO: Auto-generated Javadoc
/**
 * Represents a system code, for which a label has to be retrieved. Contains the code, an indicator (retrieval method)
 * and the value of the indicator.
 *
 * @author Vilizar Tsonev
 */
public class SystemCode {

	/** The system code, for which a label to be retrieved. Can also be a user URI **/
	private String code;

	/** The field. */
	private String field;

	/**
	 * Indicates the retrieval method - ex. codelist, usernamebyuri, or any other defined in {@link FieldId}
	 **/
	private String indicator;

	/**
	 * The value of the indicator - for example, a codelist number, or just 0 when retrieving user display names.
	 **/
	private String indicatorValue;

	/**
	 * Getter method for code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Setter method for code.
	 *
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Getter method for indicator.
	 *
	 * @return the indicator
	 */
	public String getIndicator() {
		return indicator;
	}

	/**
	 * Setter method for indicator.
	 *
	 * @param indicator
	 *            the indicator to set
	 */
	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	/**
	 * Getter method for indicatorValue.
	 *
	 * @return the indicatorValue
	 */
	public String getIndicatorValue() {
		return indicatorValue;
	}

	/**
	 * Setter method for indicatorValue.
	 *
	 * @param indicatorValue
	 *            the indicatorValue to set
	 */
	public void setIndicatorValue(String indicatorValue) {
		this.indicatorValue = indicatorValue;
	}

	/**
	 * Gets the field.
	 *
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * Sets the field.
	 *
	 * @param field
	 *            the new field
	 */
	public void setField(String field) {
		this.field = field;
	}

}
