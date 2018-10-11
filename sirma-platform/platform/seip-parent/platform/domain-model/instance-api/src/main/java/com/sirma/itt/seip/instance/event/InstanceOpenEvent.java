package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired on opening an instance by user.
 *
 * TODO: this event is not fired / used at all at the moment. Perhaps we should remove it?
 *
 * @param <I>
 *            the Instance type
 * @author BBonev
 */
@Documentation("Event fired on opening an instance by user.")
public class InstanceOpenEvent<I extends Instance> extends AbstractInstanceEvent<I> {

	/**
	 * Instantiates a new instance open event.
	 *
	 * @param instance
	 *            the instance
	 */
	public InstanceOpenEvent(I instance) {
		super(instance);
	}
}
