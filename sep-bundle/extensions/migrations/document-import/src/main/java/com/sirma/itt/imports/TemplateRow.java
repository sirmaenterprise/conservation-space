package com.sirma.itt.imports;

/**
 * Single option of a template
 *
 * @author kirq4e
 */
public class TemplateRow {

	private String parameter;
	private String subject;
	private String predicate;
	private String value;

	/**
	 * Creates new template option with the given parameters
	 *
	 * @param parameter
	 *            Parameter
	 * @param subject
	 *            Subject
	 * @param predicate
	 *            Predicate
	 * @param value
	 *            Value
	 */
	public TemplateRow(String parameter, String subject, String predicate, String value) {
		this.parameter = parameter;
		this.subject = subject;
		this.predicate = predicate;
		this.value = value;
	}

	/**
	 * Getter method for subject.
	 *
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Setter method for subject.
	 *
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Getter method for predicate.
	 *
	 * @return the predicate
	 */
	public String getPredicate() {
		return predicate;
	}

	/**
	 * Setter method for predicate.
	 *
	 * @param predicate
	 *            the predicate to set
	 */
	public void setPredicate(String predicate) {
		this.predicate = predicate;
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

}
