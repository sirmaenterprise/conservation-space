package com.sirma.itt.seip.event;

import java.lang.annotation.Annotation;

/**
 * Common entry point for firing events.
 *
 * @author BBonev
 */
public interface EventService {

	/**
	 * Fire an event using the given list of qualifiers if any.
	 *
	 * @param event
	 *            the event
	 * @param qualifiers
	 *            the qualifiers
	 */
	void fire(EmfEvent event, Annotation... qualifiers);

	/**
	 * Fire next phase of the given event
	 *
	 * @param event
	 *            the event
	 * @param qualifiers
	 *            the qualifiers
	 */
	default void fireNextPhase(TwoPhaseEvent event, Annotation... qualifiers) {
		if (event == null) {
			return;
		}
		TwoPhaseEvent nextPhaseEvent = event.getNextPhaseEvent();
		if (nextPhaseEvent != null) {
			fire(nextPhaseEvent, qualifiers);
		}
	}
}
