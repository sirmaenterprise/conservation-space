package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that standalone task will be activated from hold state.
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify that standalone task will be activated from hold state")
public class StandaloneTaskActivateEvent extends AbstractInstanceEvent<StandaloneTaskInstance> {

	/**
	 * Instantiates a new standalone task activate event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public StandaloneTaskActivateEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

}
