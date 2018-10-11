package com.sirma.itt.objects.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.event.InstanceOpenEvent;

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
