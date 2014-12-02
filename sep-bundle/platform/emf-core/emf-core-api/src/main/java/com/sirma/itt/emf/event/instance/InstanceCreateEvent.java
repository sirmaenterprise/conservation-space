package com.sirma.itt.emf.event.instance;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

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
