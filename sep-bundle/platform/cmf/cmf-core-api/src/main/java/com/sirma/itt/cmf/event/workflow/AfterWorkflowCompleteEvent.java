package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after the workflow has been completed and workflow has been removed from the list of
 * active workflows from the owning case and all properties associated with the completion has been
 * set.
 * 
 * @author BBonev
 */
@Documentation("Event fired after the workflow has been completed and workflow has been removed from the list of active workflows from the owning case and all properties associated with the completion has been set.")
public class AfterWorkflowCompleteEvent extends
		AbstractInstanceTwoPhaseEvent<WorkflowInstanceContext, TwoPhaseEvent> implements
		OperationEvent {

	/** The task instance. */
	private final TaskInstance taskInstance;
	/** The operation id. */
	private final String operationId;

	/**
	 * Instantiates a new after workflow complete event.
	 * 
	 * @param instance
	 *            the instance
	 * @param taskInstance
	 *            the task instance
	 * @param operationId
	 *            the operation id
	 */
	public AfterWorkflowCompleteEvent(WorkflowInstanceContext instance, TaskInstance taskInstance,
			String operationId) {
		super(instance);
		this.taskInstance = taskInstance;
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
	 * Getter method for taskInstance.
	 * 
	 * @return the taskInstance
	 */
	public AbstractTaskInstance getTaskInstance() {
		return taskInstance;
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
