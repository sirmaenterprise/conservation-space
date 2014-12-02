package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when direct child is detached from the workflow task instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when direct child is detached from the workflow task instance.")
public class DetachedChildToTaskEvent extends InstanceDetachedEvent<TaskInstance> {

	/**
	 * Instantiates a new detached child to task event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToTaskEvent(TaskInstance target, Instance child) {
		super(target, child);
	}

}
