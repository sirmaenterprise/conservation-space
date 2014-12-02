package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before workflow to be started in Activiti engine.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before workflow to be started in Activiti engine.")
public class BeforeWorkflowStartEvent extends
		AbstractInstanceTwoPhaseEvent<WorkflowInstanceContext, AfterWorkflowStartEvent> implements
		OperationEvent {

	/** The operation id. */
	private final String operationId;

	/**
	 * Instantiates a new before workflow start event.
	 * 
	 * @param instance
	 *            the instance
	 * @param operationId
	 *            the operation id
	 */
	public BeforeWorkflowStartEvent(WorkflowInstanceContext instance, String operationId) {
		super(instance);
		this.operationId = operationId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterWorkflowStartEvent createNextEvent() {
		return new AfterWorkflowStartEvent(getInstance(), getOperationId());
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
