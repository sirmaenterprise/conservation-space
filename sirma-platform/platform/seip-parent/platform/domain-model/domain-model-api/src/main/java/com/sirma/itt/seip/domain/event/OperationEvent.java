package com.sirma.itt.seip.domain.event;


import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Event that provides an operation identifier. These events can be used for instance state management
 *
 * @author BBonev
 */
public interface OperationEvent extends EmfEvent {

	/**
	 * Gets the operation id that this event corresponds to.
	 *
	 * @return the operation id
	 */
	String getOperationId();

	/**
	 * Gets the operation object that represent the event
	 *
	 * @return the operation
	 */
	default Operation getOperation() {
		return new Operation(getOperationId());
	}

}
