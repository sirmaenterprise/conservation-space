package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before saving a {@link WorkflowInstanceContext} to DMS/DB.
 * 
 * @author BBonev
 */
@Documentation("Event fired before saving a {@link WorkflowInstanceContext} to DMS/DB.")
public class WorkflowChangeEvent extends InstanceChangeEvent<WorkflowInstanceContext> {

	/**
	 * Instantiates a new workflow change event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public WorkflowChangeEvent(WorkflowInstanceContext instance) {
		super(instance);
	}

}
