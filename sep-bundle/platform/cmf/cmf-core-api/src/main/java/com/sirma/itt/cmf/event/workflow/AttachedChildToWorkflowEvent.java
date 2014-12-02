package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when new direct child is attached to the workflow instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new direct child is attached to the workflow instance.")
public class AttachedChildToWorkflowEvent extends InstanceAttachedEvent<WorkflowInstanceContext> {

	/**
	 * Instantiates a new attach child to workflow event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToWorkflowEvent(WorkflowInstanceContext target, Instance child) {
		super(target, child);
	}
}
