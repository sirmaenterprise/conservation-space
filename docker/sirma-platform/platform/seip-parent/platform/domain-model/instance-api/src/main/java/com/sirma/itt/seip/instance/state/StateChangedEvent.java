package com.sirma.itt.seip.instance.state;

import java.io.Serializable;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired to notify that a instance object has a state change. The event provides the old state, the executed
 * operation and the new state.
 *
 * @author BBonev
 */
@Documentation("Event fired to notify that a instance object has a state change. The event provides the old state, the executed operation and the new state.")
public class StateChangedEvent extends AbstractInstanceEvent<Instance> {

	/** The operation. */
	private final Operation operation;

	/** The state. */
	private final String newState;

	private final String oldState;

	/**
	 * Instantiates a new state changed event.
	 *
	 * @param oldState
	 *            the old state
	 * @param operation
	 *            the operation
	 * @param newState
	 *            the state
	 * @param target
	 *            the target
	 */
	public StateChangedEvent(String oldState, Operation operation, String newState, Instance target) {
		super(target);
		this.oldState = oldState;
		this.operation = operation;
		this.newState = newState;
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
	 * Getter method for state.
	 *
	 * @return the state
	 */
	public String getNewState() {
		return newState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[oldState=");
		builder.append(oldState);
		builder.append(", operation=");
		builder.append(operation);
		builder.append(", newState=");
		builder.append(newState);
		builder.append(", instance=");
		builder.append(instance.getId());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for oldState.
	 *
	 * @return the oldState
	 */
	public String getOldState() {
		return oldState;
	}
}
