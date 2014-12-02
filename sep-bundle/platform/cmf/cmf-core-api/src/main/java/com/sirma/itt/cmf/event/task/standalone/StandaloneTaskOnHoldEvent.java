package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that standalone task will be put on hold.
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that standalone task will be put on hold")
public class StandaloneTaskOnHoldEvent extends AbstractInstanceEvent<StandaloneTaskInstance> {

	/**
	 * Instantiates a new standalone task on hold event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public StandaloneTaskOnHoldEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

}
