package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on putting an workflow on hold.
 * 
 * @author BBonev
 */
@Documentation("Event fired on putting an workflow on hold.")
public class WorkflowHoldEvent extends AbstractInstanceEvent<WorkflowInstanceContext> implements
		OperationEvent {

	/**
	 * Instantiates a new workflow hold event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public WorkflowHoldEvent(WorkflowInstanceContext instance) {
		super(instance);
	}

	@Override
	public String getOperationId() {
		return "holdWorkflow";
	}

}
