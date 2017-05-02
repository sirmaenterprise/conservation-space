package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;
import com.sirma.itt.seip.resources.Resource;

/**
 * Event fired when child is removed from the given target resource. Note: child could be removed only from groups.
 *
 * @author BBonev
 */
@Documentation("Event fired when child is removed from the given target resource. Note: child could be removed only from groups.")
public class DetachedChildToResourceEvent extends InstanceDetachedEvent<Resource> {

	/**
	 * Instantiates a new detached child to resource event.
	 *
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToResourceEvent(Resource target, Instance child) {
		super(target, child);
	}

}
