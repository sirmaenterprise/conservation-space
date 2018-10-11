package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.AbstractInstanceEvent;
import com.sirma.itt.seip.resources.Resource;

/**
 * Event fired when resource change has been detected.
 *
 * @author BBonev
 */
@Documentation("Base event fired when resource change has been detected.")
public class ResourceUpdatedEvent extends AbstractInstanceEvent<Resource> {

	/** The old instance. */
	private final Resource oldInstance;

	/**
	 * Instantiates a new resource updated event.
	 *
	 * @param instance
	 *            the resource instance
	 * @param oldInstance
	 *            the old instance
	 */
	public ResourceUpdatedEvent(Resource instance, Resource oldInstance) {
		super(instance);
		this.oldInstance = oldInstance;
	}

	/**
	 * Getter method for oldInstance.
	 *
	 * @return the oldInstance
	 */
	public Resource getOldInstance() {
		return oldInstance;
	}

}
