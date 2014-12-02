package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event when a owner of a {@link StandaloneTaskInstance} has been changed
 * 
 * @author BBonev
 */
@Documentation("Event when a owner of a {@link StandaloneTaskInstance} has been changed")
public class StandaloneTaskReassignEvent extends AbstractInstanceEvent<StandaloneTaskInstance> {

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
	public StandaloneTaskReassignEvent(StandaloneTaskInstance instance, String oldUser) {
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
