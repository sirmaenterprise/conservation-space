package com.sirma.itt.objects.event;

import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.objects.domain.model.ObjectInstance;

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
