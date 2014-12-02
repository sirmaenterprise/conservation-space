package com.sirma.itt.objects.event;

import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Event fired when new direct child is attached to the object instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new direct child is attached to the object instance.")
public class AttachedChildToObjectEvent extends InstanceAttachedEvent<ObjectInstance> {

	/**
	 * Instantiates a new attach child to object event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToObjectEvent(ObjectInstance target, Instance child) {
		super(target, child);
	}

}
