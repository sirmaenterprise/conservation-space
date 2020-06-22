package com.sirma.itt.objects.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;

/**
 * Event fired when direct child is detached from the object instance.
 *
 * @author BBonev
 */
@Documentation("Event fired when direct child is detached from the object instance.")
public class DetachedChildToObjectEvent extends InstanceDetachedEvent<ObjectInstance> {

	/**
	 * Instantiates a new detached child to object event.
	 *
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToObjectEvent(ObjectInstance target, Instance child) {
		super(target, child);
	}

}
