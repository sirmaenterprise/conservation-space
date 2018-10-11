package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.event.InstancePersistedEvent;
import com.sirma.itt.seip.resources.Resource;

/**
 * Event fired when a resource is persisted.
 *
 * @author BBonev
 */
@Documentation("Event fired when a resource is persisted")
public class ResourcePersistedEvent extends InstancePersistedEvent<Resource> {

	/**
	 * Instantiates a new resource persisted event.
	 *
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public ResourcePersistedEvent(Resource instance, Resource old, String operationId) {
		super(instance, old, operationId);
	}

}
