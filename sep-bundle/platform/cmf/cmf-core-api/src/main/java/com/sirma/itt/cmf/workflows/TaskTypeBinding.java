package com.sirma.itt.cmf.workflows;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Annotation literal for event selection for the {@link TaskType} annotation.
 *
 * @author BBonev
 */
public final class TaskTypeBinding extends AnnotationLiteral<TaskType> implements TaskType {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4633842233859522288L;

	/** The task. */
	private final String task;

	/**
	 * Instantiates a new task type binding.
	 *
	 * @param task
	 *            the task
	 */
	public TaskTypeBinding(String task) {
		this.task = task;
	}

	@Override
	public String task() {
		return task;
	}

}
