package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before saving a {@link TaskInstance} to DMS/DB.
 * 
 * @author BBonev
 */
@Documentation("Event fired before saving a {@link TaskInstance} to DMS/DB.")
public class TaskChangeEvent extends InstanceChangeEvent<TaskInstance> {

	/**
	 * Instantiates a new task change event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public TaskChangeEvent(TaskInstance instance) {
		super(instance);
	}

}
