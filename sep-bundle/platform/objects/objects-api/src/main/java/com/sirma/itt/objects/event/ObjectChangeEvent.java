package com.sirma.itt.objects.event;

import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.objects.domain.model.ObjectInstance;

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
