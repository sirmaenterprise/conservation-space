package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that task will be put on hold
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that task will be put on hold")
public class TaskOnHoldEvent extends AbstractInstanceEvent<TaskInstance> {

	/**
	 * Instantiates a new task on hold event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public TaskOnHoldEvent(TaskInstance instance) {
		super(instance);
	}

}
