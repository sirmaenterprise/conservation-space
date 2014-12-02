package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceCancelEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after workflow has been canceled in Activiti and before save.
 * 
 * @author BBonev
 */
@Documentation("Event fired after workflow has been canceled in Activiti and before save.")
public class AfterWorkflowCancelEvent extends
		AfterInstanceCancelEvent<WorkflowInstanceContext, TwoPhaseEvent> implements
		OperationEvent {

	/** The operation id. */
	private final String operationId;

	/**
	 * Instantiates a new after workflow cancel event.
	 * 
	 * @param instance
	 *            the instance
	 * @param operationId
	 *            the operation id
	 */
	public AfterWorkflowCancelEvent(WorkflowInstanceContext instance, String operationId) {
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
