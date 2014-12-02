package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after starting a {@link StandaloneTaskInstance} in Activiti and before task
 * persisting. The event provides also the operation via which the task has been created.
 * 
 * @author BBonev
 */
@Documentation("Event fired after starting a {@link StandaloneTaskInstance} in Activiti and before task persisting. "
		+ "The event provides also the operation via which the task has been created.")
public class AfterStandaloneTaskStarEvent extends
		AbstractInstanceTwoPhaseEvent<StandaloneTaskInstance, TwoPhaseEvent> {

	/** The operation. */
	private final Operation operation;

	/**
	 * Instantiates a new after standalone task star event.
	 * 
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 */
	public AfterStandaloneTaskStarEvent(StandaloneTaskInstance instance, Operation operation) {
		super(instance);
		this.operation = operation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
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
