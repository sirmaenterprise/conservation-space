package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after the workflow has been started into Activiti and before instance save.
 * 
 * @author BBonev
 */
@Documentation("Event fired after the workflow has been started into Activiti and before instance save.")
public class AfterWorkflowStartEvent extends
		AbstractInstanceTwoPhaseEvent<WorkflowInstanceContext, TwoPhaseEvent> implements
		OperationEvent {

	/** The operation id. */
	private final String operationId;

	/**
	 * Instantiates a new after workflow start event.
	 * 
	 * @param instance
	 *            the instance
	 * @param operationId
	 *            the operation id
	 */
	public AfterWorkflowStartEvent(WorkflowInstanceContext instance, String operationId) {
		super(instance);
		this.operationId = operationId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}

	/**
	 * Getter method for operationId.
	 * 
	 * @return the operationId
	 */
	@Override
	public String getOperationId() {
		return operationId;
	}

}
