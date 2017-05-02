package com.sirma.itt.objects.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;

/**
 * Event fired before saving a {@link ObjectInstance} to DMS/DB.
 *
 * @author BBonev
 */
@Documentation("Event fired before saving a {@link ObjectInstance} to DMS/DB.")
public class ObjectChangeEvent extends InstanceChangeEvent<ObjectInstance> {

	/**
	 * Instantiates a new object change event.
	 *
	 * @param instance
	 *            the instance
	 */
	public ObjectChangeEvent(ObjectInstance instance) {
		super(instance);
	}

}
