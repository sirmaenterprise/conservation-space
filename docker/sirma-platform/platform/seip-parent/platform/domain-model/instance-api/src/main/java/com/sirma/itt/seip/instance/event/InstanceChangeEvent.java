package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired on every save of an {@link Instance} before instance persist to DMS/LOCAL DB. The event is fired to
 * notify that the Instance has been changed and is going to be persisted. Changes done to the instance will be
 * persisted on the end of the event.
 *
 * @param <I>
 *            the Instance type
 * @author BBonev
 */
@Documentation("Event fired on every save of an {@link Instance} before instance persist to DMS/LOCAL DB. The event is fired to notify that the Instance has been changed and is going to be persisted. Changes done to the instance will be persisted on the end of the event.")
public class InstanceChangeEvent<I extends Instance> extends AbstractInstanceEvent<I> {

	/**
	 * Instantiates a new instance change event.
	 *
	 * @param instance
	 *            the instance
	 */
	public InstanceChangeEvent(I instance) {
		super(instance);
	}

}
