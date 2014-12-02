package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link TaskInstance} has been persisted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link TaskInstance} has been persisted.")
public class TaskPersistedEvent extends InstancePersistedEvent<TaskInstance> {

	/**
	 * Instantiates a new task persisted event.
	 * 
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public TaskPersistedEvent(TaskInstance instance, TaskInstance old, String operationId) {
		super(instance, old, operationId);
	}

}
