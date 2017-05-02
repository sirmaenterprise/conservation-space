package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;
import com.sirma.itt.seip.resources.Resource;

/**
 * Event fired after deleting of a resource
 *
 * @author BBonev
 */
public class AfterResourceDeleteEvent extends AfterInstanceDeleteEvent<Resource, TwoPhaseEvent> {

	/**
	 * Instantiates a new after resource delete event.
	 *
	 * @param instance
	 *            the instance
	 */
	public AfterResourceDeleteEvent(Resource instance) {
		super(instance);
	}

	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}

}
