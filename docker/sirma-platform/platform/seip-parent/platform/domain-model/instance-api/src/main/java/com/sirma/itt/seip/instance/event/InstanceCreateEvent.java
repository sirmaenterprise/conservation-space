package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired when new instance is created but is not displayed to the user, yet.
 *
 * @param <I>
 *            the instance type
 * @author BBonev
 */
@Documentation("Event fired when new instance is created but is not displayed to the user, yet.")
public class InstanceCreateEvent<I extends Instance> extends AbstractInstanceEvent<I> {

	/**
	 * Instantiates a new instance create event.
	 *
	 * @param instance
	 *            the instance
	 */
	public InstanceCreateEvent(I instance) {
		super(instance);
	}

}
