package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired when direct child is detached from the project instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when direct child is detached from the project instance.")
public class DetachedChildToProjectEvent extends InstanceDetachedEvent<ProjectInstance> {

	/**
	 * Instantiates a new detached child to project event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToProjectEvent(ProjectInstance target, Instance child) {
		super(target, child);
	}

}
