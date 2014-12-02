package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before starting a {@link StandaloneTaskInstance} in Activiti. The event provides the
 * operation used by user to complete the operation.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before starting a {@link StandaloneTaskInstance} in Activiti. "
		+ "The event provides the operation used by user to complete the operation.")
public class BeforeStandaloneTaskStartEvent extends
		AbstractInstanceTwoPhaseEvent<StandaloneTaskInstance, AfterStandaloneTaskStarEvent> {

	/** The operation. */
	private final Operation operation;

	/**
	 * Instantiates a new before standalone task start event.
	 * 
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 */
	public BeforeStandaloneTaskStartEvent(StandaloneTaskInstance instance, Operation operation) {
		super(instance);
		this.operation = operation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterStandaloneTaskStarEvent createNextEvent() {
		return new AfterStandaloneTaskStarEvent(getInstance(), getOperation());
	}

	/**
	 * Getter method for operation.
	 * 
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

}
