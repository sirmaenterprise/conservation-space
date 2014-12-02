package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before {@link WorkflowInstanceContext} is persisted for the first time.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before {@link WorkflowInstanceContext} is beeing persisted for the first time.")
public class BeforeWorkflowPersistEvent extends
		BeforeInstancePersistEvent<WorkflowInstanceContext, AfterWorkflowPersistEvent> {

	/**
	 * Instantiates a new before workflow persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeWorkflowPersistEvent(WorkflowInstanceContext instance) {
		super(instance);
	}

	@Override
	protected AfterWorkflowPersistEvent createNextEvent() {
		return new AfterWorkflowPersistEvent(getInstance());
	}

}
