package com.sirma.itt.emf.event.instance;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on every save of an {@link Instance} before instance persist to DMS/LOCAL DB. The
 * event is fired to notify that the Instance has been changed and is going to be persisted. Changes
 * done to the instance will be persisted on the end of the event.
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
