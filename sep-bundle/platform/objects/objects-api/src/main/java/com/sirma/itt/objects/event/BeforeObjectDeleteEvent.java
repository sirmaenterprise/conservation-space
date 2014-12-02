package com.sirma.itt.objects.event;

import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Event fired before the ObjectInstance deletion.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before the ObjectInstance deletion.")
public class BeforeObjectDeleteEvent extends
		BeforeInstanceDeleteEvent<ObjectInstance, AfterObjectDeleteEvent> {

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
