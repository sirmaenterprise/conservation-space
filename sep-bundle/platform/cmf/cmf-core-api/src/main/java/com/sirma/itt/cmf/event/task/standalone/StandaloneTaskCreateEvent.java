package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when new {@link StandaloneTaskInstance} has been created before user displayed to the
 * user.
 *
 * @author BBonev
 */
@Documentation("Event fired when new {@link StandaloneTaskInstance} has been created before user dysplayed to the user.")
public class StandaloneTaskCreateEvent extends InstanceCreateEvent<StandaloneTaskInstance> {

	/** The target. */
	private final Instance target;

	/**
	 * Instantiates a new standalone task created event.
	 *
	 * @param instance
	 *            the instance
	 * @param target
	 *            the target
	 */
	public StandaloneTaskCreateEvent(StandaloneTaskInstance instance, Instance target) {
		super(instance);
		this.target = target;
	}

	/**
	 * Getter method for target.
	 *
	 * @return the target
	 */
	public Instance getTarget() {
		return target;
	}

}
