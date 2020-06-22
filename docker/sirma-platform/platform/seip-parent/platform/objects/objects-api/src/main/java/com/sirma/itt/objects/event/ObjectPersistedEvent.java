package com.sirma.itt.objects.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.event.InstancePersistedEvent;

/**
 * Event fired when {@link ObjectInstance} has been persisted.
 *
 * @author BBonev
 */
@Documentation("Event fired when {@link ObjectInstance} has been persisted.")
public class ObjectPersistedEvent extends InstancePersistedEvent<ObjectInstance> {

	/**
	 * Instantiates a new object persisted event.
	 *
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public ObjectPersistedEvent(ObjectInstance instance, ObjectInstance old, String operationId) {
		super(instance, old, operationId);
	}

}
