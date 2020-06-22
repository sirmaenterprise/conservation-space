package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.instance.event.BeforeInstanceDeleteEvent;
import com.sirma.itt.seip.resources.Resource;

/**
 * Event fired before deleting of a resource
 *
 * @author BBonev
 */
@SuppressWarnings("unchecked")
public class BeforeResourceDeleteEvent extends BeforeInstanceDeleteEvent<Resource, AfterResourceDeleteEvent> {

	/**
	 * Instantiates a new after resource delete event.
	 *
	 * @param instance
	 *            the instance
	 */
	public BeforeResourceDeleteEvent(Resource instance) {
		super(instance);
	}

	@Override
	protected AfterResourceDeleteEvent createNextEvent() {
		return new AfterResourceDeleteEvent(getInstance());
	}

}
