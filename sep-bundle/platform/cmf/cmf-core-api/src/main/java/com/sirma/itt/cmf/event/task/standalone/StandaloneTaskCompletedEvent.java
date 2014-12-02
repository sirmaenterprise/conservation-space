package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on {@link StandaloneTaskInstance} completion, before calling the Activiti engine. The
 * event will provided the operation used to complete the task.
 * 
 * @author BBonev
 */
@Documentation("Event fired on {@link StandaloneTaskInstance} completion, before calling the Activiti engine. "
		+ "The event will provided the operation used to complete the task.")
public class StandaloneTaskCompletedEvent extends AbstractInstanceEvent<StandaloneTaskInstance> {

	/** The operation. */
	private final Operation operation;

	/** The transition definition. */
	private final TransitionDefinition transitionDefinition;

	/**
	 * Instantiates a new standalone task completed event.
	 * 
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @param transitionDefinition
	 *            the transition definition
	 */
	public StandaloneTaskCompletedEvent(StandaloneTaskInstance instance, Operation operation,
			TransitionDefinition transitionDefinition) {
		super(instance);
		this.operation = operation;
		this.transitionDefinition = transitionDefinition;
	}

	/**
	 * Getter method for operation.
	 * 
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Getter method for transitionDefinition.
	 * 
	 * @return the transitionDefinition
	 */
	public TransitionDefinition getTransitionDefinition() {
		return transitionDefinition;
	}

}
