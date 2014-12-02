package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that standalone task will be claimed for execution
 *
 * @author bbanchev
 */
@Documentation("Fired on task claim operation of pool standalone task")
public class StandaloneTaskClaimEvent extends AbstractInstanceEvent<StandaloneTaskInstance> {

	/**
	 * Instantiates a new standalone task claim event.
	 *
	 * @param instance
	 *            the instance
	 */
	public StandaloneTaskClaimEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

}
