package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.AbstractInstanceEvent;
import com.sirma.itt.seip.resources.Resource;

/**
 * Event fired when new resource has been added to the system.
 *
 * @author BBonev
 */
@Documentation("Event fired when new resource has been added to the system.")
public class ResourceAddedEvent extends AbstractInstanceEvent<Resource> {

	/**
	 * Instantiates a new resource added event.
	 *
	 * @param instance
	 *            the resource instance
	 */
	public ResourceAddedEvent(Resource instance) {
		super(instance);
	}

}
