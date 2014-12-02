package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link StandaloneTaskInstance} has been persisted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link StandaloneTaskInstance} has been persisted.")
public class StandaloneTaskPersistedEvent extends InstancePersistedEvent<StandaloneTaskInstance> {

	/**
	 * Instantiates a new standalone task persisted event.
	 * 
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public StandaloneTaskPersistedEvent(StandaloneTaskInstance instance,
			StandaloneTaskInstance old, String operationId) {
		super(instance, old, operationId);
	}

}
