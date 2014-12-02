package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when a workflow is to be opened for preview.
 * 
 * @author svelikov
 */
@Documentation("Event fired when a workflow is to be opened for preview.")
public class WorkflowOpenEvent extends InstanceOpenEvent<WorkflowInstanceContext> {

	/**
	 * Instantiates a new workflow open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public WorkflowOpenEvent(WorkflowInstanceContext instance) {
		super(instance);
	}

}
