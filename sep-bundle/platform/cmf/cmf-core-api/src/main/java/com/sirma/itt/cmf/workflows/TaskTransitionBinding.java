package com.sirma.itt.cmf.workflows;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Binding class for {@link TaskTransition} annotation
 * 
 * @author BBonev
 */
public final class TaskTransitionBinding extends AnnotationLiteral<TaskTransition> implements
		TaskTransition {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -451824284044189502L;
	/** The event. */
	private final String event;

	/**
	 * Instantiates a new workflow transition biding.
	 *
	 * @param event
	 *            the event
	 */
	public TaskTransitionBinding(String event) {
		this.event = event;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String event() {
		return event;
	}

}
