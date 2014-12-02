package com.sirma.itt.emf.state.operation;

import javax.enterprise.event.Observes;

import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;

/**
 * Handles an events of type {@link OperationExecutedEvent}
 *
 * @author BBonev
 */
public interface OperationHandler {

	/**
	 * Handle event operation.
	 *
	 * @param event
	 *            the event
	 */
	void handleOperation(@Observes OperationExecutedEvent event);
}
