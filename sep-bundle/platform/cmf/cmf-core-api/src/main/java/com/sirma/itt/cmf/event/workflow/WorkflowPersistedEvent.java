package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link WorkflowInstanceContext} has been persisted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link WorkflowInstanceContext} has been persisted.")
public class WorkflowPersistedEvent extends InstancePersistedEvent<WorkflowInstanceContext> {

	/**
	 * Instantiates a new workflow persisted event.
	 * 
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public WorkflowPersistedEvent(WorkflowInstanceContext instance, WorkflowInstanceContext old,
			String operationId) {
		super(instance, old, operationId);
	}

}
