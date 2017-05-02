package com.sirma.itt.objects.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.event.InstanceCreateEvent;

/**
 * Event fired when new {@link ObjectInstance} is created and his properties has been populated just before the instance
 * is displayed to the user.
 *
 * @author BBonev
 */
@Documentation("Event fired when new {@link ObjectInstance} is created and his properties has been populated just before the instance is displayed to the user.")
public class ObjectCreateEvent extends InstanceCreateEvent<ObjectInstance> {

	/**
	 * Instantiates a new object create event.
	 *
	 * @param instance
	 *            the instance
	 */
	public ObjectCreateEvent(ObjectInstance instance) {
		super(instance);
	}

}
