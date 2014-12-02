package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired when new direct child is attached to the project instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new direct child is attached to the project instance.")
public class AttachedChildToProjectEvent extends InstanceAttachedEvent<ProjectInstance> {

	/**
	 * Instantiates a new attached child to project event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToProjectEvent(ProjectInstance target, Instance child) {
		super(target, child);
	}

}
