package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before saving a {@link StandaloneTaskInstance} to DMS/DB.
 * 
 * @author BBonev
 */
@Documentation("Event fired before saving a {@link StandaloneTaskInstance} to DMS/DB.")
public class StandaloneTaskChangeEvent extends InstanceChangeEvent<StandaloneTaskInstance> {

	/**
	 * Instantiates a new standalone task change event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public StandaloneTaskChangeEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

}
