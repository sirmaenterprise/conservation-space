package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before workflow completion. The event is fired after Activiti does not return more
 * tasks after transition. Workflow completion means only updating workflow fields/properties to
 * mark it as completed and does not involve Activiti calls.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before workflow completion. The event is fired after Activiti does not return more tasks after transition. Workflow completion means only updating workflow fields/properties to mark it as completed and does not involve Activiti calls.")
public class BeforeWorkflowCompleteEvent extends
		AbstractInstanceTwoPhaseEvent<WorkflowInstanceContext, AfterWorkflowCompleteEvent>
		implements OperationEvent {

	/** The task instance. */
	private final TaskInstance taskInstance;
	/** The operation id. */
	private final String operationId;

	/**
	 * Instantiates a new before workflow complete event.
	 * 
	 * @param instance
	 *            the instance
	 * @param taskInstance
	 *            the task instance
	 * @param operationId
	 *            the operation id
	 */
	public BeforeWorkflowCompleteEvent(WorkflowInstanceContext instance, TaskInstance taskInstance,
			String operationId) {
		super(instance);
		this.taskInstance = taskInstance;
		this.operationId = operationId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterWorkflowCompleteEvent createNextEvent() {
		return new AfterWorkflowCompleteEvent(getInstance(), getTaskInstance(), getOperationId());
	}

	/**
	 * Getter method for taskInstance.
	 * 
	 * @return the taskInstance
	 */
	public TaskInstance getTaskInstance() {
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
