package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify that standalone task will be released to pool for claiming again.
 *
 * @author bbanchev
 */
@Documentation("Fired on task release to pool operation of standalone task from pool")
public class StandaloneTaskReleaseEvent extends AbstractInstanceEvent<StandaloneTaskInstance> {

	/**
	 * Instantiates a new standalone task release event.
	 *
	 * @param instance
	 *            the instance
	 */
	public StandaloneTaskReleaseEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

}
