package com.sirma.cmf.web.form.action.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class DynamicFormActionEventTypeBinding.
 * 
 * @author svelikov
 */
public class DynamicFormActionEventTypeBinding extends
		AnnotationLiteral<DynamicFormActionEventType> implements DynamicFormActionEventType {

	private static final long serialVersionUID = -7482160949692488743L;

	/** The event type. */
	private final String eventId;

	/**
	 * Instantiates a new dynamic form action event id binding.
	 * 
	 * @param eventId
	 *            the event id
	 */
	public DynamicFormActionEventTypeBinding(String eventId) {
		this.eventId = eventId;
	}

	@Override
	public String value() {
		return eventId;
	}

}
