package com.sirma.itt.emf.state.operation;

import java.io.Serializable;

import com.sirma.itt.emf.event.OperationEvent;

/**
 * Represents an state change operation. The caller may define the next primary
 * and/or secondary state.
 *
 * @author BBonev
 */
public class Operation implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -922660675182387815L;

	/** The operation. */
	private String operation;

	/** The next primary state. */
	private String nextPrimaryState;

	/** The next secondary state. */
	private String nextSecondaryState;

	/** The Constant NO_OPERATION. */
	public static final Operation NO_OPERATION = new Operation(null);

	/**
	 * Creates the operation instance based on the given {@link OperationEvent}.
	 * 
	 * @param event
	 *            the event
	 * @return the operation
	 */
	public static Operation createOperation(OperationEvent event) {
		return new Operation(event.getOperationId());
	}

	/**
	 * Instantiates a new operation.
	 */
	public Operation() {
		// nothing to do here
	}

	/**
	 * Instantiates a new operation.
	 *
	 * @param operation
	 *            the operation
	 */
	public Operation(String operation) {
		this.operation = operation;
	}

	/**
	 * Instantiates a new operation.
	 *
	 * @param operation the operation
	 * @param nextPrimaryState the next primary state
	 * @param nextSecondaryState the next secondary state
	 */
	public Operation(String operation, String nextPrimaryState, String nextSecondaryState) {
		this.operation = operation;
		this.nextPrimaryState = nextPrimaryState;
		this.nextSecondaryState = nextSecondaryState;
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
	 * Setter method for operation.
	 *
	 * @param operation the operation to set
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}
	/**
	 * Getter method for nextPrimaryState.
	 *
	 * @return the nextPrimaryState
	 */
	public String getNextPrimaryState() {
		return nextPrimaryState;
	}
	/**
	 * Setter method for nextPrimaryState.
	 *
	 * @param nextPrimaryState the nextPrimaryState to set
	 */
	public void setNextPrimaryState(String nextPrimaryState) {
		this.nextPrimaryState = nextPrimaryState;
	}
	/**
	 * Getter method for nextSecondaryState.
	 *
	 * @return the nextSecondaryState
	 */
	public String getNextSecondaryState() {
		return nextSecondaryState;
	}
	/**
	 * Setter method for nextSecondaryState.
	 *
	 * @param nextSecondaryState the nextSecondaryState to set
	 */
	public void setNextSecondaryState(String nextSecondaryState) {
		this.nextSecondaryState = nextSecondaryState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Operation [operation=");
		builder.append(operation);
		builder.append(", nextPrimaryState=");
		builder.append(nextPrimaryState);
		builder.append(", nextSecondaryState=");
		builder.append(nextSecondaryState);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((operation == null) ? 0 : operation.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Operation)) {
			return false;
		}
		Operation other = (Operation) obj;
		if (operation == null) {
			if (other.operation != null) {
				return false;
			}
		} else if (!operation.equals(other.operation)) {
			return false;
		}
		return true;
	}

}
