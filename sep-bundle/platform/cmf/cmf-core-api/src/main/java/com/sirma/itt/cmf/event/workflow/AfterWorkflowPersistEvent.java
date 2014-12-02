package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after {@link WorkflowInstanceContext} is persisted for the first time.
 * 
 * @author BBonev
 */
@Documentation("Event fired after {@link WorkflowInstanceContext} is beeing persisted for the first time.")
public class AfterWorkflowPersistEvent extends
		AfterInstancePersistEvent<WorkflowInstanceContext, TwoPhaseEvent> {

	/**
	 * Instantiates a new before workflow persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterWorkflowPersistEvent(WorkflowInstanceContext instance) {
		super(instance);
	}

}
