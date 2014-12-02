package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when Task instance is created.
 * 
 * @author BBonev
 */
@Documentation("Event fired when Task instance is created.")
public class TaskCreateEvent extends InstanceCreateEvent<TaskInstance> {

	/**
	 * Instantiates a new task create event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public TaskCreateEvent(TaskInstance instance) {
		super(instance);
	}

}
