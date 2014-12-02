package com.sirma.itt.emf.event.instance;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on opening an instance by user.
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
