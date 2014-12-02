package com.sirma.itt.objects.event;

import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Event fired before object to be visualized on screen.
 *
 * @author BBonev
 */
@Documentation("Event fired before project to be visualized on screen.")
public class ObjectOpenEvent extends InstanceOpenEvent<ObjectInstance> {

	/**
	 * Instantiates a new object open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ObjectOpenEvent(ObjectInstance instance) {
		super(instance);
	}

}
