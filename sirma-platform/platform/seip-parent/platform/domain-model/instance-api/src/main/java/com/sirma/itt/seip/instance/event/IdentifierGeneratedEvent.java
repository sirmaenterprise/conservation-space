package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Fired after instance identifier has been generated and set in the instance.
 * 
 * @author svelikov
 */
public class IdentifierGeneratedEvent extends AbstractInstanceEvent<Instance> {

	/**
	 * Instantiates the event.
	 *
	 * @param instance
	 *            the instance
	 */
	public IdentifierGeneratedEvent(Instance instance) {
		super(instance);
	}
}
