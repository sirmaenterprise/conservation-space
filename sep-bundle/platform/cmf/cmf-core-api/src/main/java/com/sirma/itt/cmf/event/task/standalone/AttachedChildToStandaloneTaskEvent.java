package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when new direct child is attached to the standalone task instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new direct child is attached to the standalone task instance.")
public class AttachedChildToStandaloneTaskEvent extends
		InstanceAttachedEvent<StandaloneTaskInstance> {

	/**
	 * Instantiates a new attached child to standalone task event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToStandaloneTaskEvent(StandaloneTaskInstance target, Instance child) {
		super(target, child);
	}

}
