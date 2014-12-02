package com.sirma.itt.emf.state.event;

import java.io.Serializable;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that a instance object has a state change. The event provides the old
 * state, the executed operation and the new state.
 *
 * @author BBonev
 */
@Documentation("Event fired to notify that a instance object has a state change. The event provides the old state, the executed operation and the new state.")
public class StateChangedEvent extends AbstractInstanceEvent<Instance> implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2831935346895683557L;

	/** The operation. */
	private final String operation;

	/** The state. */
	private final String newState;

	private final String oldState;

	/**
	 * Instantiates a new state changed event.
	 * @param oldState
	 *            the old state
	 * @param operation
	 *            the operation
	 * @param newState
	 *            the state
	 * @param target
	 *            the target
	 */
	public StateChangedEvent(String oldState, String operation, String newState, Instance target) {
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
	public String getOperation() {
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
		builder.append(", instanceType=");
		builder.append(getInstance().getClass());
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
