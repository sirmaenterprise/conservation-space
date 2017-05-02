package com.sirma.itt.seip.instance.state;

import java.io.Serializable;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired to notify that a concrete operation on an instance is executed. Used to trigger an instance state change
 * check.
 *
 * @author BBonev
 */
@Documentation("Event fired to notify that a concrete operation on an instance is executed. Used to trigger an instance state change check.")
public class OperationExecutedEvent extends AbstractInstanceEvent<Instance>implements Serializable, OperationEvent {

	private static final long serialVersionUID = 32580879612106680L;

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
		return Operation.getOperationId(getOperation());
	}

}
