package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that task will be released to pool for claiming again.
 *
 * @author bbanchev
 */
@Documentation("Fired on task release to pool operation of task from pool")
public class TaskReleaseEvent extends AbstractInstanceEvent<TaskInstance> {

	/**
	 * Instantiates a new task release event.
	 *
	 * @param instance
	 *            the instance
	 */
	public TaskReleaseEvent(TaskInstance instance) {
		super(instance);
	}

}
