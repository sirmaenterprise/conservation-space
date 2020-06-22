package com.sirma.itt.seip.instance.state;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Represents an state change operation. The caller may define the next primary and/or secondary state.
 * <p>
 * The operation object supports a 2 separate states and types of operations. The normal mode is the external operation
 * passed from the user (rest or other public service). The methods are in experimental phase and are subject to change.
 *
 * @author BBonev
 */
public class Operation implements Serializable {

	private static final long serialVersionUID = 7196191869122463140L;

	/** The operation. */
	private String operation;

	/** The next primary state. */
	private String nextPrimaryState;

	/** The next secondary state. */
	private String nextSecondaryState;

	/** The real executed operation */
	private String userOperationId;

	/** show is the operation user or not */
	private boolean isUserOperation;

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
	 * Gets the operation id.
	 *
	 * @param operation
	 *            the operation
	 * @return the operation id or <code>null</code> if the operation object is <code>null</code>.
	 */
	public static String getOperationId(Operation operation) {
		if (operation == null) {
			return null;
		}
		return operation.getOperation();
	}

	/**
	 * Gets the user operation id.
	 *
	 * @param operation
	 *            the operation
	 * @return the user operation id or <code>null</code> if the operation object is <code>null</code>.
	 */
	public static String getUserOperationId(Operation operation) {
		if (operation == null) {
			return null;
		}
		return operation.getUserOperationId();
	}

	/**
	 * Checks if the operation id of the given operation object is the same as the given operation id.
	 *
	 * @param operation
	 *            the operation
	 * @param toCheck
	 *            the to check
	 * @return true, if the operation ids match.
	 */
	public static boolean isOperationAs(Operation operation, String toCheck) {
		return EqualsHelper.nullSafeEquals(getOperationId(operation), toCheck);
	}

	/**
	 * Checks if the operation id of the given operation object is the same as the given operation id.
	 *
	 * @param operation
	 *            the operation
	 * @param toCheck
	 *            the to check
	 * @return true, if the operation ids match.
	 */
	public static boolean isUserOperationAs(Operation operation, String toCheck) {
		return EqualsHelper.nullSafeEquals(getUserOperationId(operation), toCheck);
	}

	/**
	 * Checks if the operation id of the given operation object is the same as the given operation id.
	 *
	 * @param operation
	 *            the operation
	 * @param toCheck
	 *            the to check
	 * @param others
	 *            the others
	 * @return true, if the operation ids match.
	 */
	public static boolean isUserOperationAs(Operation operation, String toCheck, String... others) {
		if (EqualsHelper.nullSafeEquals(getUserOperationId(operation), toCheck)) {
			return true;
		}
		if (others == null || others.length == 0) {
			return false;
		}
		for (int i = 0; i < others.length; i++) {
			if (EqualsHelper.nullSafeEquals(getUserOperationId(operation), others[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the operation is not null
	 *
	 * @param operation
	 *            the operation
	 * @return true, if the operation object is not <code>null</code> and the operation id is not <code>null</code>.
	 */
	public static boolean isSet(Operation operation) {
		return StringUtils.isNotBlank(getOperationId(operation));
	}

	/**
	 * Gets the not null operation object. If the operation argument is not <code>null</code> and the operation id is
	 * not <code>null</code> then the operation object will be returned otherwise new operation object will be returned
	 * initialized with the given default operation id.
	 *
	 * @param operation
	 *            the operation to check and return if the object is not <code>null</code> and the operation id is not
	 *            <code>null</code>
	 * @param defaultOperation
	 *            the default operation to return if the operation parameter is not set
	 * @return the not <code>null</code> {@link Operation}
	 * @see Operation#isSet(Operation)
	 */
	public static Operation getNotNull(Operation operation, String defaultOperation) {
		return isSet(operation) ? operation : new Operation(defaultOperation);
	}

	/**
	 * Gets the not null copy of the given operation object. If the operation argument is not <code>null</code> and the
	 * operation id is not <code>null</code> then a copy of the operation object will be returned otherwise new
	 * operation object will be returned initialized with the given default operation id.
	 *
	 * @param operation
	 *            the operation to check and return if the object is not <code>null</code> and the operation id is not
	 *            <code>null</code>
	 * @param defaultOperation
	 *            the default operation to return if the operation parameter is not set
	 * @return the not <code>null</code> {@link Operation} that is copy of the original operation or new initialized
	 *         with the given operation id.
	 * @see Operation#isSet(Operation)
	 */
	public static Operation getNotNullCopy(Operation operation, String defaultOperation) {
		return isSet(operation) ? copy(operation) : new Operation(defaultOperation);
	}

	/**
	 * Copy the operation argument to new object
	 *
	 * @param toCopy
	 *            the to copy
	 * @return the operation copy or <code>null</code> if the source is <code>null</code>.
	 */
	public static Operation copy(Operation toCopy) {
		if (toCopy == null) {
			return null;
		}
		return new Operation(toCopy.getOperation(), toCopy.getUserOperationId(), toCopy.isUserOperation(),
				toCopy.getNextPrimaryState(), toCopy.getNextSecondaryState());
	}

	/**
	 * Sets the operation id but keep user operation.
	 * <p>
	 * If the operation object is <code>null</code> new operation object will be returned with operation id and user
	 * operation same as the given operation id. <br>
	 * If the operation object is not <code>null</code> then it will be updated with the given operation id but the user
	 * operation will be kept.
	 *
	 * @param operationToUpdate
	 *            the operation to update
	 * @param operationToSet
	 *            the operation id to set
	 * @return new operation instance or one updated with the given operationToSet.
	 */
	public static Operation setOperationId(Operation operationToUpdate, String operationToSet) {
		Operation operation = getNotNullCopy(operationToUpdate, operationToSet);
		operation.setOperation(operationToSet);
		return operation;
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
		this(operation, operation);
	}

	/**
	 * Instantiates a new operation.
	 *
	 * @param operation
	 *            the operation
	 * @param isUserOperation
	 *            true if it's user operation. False otherwise
	 */
	public Operation(String operation, boolean isUserOperation) {
		this(operation, operation, isUserOperation);
	}

	/**
	 * Instantiates a new operation.
	 *
	 * @param operation
	 *            the operation
	 * @param userOperationId
	 *            the user operation
	 */
	public Operation(String operation, String userOperationId) {
		this(operation, userOperationId, false, null, null);
	}

	/**
	 * Instantiates a new operation.
	 *
	 * @param operation
	 *            the operation
	 * @param userOperationId
	 *            the user operation
	 * @param isUserOperation
	 *            true if it's user operation. False otherwise
	 */
	public Operation(String operation, String userOperationId, boolean isUserOperation) {
		this(operation, userOperationId, isUserOperation, null, null);
	}

	/**
	 * Instantiates a new operation.
	 *
	 * @param operation
	 *            the operation
	 * @param nextPrimaryState
	 *            the next primary state
	 * @param nextSecondaryState
	 *            the next secondary state
	 */
	public Operation(String operation, String nextPrimaryState, String nextSecondaryState) {
		this(operation, operation, false, nextPrimaryState, nextSecondaryState);
	}

	/**
	 * Instantiates a new operation.
	 *
	 * @param operation
	 *            the operation
	 * @param userOperationId
	 *            the user operation
	 * @param isUserOperation
	 *            true if it's user operation. False otherwise
	 * @param nextPrimaryState
	 *            the next primary state
	 * @param nextSecondaryState
	 *            the next secondary state
	 */
	public Operation(String operation, String userOperationId, boolean isUserOperation, String nextPrimaryState,
			String nextSecondaryState) {
		this.operation = operation;
		this.userOperationId = userOperationId;
		this.isUserOperation = isUserOperation;
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
	 * @param operation
	 *            the operation to set
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
	 * @param nextPrimaryState
	 *            the nextPrimaryState to set
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
	 * @param nextSecondaryState
	 *            the nextSecondaryState to set
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
		builder.append(", userOperationId=");
		builder.append(userOperationId);
		builder.append(", isUserOperation=");
		builder.append(isUserOperation);
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
		result = prime * result + (operation == null ? 0 : operation.hashCode());
		result = prime * result + (userOperationId == null ? 0 : userOperationId.hashCode());
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
		if (!(obj instanceof Operation)) {
			return false;
		}
		Operation other = (Operation) obj;
		return EqualsHelper.nullSafeEquals(userOperationId, other.userOperationId)
				&& EqualsHelper.nullSafeEquals(operation, other.operation);
	}

	/**
	 * Getter method for userOperationId.
	 *
	 * @return the userOperationId
	 */
	public String getUserOperationId() {
		return userOperationId;
	}

	/**
	 * Setter method for userOperationId.
	 *
	 * @param userOperationId
	 *            the userOperationId to set
	 */
	public void setUserOperationId(String userOperationId) {
		this.userOperationId = userOperationId;
	}

	/**
	 * Getter method for isUserOperation.
	 *
	 * @return true is it's a user operation false otherwise
	 */
	public boolean isUserOperation() {
		return isUserOperation;
	}

}