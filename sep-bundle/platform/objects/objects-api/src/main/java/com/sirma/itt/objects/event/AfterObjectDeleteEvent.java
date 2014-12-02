package com.sirma.itt.objects.event;

import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Event fired after the ObjectInstance deletion.
 * 
 * @author BBonev
 */
@Documentation("Event fired after the ObjectInstance deletion.")
public class AfterObjectDeleteEvent extends AfterInstanceDeleteEvent<ObjectInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after object delete event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterObjectDeleteEvent(ObjectInstance instance) {
		super(instance);
	}

	/**
	 * Creates the next event.
	 * 
	 * @return the two phase event
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}

}
