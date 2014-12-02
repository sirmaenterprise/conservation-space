package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event when a owner of a {@link TaskInstance} has been changed
 * 
 * @author BBonev
 */
@Documentation("Event when a owner of a {@link TaskInstance} has been changed")
public class TaskReassignEvent extends AbstractInstanceEvent<TaskInstance> {

	/** The old user. */
	private final String oldUser;

	/**
	 * Instantiates a new standalone task reassign event.
	 * 
	 * @param instance
	 *            the instance
	 * @param oldUser
	 *            the old user
	 */
	public TaskReassignEvent(TaskInstance instance, String oldUser) {
		super(instance);
		this.oldUser = oldUser;
	}

	/**
	 * Getter method for oldUser.
	 * 
	 * @return the oldUser
	 */
	public String getOldUser() {
		return oldUser;
	}
}
