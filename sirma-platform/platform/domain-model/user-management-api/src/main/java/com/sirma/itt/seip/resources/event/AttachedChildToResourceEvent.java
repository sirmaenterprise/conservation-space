package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.resources.Resource;

/**
 * Event fired when child is added to the given target resource. Note: child could be added only to groups.
 *
 * @author BBonev
 */
@Documentation("Event fired when child is added to the given target resource. Note: child could be added only to groups.")
public class AttachedChildToResourceEvent extends InstanceAttachedEvent<Resource> {

	/**
	 * Instantiates a new attached child to resource event.
	 *
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToResourceEvent(Resource target, Instance child) {
		super(target, child);
	}

}
