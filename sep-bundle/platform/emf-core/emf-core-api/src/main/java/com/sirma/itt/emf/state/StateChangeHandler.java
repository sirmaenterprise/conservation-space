package com.sirma.itt.emf.state;

import javax.enterprise.event.Observes;

import com.sirma.itt.emf.state.event.StateChangedEvent;

/**
 * Handles an events of type {@link StateChangedEvent}
 *
 * @author BBonev
 */
public interface StateChangeHandler {

	/**
	 * Handle event operation.
	 *
	 * @param event
	 *            the event
	 */
	void handleOperation(@Observes StateChangedEvent event);
}
