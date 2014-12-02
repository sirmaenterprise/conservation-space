package com.sirma.itt.emf.resources.event;

import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when child is added to the given target resource. Note: child could be added only to
 * groups.
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
