package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when new direct child is attached to the workflow task instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new direct child is attached to the workflow task instance.")
public class AttachedChildToTaskEvent extends InstanceAttachedEvent<TaskInstance> {

	/**
	 * Instantiates a new attach child to task event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToTaskEvent(TaskInstance target, Instance child) {
		super(target, child);
	}

}
