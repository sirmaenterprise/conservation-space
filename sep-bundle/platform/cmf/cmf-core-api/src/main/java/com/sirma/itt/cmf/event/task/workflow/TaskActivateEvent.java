package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that task will be activated from hold state
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that task will be activated from hold state")
public class TaskActivateEvent extends AbstractInstanceEvent<TaskInstance> {

	/**
	 * Instantiates a new task activate event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public TaskActivateEvent(TaskInstance instance) {
		super(instance);
	}

}
