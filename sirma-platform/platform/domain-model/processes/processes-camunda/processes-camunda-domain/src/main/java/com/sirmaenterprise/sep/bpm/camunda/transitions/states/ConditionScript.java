package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

/**
 * Class to represent the condition script.
 */
public class ConditionScript extends ConditionExpression {

	String language;
	String source;

	/**
	 * Instantiates a new condition script.
	 * 
	 * @param scopeId
	 * @param value
	 *            the value
	 * @param language
	 *            the language
	 */
	public ConditionScript(String scopeId, String value, String language) {
		super(scopeId, value);
		this.language = language;
	}

	/**
	 * Gets the script language.
	 * 
	 * @return the script language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Get the script source.
	 * 
	 * @return the script source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the script source.
	 * 
	 * @param source
	 *            the script source
	 */
	public void setSource(String source) {
		this.source = source;
	}
}
