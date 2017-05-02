package com.sirma.itt.seip.instance.content.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired when a content is checked out.
 *
 * @author nvelkov
 */
public class CheckOutEvent extends AbstractInstanceEvent<Instance> {

	/**
	 * Instantiates a new checkOut event.
	 *
	 * @param instance
	 *            the instance
	 */
	public CheckOutEvent(Instance instance) {
		super(instance);
	}

}
