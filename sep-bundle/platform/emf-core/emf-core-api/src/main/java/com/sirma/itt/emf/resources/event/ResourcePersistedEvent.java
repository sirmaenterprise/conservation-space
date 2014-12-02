package com.sirma.itt.emf.resources.event;

import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.util.Documentation;

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
