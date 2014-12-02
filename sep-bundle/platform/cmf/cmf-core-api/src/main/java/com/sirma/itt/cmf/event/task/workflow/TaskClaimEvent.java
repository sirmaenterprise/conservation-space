package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that task will be claimed for execution
 *
 * @author bbanchev
 */
@Documentation("Fired on task claim operation of pool task part of workflow")
public class TaskClaimEvent extends AbstractInstanceEvent<TaskInstance> {

	/**
	 * Instantiates a new task claim event.
	 *
	 * @param instance
	 *            the instance
	 */
	public TaskClaimEvent(TaskInstance instance) {
		super(instance);
	}

}
