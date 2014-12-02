package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when direct child is detached from the workflow instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when direct child is detached from the workflow instance.")
public class DetachedChildToWorkflowEvent extends InstanceDetachedEvent<WorkflowInstanceContext> {

	/**
	 * Instantiates a new detached child to workflow event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToWorkflowEvent(WorkflowInstanceContext target, Instance child) {
		super(target, child);
	}
}
