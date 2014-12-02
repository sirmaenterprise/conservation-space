package com.sirma.itt.emf.resources.event;

import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when child is removed from the given target resource. Note: child could be removed
 * only from groups.
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
