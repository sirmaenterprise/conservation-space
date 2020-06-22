package com.sirma.itt.objects.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.event.BeforeInstanceDeleteEvent;

/**
 * Event fired before the ObjectInstance deletion.
 *
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before the ObjectInstance deletion.")
public class BeforeObjectDeleteEvent extends BeforeInstanceDeleteEvent<ObjectInstance, AfterObjectDeleteEvent> {

	/**
	 * Instantiates a new before object delete event.
	 *
	 * @param instance
	 *            the instance
	 */
	public BeforeObjectDeleteEvent(ObjectInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterObjectDeleteEvent createNextEvent() {
		return new AfterObjectDeleteEvent(getInstance());
	}
}
