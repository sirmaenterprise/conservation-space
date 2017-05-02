package com.sirma.itt.seip.instance.actions.move;

import java.io.Serializable;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * When there is a request for a move operation execution, this class is used to contain the request information.
 *
 * @author nvelkov
 */
public class MoveActionRequest extends ActionRequest {

	protected static final String OPERATION_NAME = "move";

	private static final long serialVersionUID = 28222007700931031L;
	private Serializable destinationId;

	/**
	 * Getter method for destinationId.
	 *
	 * @return the destinationId
	 */
	public Serializable getDestinationId() {
		return destinationId;
	}

	/**
	 * Setter method for destinationId.
	 *
	 * @param destinationId
	 *            the destinationId to set
	 */
	public void setDestinationId(Serializable destinationId) {
		this.destinationId = destinationId;
	}

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

}
