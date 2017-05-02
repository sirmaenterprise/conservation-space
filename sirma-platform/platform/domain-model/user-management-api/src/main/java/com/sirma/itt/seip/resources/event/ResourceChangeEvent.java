package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;
import com.sirma.itt.seip.resources.Resource;

/**
 * Event filed to notify for changes in the resource object
 *
 * @author BBonev
 */
@Documentation("Event filed to notify for changes in the resource object")
public class ResourceChangeEvent extends InstanceChangeEvent<Resource> {

	/**
	 * Instantiates a new resource change event.
	 *
	 * @param instance
	 *            the instance
	 */
	public ResourceChangeEvent(Resource instance) {
		super(instance);
	}

}
