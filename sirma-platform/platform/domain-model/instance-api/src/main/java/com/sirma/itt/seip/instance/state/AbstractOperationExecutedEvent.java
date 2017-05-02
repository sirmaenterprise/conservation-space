package com.sirma.itt.seip.instance.state;

import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;

/**
 * Abstract class for operation executed events (Before and After, see
 * successors).
 *
 * @author Ivo Rusev
 *
 */
public class AbstractOperationExecutedEvent extends AbstractInstanceTwoPhaseEvent<Instance, TwoPhaseEvent>
		implements OperationEvent {

	/** The operation. */
	protected final Operation operation;

	/**
	 * Constructor.
	 *
	 * @param operation
	 *            the operation
	 * @param target
	 *            the target instance
	 */
	public AbstractOperationExecutedEvent(Operation operation, Instance target) {
		super(target);
		this.operation = operation;
	}

	/**
	 * Getter method for operation.
	 *
	 * @return the operation
	 */
	@Override
	public Operation getOperation() {
		return operation;
	}

	@Override
	public String getOperationId() {
		return Operation.getOperationId(getOperation());
	}

	@Override
	protected AfterOperationExecutedEvent createNextEvent() {
		return null;
	}

	/**
	 * Creates a String that contains information about the event like operation id, target, etc. In the successors is
	 * added the name of the concrete event.
	 *
	 * @return events info
	 */
	protected StringBuilder getEventInformation() {
		StringBuilder builder = new StringBuilder();
		builder.append(operation);
		builder.append(", target=");
		builder.append(getInstance());
		builder.append("]");
		return builder;
	}

}
