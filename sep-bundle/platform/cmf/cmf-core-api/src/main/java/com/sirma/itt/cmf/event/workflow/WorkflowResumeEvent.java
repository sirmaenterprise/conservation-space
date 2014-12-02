package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when workflow has been activated after have been placed on hold.
 * 
 * @author BBonev
 */
@Documentation("Event fired when workflow has been activated after have been placed on hold.")
public class WorkflowResumeEvent extends AbstractInstanceEvent<WorkflowInstanceContext> implements
		OperationEvent {

	/**
	 * Instantiates a new workflow resume event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public WorkflowResumeEvent(WorkflowInstanceContext instance) {
		super(instance);
	}

	@Override
	public String getOperationId() {
		return "activateWorkflow";
	}

}
