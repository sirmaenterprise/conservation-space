package com.sirma.itt.seip.instance.actions.move;

import java.io.Serializable;

import com.sirma.itt.seip.domain.instance.Instance;
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

	private Instance destination;

	private Instance target;

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	public Instance getDestination() {
		return destination;
	}

	public void setDestination(Instance destination) {
		this.destination = destination;
	}

	public Serializable getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(Serializable destinationId) {
		this.destinationId = destinationId;
	}

	public Instance getTarget() {
		return target;
	}

	public void setTarget(Instance target) {
		this.target = target;
	}

}
