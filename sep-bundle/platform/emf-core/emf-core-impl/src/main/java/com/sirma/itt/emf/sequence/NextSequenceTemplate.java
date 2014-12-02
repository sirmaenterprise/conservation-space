package com.sirma.itt.emf.sequence;

import java.util.Map;

/**
 * Dao object used to trigger sequence generation
 * 
 * @author BBonev
 */
public class NextSequenceTemplate {

	/** The next sequence. */
	private String nextSequence;

	/** The template. */
	private String template;

	/** The context. */
	private Map<String, String> context;

	/**
	 * Instantiates a new next sequence by template.
	 */
	public NextSequenceTemplate() {
	}

	/**
	 * Instantiates a new next sequence by template.
	 * 
	 * @param template
	 *            the template
	 */
	public NextSequenceTemplate(String template) {
		super();
		this.template = template;
	}

	/**
	 * Instantiates a new next sequence by template.
	 * 
	 * @param template
	 *            the template
	 * @param context
	 *            the context
	 */
	public NextSequenceTemplate(String template, Map<String, String> context) {
		super();
		this.template = template;
		this.context = context;
	}

	/**
	 * Getter method for nextSequence.
	 * 
	 * @return the nextSequence
	 */
	public String getNextSequence() {
		return nextSequence;
	}

	/**
	 * Setter method for nextSequence.
	 * 
	 * @param nextSequence
	 *            the nextSequence to set
	 */
	public void setNextSequence(String nextSequence) {
		this.nextSequence = nextSequence;
	}

	/**
	 * Getter method for template.
	 * 
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * Setter method for template.
	 * 
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * Getter method for context.
	 * 
	 * @return the context
	 */
	public Map<String, String> getContext() {
		return context;
	}

	/**
	 * Setter method for context.
	 * 
	 * @param context
	 *            the context to set
	 */
	public void setContext(Map<String, String> context) {
		this.context = context;
	}

}
