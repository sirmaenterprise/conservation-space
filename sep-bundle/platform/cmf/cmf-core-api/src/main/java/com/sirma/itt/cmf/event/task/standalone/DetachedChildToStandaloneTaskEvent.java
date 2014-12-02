package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when direct child is detached from the standalone task instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when direct child is detached from the standalone task instance.")
public class DetachedChildToStandaloneTaskEvent extends
		InstanceDetachedEvent<StandaloneTaskInstance> {

	/**
	 * Instantiates a new detached child to standalone task event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToStandaloneTaskEvent(StandaloneTaskInstance target, Instance child) {
		super(target, child);
	}

}
