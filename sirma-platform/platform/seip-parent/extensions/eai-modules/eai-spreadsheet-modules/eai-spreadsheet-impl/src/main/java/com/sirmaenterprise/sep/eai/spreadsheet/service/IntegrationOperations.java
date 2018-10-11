package com.sirmaenterprise.sep.eai.spreadsheet.service;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.CREATE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;

import com.sirma.itt.seip.instance.state.Operation;

/**
 * Holder for operation constants.
 *
 * @author bbanchev
 */
public enum IntegrationOperations {
	/** Operation to integrate new object. */
	CREATE_OP(new Operation(CREATE)),
	/** Operation to update existing object. */
	UPDATE_OP(new Operation(EDIT_DETAILS));

	private Operation operation;

	/**
	 * Instantiates a new operation provider.
	 *
	 * @param operation
	 *            the operation
	 */
	IntegrationOperations(Operation operation) {
		this.operation = operation;
	}

	/**
	 * Gets the operation for this value.
	 *
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

}
