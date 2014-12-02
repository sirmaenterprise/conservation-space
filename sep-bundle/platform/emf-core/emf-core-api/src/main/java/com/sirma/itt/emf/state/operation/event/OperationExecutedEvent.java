package com.sirma.itt.emf.state.operation.event;

import java.io.Serializable;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that a concrete operation on an instance is executed. Used to trigger an
 * instance state change check.
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that a concrete operation on an instance is executed. Used to trigger an instance state change check.")
public class OperationExecutedEvent extends AbstractInstanceEvent<Instance> implements
		Serializable, OperationEvent {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 32580879612106680L;

	/** The operation. */
	private final Operation operation;

	/**
	 * Instantiates a new operation executed event.
	 *
	 * @param operation
	 *            the operation
	 * @param target
	 *            the target
	 */
	public OperationExecutedEvent(Operation operation, Instance target) {
		super(target);
		this.operation = operation;
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OperationExecutedEvent [operation=");
		builder.append(operation);
		builder.append(", target=");
		builder.append(getInstance());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getOperationId() {
		return getOperation() != null ? getOperation().getOperation() : null;
	}

}
